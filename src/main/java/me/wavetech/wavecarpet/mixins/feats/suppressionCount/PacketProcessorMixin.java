package me.wavetech.wavecarpet.mixins.feats.suppressionCount;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import me.wavetech.wavecarpet.core.ObjectiveCriteriaRegistry;
import net.minecraft.ReportedException;
import net.minecraft.network.PacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.scores.ScoreAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/network/PacketProcessor$ListenerAndPacket")
public class PacketProcessorMixin<T extends PacketListener> {
	@Shadow
	@Final
	private T listener;

	@Definition(id = "ReportedException", type = ReportedException.class)
	@Expression("? instanceof ReportedException")
	@Inject(method = "handle", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
	private void countSuppression(CallbackInfo ci) {
		if (this.listener instanceof ServerGamePacketListenerImpl gamePL) {
			gamePL.player.level().getServer().getScoreboard()
				.forAllObjectives(ObjectiveCriteriaRegistry.SUPPRESSION_COUNT, gamePL.player, ScoreAccess::increment);
		}
	}
}
