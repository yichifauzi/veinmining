package com.illusivesoulworks.veinmining.common.network;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class VeinMiningServerPayloadHandler {

  private static final VeinMiningServerPayloadHandler INSTANCE =
      new VeinMiningServerPayloadHandler();

  public static VeinMiningServerPayloadHandler getInstance() {
    return INSTANCE;
  }

  public void handleState(final CPacketState packet, final IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
          if (ctx.player() instanceof ServerPlayer serverPlayer) {
            CPacketState.handle(packet.activate(), serverPlayer);
          }
        })
        .exceptionally(e -> {
          ctx.disconnect(Component.translatable("veinmining.networking.failed", e.getMessage()));
          return null;
        });
  }
}
