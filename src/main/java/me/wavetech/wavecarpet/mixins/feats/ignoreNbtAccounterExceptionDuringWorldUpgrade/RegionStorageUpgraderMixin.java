package me.wavetech.wavecarpet.mixins.feats.ignoreNbtAccounterExceptionDuringWorldUpgrade;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.wavetech.wavecarpet.WaveCarpetMod;
import net.minecraft.nbt.NbtAccounterException;
import net.minecraft.util.worldupdate.RegionStorageUpgrader;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RegionStorageUpgrader.class)
public class RegionStorageUpgraderMixin {
	@WrapOperation(method = "processOnePosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/worldupdate/RegionStorageUpgrader;tryProcessOnePosition(Lnet/minecraft/world/level/chunk/storage/SimpleRegionStorage;Lnet/minecraft/world/level/ChunkPos;)Z"))
	private boolean ignoreNbtAccounterException(RegionStorageUpgrader instance, SimpleRegionStorage storage, ChunkPos pos, Operation<Boolean> original) {
		try {
			return original.call(instance, storage, pos);
		} catch (NbtAccounterException e) {
			WaveCarpetMod.LOGGER.info("Encountered NbtAccounterException. Skipping region...");
			return false;
		}
	}
}
