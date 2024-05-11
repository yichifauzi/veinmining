package com.illusivesoulworks.veinmining;

import com.illusivesoulworks.spectrelib.config.SpectreConfigInitializer;
import com.illusivesoulworks.veinmining.common.config.VeinMiningConfig;

public class VeinMiningConfigInitializer implements SpectreConfigInitializer {

  @Override
  public void onInitializeConfig() {
    VeinMiningConfig.setup();
  }
}
