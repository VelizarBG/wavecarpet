package me.wavetech.wavecarpet.mixins.command.player.loadItems;

import carpet.helpers.EntityPlayerActionPack;
import com.llamalad7.mixinextras.sugar.Local;
import me.wavetech.wavecarpet.core.ContainerMerger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Hey, I didn't come up with this naming
@Mixin(targets = "carpet/helpers/EntityPlayerActionPack$ActionType$1") // 1 == USE
public class EntityPlayerActionPackMixin {
	@Inject(method = "execute", at = @At(value = "RETURN", ordinal = 2))
	private void loadItemsInBlock(ServerPlayer player, EntityPlayerActionPack.Action action, CallbackInfoReturnable<Boolean> cir, @Local BlockHitResult target) {
		if (player.level().getBlockEntity(target.getBlockPos()) instanceof Container container) {
			transfer(player, container);
		}
	}

    @Unique
    private void transfer(ServerPlayer player, Container container) {
        if (!container.stillValid(player) || !player.getLoadItems$wavecarpet())
            return;

        var inventory = player.getInventory();
        var removedStacks = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();

        // Temporarily remove shulker boxes
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() instanceof ShulkerBoxBlock) {
                removedStacks.add(stack.copy());
                inventory.setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }

        // Transfer remaining items using ContainerMerger
        ContainerMerger.transfer(inventory, container);

        // Restore shulker boxes
        for (var stack : removedStacks) {
            inventory.add(stack);
        }

        player.closeContainer();
    }
}
