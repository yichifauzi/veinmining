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

package com.illusivesoulworks.veinmining.client;

import com.illusivesoulworks.veinmining.VeinMiningQuiltMod;
import com.illusivesoulworks.veinmining.common.network.SPacketNotify;
import com.illusivesoulworks.veinmining.common.veinmining.VeinMiningKey;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.tooltip.api.client.ItemTooltipCallback;

public class VeinMiningQuiltClientMod implements ClientModInitializer {

  @Override
  public void onInitializeClient(ModContainer modContainer) {
    VeinMiningKey.setup();
    KeyBindingHelper.registerKeyBinding(VeinMiningKey.get());
    ClientTickEvents.END.register(client -> VeinMiningClientEvents.tick());
    ItemTooltipCallback.EVENT.register(
        (stack, player, context, lines) -> VeinMiningClientEvents.tooltip(stack, lines));
    ClientPlayNetworking.registerGlobalReceiver(VeinMiningQuiltMod.NOTIFY_PACKET,
        (client, handler, buf, responseSender) -> client.execute(
            () -> SPacketNotify.handle(new SPacketNotify())));
  }
}
