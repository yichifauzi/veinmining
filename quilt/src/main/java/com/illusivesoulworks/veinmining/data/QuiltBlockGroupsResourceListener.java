package com.illusivesoulworks.veinmining.data;

import com.illusivesoulworks.veinmining.VeinMiningConstants;
import com.illusivesoulworks.veinmining.common.data.BlockGroupsResourceListener;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;

public class QuiltBlockGroupsResourceListener extends BlockGroupsResourceListener implements
    IdentifiableResourceReloader {

  private static final ResourceLocation ID =
      new ResourceLocation(VeinMiningConstants.MOD_ID, "groups");

  @Override
  public @Nonnull ResourceLocation getQuiltId() {
    return ID;
  }
}
