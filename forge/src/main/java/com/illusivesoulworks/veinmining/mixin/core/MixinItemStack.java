package com.illusivesoulworks.veinmining.mixin.core;

import com.illusivesoulworks.veinmining.mixin.VeinMiningMixinHooks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class MixinItemStack {

  @Inject(
      at = @At(
          value = "INVOKE",
          target = "net/minecraft/world/item/ItemStack.isDamageableItem()Z"),
      method = "hurtAndBreak(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/Runnable;)V",
      cancellable = true)
  private void veinmining$itemDamage(int amount, RandomSource randomSource,
                                     ServerPlayer serverPlayer, Runnable runnable,
                                     CallbackInfo ci) {

    if (VeinMiningMixinHooks.shouldCancelItemDamage(serverPlayer)) {
      ci.cancel();
    }
  }

  @SuppressWarnings("ConstantConditions")
  @ModifyVariable(
      at = @At("HEAD"),
      method = "hurtAndBreak(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/Runnable;)V",
      argsOnly = true
  )
  private int veinmining$changeBreak(int amount, int unused, RandomSource randomSource,
                                     ServerPlayer player) {
    return VeinMiningMixinHooks.modifyItemDamage((ItemStack) (Object) this, amount, player);
  }
}
