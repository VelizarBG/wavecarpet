package me.wavetech.wavecarpet.network;

import me.wavetech.wavecarpet.core.Subscriptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record ClientboundSubscriptionsPacket(List<Identifier> subscriptions) implements CustomPacketPayload {
	public static final Type<ClientboundSubscriptionsPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("wavecarpet", "subscriptions"));
	public static final StreamCodec<FriendlyByteBuf, ClientboundSubscriptionsPacket> STREAM_CODEC =
		Subscriptions.STREAM_CODEC.map(ClientboundSubscriptionsPacket::new, ClientboundSubscriptionsPacket::subscriptions);

	public ClientboundSubscriptionsPacket {
		subscriptions = List.copyOf(subscriptions);
	}

	@Override
	public Type<ClientboundSubscriptionsPacket> type() {
		return TYPE;
	}
}
