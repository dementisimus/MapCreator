package dev.dementisimus.mapcreator.creator;

import dev.dementisimus.capi.core.helpers.Helper;
import dev.dementisimus.capi.core.helpers.bukkit.BukkitHelper;
import dev.dementisimus.mapcreator.MapCreator;
import dev.dementisimus.relocation.gson.Gson;
import dev.dementisimus.relocation.gson.GsonBuilder;
import dev.dementisimus.relocation.gson.JsonArray;
import dev.dementisimus.relocation.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class PreviousLocationObject @ MapCreator
 *
 * @author dementisimus
 * @since 11.11.2020:20:42
 */
public class PreviousLocationObject {

    private String player;
    private List<String> contents;
    private int heldItemSlot;
    private GameMode gameMode;
    private Map<String, Object> location;
    private boolean isFlying;
    private boolean allowFlight;

    public PreviousLocationObject(Player player) {
        this.player = player.getUniqueId().toString();
        this.contents = BukkitHelper.encodeItems(player.getInventory().getContents());
        this.heldItemSlot = player.getInventory().getHeldItemSlot();
        this.gameMode = player.getGameMode();
        this.location = player.getLocation().serialize();
        this.isFlying = player.isFlying();
        this.allowFlight = player.getAllowFlight();
    }

    public static PreviousLocationObject fromJsonString(Player player, String jsonString) {
        PreviousLocationObject previousLocationObject = new PreviousLocationObject(player);
        JsonObject jsonObject = getGson().fromJson(jsonString, JsonObject.class);
        previousLocationObject.setPlayer(jsonObject.get("player").getAsString());
        previousLocationObject.setContents(Helper.toList(jsonObject.get("contents").getAsJsonArray()));
        previousLocationObject.setHeldItemSlot(jsonObject.get("heldItemSlot").getAsInt());
        previousLocationObject.setGameMode(GameMode.valueOf(jsonObject.get("gameMode").getAsString()));
        previousLocationObject.setLocation(Helper.toMap(jsonObject.get("location").getAsString()));
        previousLocationObject.setFlying(jsonObject.get("isFlying").getAsBoolean());
        previousLocationObject.setAllowFlight(jsonObject.get("allowFlight").getAsBoolean());
        return previousLocationObject;
    }

    private static Gson getGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    public void restorePreviousLocation(boolean teleport, boolean resetPreviousLocation) {
        Player p = Bukkit.getPlayer(UUID.fromString(getPlayer()));
        Bukkit.getScheduler().runTask(MapCreator.getMapCreator(), () -> {
            if(p != null) {
                if(teleport) {
                    if(Bukkit.getWorld(getLocation().get("world").toString()) != null) {
                        p.teleport(Location.deserialize(getLocation()));
                    }
                }
                p.setGameMode(getGameMode());
                List<ItemStack> itemList = BukkitHelper.decodeItems(getContents());
                ItemStack[] items = new ItemStack[itemList.size()];
                p.getInventory().setContents(itemList.toArray(items));
                p.getInventory().setHeldItemSlot(getHeldItemSlot());
                p.setAllowFlight(getAllowFlight());
                p.setFlying(isFlying());
                if(resetPreviousLocation) {
                    AbstractCreator.resetPreviousLocation(p);
                }
            }
        });
    }

    public String toJsonString() {
        JsonObject jsonObject = new JsonObject();
        JsonArray array = new JsonArray();
        for(String item : getContents()) {
            array.add(item);
        }
        jsonObject.addProperty("player", getPlayer());
        jsonObject.add("contents", array);
        jsonObject.addProperty("heldItemSlot", getHeldItemSlot());
        jsonObject.addProperty("gameMode", getGameMode().name());
        jsonObject.addProperty("location", getLocation().toString());
        jsonObject.addProperty("isFlying", isFlying());
        jsonObject.addProperty("allowFlight", getAllowFlight());
        return getGson().toJson(jsonObject);
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public List<String> getContents() {
        return contents;
    }

    public void setContents(List<String> contents) {
        this.contents = contents;
    }

    public int getHeldItemSlot() {
        return heldItemSlot;
    }

    public void setHeldItemSlot(int heldItemSlot) {
        this.heldItemSlot = heldItemSlot;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Map<String, Object> getLocation() {
        return location;
    }

    public void setLocation(Map<String, Object> location) {
        this.location = location;
    }

    public boolean isFlying() {
        return isFlying;
    }

    public void setFlying(boolean flying) {
        isFlying = flying;
    }

    public boolean getAllowFlight() {
        return allowFlight;
    }

    public void setAllowFlight(boolean allowFlight) {
        this.allowFlight = allowFlight;
    }

}
