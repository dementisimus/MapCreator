package dev.dementisimus.mapcreator.creator.interfaces;

import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.plugin.config.WorldData;
import dev.dementisimus.capi.core.callback.Callback;
import dev.dementisimus.capi.core.callback.EmptyCallback;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    Map<String, SlimeWorld> slimeWorlds = new HashMap<>();

    void perform(Action action, CustomMapCreatorMap customMapCreatorMap, Callback<Performance> performanceCallback);

    SlimeLoader getSlimeLoader();

    WorldData getWorldData();

    SlimePropertyMap getSlimePropertyMap();

    Map<String, SlimeWorld> getSlimeWorlds();

    CustomMapCreatorInventory getCustomMapCreatorInventory();

    List<String> getWorlds() throws IOException;

    @Nullable SlimeWorld getSlimeWorld(CustomMapCreatorMap customMapCreatorMap);

    void addSlimeWorld(String mapName, SlimeWorld slimeWorld);

    void removeSlimeWorld(String mapName);

    void ensureNoPlayersLeftOnMap(Action action, CustomMapCreatorMap customMapCreatorMap, EmptyCallback emptyCallback);

    void manageWorldConfig(Action action, CustomMapCreatorMap customMapCreatorMap);

    enum Action {
        CREATE,
        LOAD,
        SAVE,
        DELETE,
        LEAVE
    }

    class Performance {

        private Action action;
        private SlimeWorld slimeWorld;
        private boolean success;
        private FailureReason failureReason;

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
            return this;
        }

        public Performance setSuccess() {
            this.success = true;
            return this;
        }

        public FailureReason getFailureReason() {
            return this.failureReason;
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
            WORLD_ALREADY_LOADED("mapcreator.performance.failure.reason.world.already.loaded");

            String translationProperty;

            FailureReason(String translationProperty) {
                this.translationProperty = translationProperty;
            }
        }
    }
}
