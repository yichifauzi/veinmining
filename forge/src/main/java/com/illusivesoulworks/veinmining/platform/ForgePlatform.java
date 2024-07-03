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
import com.illusivesoulworks.veinmining.common.network.VeinMiningForgeNetwork;
import com.illusivesoulworks.veinmining.common.platform.services.IPlatform;
import com.illusivesoulworks.veinmining.common.veinmining.VeinMiningPlayers;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

public class ForgePlatform implements IPlatform {

  @Override
  public void sendNotifyS2C(ServerPlayer player) {
    VeinMiningForgeNetwork.get()
        .send(SPacketNotify.INSTANCE, PacketDistributor.PLAYER.with(player));
  }

  @Override
  public Set<String> getBlocksFromTag(ResourceLocation resourceLocation) {
    Set<String> result = new HashSet<>();
    ITagManager<Block> tagManager = ForgeRegistries.BLOCKS.tags();

    if (tagManager != null) {

      for (Block block : tagManager.getTag(
          tagManager.createOptionalTagKey(resourceLocation, new HashSet<>()))) {
        ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);

        if (rl != null) {
          result.add(rl.toString());
        }
      }
    }
    return result;
  }

  @Override
  public Block getBlock(ResourceLocation resourceLocation) {
    return ForgeRegistries.BLOCKS.getValue(resourceLocation);
  }

  @Override
  public Optional<ResourceLocation> getResourceLocation(Block block) {
    return Optional.ofNullable(ForgeRegistries.BLOCKS.getKey(block));
  }

  @Override
  public boolean canHarvestDrops(ServerPlayer playerEntity, BlockState state, BlockPos blockPos) {
    return ForgeHooks.isCorrectToolForDrops(state, playerEntity);
  }

  @Override
  public boolean harvest(ServerPlayer player, BlockPos pos, BlockPos originPos) {
    ServerLevel world = player.serverLevel();
    BlockState blockstate = world.getBlockState(pos);
    GameType gameType = player.gameMode.getGameModeForPlayer();
    int exp = ForgeHooks.onBlockBreakEvent(world, gameType, player, pos);

    if (exp == -1) {
      return false;
    } else {
      BlockEntity blockentity = world.getBlockEntity(pos);
      Block block = blockstate.getBlock();

      if ((block instanceof CommandBlock || block instanceof StructureBlock ||
          block instanceof JigsawBlock) && !player.canUseGameMasterBlocks()) {
        world.sendBlockUpdated(pos, blockstate, blockstate, 3);
        return false;
      } else if (player.getMainHandItem().onBlockStartBreak(pos, player)) {
        return true;
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
            ForgeEventFactory.onPlayerDestroyItem(player, itemstack1, InteractionHand.MAIN_HAND);
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

          if (flag && exp > 0) {
            blockstate.getBlock().popExperience(world, spawnPos, exp);
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
