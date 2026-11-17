package me.wavetech.wavecarpet.mixins.rule.individualLocatorBar;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.wavetech.wavecarpet.WaveCarpetSettings;
import me.wavetech.wavecarpet.core.SubscriptionIds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.waypoints.ServerWaypointManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerWaypointManager.class)
public class ServerWaypointManagerMixin {
	@WrapMethod(method = "isLocatorBarEnabledFor")
	private static boolean enforceIndividualLocatorBar(ServerPlayer player, Operation<Boolean> original) {
		if (WaveCarpetSettings.individualLocatorBar) {
			return player.hasAvailableSubscription$wavecarpet(SubscriptionIds.INDIVIDUAL_LOCATOR_BAR) && original.call(player);
		}
		return original.call(player);
	}
}
