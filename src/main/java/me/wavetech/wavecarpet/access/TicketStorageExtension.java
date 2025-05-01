package me.wavetech.wavecarpet.access;

import net.minecraft.server.level.ChunkMap;

public interface TicketStorageExtension {
	default void setChunkMap$wavecarpet(ChunkMap chunkMap) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}
