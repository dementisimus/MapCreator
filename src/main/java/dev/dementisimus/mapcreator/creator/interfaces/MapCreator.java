package dev.dementisimus.mapcreator.creator.interfaces;

import com.grinderwolf.swm.api.exceptions.*;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.plugin.config.WorldData;
import dev.dementisimus.capi.core.callback.Callback;
import dev.dementisimus.capi.core.callback.EmptyCallback;
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.CustomPlayerMapActions;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
/**
 * Copyright (c) by dementisimus,
 * licensed under Attribution-NonCommercial-NoDerivatives 4.0 International
 *
 * Class CustomMapCreator @ MapCreatorPlugin
 *
 * @author dementisimus
 * @since 24.07.2021:19:28
 */
public interface MapCreator {

    Map<String, CustomMapCreatorMap> MAP_CREATOR_MAPS = new HashMap<>();

    void perform(Action action, CustomMapCreatorMap customMapCreatorMap, Callback<Performance> performanceCallback);

    SlimeLoader getSlimeLoader();

    WorldData getWorldData();

    SlimePropertyMap getSlimePropertyMap();

    Map<String, CustomMapCreatorMap> getMapCreatorMaps();

    CustomMapCreatorInventory getCustomMapCreatorInventory();

    List<String> getSlimeLoaderWorlds() throws IOException;

    @Nullable CustomMapCreatorMap getMapCreatorMap(String mapName);

    void addMapCreatorMap(CustomMapCreatorMap customMapCreatorMap);

    void removeMapCreatorMap(CustomMapCreatorMap customMapCreatorMap);

    void ensureNoPlayersLeftOnMap(Action action, CustomMapCreatorMap customMapCreatorMap, EmptyCallback emptyCallback);

    void manageWorldConfig(Action action, CustomMapCreatorMap customMapCreatorMap);

    enum Action {

        LOAD("mapcreator.action.item.load", 11, Material.SPYGLASS, true),
        SAVE("mapcreator.action.item.save", 13, Material.CLOCK, true),
        LEAVE("mapcreator.action.item.leave", 14, Material.GLASS_BOTTLE, false),
        DELETE("mapcreator.action.item.delete", 15, Material.STRUCTURE_VOID, false),
        IMPORT("", -1, Material.AIR, false);

        @Getter String translationProperty;
        @Getter int actionItemSlot;
        @Getter Material actionItemMaterial;
        @Getter boolean preActionRequired;

        Action(String translationProperty, int actionItemSlot, Material actionItemMaterial, boolean preActionRequired) {
            this.translationProperty = translationProperty;
            this.actionItemSlot = actionItemSlot;
            this.actionItemMaterial = actionItemMaterial;
            this.preActionRequired = preActionRequired;
        }

        public enum Player {

            TELEPORT("inventory.section.category.maps.map.management.teleport", 12, Material.ENDER_EYE),
            BACK("back", 18, Material.RED_DYE);

            @Getter String translationProperty;
            @Getter int actionItemSlot;
            @Getter Material actionItemMaterial;

            Player(String translationProperty, int actionItemSlot, Material actionItemMaterial) {
                this.translationProperty = translationProperty;
                this.actionItemSlot = actionItemSlot;
                this.actionItemMaterial = actionItemMaterial;
            }
        }
    }

    class Performance {

        private Action action;
        private SlimeWorld slimeWorld;
        private boolean success;
        private FailureReason failureReason;
        private FailureAnnouncement failureAnnouncement;

        public Performance() {

        }

        public Performance(Action action) {
            this.action = action;
        }

        public Performance(SlimeWorld slimeWorld) {
            this.slimeWorld = slimeWorld;
        }

        public Performance(boolean success) {
            this.success = success;
        }

        public Performance(Action action, SlimeWorld slimeWorld) {
            this.action = action;
            this.slimeWorld = slimeWorld;
        }

        public Performance(Action action, SlimeWorld slimeWorld, boolean success) {
            this.action = action;
            this.slimeWorld = slimeWorld;
            this.success = success;
        }

        public Action getAction() {
            return this.action;
        }

        public void setAction(Action action) {
            this.action = action;
        }

        public SlimeWorld getSlimeWorld() {
            return this.slimeWorld;
        }

