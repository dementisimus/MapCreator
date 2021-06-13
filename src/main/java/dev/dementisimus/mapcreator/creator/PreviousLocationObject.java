package dev.dementisimus.mapcreator.creator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.dementisimus.capi.core.helpers.Helper;
import dev.dementisimus.capi.core.helpers.bukkit.BukkitHelper;
import dev.dementisimus.mapcreator.MapCreator;
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
        Player player = Bukkit.getPlayer(UUID.fromString(this.getPlayer()));
        Bukkit.getScheduler().runTask(MapCreator.getMapCreator(), () -> {
            if(player != null) {
                if(teleport) {
                    if(Bukkit.getWorld(this.getLocation().get("world").toString()) != null) {
                        player.teleport(Location.deserialize(this.getLocation()));
                    }
                }
                player.setGameMode(this.getGameMode());
                List<ItemStack> itemList = BukkitHelper.decodeItems(this.getContents());
                ItemStack[] items = new ItemStack[itemList.size()];
                player.getInventory().setContents(itemList.toArray(items));
                player.getInventory().setHeldItemSlot(this.getHeldItemSlot());
                player.setAllowFlight(this.getAllowFlight());
                player.setFlying(this.isFlying());
                if(resetPreviousLocation) {
                    AbstractCreator.resetPreviousLocation(player);
                }
            }
        });
    }

    public String toJsonString() {
        JsonObject jsonObject = new JsonObject();
        JsonArray array = new JsonArray();
        for(String item : this.getContents()) {
            array.add(item);
        }
        jsonObject.addProperty("player", this.getPlayer());
        jsonObject.add("contents", array);
        jsonObject.addProperty("heldItemSlot", this.getHeldItemSlot());
        jsonObject.addProperty("gameMode", this.getGameMode().name());
        jsonObject.addProperty("location", this.getLocation().toString());
        jsonObject.addProperty("isFlying", this.isFlying());
        jsonObject.addProperty("allowFlight", this.getAllowFlight());
        return getGson().toJson(jsonObject);
    }

    public String getPlayer() {
        return this.player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public List<String> getContents() {
        return this.contents;
    }

    public void setContents(List<String> contents) {
        this.contents = contents;
    }

    public int getHeldItemSlot() {
        return this.heldItemSlot;
    }

    public void setHeldItemSlot(int heldItemSlot) {
        this.heldItemSlot = heldItemSlot;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Map<String, Object> getLocation() {
        return this.location;
    }

    public void setLocation(Map<String, Object> location) {
        this.location = location;
    }

    public boolean isFlying() {
        return this.isFlying;
    }

    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }

    public boolean getAllowFlight() {
        return this.allowFlight;
    }

    public void setAllowFlight(boolean allowFlight) {
        this.allowFlight = allowFlight;
    }

}
