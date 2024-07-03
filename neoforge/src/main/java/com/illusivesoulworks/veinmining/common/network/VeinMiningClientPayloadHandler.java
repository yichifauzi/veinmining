package com.illusivesoulworks.veinmining.common.network;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class VeinMiningClientPayloadHandler {

  private static final VeinMiningClientPayloadHandler INSTANCE =
      new VeinMiningClientPayloadHandler();

  public static VeinMiningClientPayloadHandler getInstance() {
    return INSTANCE;
  }

  public void handleNotify(SPacketNotify msg, final IPayloadContext ctx) {
    ctx.enqueueWork(SPacketNotify::handle)
        .exceptionally(e -> {
          ctx.disconnect(Component.translatable("veinmining.networking.failed", e.getMessage()));
          return null;
        });
  }
}
