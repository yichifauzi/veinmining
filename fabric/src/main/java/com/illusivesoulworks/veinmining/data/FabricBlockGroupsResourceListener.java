package com.illusivesoulworks.veinmining.data;

import com.illusivesoulworks.veinmining.VeinMiningConstants;
import com.illusivesoulworks.veinmining.common.data.BlockGroupsResourceListener;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class FabricBlockGroupsResourceListener extends BlockGroupsResourceListener implements
    IdentifiableResourceReloadListener {

  private static final ResourceLocation ID =
      new ResourceLocation(VeinMiningConstants.MOD_ID, "groups");

  @Override
  public ResourceLocation getFabricId() {
    return ID;
  }
}
