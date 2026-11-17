package me.wavetech.wavecarpet.mixins.rule.individualLocatorBar;

import com.google.common.collect.Table;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.waypoints.ServerWaypointManager;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerWaypointManager.class)
public interface ServerWaypointManagerAccessor {
	@Accessor
	Table<ServerPlayer, WaypointTransmitter, WaypointTransmitter.Connection> getConnections();
}
