package me.wavetech.wavecarpet.mixins.rule.waveInstamineDeepslate;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.wavetech.wavecarpet.Constants;
import me.wavetech.wavecarpet.WaveCarpetSettings;
import me.wavetech.wavecarpet.core.SubscriptionIds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
	@ModifyExpressionValue(method = "getDestroyProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroySpeed(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
	public float modifyDeepslateDestroySpeed(float original, @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) Player player) {
		if (WaveCarpetSettings.waveInstamineDeepslate && state.is(Blocks.DEEPSLATE) && player instanceof ServerPlayer serverPlayer
			&& serverPlayer.hasAvailableSubscription$wavecarpet(SubscriptionIds.INSTAMINE_DEEPSLATE)
		) {
			return Constants.INSTAMINEABLE_DEEPSLATE_SPEED;
		}
		return original;
	}
}
