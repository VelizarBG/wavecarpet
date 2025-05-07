package me.wavetech.wavecarpet.mixins.rule.fixEarlyTicketExpiration;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import me.wavetech.wavecarpet.WaveCarpetSettings;
import me.wavetech.wavecarpet.access.TicketStorageExtension;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@Mixin(value = TicketStorage.class, priority = 999)
public abstract class TicketStorageMixin extends SavedData implements TicketStorageExtension {
	@Shadow @Final private Long2ObjectOpenHashMap<List<Ticket>> tickets;
	@Shadow @Nullable private TicketStorage.@Nullable ChunkUpdated loadingChunkUpdatedListener;

	@Shadow private static int getTicketLevelAt(List<Ticket> tickets, boolean requireSimulation) {
		throw new UnsupportedOperationException();
	}

	@Shadow @Nullable private TicketStorage.@Nullable ChunkUpdated simulationChunkUpdatedListener;

	@Shadow protected abstract void updateForcedChunks();

	@Unique
	private ChunkMap chunkMap;

	/**
	 * @author VelizarBG
	 * @reason Necessitated by implementation
	 */
	@Overwrite
	public void removeTicketIf(Predicate<Ticket> predicate, @Nullable Long2ObjectOpenHashMap<List<Ticket>> tickets) {
		ObjectIterator<Long2ObjectMap.Entry<List<Ticket>>> objectIterator = this.tickets.long2ObjectEntrySet().fastIterator();
		boolean bl = false;

		while (objectIterator.hasNext()) {
			Long2ObjectMap.Entry<List<Ticket>> entry = objectIterator.next();
			Iterator<Ticket> iterator = entry.getValue().iterator();
			boolean bl2 = false;
			boolean bl3 = false;

			if (WaveCarpetSettings.fixEarlyTicketExpiration) {
				ChunkHolder updatingChunk = ((ChunkMapAccessor) chunkMap).callGetUpdatingChunkIfPresent(entry.getLongKey());
				if (updatingChunk != null) {
					boolean isSaveSyncDone = updatingChunk.getSaveSyncFuture().isDone();
					if (!isSaveSyncDone) {
						continue;
					}
				}
			}

			while (iterator.hasNext()) {
				Ticket ticket = iterator.next();
				if (predicate.test(ticket)) {
					if (tickets != null) {
						List<Ticket> list = tickets.computeIfAbsent(
							entry.getLongKey(), (Long2ObjectFunction<? extends List<Ticket>>) (chunkPos -> new ObjectArrayList<>(entry.getValue().size()))
						);
						list.add(ticket);
					}

					iterator.remove();
					if (ticket.getType().doesLoad()) {
						bl3 = true;
					}

					if (ticket.getType().doesSimulate()) {
						bl2 = true;
					}

					if (ticket.getType().equals(TicketType.FORCED)) {
						bl = true;
					}
				}
			}

			if (bl3 || bl2) {
				if (bl3 && this.loadingChunkUpdatedListener != null) {
					this.loadingChunkUpdatedListener.update(entry.getLongKey(), getTicketLevelAt(entry.getValue(), false), false);
				}

				if (bl2 && this.simulationChunkUpdatedListener != null) {
					this.simulationChunkUpdatedListener.update(entry.getLongKey(), getTicketLevelAt(entry.getValue(), true), false);
				}

				this.setDirty();
				if (entry.getValue().isEmpty()) {
					objectIterator.remove();
				}
			}
		}

		if (bl) {
			this.updateForcedChunks();
		}
	}

	@Override
	public void setChunkMap$wavecarpet(ChunkMap chunkMap) {
		this.chunkMap = chunkMap;
	}
}