        public Performance setSlimeWorld(SlimeWorld slimeWorld) {
            this.slimeWorld = slimeWorld;
            return this;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public Performance setSuccess(FailureReason failureReason) {
            this.success = false;
            this.failureReason = failureReason;

            BukkitTranslation message = new BukkitTranslation(MapCreatorPlugin.Translations.MAPCREATOR_PERFORMANCE_FAILURE);
            BukkitTranslation failureMessage = new BukkitTranslation(this.failureReason.getTranslationProperty());
            String[] targets = new String[]{"$action$", "$failureReason$"};

            this.failureAnnouncement = new FailureAnnouncement(message, failureMessage, targets);

            return this;
        }

        public Performance setSuccess(Exception exception) {
            if(exception instanceof UnknownWorldException) {
                this.setSuccess(FailureReason.WORLD_NOT_FOUND);
            }else if(exception instanceof IOException) {
                this.setSuccess(FailureReason.NOT_ABLE_TO_OBTAIN_FROM_DATA_SOURCE);
            }else if(exception instanceof CorruptedWorldException) {
                this.setSuccess(FailureReason.CORRUPTED_WORLD);
            }else if(exception instanceof NewerFormatException) {
                this.setSuccess(FailureReason.WORLD_USES_NEWER_VERSION_OF_SRF);
            }else if(exception instanceof WorldInUseException) {
                this.setSuccess(FailureReason.WORLD_IS_ALREADY_BEING_USED_BY_ANOTHER_SERVER);
            }else if(exception instanceof WorldAlreadyExistsException) {
                this.setSuccess(FailureReason.WORLD_ALREADY_EXISTS_IN_DATA_SOURCE);
            }else if(exception instanceof InvalidWorldException) {
                this.setSuccess(FailureReason.INVALID_WORLD);
            }else if(exception instanceof WorldLoadedException) {
                this.setSuccess(FailureReason.WORLD_ALREADY_LOADED);
            }else if(exception instanceof WorldTooBigException) {
                this.setSuccess(FailureReason.WORLD_TOO_BIG);
            }
            return this;
        }

        public Performance setSuccess() {
            this.success = true;
            return this;
        }

        public FailureReason getFailureReason() {
            return this.failureReason;
        }

        public void announceFailure(@NotNull Player player) {
            if(this.failureReason != null) {
                player.sendMessage(MapCreatorPlugin.Strings.PREFIX + this.failureAnnouncement.message().get(player, this.failureAnnouncement.targets(), new String[]{this.getAction().name(), this.failureAnnouncement.failureMessage().get(player)}));
            }
        }

        public void announceFailure() {
            if(this.failureReason != null) {
                MapCreatorPlugin.getMapCreatorPlugin().getLogger().log(Level.WARNING, this.failureAnnouncement.message().get(this.failureAnnouncement.targets(), new String[]{this.getAction().name(), this.failureAnnouncement.failureMessage().get(true)}, true));
            }
        }


        public void performCustomPlayerMapAction(Player player) {
            if(this.getAction() != null) {
                CustomPlayerMapActions customPlayerMapActions = new CustomPlayerMapActions(player, this.getSlimeWorld());
                switch(this.getAction()) {
                    case LOAD -> customPlayerMapActions.load();
                    case SAVE -> customPlayerMapActions.save();
                    case LEAVE -> customPlayerMapActions.leave();
                    case DELETE -> customPlayerMapActions.delete();
                }

            }
        }

        public enum FailureReason {

            PLAYERS_ON_MAP("mapcreator.performance.failure.reason.players.on.map"),
            WORLD_NOT_LOADED("mapcreator.performance.failure.reason.world.not.loaded"),
            WORLD_DOES_NOT_EXIST("mapcreator.performance.failure.reason.world.does.not.exist"),
            NOT_ABLE_TO_OBTAIN_FROM_DATA_SOURCE("mapcreator.performance.failure.reason.not.able.to.obtain.from.data.source"),
            WORLD_ALREADY_EXISTS_IN_DATA_SOURCE("mapcreator.performance.failure.reason.world.already.exists.in.data.source"),
            CORRUPTED_WORLD("mapcreator.performance.failure.reason.corrupted.world"),
            WORLD_USES_NEWER_VERSION_OF_SRF("mapcreator.performance.failure.reason.world.uses.newer.version.of.srf"),
            WORLD_IS_ALREADY_BEING_USED_BY_ANOTHER_SERVER("mapcreator.performance.failure.reason.world.is.already.being.used.by.another.server"),
            WORLD_NOT_FOUND("mapcreator.performance.failure.reason.world.not.found"),
            WORLD_LOCKED("mapcreator.performance.failure.reason.world.locked"),
            INVALID_WORLD("mapcreator.performance.failure.reason.invalid.world"),
            WORLD_ALREADY_LOADED("mapcreator.performance.failure.reason.world.already.loaded"),
            WORLD_TOO_BIG("mapcreator.performance.failure.reason.world.too.big"),
            NO_IMPORTABLE_WORLD("mapcreator.performance.failure.reason.no.importable.world");

            @Getter String translationProperty;

            FailureReason(String translationProperty) {
                this.translationProperty = translationProperty;
            }
        }

        record FailureAnnouncement(BukkitTranslation message, BukkitTranslation failureMessage, String[] targets) {}
    }
}
