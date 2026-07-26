package me.wavetech.wavecarpet.mixins.feats.serversideGlobalConfig;

import carpet.script.CarpetScriptServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CarpetScriptServer.class)
public class CarpetScriptServerMixin {
	@Unique
	private boolean loadGlobalModules = false;

	@Inject(method = "initializeForWorld", at = @At("HEAD"))
	private void enableLoadGlobalModules(CallbackInfo ci) {
		loadGlobalModules = true;
	}

	@Inject(method = "initializeForWorld", at = @At("RETURN"))
	private void disableLoadGlobalModules(CallbackInfo ci) {
		loadGlobalModules = false;
	}

	@ModifyArg(method = "listAvailableModules", at = @At(value = "INVOKE", target = "Lcarpet/script/external/Carpet;addGlobalModules(Ljava/util/List;Z)V"), index = 1)
	private boolean overrideBuiltInsFlag(boolean includeBuiltIns) {
		return loadGlobalModules || includeBuiltIns;
	}
}
