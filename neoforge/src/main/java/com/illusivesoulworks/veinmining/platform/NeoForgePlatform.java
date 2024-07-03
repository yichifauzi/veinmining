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

import com.illusivesoulworks.veinmining.common.config.VeinMiningConfig;
import com.illusivesoulworks.veinmining.common.network.SPacketNotify;
import com.illusivesoulworks.veinmining.common.platform.services.IPlatform;
import com.illusivesoulworks.veinmining.common.veinmining.VeinMiningPlayers;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgePlatform implements IPlatform {

  @Override
  public void sendNotifyS2C(ServerPlayer player) {
    PacketDistributor.sendToPlayer(player, SPacketNotify.INSTANCE);
  }

  @Override
  public Set<String> getBlocksFromTag(ResourceLocation resourceLocation) {
    Set<String> result = new HashSet<>();
    BuiltInRegistries.BLOCK.getTag(TagKey.create(Registries.BLOCK, resourceLocation))
        .ifPresent(block -> {
          for (Holder<Block> blockHolder : block) {
            blockHolder.unwrapKey().ifPresent(key -> result.add(key.location().toString()));
          }
        });
    return result;
  }

  @Override
  public Block getBlock(ResourceLocation resourceLocation) {
    return BuiltInRegistries.BLOCK.get(resourceLocation);
  }

  @Override
  public Optional<ResourceLocation> getResourceLocation(Block block) {
    return Optional.of(BuiltInRegistries.BLOCK.getKey(block));
  }

  @Override
  public boolean canHarvestDrops(ServerPlayer playerEntity, BlockState state, BlockPos pos) {
    return EventHooks.doPlayerHarvestCheck(playerEntity, state, playerEntity.level(), pos);
  }

  public boolean harvest(ServerPlayer player, BlockPos pos, BlockPos originPos) {
    ServerLevel world = player.serverLevel();
    BlockState blockstate = world.getBlockState(pos);
    GameType gameType = player.gameMode.getGameModeForPlayer();
    BlockEvent.BreakEvent evt =
        CommonHooks.fireBlockBreak(world, gameType, player, pos, blockstate);

    if (evt.isCanceled()) {
      return false;
    } else {
      BlockEntity blockentity = world.getBlockEntity(pos);
      Block block = blockstate.getBlock();

      if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
        world.sendBlockUpdated(pos, blockstate, blockstate, 3);
        return false;
      } else if (player.blockActionRestricted(world, pos, gameType)) {
        return false;
      } else {

        if (gameType.isCreative()) {
          removeBlock(player, pos, false);
        } else {
          ItemStack itemstack = player.getMainHandItem();
          ItemStack itemstack1 = itemstack.copy();
          boolean flag1 = blockstate.canHarvestBlock(world, pos, player);
          itemstack.mineBlock(world, blockstate, pos, player);

          if (itemstack.isEmpty() && !itemstack1.isEmpty()) {
            EventHooks.onPlayerDestroyItem(player, itemstack1, InteractionHand.MAIN_HAND);
          }
          boolean flag = removeBlock(player, pos, flag1);
          BlockPos spawnPos = VeinMiningConfig.SERVER.relocateDrops.get() ? originPos : pos;

          if (flag && flag1) {
            FoodData foodData = player.getFoodData();
            float currentExhaustion = foodData.getExhaustionLevel();
            VeinMiningPlayers.addMiningBlock(world, pos, spawnPos);
            block.playerDestroy(world, player, pos, blockstate, blockentity, itemstack1);
            VeinMiningPlayers.removeMiningBlock(world, pos);

            if (VeinMiningConfig.SERVER.addExhaustion.get()) {
              float diff = foodData.getExhaustionLevel() - currentExhaustion;
              foodData.setExhaustion(currentExhaustion);
              foodData.addExhaustion(
                  (float) (diff * VeinMiningConfig.SERVER.exhaustionMultiplier.get()));
            } else {
              foodData.setExhaustion(currentExhaustion);
            }
          }
        }
        return true;
      }
    }
  }

  private static boolean removeBlock(Player player, BlockPos pos, boolean canHarvest) {
    Level world = player.getCommandSenderWorld();
    BlockState state = world.getBlockState(pos);
    boolean removed =
        state.onDestroyedByPlayer(world, pos, player, canHarvest, world.getFluidState(pos));

    if (removed) {
      state.getBlock().destroy(world, pos, state);
    }
    return removed;
  }
}
