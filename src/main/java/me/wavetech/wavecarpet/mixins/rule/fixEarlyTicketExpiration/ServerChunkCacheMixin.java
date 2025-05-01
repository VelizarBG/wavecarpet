package me.wavetech.wavecarpet.mixins.rule.fixEarlyTicketExpiration;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.TicketStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {
	@Shadow @Final private TicketStorage ticketStorage;

	@Shadow @Final public ChunkMap chunkMap;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void setTicketStorageChunkMap(CallbackInfo ci) {
		ticketStorage.setChunkMap$wavecarpet(chunkMap);
	}
}
