package me.wavetech.wavecarpet.mixins.feats.suppressionCount;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.wavetech.wavecarpet.core.ObjectiveCriteriaRegistry;
import net.minecraft.ReportedException;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.scores.ScoreAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(PacketUtils.class)
public class PacketUtilsMixin {
	// TODO: Incinerate this crap when MixinExtras expressions drop in Loader
	@WrapOperation(method = "lambda$ensureRunningOnSameThread$0", constant = @Constant(classValue = ReportedException.class, ordinal = 0))
	private static boolean countSuppression(Object object, Operation<Boolean> original, @Local(argsOnly = true) PacketListener packetListener) {
		if (packetListener instanceof ServerGamePacketListenerImpl gamePL) {
			gamePL.player.getServer().getScoreboard()
				.forAllObjectives(ObjectiveCriteriaRegistry.SUPPRESSION_COUNT, gamePL.player, ScoreAccess::increment);
		}
		return original.call(object);
	}
}
