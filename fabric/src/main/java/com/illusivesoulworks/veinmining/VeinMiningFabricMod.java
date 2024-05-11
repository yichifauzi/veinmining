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

import com.illusivesoulworks.veinmining.common.network.CPacketState;
import com.illusivesoulworks.veinmining.common.veinmining.VeinMiningEvents;
import com.illusivesoulworks.veinmining.common.veinmining.enchantment.VeinMiningEnchantment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;

public class VeinMiningFabricMod implements ModInitializer {

  public static Enchantment VEIN_MINING_ENCHANTMENT;

  @Override
  public void onInitialize() {
    VEIN_MINING_ENCHANTMENT =
        Registry.register(BuiltInRegistries.ENCHANTMENT, VeinMiningConstants.ENCHANTMENT_ID,
            new VeinMiningEnchantment());
    ServerLifecycleEvents.SERVER_STARTED.register(server -> VeinMiningEvents.reloadDatapack());
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
        (server, resourceManager, success) -> VeinMiningEvents.reloadDatapack());
    ServerTickEvents.END_WORLD_TICK.register(VeinMiningEvents::tick);
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> VeinMiningEvents.playerLoggedOut(handler.getPlayer()));
    PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
      if (player instanceof ServerPlayer serverPlayer) {
        VeinMiningEvents.blockBreak(serverPlayer, pos, state);
      }
    });
    PayloadTypeRegistry.playC2S().register(CPacketState.TYPE, CPacketState.STREAM_CODEC);
    ServerPlayNetworking.registerGlobalReceiver(CPacketState.TYPE, (payload, context) -> {
      ServerPlayer player = context.player();
      MinecraftServer server = player.getServer();
      boolean activate = payload.activate();

      if (server != null) {
        server.execute(() -> CPacketState.handle(activate, player));
      }
    });
  }
}
