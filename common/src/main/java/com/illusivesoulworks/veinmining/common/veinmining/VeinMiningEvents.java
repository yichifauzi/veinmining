/*
 * Copyright (C) 2020-2022 Illusive Soulworks
 *
 * Vein Mining is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Vein Mining is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Vein Mining.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.illusivesoulworks.veinmining.common.veinmining;

import com.illusivesoulworks.veinmining.VeinMiningConstants;
import com.illusivesoulworks.veinmining.common.config.VeinMiningConfig;
import com.illusivesoulworks.veinmining.common.platform.Services;
import com.illusivesoulworks.veinmining.common.veinmining.logic.BlockProcessor;
import com.illusivesoulworks.veinmining.common.veinmining.logic.VeinMiningLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class VeinMiningEvents {

  public static void tick(Level level) {

    if (!level.isClientSide()) {
      VeinMiningPlayers.validate(level.getGameTime());
    }
  }

  public static void reloadDatapack() {
    BlockProcessor.rebuild();
  }

  public static void blockBreak(ServerPlayer player, BlockPos pos, BlockState state) {

    if (VeinMiningPlayers.canStartVeinMining(player) && !VeinMiningPlayers.isVeinMining(player)) {
      VeinMiningPlayers.startVeinMining(player);
      VeinMiningLogic.veinMine(player, pos, state);
      VeinMiningPlayers.stopVeinMining(player);
    }
  }

  public static void playerLoggedOut(ServerPlayer player) {
    VeinMiningPlayers.deactivateVeinMining(player);
    VeinMiningPlayers.stopVeinMining(player);
  }

  public static void toolEquip(ItemStack to, ItemStack from, EquipmentSlot slot,
                               LivingEntity livingEntity) {

    if (!ItemStack.isSameItem(to, from) && livingEntity instanceof ServerPlayer player) {
      boolean needsEnchantment = VeinMiningConfig.SERVER.maxBlocksBase.get() == 0;

      if (needsEnchantment && slot == EquipmentSlot.MAINHAND) {
        Holder<Enchantment> enchantment = player.registryAccess().lookup(Registries.ENCHANTMENT)
            .map(enchantmentRegistryLookup -> enchantmentRegistryLookup.getOrThrow(
                VeinMiningConstants.ENCHANTMENT)).orElse(null);
        int level =
            enchantment != null ? EnchantmentHelper.getItemEnchantmentLevel(enchantment, to) : 0;

        if (level > 0) {
          Services.PLATFORM.sendNotifyS2C(player);
        }
      }
    }
  }
}
