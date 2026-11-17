package me.wavetech.wavecarpet.client;

import me.wavetech.wavecarpet.network.ClientboundSubscriptionsPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class WaveCarpetClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(ClientboundSubscriptionsPacket.TYPE,
			(payload, _) -> ClientSubscriptions.replace(payload.subscriptions()));
		ClientPlayConnectionEvents.INIT.register((_, _) -> ClientSubscriptions.clear());
		ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> ClientSubscriptions.clear());
	}
}
