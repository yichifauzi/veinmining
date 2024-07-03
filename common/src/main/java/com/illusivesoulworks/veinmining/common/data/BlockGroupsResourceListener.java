package com.illusivesoulworks.veinmining.common.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.illusivesoulworks.veinmining.VeinMiningConstants;
import com.illusivesoulworks.veinmining.common.veinmining.VeinMiningEvents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

public class BlockGroupsResourceListener extends SimpleJsonResourceReloadListener {

  private static final Gson GSON =
      (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
  public static BlockGroupsResourceListener INSTANCE;

  private List<Set<String>> groups = new ArrayList<>();

  public BlockGroupsResourceListener() {
    super(GSON, "veinmining");
  }

  @Override
  protected void apply(@Nonnull Map<ResourceLocation, JsonElement> object,
                       @Nonnull ResourceManager resourceManager,
                       @Nonnull ProfilerFiller profilerFiller) {
    Map<ResourceLocation, JsonElement> sorted = new LinkedHashMap<>();
    resourceManager.listPacks().forEach(packResources -> {
      Set<String> namespaces = packResources.getNamespaces(PackType.SERVER_DATA);
      namespaces.forEach(
          namespace -> packResources.listResources(PackType.SERVER_DATA, namespace, "veinmining",
              (resourceLocation, inputStreamIoSupplier) -> {
                ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(namespace, "groups");
                JsonElement el = object.get(rl);

                if (el != null) {
                  sorted.put(rl, el);
                }
              }));
    });
    List<Set<String>> blocks = new ArrayList<>();

    for (Map.Entry<ResourceLocation, JsonElement> entry : sorted.entrySet()) {
      ResourceLocation resourcelocation = entry.getKey();

      try {
        fromJson(blocks, GsonHelper.convertToJsonObject(entry.getValue(), "top element"));
      } catch (IllegalArgumentException | JsonParseException e) {
        VeinMiningConstants.LOG.error("Parsing error loading veinmining groups {}",
            resourcelocation, e);
      }
    }
    this.groups = blocks;
    VeinMiningEvents.reloadDatapack();
  }

  public List<Set<String>> getGroups() {
    return Collections.unmodifiableList(this.groups);
  }

  private static void fromJson(List<Set<String>> blocks, JsonObject jsonObject)
      throws IllegalArgumentException, JsonParseException {
    boolean replace = GsonHelper.getAsBoolean(jsonObject, "replace", false);
    JsonArray groups = GsonHelper.getAsJsonArray(jsonObject, "groups", new JsonArray());

    if (replace) {
      blocks.clear();
    }

    for (JsonElement element : groups) {

      if (element.isJsonArray()) {
        JsonArray jsonArray = element.getAsJsonArray();
        Set<String> newBlocks = new HashSet<>();

        for (JsonElement element1 : jsonArray) {

          if (element1 instanceof JsonPrimitive) {
            String block = element1.getAsString();
            newBlocks.add(block);
          }
        }
        blocks.add(newBlocks);
      }
    }
  }
}
