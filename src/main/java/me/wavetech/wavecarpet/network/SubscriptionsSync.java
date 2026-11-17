package me.wavetech.wavecarpet.network;

import net.fabricmc.fabric.api.networking.v1.ClientboundPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class SubscriptionsSync {
	private SubscriptionsSync() {}

	public static void init() {
		ClientboundPlayChannelEvents.REGISTER.register((listener, _, _, channels) -> {
			if (channels.contains(ClientboundSubscriptionsPacket.TYPE.id())) {
				sync(listener.getPlayer());
			}
		});
	}

	public static void sync(ServerPlayer player) {
		try {
			if (!ServerPlayNetworking.canSend(player, ClientboundSubscriptionsPacket.TYPE)) {
				return;
			}
		} catch (Throwable _) { return;	}

		// Only sync the *available* subscriptions to keep the server authoritative
		var subscriptions = player.getAvailableSubscriptions$wavecarpet();
		ServerPlayNetworking.send(player, new ClientboundSubscriptionsPacket(subscriptions));
	}
}
