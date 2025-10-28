package me.wavetech.wavecarpet.mixins.rule.obtainableInvisibleItemFrames;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.wavetech.wavecarpet.WaveCarpetSettings;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemFrameItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {
	@Shadow private @Nullable String itemName;

	@SuppressWarnings("unchecked")
	@Definition(id = "set", method = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;")
	@Definition(id = "CUSTOM_NAME", field = "Lnet/minecraft/core/component/DataComponents;CUSTOM_NAME:Lnet/minecraft/core/component/DataComponentType;")
	@Definition(id = "literal", method = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
	@Definition(id = "itemName", field = "Lnet/minecraft/world/inventory/AnvilMenu;itemName:Ljava/lang/String;")
	@Expression("?.set(CUSTOM_NAME, literal(this.itemName))")
	@WrapOperation(method = "createResult", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
	private <T> T tryConvertRenamedFrame(ItemStack stack, DataComponentType<? super T> component, @Nullable T value, Operation<T> original) {
		Item item = stack.getItem();
		if (WaveCarpetSettings.obtainableInvisibleItemFrames
			&& item instanceof ItemFrameItem && this.itemName.toLowerCase().equals("invisible")) {
			var data = new CompoundTag();
			data.put("Invisible", ByteTag.ONE);
			stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(((HangingEntityItemAccessor) item).getType(), data));
			value = (T) Component.literal("Invisible " + item.getName().getString())
					.setStyle(Style.EMPTY.withItalic(false));
		}
		return original.call(stack, component, value);
	}
}
