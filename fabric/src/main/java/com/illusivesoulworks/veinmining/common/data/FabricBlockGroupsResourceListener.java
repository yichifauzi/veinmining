package com.illusivesoulworks.veinmining.common.data;

import com.illusivesoulworks.veinmining.VeinMiningConstants;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class FabricBlockGroupsResourceListener extends BlockGroupsResourceListener implements
    IdentifiableResourceReloadListener {

  private static final ResourceLocation ID =
      ResourceLocation.fromNamespaceAndPath(VeinMiningConstants.MOD_ID, "groups");

  @Override
  public ResourceLocation getFabricId() {
    return ID;
  }
}
