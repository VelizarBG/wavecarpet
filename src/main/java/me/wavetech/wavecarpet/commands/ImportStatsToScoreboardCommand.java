package me.wavetech.wavecarpet.commands;

import carpet.utils.CommandHelper;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.brigadier.CommandDispatcher;
import me.wavetech.wavecarpet.WaveCarpetSettings;
import me.wavetech.wavecarpet.mixins.command.importStatsToScoreboard.StatsCounterAccessor;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import static carpet.utils.Translations.tr;
import static me.wavetech.wavecarpet.WaveCarpetMod.LOGGER;
import static net.minecraft.commands.Commands.literal;

public class ImportStatsToScoreboardCommand {
	private static long timeOfLastRequestMs = 0;
	private static boolean isImporting = false;

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("importstatstoscoreboard")
			.requires(source -> CommandHelper.canUseCommand(source, WaveCarpetSettings.commandImportStatsToScoreboard))
			.executes(context -> {
				if (isImporting) {
					context.getSource().sendFailure(Component.literal(tr("commands.importstatstoscoreboard.importing")));
					return 0;
				}

				isImporting = true;
				MinecraftServer server = context.getSource().getServer();
				var statToObjective = createStatObjectives(server.getScoreboard());
				Executor importExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Import thread").build());
				CompletableFuture.runAsync(() -> {
					try {
						executeImportStats(server, statToObjective);
					} finally {
						isImporting = false;
					}
				}, importExecutor);

				context.getSource().sendSuccess(() -> Component.literal(tr("commands.importstatstoscoreboard.success")), true);
				return 1;
			})
		);
	}

	private static void executeImportStats(MinecraftServer server, Map<Stat<?>, Objective> statToObjective) {
		Executor fileExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("File thread").build());
		Executor requestExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Request thread").build());
		var stopwatch = Stopwatch.createStarted();

		File statsDir = server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();
		List<UUID> skippedUUIDs = new ArrayList<>();
		Set<String> skippedStats = new HashSet<>();
		ServerScoreboard scoreboard = server.getScoreboard();
		List<CompletableFuture<Void>> setScoreTasks = new ArrayList<>();
		MutableInt importedFiles = new MutableInt();
		Collection<File> statsFiles = FileUtils.listFiles(statsDir, new String[]{"json"}, false);
		BiConsumer<PlayerStatsInfo, Throwable> importStatsFile = (playerStatsInfo, ex) -> {
			if (ex != null) {
				LOGGER.error("Unexpected error occurred while fetching player profile", ex);
				return;
			}

			var name = playerStatsInfo.name();
			if (name.isEmpty()) {
				skippedUUIDs.add(playerStatsInfo.uuid());
				return;
			}

			ServerStatsCounter statsCounter = new ServerStatsCounter(server, playerStatsInfo.statsFile());
			((StatsCounterAccessor) statsCounter).getStats().forEach((stat, count) -> {
				var objective = statToObjective.get(stat);
				if (objective == null) {
					skippedStats.add(stat.getName());
					return;
				}
				ScoreHolder scoreHolder = ScoreHolder.forNameOnly(name.get());
				setScoreTasks.add(
					CompletableFuture.runAsync(
						() -> scoreboard.getOrCreatePlayerScore(scoreHolder, objective).set(count),
						server
					)
				);
			});

			importedFiles.increment();
			int currentPercentage = (importedFiles.intValue() * 100) / statsFiles.size();
			int nextPercentage = ((importedFiles.intValue() + 1) * 100) / statsFiles.size();
			if (currentPercentage % 10 < 9 && nextPercentage % 10 >= 9) {
				LOGGER.info("Stats files imported: {}%", nextPercentage);
			}
		};

		List<CompletableFuture<PlayerStatsInfo>> importStatsTasks = new ArrayList<>();
		UserNameToIdResolver nameToIdCache = server.services().nameToIdCache();
		MinecraftSessionService sessionService = server.services().sessionService();
		for (File statsFile : statsFiles) {
			UUID uuid = UUID.fromString(statsFile.getName().substring(0, 36));
			var gameProfile = nameToIdCache.get(uuid);
			if (gameProfile.isPresent()) {
				importStatsTasks.add(CompletableFuture.completedFuture(new PlayerStatsInfo(uuid, Optional.of(gameProfile.get().name()), statsFile))
					.whenCompleteAsync(importStatsFile, fileExecutor));
			} else {
				importStatsTasks.add(CompletableFuture.supplyAsync(() -> {
					try {
						// https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin.2FCape
						// 60000/200 = 300/1
						var nowMs = Util.getMillis();
						if (nowMs - timeOfLastRequestMs < 300) {
							Thread.sleep(300 - (nowMs - timeOfLastRequestMs));
						}
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					timeOfLastRequestMs = Util.getMillis();
					return new PlayerStatsInfo(
						uuid,
						Optional.ofNullable(sessionService.fetchProfile(uuid, false))
							.map(ProfileResult::profile).map(GameProfile::name),
						statsFile
					);
				}, requestExecutor)
					.whenCompleteAsync(importStatsFile, fileExecutor));
			}
		}

		CompletableFuture.allOf(importStatsTasks.toArray(CompletableFuture[]::new)).join();
		CompletableFuture.allOf(setScoreTasks.toArray(CompletableFuture[]::new)).join();

		Duration elapsed = stopwatch.elapsed();
		LOGGER.info("Import stats to scoreboard took {}m {}s", elapsed.toMinutesPart(), elapsed.toSecondsPart());
		if (!skippedUUIDs.isEmpty())
			LOGGER.warn("UUIDs skipped: {}", StringUtils.join(skippedUUIDs, ", "));
		if (!skippedStats.isEmpty())
			LOGGER.warn("Stats skipped: {}", StringUtils.join(skippedStats, ", "));
	}

	private static Map<Stat<?>, Objective> createStatObjectives(ServerScoreboard scoreboard) {
		Map<Stat<?>, Objective> statToObjective = new HashMap<>();

		for (Block block : BuiltInRegistries.BLOCK) {
			var stat = Stats.BLOCK_MINED.get(block);
			var objectiveName = "m-" + BuiltInRegistries.BLOCK.wrapAsHolder(block).unwrapKey().orElseThrow().location().getPath();
			var displayName = Component.literal("Mined - ").append(block.getName());
			statToObjective.put(stat, createObjective(scoreboard, stat, objectiveName, displayName));
		}

		for (Item item : BuiltInRegistries.ITEM) {
			var stat = Stats.ITEM_CRAFTED.get(item);
			var objectiveName = "c-" + BuiltInRegistries.ITEM.wrapAsHolder(item).unwrapKey().orElseThrow().location().getPath();
			var displayName = Component.literal("Crafted - ").append(item.getName());
			statToObjective.put(stat, createObjective(scoreboard, stat, objectiveName, displayName));
		}

		for (Item item : BuiltInRegistries.ITEM) {
			var stat = Stats.ITEM_USED.get(item);
			var objectiveName = "u-" + BuiltInRegistries.ITEM.wrapAsHolder(item).unwrapKey().orElseThrow().location().getPath();
			var displayName = Component.literal("Used - ").append(item.getName());
			statToObjective.put(stat, createObjective(scoreboard, stat, objectiveName, displayName));
		}

		for (Item item : BuiltInRegistries.ITEM) {
			var stat = Stats.ITEM_BROKEN.get(item);
			var objectiveName = "b-" + BuiltInRegistries.ITEM.wrapAsHolder(item).unwrapKey().orElseThrow().location().getPath();
			var displayName = Component.literal("Broken - ").append(item.getName());
			statToObjective.put(stat, createObjective(scoreboard, stat, objectiveName, displayName));
		}

		for (Item item : BuiltInRegistries.ITEM) {
			var stat = Stats.ITEM_PICKED_UP.get(item);
			var objectiveName = "p-" + BuiltInRegistries.ITEM.wrapAsHolder(item).unwrapKey().orElseThrow().location().getPath();
			var displayName = Component.literal("Picked Up - ").append(item.getName());
			statToObjective.put(stat, createObjective(scoreboard, stat, objectiveName, displayName));
		}

		for (Item item : BuiltInRegistries.ITEM) {
			var stat = Stats.ITEM_DROPPED.get(item);
			var objectiveName = "d-" + BuiltInRegistries.ITEM.wrapAsHolder(item).unwrapKey().orElseThrow().location().getPath();
			var displayName = Component.literal("Dropped - ").append(item.getName());
			statToObjective.put(stat, createObjective(scoreboard, stat, objectiveName, displayName));
		}

		for (EntityType<?> entity : BuiltInRegistries.ENTITY_TYPE) {
			var stat = Stats.ENTITY_KILLED.get(entity);
			var objectiveName = "k-" + BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity).unwrapKey().orElseThrow().location().getPath();
			var displayName = Component.literal("Killed - ").append(entity.getDescription());
			statToObjective.put(stat, createObjective(scoreboard, stat, objectiveName, displayName));
		}

		for (EntityType<?> entity : BuiltInRegistries.ENTITY_TYPE) {
			var stat = Stats.ENTITY_KILLED_BY.get(entity);
			var objectiveName = "kb-" + BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity).unwrapKey().orElseThrow().location().getPath();
			var displayName = Component.literal("Killed By - ").append(entity.getDescription());
			statToObjective.put(stat, createObjective(scoreboard, stat, objectiveName, displayName));
		}

		for (ResourceLocation custom : BuiltInRegistries.CUSTOM_STAT) {
			var stat = Stats.CUSTOM.get(custom);
			var objectiveName = "z-" + custom.getPath();
			var displayName = Component.translatable(getTranslationKey(stat));
			statToObjective.put(stat, createObjective(scoreboard, stat, objectiveName, displayName));
		}

		return statToObjective;
	}

	private static Objective createObjective(Scoreboard scoreboard, Stat<?> stat, String objectiveName, Component displayName) {
		var existingObjective = scoreboard.getObjective(objectiveName);
		if (existingObjective != null) {
			scoreboard.removeObjective(existingObjective);
		}
		return scoreboard.addObjective(
			objectiveName,
			stat,
			displayName,
			ObjectiveCriteria.RenderType.INTEGER,
			false,
			null
		);
	}

	private static String getTranslationKey(Stat<ResourceLocation> stat) {
		return "stat." + stat.getValue().toString().replace(':', '.');
	}

	private record PlayerStatsInfo(UUID uuid, Optional<String> name, File statsFile) {}
}
