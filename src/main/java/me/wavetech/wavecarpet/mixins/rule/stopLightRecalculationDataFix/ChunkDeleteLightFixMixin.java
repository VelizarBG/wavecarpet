package me.wavetech.wavecarpet.mixins.rule.stopLightRecalculationDataFix;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import me.wavetech.wavecarpet.WaveCarpetSettings;
import net.minecraft.util.datafix.fixes.ChunkDeleteLightFix;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkDeleteLightFix.class)
public class ChunkDeleteLightFixMixin {
	@WrapMethod(method = "lambda$makeRule$0")
	private static Typed<?> skipLightDeletion(OpticFinder<?> sectionsFinder, Typed<?> chunk, Operation<Typed<?>> original) {
		return WaveCarpetSettings.stopLightRecalculationDataFix ? chunk : original.call(sectionsFinder, chunk);
	}
}
