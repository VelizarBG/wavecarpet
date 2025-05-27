package me.wavetech.wavecarpet.mixins.rule.fixInteractiveCollisions;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.wavetech.wavecarpet.WaveCarpetSettings;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(value = Entity.class, priority = 900)
public abstract class EntityMixin {
	@Shadow protected abstract boolean isAffectedByBlocks();
	@Shadow @Final private LongSet visitedBlocks;
	@Shadow protected abstract AABB makeBoundingBox(Vec3 position);
	@Shadow public abstract boolean isAlive();
	@Shadow public abstract Level level();
	@Shadow public abstract boolean collidedWithShapeMovingFrom(Vec3 from, Vec3 to, List<AABB> boxes);
	@Shadow protected abstract void onInsideBlock(BlockState state);
	@Shadow public abstract void fillCrashReportCategory(CrashReportCategory category);
	@Shadow public abstract boolean collidedWithFluid(FluidState fluid, BlockPos pos, Vec3 from, Vec3 to);
	@Shadow protected abstract AABB makeBoundingBox();
	@Shadow private Vec3 position;

	@WrapMethod(method = "checkInsideBlocks")
	private void checkInsideBlocks(List<Entity.Movement> movements, InsideBlockEffectApplier.StepBasedCollector stepBasedCollector, Operation<Void> original) {
		if (!WaveCarpetSettings.fixInteractiveCollisions) {
			original.call(movements, stepBasedCollector);
			return;
		}

		if (this.isAffectedByBlocks()) {
			LongSet visitedBlocks = this.visitedBlocks;

			for (Entity.Movement movement : movements) {
				Vec3 from = movement.from();
				Vec3 to = movement.to();
				AABB deflatedBb = this.makeBoundingBox(to).deflate(1.0E-5F);

				// Ensure interaction with the blocks at the final position
				boolean shouldContinueInteracting = interactWithBlocksAlongPath(stepBasedCollector, from, to, deflatedBb, visitedBlocks);
				if (!shouldContinueInteracting) {
					deflatedBb = this.makeBoundingBox().deflate(1.0E-5F);
					interactWithBlocksAlongPath(stepBasedCollector, this.position, this.position, deflatedBb, visitedBlocks);
					break;
				}
			}

			visitedBlocks.clear();
		}
	}

	@Unique
	private boolean interactWithBlocksAlongPath(InsideBlockEffectApplier.StepBasedCollector stepBasedCollector, Vec3 from, Vec3 to, AABB deflatedBb, LongSet visitedBlocks) {
		return BlockGetter.forEachBlockIntersectedBetween(from, to, deflatedBb, (blockPos, blocksVisited) -> {
			// Cap to an arbitrary amount to prevent lag caused by excessive chunk loading
			if (blocksVisited > 16 || !this.isAlive()) {
				return false;
			} else {
				BlockState blockState = this.level().getBlockState(blockPos);
				if (blockState.isAir()) {
					return true;
				} else if (!visitedBlocks.add(blockPos.asLong())) {
					return true;
				} else {
					VoxelShape voxelShape = blockState.getEntityInsideCollisionShape(this.level(), blockPos, (Entity) (Object) this);
					boolean bl = voxelShape == Shapes.block() || this.collidedWithShapeMovingFrom(from, to, voxelShape.move(new Vec3(blockPos)).toAabbs());
					if (bl) {
						try {
							stepBasedCollector.advanceStep(blocksVisited);
							blockState.entityInside(this.level(), blockPos, (Entity) (Object) this, stepBasedCollector);
							this.onInsideBlock(blockState);
						} catch (Throwable var14) {
							CrashReport crashReport = CrashReport.forThrowable(var14, "Colliding entity with block");
							CrashReportCategory crashReportCategory = crashReport.addCategory("Block being collided with");
							CrashReportCategory.populateBlockDetails(crashReportCategory, this.level(), blockPos, blockState);
							CrashReportCategory crashReportCategory2 = crashReport.addCategory("Entity being checked for collision");
							this.fillCrashReportCategory(crashReportCategory2);
							throw new ReportedException(crashReport);
						}
					}

					boolean bl2 = this.collidedWithFluid(blockState.getFluidState(), blockPos, from, to);
					if (bl2) {
						stepBasedCollector.advanceStep(blocksVisited);
						blockState.getFluidState().entityInside(this.level(), blockPos, (Entity) (Object) this, stepBasedCollector);
					}

					return true;
				}
			}
		});
	}
}
