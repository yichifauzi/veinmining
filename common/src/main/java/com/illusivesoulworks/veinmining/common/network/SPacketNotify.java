package com.illusivesoulworks.veinmining.common.network;

import net.minecraft.network.FriendlyByteBuf;

public record SPacketNotify() {

  public static void encode(SPacketNotify msg, FriendlyByteBuf buf) {
    // NO-OP
  }

  public static SPacketNotify decode(FriendlyByteBuf buf) {
    return new SPacketNotify();
  }

  public static void handle(SPacketNotify msg) {
    ClientPackets.handleNotification();
  }
}
