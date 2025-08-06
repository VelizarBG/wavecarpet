package me.wavetech.wavecarpet.mixins.rule.allowPlacingMasterBlocks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.wavetech.wavecarpet.WaveCarpetSettings;
import net.minecraft.world.item.GameMasterBlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameMasterBlockItem.class)
public class GameMasterBlockItemMixin {
	@ModifyExpressionValue(method = "getPlacementState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canUseGameMasterBlocks()Z"))
	private boolean allowPlacingMasterBlocks(boolean canUseGameMasterBlocks){
		return canUseGameMasterBlocks || WaveCarpetSettings.allowPlacingMasterBlocks;
	}
}
