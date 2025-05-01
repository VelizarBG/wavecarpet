package me.wavetech.wavecarpet.mixins.rule.portablePearlCannon;

import me.wavetech.wavecarpet.WaveCarpetSettings;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin extends ThrowableItemProjectile {
	public ThrownEnderpearlMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
		super(entityType, level);
	}

	@NotNull
	@Override
	public Vec3 getMovementToShoot(double d, double e, double f, float g, float h) {
		Vec3 movementToShoot = super.getMovementToShoot(d, e, f, g, h);
		if (WaveCarpetSettings.portablePearlCannon) {
			movementToShoot = movementToShoot.multiply(1000, 1, 1000);
		}
		return movementToShoot;
	}
}
