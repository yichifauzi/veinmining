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

package com.illusivesoulworks.veinmining.platform;

import com.google.common.collect.ImmutableMap;
import com.illusivesoulworks.veinmining.VeinMiningFabricMod;
import com.illusivesoulworks.veinmining.common.config.VeinMiningConfig;
import com.illusivesoulworks.veinmining.common.platform.services.IPlatform;
import com.illusivesoulworks.veinmining.common.veinmining.VeinMiningPlayers;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FabricPlatform implements IPlatform {

  @Override
  public Enchantment getVeinMiningEnchantment() {
    return VeinMiningFabricMod.VEIN_MINING_ENCHANTMENT;
  }

  @Override
  public Optional<Enchantment> getEnchantment(ResourceLocation resourceLocation) {
    return Optional.ofNullable(BuiltInRegistries.ENCHANTMENT.get(resourceLocation));
  }

  @Override
  public Optional<Item> getItem(ResourceLocation resourceLocation) {
    return Optional.of(BuiltInRegistries.ITEM.get(resourceLocation));
  }

  @Override
  public Optional<ResourceLocation> getResourceLocation(Block block) {
    return Optional.of(BuiltInRegistries.BLOCK.getKey(block));
  }

  @Override
  public Map<String, Predicate<ItemStack>> buildEnchantableItems() {
    return ImmutableMap.of();
  }

  @Override
  public boolean canHarvestDrops(ServerPlayer playerEntity, BlockState state) {
    return playerEntity.hasCorrectToolForDrops(state);
  }

  @Override
  public boolean harvest(ServerPlayer player, BlockPos pos, BlockPos originPos) {
    Level level = player.level();
    BlockState blockState = level.getBlockState(pos);
    GameType gameModeForPlayer = player.gameMode.getGameModeForPlayer();

    if (!player.getMainHandItem().getItem().canAttackBlock(blockState, level, pos, player)) {
      return false;
    }
    BlockEntity blockEntity = level.getBlockEntity(pos);
    Block block = blockState.getBlock();

    if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
      level.sendBlockUpdated(pos, blockState, blockState, 3);
      return false;
    }
    if (player.blockActionRestricted(level, pos, gameModeForPlayer)) {
      return false;
    }
    boolean result = PlayerBlockBreakEvents.BEFORE.invoker()
        .beforeBlockBreak(level, player, pos, blockState, blockEntity);

    if (!result) {
      PlayerBlockBreakEvents.CANCELED.invoker()
          .onBlockBreakCanceled(level, player, pos, blockState, blockEntity);
      return false;
    }
    block.playerWillDestroy(level, pos, blockState, player);
    boolean bl = level.removeBlock(pos, false);

    if (bl) {
      PlayerBlockBreakEvents.AFTER.invoker()
          .afterBlockBreak(level, player, pos, blockState, blockEntity);
      block.destroy(level, pos, blockState);
    }
    if (player.isCreative()) {
      return true;
    }
    ItemStack itemStack = player.getMainHandItem();
    ItemStack itemStack2 = itemStack.copy();
    boolean bl2 = player.hasCorrectToolForDrops(blockState);
    itemStack.mineBlock(level, blockState, pos, player);

    if (bl && bl2) {
      BlockPos spawnPos = VeinMiningConfig.SERVER.relocateDrops.get() ? originPos : pos;
      FoodData foodData = player.getFoodData();
      float currentExhaustion = foodData.getExhaustionLevel();
      VeinMiningPlayers.addMiningBlock(level, pos, spawnPos);
      block.playerDestroy(level, player, pos, blockState, blockEntity, itemStack2);
      VeinMiningPlayers.removeMiningBlock(level, pos);

      if (VeinMiningConfig.SERVER.addExhaustion.get()) {
        float diff = foodData.getExhaustionLevel() - currentExhaustion;
        foodData.setExhaustion(currentExhaustion);
        foodData.addExhaustion(
            (float) (diff * VeinMiningConfig.SERVER.exhaustionMultiplier.get()));
      } else {
        foodData.setExhaustion(currentExhaustion);
      }
    }
    return true;
  }

  @Override
  public Set<String> getItemsFromTag(ResourceLocation resourceLocation) {
    Set<String> result = new HashSet<>();
    BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, resourceLocation))
        .ifPresent(item -> {
          for (Holder<Item> itemHolder : item) {
            itemHolder.unwrapKey().ifPresent(key -> result.add(key.location().toString()));
          }
        });
    return result;
  }

  @Override
  public List<String> getDefaultItemsConfig() {
    return Arrays.asList("#minecraft:axes", "#minecraft:pickaxes", "#minecraft:shovels", "#minecraft:hoes");
  }
}
