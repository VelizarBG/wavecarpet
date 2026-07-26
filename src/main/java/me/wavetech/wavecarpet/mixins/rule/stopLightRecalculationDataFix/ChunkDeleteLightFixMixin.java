package me.wavetech.wavecarpet.mixins.rule.stopLightRecalculationDataFix;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.serialization.Dynamic;
import me.wavetech.wavecarpet.WaveCarpetSettings;
import net.minecraft.util.datafix.fixes.ChunkDeleteLightFix;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkDeleteLightFix.class)
public class ChunkDeleteLightFixMixin {
	@WrapMethod(method = "lambda$makeRule$3")
	private static Dynamic<?> skipLightDeletion(Dynamic<?> tag, Operation<Dynamic<?>> original) {
		return WaveCarpetSettings.stopLightRecalculationDataFix ? tag : original.call(tag);
	}
}
