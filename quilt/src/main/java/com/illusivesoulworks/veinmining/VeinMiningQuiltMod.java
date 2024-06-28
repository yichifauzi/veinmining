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

package com.illusivesoulworks.veinmining;

import com.illusivesoulworks.veinmining.common.data.BlockGroupsResourceListener;
import com.illusivesoulworks.veinmining.common.network.CPacketState;
import com.illusivesoulworks.veinmining.common.veinmining.VeinMiningEvents;
import com.illusivesoulworks.veinmining.data.QuiltBlockGroupsResourceListener;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;

public class VeinMiningQuiltMod implements ModInitializer {

  public static final ResourceLocation STATE_PACKET =
      new ResourceLocation(VeinMiningConstants.MOD_ID, "state");
  public static final ResourceLocation NOTIFY_PACKET =
      new ResourceLocation(VeinMiningConstants.MOD_ID, "notify");

  @Override
  public void onInitialize(ModContainer modContainer) {
    Registry.register(BuiltInRegistries.ENCHANTMENT, VeinMiningConstants.ENCHANTMENT_ID,
        VeinMiningMod.ENCHANTMENT);
    ServerLifecycleEvents.READY.register(server -> VeinMiningEvents.reloadDatapack());
    ResourceLoader resourceManagerHelper = ResourceLoader.get(PackType.SERVER_DATA);
    QuiltBlockGroupsResourceListener quiltBlockGroupsResourceListener =
        new QuiltBlockGroupsResourceListener();
    BlockGroupsResourceListener.INSTANCE = quiltBlockGroupsResourceListener;
    resourceManagerHelper.registerReloader(quiltBlockGroupsResourceListener);
    ServerWorldTickEvents.END.register((server, world) -> VeinMiningEvents.tick(world));
    PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
      if (player instanceof ServerPlayer serverPlayer) {
        VeinMiningEvents.blockBreak(serverPlayer, pos, state);
      }
    });
    ServerEntityEvents.EQUIPMENT_CHANGE.register(
        (livingEntity, equipmentSlot, previousStack, currentStack) -> VeinMiningEvents.toolEquip(
            currentStack, previousStack, equipmentSlot, livingEntity));
    ServerPlayNetworking.registerGlobalReceiver(STATE_PACKET,
        (server, player, handler, buf, responseSender) -> {
          CPacketState msg = CPacketState.decode(buf);
          server.execute(() -> CPacketState.handle(msg, player));
        });
  }
}
