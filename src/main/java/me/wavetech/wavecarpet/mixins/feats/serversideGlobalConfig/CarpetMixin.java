package me.wavetech.wavecarpet.mixins.feats.serversideGlobalConfig;

import carpet.script.external.Carpet;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Carpet.class, remap = false)
public class CarpetMixin {
	@Definition(id = "getInstance", method = "Lnet/fabricmc/loader/api/FabricLoader;getInstance()Lnet/fabricmc/loader/api/FabricLoader;")
	@Definition(id = "getEnvironmentType", method = "Lnet/fabricmc/loader/api/FabricLoader;getEnvironmentType()Lnet/fabricmc/api/EnvType;")
	@Definition(id = "CLIENT", field = "Lnet/fabricmc/api/EnvType;CLIENT:Lnet/fabricmc/api/EnvType;")
	@Expression("getInstance().getEnvironmentType() == CLIENT")
	@ModifyExpressionValue(method = { "fetchGlobalModule", "addGlobalModules" }, at = @At("MIXINEXTRAS:EXPRESSION"))
	private static boolean workOnAnyEnvironment(boolean original) {
		return true;
	}
}
