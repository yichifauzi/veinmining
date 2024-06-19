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

package com.illusivesoulworks.veinmining.common.veinmining.enchantment;

import com.illusivesoulworks.veinmining.VeinMiningConstants;
import com.illusivesoulworks.veinmining.common.config.VeinMiningConfig;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class VeinMiningEnchantment extends Enchantment {

  private static final TagKey<Item> TAG = TagKey.create(Registries.ITEM,
      new ResourceLocation(VeinMiningConstants.MOD_ID, "enchantable/vein_mining"));

  public VeinMiningEnchantment() {
    super(
        Enchantment.definition(
            TAG,
            VeinMiningConfig.COMMON.weight.get(),
            VeinMiningConfig.COMMON.levels.get(),
            Enchantment.dynamicCost(VeinMiningConfig.COMMON.minCostBase.get(),
                VeinMiningConfig.COMMON.minCostPerLevel.get()),
            Enchantment.dynamicCost(VeinMiningConfig.COMMON.maxCostBase.get(),
                VeinMiningConfig.COMMON.maxCostPerLevel.get()),
            VeinMiningConfig.COMMON.anvilCost.get(),
            EquipmentSlot.MAINHAND
        )
    );
  }

  private static boolean canEnchantStack(ItemStack stack) {

    for (ItemProcessor.ItemChecker entry : ItemProcessor.ITEM_CHECKERS) {

      if (entry.test(stack)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isTreasureOnly() {
    return VeinMiningConfig.COMMON.isTreasure.get();
  }

  @Override
  public boolean isTradeable() {
    return VeinMiningConfig.COMMON.isVillagerTrade.get();
  }

  @Override
  public boolean isDiscoverable() {
    return VeinMiningConfig.COMMON.isLootable.get();
  }

  @Override
  protected boolean checkCompatibility(@Nonnull Enchantment ench) {
    return !VeinMiningConfig.COMMON.incompatibleEnchantments.getTransformed().contains(ench) &&
        super.checkCompatibility(ench);
  }

  @Override
  public boolean canEnchant(@Nonnull ItemStack stack) {
    return stack.is(this.getSupportedItems()) || canEnchantStack(stack);
  }

  public boolean canApplyAtEnchantingTable(@Nonnull ItemStack stack) {
    return this.canEnchant(stack) && VeinMiningConfig.COMMON.canApplyAtEnchantingTable.get();
  }

  public boolean isAllowedOnBooks() {
    return VeinMiningConfig.COMMON.canApplyOnBooks.get();
  }

  public boolean allowedInCreativeTab(Item book, Set<TagKey<Item>> allowedCategories) {
    return this.isAllowedOnBooks();
  }
}
