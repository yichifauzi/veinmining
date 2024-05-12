package com.illusivesoulworks.veinmining.mixin;

import com.illusivesoulworks.veinmining.common.config.VeinMiningConfig;
import com.illusivesoulworks.veinmining.common.veinmining.VeinMiningPlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VeinMiningMixinHooks {

  public static BlockPos getActualSpawnPos(Level level, BlockPos pos) {
    return VeinMiningPlayers.getNewSpawnPosForDrop(level, pos).orElse(pos);
  }

  public static boolean shouldCancelItemDamage(ServerPlayer serverPlayer) {
    return serverPlayer != null && VeinMiningPlayers.isVeinMining(serverPlayer) &&
        !VeinMiningConfig.SERVER.addToolDamage.get();
  }

  public static int modifyItemDamage(ItemStack stack, int damage,
                                     ServerPlayer player) {
    int newDamage = damage;

    if (player != null && VeinMiningPlayers.isVeinMining(player)) {
      float multiplier = VeinMiningConfig.SERVER.toolDamageMultiplier.get();

      if (multiplier != 1.0f) {
        newDamage = Math.max(0, (int) ((float) newDamage * multiplier));
      }

      if (VeinMiningConfig.SERVER.preventToolDestruction.get()) {
        newDamage = Math.min(damage, stack.getMaxDamage() - stack.getDamageValue() - 2);
      }
    }
    return newDamage;
  }
}
