package me.wavetech.wavecarpet.core;

import com.mojang.serialization.Codec;
import me.wavetech.wavecarpet.WaveCarpetSettings;
import me.wavetech.wavecarpet.mixins.rule.individualLocatorBar.ServerWaypointManagerAccessor;
import me.wavetech.wavecarpet.network.SubscriptionsSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.function.BooleanSupplier;

public final class Subscriptions {
	public static final Codec<List<Identifier>> CODEC = Identifier.CODEC.listOf();
	public static final StreamCodec<FriendlyByteBuf, List<Identifier>> STREAM_CODEC = ByteBufCodecs.collection(ArrayList::new, Identifier.STREAM_CODEC);
	private static final Map<Identifier, BooleanSupplier> AVAILABLE = new HashMap<>();

	private Subscriptions() {}

	public static void registerDefaults() {
		register(SubscriptionIds.INSTAMINE_DEEPSLATE, () -> WaveCarpetSettings.waveInstamineDeepslate);
		register(SubscriptionIds.INDIVIDUAL_LOCATOR_BAR, () -> WaveCarpetSettings.individualLocatorBar);
	}

	public static void register(Identifier id, BooleanSupplier available) {
		if (AVAILABLE.putIfAbsent(id, available) != null) {
			throw new IllegalArgumentException("Duplicate subscription " + id);
		}
	}

	public static boolean isAvailable(Identifier id) {
		BooleanSupplier available = AVAILABLE.get(id);
		return available != null && available.getAsBoolean();
	}

	public static boolean anyAvailable() {
		return AVAILABLE.keySet().stream().anyMatch(Subscriptions::isAvailable);
	}

	public static Collection<Identifier> availableIds() {
		return AVAILABLE.keySet().stream().filter(Subscriptions::isAvailable).toList();
	}

	public static void onSubscriptionChange(ServerPlayer player, Identifier subscription, boolean subscribed) {
		SubscriptionsSync.sync(player);

		if (subscription.equals(SubscriptionIds.INDIVIDUAL_LOCATOR_BAR) && !subscribed) {
			var waypointManager = ((ServerWaypointManagerAccessor) player.level().getWaypointManager());
			waypointManager.getConnections().row(player).values().removeIf(connection -> {
				connection.disconnect();
				return true;
			});
		}
	}
}
