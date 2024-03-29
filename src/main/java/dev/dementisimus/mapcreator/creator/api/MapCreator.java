package dev.dementisimus.mapcreator.creator.api;

import com.grinderwolf.swm.api.exceptions.*;
import com.grinderwolf.swm.api.world.SlimeWorld;
import dev.dementisimus.capi.core.callback.Callback;
import dev.dementisimus.capi.core.helpers.bukkit.BukkitPlayerHelper;
import dev.dementisimus.capi.core.language.bukkit.BukkitTranslation;
import dev.dementisimus.mapcreator.MapCreatorPlugin;
import dev.dementisimus.mapcreator.creator.CustomMapCreator;
import dev.dementisimus.mapcreator.creator.CustomMapCreatorMap;
import dev.dementisimus.mapcreator.creator.CustomPlayerMapActions;
import dev.dementisimus.mapcreator.gui.CustomMapCreatorInventory;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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

    /**
     * Performs the {@link Action} on {@link MapCreatorMap} asynchronously
     *
     * @param action              the {@link Action} to perform on {@link MapCreatorMap}
     * @param mapCreatorMap       the {@link MapCreatorMap} to perform the {@link Action} on
     * @param performanceCallback the {@link Callback} which returns the {@link Performance} of the executed {@link Action}
     */
    void perform(MapCreator.Action action, MapCreatorMap mapCreatorMap, Callback<MapCreator.Performance> performanceCallback);

    /**
     * Lists all maps by category
     *
     * @param category the category to search maps for
     *
     * @return a list of all maps in the given category
     */
    List<String> listMapsByCategory(String category);

    enum Action {

        /**
         * Loads {@link MapCreatorMap} from the data source
         */
        LOAD("mapcreator.action.item.load", "mapcreator.action.message.load", 11, Material.SPYGLASS, true, true, "mapcreator.action.loading.actionbar.load"),

        /**
         * Saves {@link MapCreatorMap} to the data source
         */
        SAVE("mapcreator.action.item.save", "mapcreator.action.message.save", 13, Material.CLOCK, true, false, ""),

        /**
         * Leaves without saving {@link MapCreatorMap}
         */
        LEAVE_WITHOUT_SAVING("mapcreator.action.item.leave.without.saving", "mapcreator.action.message.leave.without.saving", 14, Material.GLASS_BOTTLE, false, false, ""),

        /**
         * Deletes {@link MapCreatorMap} from the data source
         */
        DELETE("mapcreator.action.item.delete", "mapcreator.action.message.delete", 15, Material.STRUCTURE_VOID, false, false, ""),

        /**
         * Imports a traditional world folder, named by {@link MapCreatorMap} properties, to the data source
         */
        IMPORT("", "mapcreator.action.message.import", -1, Material.AIR, true, true, "mapcreator.action.loading.actionbar.import"),

        /**
         * Clones a {@link MapCreatorMap} from the data source and loads it
         */
        CLONE("", "mapcreator.action.message.clone", -1, Material.AIR, true, true, "mapcreator.action.loading.actionbar.clone"),

        /**
         * Renames a {@link MapCreatorMap}
         */
        RENAME("", "mapcreator.action.message.rename", -1, Material.AIR, true, true, "mapcreator.action.loading.actionbar.rename");

        @Getter String translationProperty;
        @Getter String actionMessageTranslationProperty;
        @Getter int actionItemSlot;
        @Getter Material actionItemMaterial;
        @Getter boolean preActionRequired;
        @Getter boolean useLoadingActionBar;
        @Getter String loadingActionBarActionTranslationProperty;


        Action(String translationProperty, String actionMessageTranslationProperty, int actionItemSlot, Material actionItemMaterial, boolean preActionRequired, boolean useLoadingActionBar, String loadingActionBarActionTranslationProperty) {
            this.translationProperty = translationProperty;
            this.actionMessageTranslationProperty = actionMessageTranslationProperty;
            this.actionItemSlot = actionItemSlot;
            this.actionItemMaterial = actionItemMaterial;
            this.preActionRequired = preActionRequired;
            this.useLoadingActionBar = useLoadingActionBar;
            this.loadingActionBarActionTranslationProperty = loadingActionBarActionTranslationProperty;
        }

        public void sendActionMessage(Player defaultPlayer, CustomMapCreatorInventory mapCreatorInventory, CustomMapCreatorMap map, String elapsed, boolean isPostAction) {
            AtomicInteger playerCount = new AtomicInteger(-1);

            BukkitPlayerHelper.stream(player -> {
                if(mapCreatorInventory.getLoadedPlayerMap(player).equals(map)) {
                    this.sendActionMessage(player, map, elapsed, isPostAction);

                    playerCount.set(1);
                }
            });

            if(playerCount.get() == -1) {
                this.sendActionMessage(defaultPlayer, map, elapsed, isPostAction);
            }
        }

        public void sendActionMessage(Player player, CustomMapCreatorMap map, String elapsed, boolean isPostAction) {
            player.sendMessage(CustomMapCreator.CustomAction.getActionMessage(player, map, this.getActionMessageTranslationProperty(), elapsed, isPostAction));
        }
    }

    class Performance {

        /**
         * The performed {@link Action}
         */
        @Getter private Action action;

        /**
         * The {@link SlimeWorld} generated by this action
         */
        @Getter
        private @Nullable SlimeWorld slimeWorld;

        /**
         * True if no error occured, false otherwise
         */
        @Getter private boolean success;

        /**
         * The {@link FailureReason} for the action
         */
        private @Nullable FailureReason failureReason;

        private @Nullable FailureAnnouncement failureAnnouncement;

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

        public void setAction(Action action) {
            this.action = action;
        }

        public Performance setSlimeWorld(SlimeWorld slimeWorld) {
            this.slimeWorld = slimeWorld;
            return this;
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
                this.setSuccess(FailureReason.WORLD_LOCKED);
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

        public Performance setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public FailureReason getFailureReason() {
            return this.failureReason;
        }

        /**
         * If {@link FailureReason} not null, a failure message will be announced to the player
         *
         * @param player The player the failure message will be announced to
         */
        public void announceFailure(@NotNull Player player) {
            if(this.failureReason != null) {
                player.sendMessage(MapCreatorPlugin.Strings.PREFIX + " " + this.failureAnnouncement.message().get(player, this.failureAnnouncement.targets(), new String[]{
                        this.getAction().name(), this.failureAnnouncement.failureMessage().get(player)
                }));
            }
        }

        /**
         * If {@link FailureReason} not null, a failure message will be announced to the console
         */
        public void announceFailure() {
            if(this.failureReason != null) {
                MapCreatorPlugin.getMapCreatorPlugin().getLogger().log(Level.WARNING, this.failureAnnouncement.message().get(this.failureAnnouncement.targets(), new String[]{
                        this.getAction().name(), this.failureAnnouncement.failureMessage().get(true)
                }, true));
            }
        }


        public void performCustomPlayerMapAction(Player player) {
            if(this.getAction() != null) {
                CustomPlayerMapActions customPlayerMapActions = new CustomPlayerMapActions(player, this.getSlimeWorld());
                switch(this.getAction()) {
                    case LOAD, CLONE -> {
                        customPlayerMapActions.load();
                    }
                    case SAVE -> customPlayerMapActions.save();
                    case LEAVE_WITHOUT_SAVING -> customPlayerMapActions.leave();
                    case DELETE -> customPlayerMapActions.delete();
                    case IMPORT -> customPlayerMapActions.worldImport();
                }
            }
        }

        public enum FailureReason {

            /**
             * There were still players on the map
             */
            PLAYERS_ON_MAP("mapcreator.performance.failure.reason.players.on.map"),

            /**
             * The world is currently not loaded
             */
            WORLD_NOT_LOADED("mapcreator.performance.failure.reason.world.not.loaded"),

            /**
             * The world does not exist in the data source
             */
            WORLD_DOES_NOT_EXIST("mapcreator.performance.failure.reason.world.does.not.exist"),

            /**
             * The map could not be obtained from the data source
             */
            NOT_ABLE_TO_OBTAIN_FROM_DATA_SOURCE("mapcreator.performance.failure.reason.not.able.to.obtain.from.data.source"),

            /**
             * The world already exists in the data source
             */
            WORLD_ALREADY_EXISTS_IN_DATA_SOURCE("mapcreator.performance.failure.reason.world.already.exists.in.data.source"),

            /**
             * The world is corrupted
             */
            CORRUPTED_WORLD("mapcreator.performance.failure.reason.corrupted.world"),

            /**
             * The world uses a newer version of the Slime Region Format
             */
            WORLD_USES_NEWER_VERSION_OF_SRF("mapcreator.performance.failure.reason.world.uses.newer.version.of.srf"),

            /**
             * The world is unknown
             */
            WORLD_NOT_FOUND("mapcreator.performance.failure.reason.world.not.found"),

            /**
             * The world is locked
             */
            WORLD_LOCKED("mapcreator.performance.failure.reason.world.locked"),

            /**
             * The world is invalid
             */
            INVALID_WORLD("mapcreator.performance.failure.reason.invalid.world"),

            /**
             * The world is already loaded on the server
             */
            WORLD_ALREADY_LOADED("mapcreator.performance.failure.reason.world.already.loaded"),

            /**
             * The world is too big
             */
            WORLD_TOO_BIG("mapcreator.performance.failure.reason.world.too.big"),

            /**
             * The world can not be imported
             */
            NO_IMPORTABLE_WORLD("mapcreator.performance.failure.reason.no.importable.world"),

            /**
             * The world may not be cloned
             */
            NO_CLONEABLE_MAP("mapcreator.performance.failure.reason.no.cloneable.map");

            @Getter String translationProperty;

            FailureReason(String translationProperty) {
                this.translationProperty = translationProperty;
            }
        }

        record FailureAnnouncement(BukkitTranslation message, BukkitTranslation failureMessage, String[] targets) {}
    }
}
