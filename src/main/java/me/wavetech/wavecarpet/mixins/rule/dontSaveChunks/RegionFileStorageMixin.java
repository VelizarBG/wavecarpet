package me.wavetech.wavecarpet.mixins.rule.dontSaveChunks;

import me.wavetech.wavecarpet.WaveCarpetSettings;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RegionFileStorage.class)
public class RegionFileStorageMixin {
	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	private void maybeJustDont(CallbackInfo ci) {
		if (WaveCarpetSettings.dontSaveChunks) {
			ci.cancel();
		}
	}
}
