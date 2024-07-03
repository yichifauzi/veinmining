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

package com.illusivesoulworks.veinmining.common.config;

import com.illusivesoulworks.spectrelib.config.SpectreConfig;
import com.illusivesoulworks.spectrelib.config.SpectreConfigLoader;
import com.illusivesoulworks.spectrelib.config.SpectreConfigSpec;
import com.illusivesoulworks.veinmining.VeinMiningConstants;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class VeinMiningConfig {

  public static final SpectreConfigSpec SERVER_SPEC;
  public static final SpectreConfigSpec CLIENT_SPEC;
  public static final Server SERVER;
  public static final Client CLIENT;
  private static final String CONFIG_PREFIX = "gui." + VeinMiningConstants.MOD_ID + ".config.";

  static {
    final Pair<Server, SpectreConfigSpec> specPairServer = new SpectreConfigSpec.Builder()
        .configure(Server::new);
    SERVER_SPEC = specPairServer.getRight();
    SERVER = specPairServer.getLeft();
    final Pair<Client, SpectreConfigSpec> specPairClient = new SpectreConfigSpec.Builder()
        .configure(Client::new);
    CLIENT_SPEC = specPairClient.getRight();
    CLIENT = specPairClient.getLeft();
  }

  public static void setup() {
    String id = VeinMiningConstants.MOD_ID;
    SpectreConfigLoader.add(SpectreConfig.Type.SERVER, VeinMiningConfig.SERVER_SPEC, id);
    SpectreConfigLoader.add(SpectreConfig.Type.CLIENT, VeinMiningConfig.CLIENT_SPEC, id);
  }

  public static class Server {

    public final SpectreConfigSpec.DoubleValue requiredDestroySpeed;
    public final SpectreConfigSpec.BooleanValue requireCorrectTool;
    public final SpectreConfigSpec.IntValue maxBlocksBase;
    public final SpectreConfigSpec.IntValue maxBlocksPerLevel;
    public final SpectreConfigSpec.BooleanValue diagonalMining;
    public final SpectreConfigSpec.BooleanValue relocateDrops;
    public final SpectreConfigSpec.BooleanValue preventToolDestruction;
    public final SpectreConfigSpec.BooleanValue addToolDamage;
    public final SpectreConfigSpec.IntValue toolDamageMultiplier;
    public final SpectreConfigSpec.BooleanValue addExhaustion;
    public final SpectreConfigSpec.DoubleValue exhaustionMultiplier;
    public final SpectreConfigSpec.BooleanValue limitedByDurability;
    public final SpectreConfigSpec.BooleanValue limitedByWorld;
    public final SpectreConfigSpec.EnumValue<BlocksType> blocks;
    public final SpectreConfigSpec.TransformableValue<List<? extends String>, Set<String>>
        blocksList;
    public final SpectreConfigSpec.EnumValue<ListType> blocksListType;

    public Server(SpectreConfigSpec.Builder builder) {

      requiredDestroySpeed = builder.comment(
              "The minimum destroy speed the used tool needs on the block to vein mine.")
          .translation(CONFIG_PREFIX + "requiredDestroySpeed")
          .defineInRange("requiredDestroySpeed", 1.0D, 0.0D, 100.0D);

      requireCorrectTool =
          builder.comment(
                  "If enabled, vein mining will not activate if the used tool cannot harvest drops from the source block.")
              .translation(CONFIG_PREFIX + "requireCorrectTool")
              .define("requireCorrectTool", false);

      maxBlocksBase =
          builder.comment("The maximum number of blocks to vein mine without the enchantment.")
              .translation(CONFIG_PREFIX + "maxBlocksBase")
              .defineInRange("maxBlocksBase", 0, 0, 1000);

      maxBlocksPerLevel =
          builder.comment("The maximum number of blocks to vein mine per level of the enchantment.")
              .translation(CONFIG_PREFIX + "maxBlocksPerLevel")
              .defineInRange("maxBlocksPerLevel", 50, 1, 1000);

      diagonalMining =
          builder.comment("If enabled, vein mining can mine diagonally.")
              .translation(CONFIG_PREFIX + "diagonalMining")
              .define("diagonalMining", true);

      limitedByDurability =
          builder.comment("If enabled, vein mining will stop when the tool can no longer be used.")
              .translation(CONFIG_PREFIX + "limitedByDurability")
              .define("limitedByDurability", true);

      relocateDrops = builder.comment(
              "If enabled, vein mining will move drops from blocks to the source location.")
          .translation(CONFIG_PREFIX + "relocateDrops")
          .define("relocateDrops", true);

      preventToolDestruction =
          builder.comment("If enabled, vein mining will never break tools.")
              .translation(CONFIG_PREFIX + "preventToolDestruction")
              .define("preventToolDestruction", true);

      addToolDamage =
          builder.comment("If enabled, vein mining will damage the tool for each block mined.")
              .translation(CONFIG_PREFIX + "addToolDamage")
              .define("addToolDamage", true);

      toolDamageMultiplier =
          builder.comment("The multiplier to tool damage from blocks that are vein mined.")
              .translation(CONFIG_PREFIX + "toolDamageMultiplier")
              .defineInRange("toolDamageMultiplier", 1, 0, 1000);

      addExhaustion =
          builder.comment(
                  "If enabled, vein mining will cause player exhaustion for each block mined.")
              .translation(CONFIG_PREFIX + "addExhaustion")
              .define("addExhaustion", true);

      exhaustionMultiplier =
          builder.comment("The multiplier to player exhaustion from blocks that are vein mined.")
              .translation(CONFIG_PREFIX + "exhaustionMultiplier")
              .defineInRange("exhaustionMultiplier", 1.0F, 0.0F, 1000.0F);

      limitedByWorld =
          builder.comment(
                  "If enabled, vein mining will not attempt to mine past the boundary of a world.")
              .translation(CONFIG_PREFIX + "limitedByWorld")
              .define("limitedByWorld", true);

      blocks = builder.comment(
              "Determines the vein mineable blocks based on a preset option or a configured list.")
          .translation(CONFIG_PREFIX + "blocks")
          .defineEnum("blocks", BlocksType.CONFIG_LIST);

      blocksList = builder.comment(
              "The blocks or block tags for vein mining if blocks is set to \"CONFIG_LIST\".")
          .translation(CONFIG_PREFIX + "blocksList")
          .defineList("blocksList", List.of("#c:ores", "#forge:ores", "#minecraft:logs"),
              s -> s instanceof String, Set::copyOf);

      blocksListType =
          builder.comment("Determines if blocksList contains allowed blocks or denied blocks.")
              .translation(CONFIG_PREFIX + "blocksListType")
              .defineEnum("blocksListType", ListType.ALLOW);
    }
  }

  public static class Client {

    public final SpectreConfigSpec.EnumValue<VeinMiningConfig.ActivationState> activationState;
    public final SpectreConfigSpec.EnumValue<VeinMiningConfig.ActivationState>
        activationStateWithoutEnchantment;

    public Client(SpectreConfigSpec.Builder builder) {

      activationState = builder.comment(
              "If maxBlocksBase is 0, determines how to activate vein mining.")
          .translation(CONFIG_PREFIX + "activationState")
          .defineEnum("activationState", ActivationState.STANDING);

      activationStateWithoutEnchantment = builder.comment(
              "If maxBlocksBase is greater than 0, determines how to activate vein mining.")
          .translation(CONFIG_PREFIX + "activationStateWithoutEnchantment")
          .defineEnum("activationStateWithoutEnchantment", ActivationState.HOLD_KEY_DOWN);
    }
  }

  public enum ListType {
    ALLOW,
    DENY
  }

  public enum ActivationState {
    STANDING,
    CROUCHING,
    HOLD_KEY_DOWN
  }

  public enum BlocksType {
    CONFIG_LIST,
    ALL,
    ORES,
    ORES_LOGS,
    ORES_STONE,
    ORES_STONE_LOGS,
    NO_BLOCK_ENTITIES
  }
}
