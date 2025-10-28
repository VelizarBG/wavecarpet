package me.wavetech.wavecarpet.mixins.rule.disableOpsBypassWhitelist;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.wavetech.wavecarpet.WaveCarpetSettings;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.players.NameAndId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DedicatedPlayerList.class)
public class DedicatedPlayerListMixin {
	@WrapOperation(method = "isWhiteListed", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedPlayerList;isOp(Lnet/minecraft/server/players/NameAndId;)Z"))
	private boolean disableOpsBypassWhitelist(DedicatedPlayerList instance, NameAndId nameAndId, Operation<Boolean> original) {
		return !WaveCarpetSettings.disableOpsBypassWhitelist && original.call(instance, nameAndId);
	}
}
