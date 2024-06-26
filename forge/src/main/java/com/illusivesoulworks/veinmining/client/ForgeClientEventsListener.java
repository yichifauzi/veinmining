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

import com.illusivesoulworks.veinmining.VeinMiningMod;
import com.illusivesoulworks.veinmining.common.config.VeinMiningConfig;
import com.illusivesoulworks.veinmining.common.veinmining.VeinMiningKey;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeClientEventsListener {

  @SubscribeEvent
  @SuppressWarnings("unused")
  public void veinMiningState(final TickEvent.ClientTickEvent evt) {

    if (evt.phase == TickEvent.Phase.END) {
      VeinMiningClientEvents.tick();
    }
  }

  @SubscribeEvent
  @SuppressWarnings("unused")
  public void itemTooltip(final ItemTooltipEvent evt) {
    VeinMiningClientEvents.tooltip(evt.getItemStack(), evt.getToolTip());
  }
}
