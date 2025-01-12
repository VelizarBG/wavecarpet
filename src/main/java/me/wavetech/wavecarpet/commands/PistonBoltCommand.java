package me.wavetech.wavecarpet.commands;

import carpet.utils.CommandHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.wavetech.wavecarpet.WaveCarpetSettings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Map;

import static carpet.utils.Translations.tr;
import static me.wavetech.wavecarpet.WaveCarpetMod.pistonBoltState;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PistonBoltCommand {
	public static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = (context, builder) -> (
		SharedSuggestionProvider.suggest(pistonBoltState.locationEncodings.keySet(), builder)
	);

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("pistonbolt")
			.requires(source -> CommandHelper.canUseCommand(source, WaveCarpetSettings.commandPistonBolt))
			.executes(context -> {
				context.getSource().sendSuccess(() -> Component.literal("he pissed on what?"), false);
				return 1;
			})
			.then(literal("location")
				.then(literal("add")
					.then(argument("locationName", StringArgumentType.word())
						.then(argument("locationEncoding", StringArgumentType.word())
							.executes(context -> {
								var name = StringArgumentType.getString(context, "locationName");
								var encoding = StringArgumentType.getString(context, "locationEncoding");
								int i;
								if (pistonBoltState.locationEncodings.containsKey(name)) {
									context.getSource().sendSuccess(() ->
										Component.literal(tr("commands.pistonbolt.location.add.updated")
											.formatted(name, pistonBoltState.locationEncodings.get(name), encoding)),
									true);
									i = 0;
								} else {
									context.getSource().sendSuccess(() -> Component.literal(tr("commands.pistonbolt.location.add.added").formatted(name)), true);
									i = 1;
								}
								pistonBoltState.locationEncodings.put(name, encoding);
								pistonBoltState.setDirty();
								return i;
							})
						)
					)
				)
				.then(literal("remove")
					.then(argument("locationName", StringArgumentType.word())
						.suggests(SUGGESTION_PROVIDER)
						.executes(context -> {
							var name = StringArgumentType.getString(context, "locationName");
							int i;
							if (pistonBoltState.locationEncodings.containsKey(name)) {
								pistonBoltState.locationEncodings.remove(name);
								pistonBoltState.setDirty();
								context.getSource().sendSuccess(() ->
										Component.literal(tr("commands.pistonbolt.location.remove.success")
											.formatted(name)),
									true);
								i = 1;
							} else {
								context.getSource().sendFailure(Component.literal(tr("commands.pistonbolt.location.invalid").formatted(name)));
								i = 0;
							}
							return i;
						})
					)
				)
				.then(literal("get")
					.then(argument("locationName", StringArgumentType.word())
						.suggests(SUGGESTION_PROVIDER)
						.executes(context -> {
							var name = StringArgumentType.getString(context, "locationName");
							var encoding = pistonBoltState.locationEncodings.get(name);
							if (encoding == null) {
								context.getSource().sendFailure(Component.literal(tr("commands.pistonbolt.location.invalid").formatted(name)));
								return 0;
							}
							context.getSource().sendSuccess(() -> Component.literal(tr("commands.pistonbolt.location.get.success").formatted(name, encoding)), false);
							return 1;
						})
					)
				)
				.then(literal("list")
					.executes(context -> {
						if (pistonBoltState.locationEncodings.isEmpty()) {
							context.getSource().sendSuccess(() -> Component.literal(tr("commands.pistonbolt.location.list.empty")), false);
							return 0;
						}
						int size = pistonBoltState.locationEncodings.size();
						StringBuilder sb = new StringBuilder(tr("commands.pistonbolt.location.list.success").formatted(size));
						for (Map.Entry<String, String> entry : pistonBoltState.locationEncodings.entrySet()) {
							sb
								.append('\n')
								.append(entry.getKey())
								.append(" -> ")
								.append(entry.getValue());
						}
						context.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
						return size;
					})
				)
			)
		);
	}
}
