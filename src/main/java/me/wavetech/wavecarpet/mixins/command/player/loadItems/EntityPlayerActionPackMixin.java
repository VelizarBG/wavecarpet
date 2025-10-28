package me.wavetech.wavecarpet.mixins.command.player.loadItems;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.BlockHitResult;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Hey, I didn't come up with this naming
@Mixin(targets = "carpet/helpers/EntityPlayerActionPack$ActionType$1") // 1 == USE
public class EntityPlayerActionPackMixin {
	@Expression("return true")
	@Inject(
		method = "execute",
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;")
		),
		at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0)
	)
	private void loadItemsInBlock(CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) ServerPlayer player, @Local BlockHitResult target) {
		if (player.level().getBlockEntity(target.getBlockPos()) instanceof Container container) {
			transfer(player, container);
		}
	}

	@Unique
	private void transfer(ServerPlayer player, Container container) {
		if (!player.getLoadItems$wavecarpet()
			|| player.containerMenu instanceof InventoryMenu
			|| !container.stillValid(player)
		) {
			return;
		}

		var menu = player.containerMenu;
		var inventory = player.getInventory();
		for (Slot slot : menu.slots) {
			if (slot.container == inventory) {
				menu.clicked(slot.index, GLFW.GLFW_MOUSE_BUTTON_LEFT, ClickType.QUICK_MOVE, player);
			}
		}

		player.closeContainer();
	}
}
