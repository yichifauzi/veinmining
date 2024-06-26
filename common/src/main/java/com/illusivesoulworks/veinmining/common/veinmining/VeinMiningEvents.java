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

import com.illusivesoulworks.veinmining.VeinMiningMod;
import com.illusivesoulworks.veinmining.common.config.VeinMiningConfig;
import com.illusivesoulworks.veinmining.common.veinmining.enchantment.ItemProcessor;
import com.illusivesoulworks.veinmining.common.veinmining.enchantment.VeinMiningEnchantment;
import com.illusivesoulworks.veinmining.common.veinmining.logic.BlockProcessor;
import com.illusivesoulworks.veinmining.common.veinmining.logic.VeinMiningLogic;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
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
    ItemProcessor.rebuild();
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

    if (!ItemStack.isSameItem(to, from) && livingEntity instanceof Player player) {
      boolean needsEnchantment = VeinMiningConfig.SERVER.maxBlocksBase.get() == 0;

      if (needsEnchantment && slot == EquipmentSlot.MAINHAND) {

        if (!(to.getItem() instanceof EnchantedBookItem)) {
          VeinMiningConfig.TutorialMode mode =
              VeinMiningConfig.CLIENT.enchantmentTutorialMode.get();

          if (mode == VeinMiningConfig.TutorialMode.ALL ||
              mode == VeinMiningConfig.TutorialMode.NOTIFICATION_ONLY) {
            int level = EnchantmentHelper.getItemEnchantmentLevel(VeinMiningMod.ENCHANTMENT, to);

            if (level > 0) {
              player.displayClientMessage(getTutorialMessage(), true);
            }
          }
        }
      }
    }
  }

  public static MutableComponent getTutorialMessage() {
    MutableComponent component = null;

    switch (VeinMiningConfig.CLIENT.activationState.get()) {
      case STANDING ->
          component = Component.translatable("tutorial.veinmining.enchantment.standing");
      case CROUCHING ->
          component = Component.translatable("tutorial.veinmining.enchantment.crouching");
      case HOLD_KEY_DOWN -> {
        Component name = VeinMiningKey.get().getTranslatedKeyMessage();

        if (name instanceof MutableComponent mutableComponent) {
          mutableComponent.withStyle(ChatFormatting.YELLOW);
        }
        component = Component.translatable("tutorial.veinmining.enchantment.key", name);
      }
    }
    return component;
  }
}
