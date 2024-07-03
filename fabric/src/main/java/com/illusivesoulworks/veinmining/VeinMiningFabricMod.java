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
import com.illusivesoulworks.veinmining.common.data.FabricBlockGroupsResourceListener;
import com.illusivesoulworks.veinmining.common.network.CPacketState;
import com.illusivesoulworks.veinmining.common.network.SPacketNotify;
import com.illusivesoulworks.veinmining.common.veinmining.VeinMiningEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;

public class VeinMiningFabricMod implements ModInitializer {

  @Override
  public void onInitialize() {
    ServerLifecycleEvents.SERVER_STARTED.register(server -> VeinMiningEvents.reloadDatapack());
    ServerTickEvents.END_WORLD_TICK.register(VeinMiningEvents::tick);
    ResourceManagerHelper resourceManagerHelper = ResourceManagerHelper.get(PackType.SERVER_DATA);
    FabricBlockGroupsResourceListener fabricBlockGroupsResourceListener =
        new FabricBlockGroupsResourceListener();
    BlockGroupsResourceListener.INSTANCE = fabricBlockGroupsResourceListener;
    resourceManagerHelper.registerReloadListener(fabricBlockGroupsResourceListener);
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> VeinMiningEvents.playerLoggedOut(handler.getPlayer()));
    PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
      if (player instanceof ServerPlayer serverPlayer) {
        VeinMiningEvents.blockBreak(serverPlayer, pos, state);
      }
    });
    PayloadTypeRegistry.playC2S().register(CPacketState.TYPE, CPacketState.STREAM_CODEC);
    PayloadTypeRegistry.playS2C().register(SPacketNotify.TYPE, SPacketNotify.STREAM_CODEC);
    ServerPlayNetworking.registerGlobalReceiver(CPacketState.TYPE, (payload, context) -> {
      ServerPlayer player = context.player();
      MinecraftServer server = player.getServer();
      boolean activate = payload.activate();

      if (server != null) {
        server.execute(() -> CPacketState.handle(activate, player));
      }
    });
    ServerEntityEvents.EQUIPMENT_CHANGE.register(
        (livingEntity, equipmentSlot, previousStack, currentStack) -> VeinMiningEvents.toolEquip(
            currentStack, previousStack, equipmentSlot, livingEntity));
  }
}
