package me.wavetech.wavecarpet.mixins.feats.emptyChunkOnEntityChunkLoadFailure;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.wavetech.wavecarpet.WaveCarpetMod;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

@Mixin(PersistentEntitySectionManager.class)
public class PersistentEntitySectionManagerMixin {
	@Shadow
	@Final
	private Queue<ChunkEntities<?>> loadingInbox;

	@Definition(id = "permanentStorage", field = "Lnet/minecraft/world/level/entity/PersistentEntitySectionManager;permanentStorage:Lnet/minecraft/world/level/entity/EntityPersistentStorage;")
	@Definition(id = "loadEntities", method = "Lnet/minecraft/world/level/entity/EntityPersistentStorage;loadEntities(Lnet/minecraft/world/level/ChunkPos;)Ljava/util/concurrent/CompletableFuture;")
	@Definition(id = "thenAccept", method = "Ljava/util/concurrent/CompletableFuture;thenAccept(Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;")
	@Expression("this.permanentStorage.loadEntities(?).thenAccept(?)")
	@ModifyExpressionValue(method = "requestChunkLoad", at = @At("MIXINEXTRAS:EXPRESSION"))
	private CompletableFuture<Void> loadEmptyChunkInstead(@NotNull CompletableFuture<Void> original, @Local(name = "pos") ChunkPos pos) {
		original.exceptionally(_ -> {
			WaveCarpetMod.LOGGER.info("Failed to load entity chunk, replacing with empty one");
			this.loadingInbox.add(new ChunkEntities<>(pos, List.of()));
			return null;
		});
		return original;
	}
}
