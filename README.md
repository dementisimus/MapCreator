![](https://repo.dementisimus.dev/dev/dementisimus/mapcreator/MapCreator/images/MapCreator.jpg)

### *an easy && lightweight Plugin / and or API // for creating/loading custom maps for your custom needs!*
------------

## some impressions first

![](https://repo.dementisimus.dev/dev/dementisimus/mapcreator/MapCreator/images/commandOutput.jpg)

![](https://repo.dementisimus.dev/dev/dementisimus/mapcreator/MapCreator/images/treeCommandOutput.jpg)

![](https://repo.dementisimus.dev/dev/dementisimus/mapcreator/MapCreator/images/loadExample.jpg)

![](https://repo.dementisimus.dev/dev/dementisimus/mapcreator/MapCreator/images/newlyCreatedMap.jpg)

![](https://repo.dementisimus.dev/dev/dementisimus/mapcreator/MapCreator/images/loadedSuccessfully.jpg)

![](https://repo.dementisimus.dev/dev/dementisimus/mrs/MapRatingSystem/images/server.png)

![](https://repo.dementisimus.dev/dev/dementisimus/mapcreator/MapCreator/images/savedSuccessfully.jpg)

![](https://repo.dementisimus.dev/dev/dementisimus/mapcreator/MapCreator/images/treeCommandOutputAfterSaving.jpg)

## Features

- **easy** installation && setup via **console** on server startup
- possibility to **choose** between **MongoDB**, **MariaDB** or **SQLite**
- **automatic** dependency **installer** - **no need** to download dependencies **by yourself**!
- **automatic** **saving** and **loading** of your **last** **Location**, **GameMode**, **Inventory** & **more**!
- async
- lightweight
- **caches** used data for **maximum performance**
- **multilingual** - by selecting the **correct language** for a player **automatically**
- high scalability
- **docs** on [docs.dementisimus.dev](https://docs.dementisimus.dev/MapCreator/ "docs.dementisimus.dev")
- **Support** @ [Discord](https://discord.gg/sTRg8A7 "Discord") && [SpigotMC](https://www.spigotmc.org/conversations/add?to=dementisimus "SpigotMC") (Discord **preferred, so your question might help other people as well!**)
- **issues** can be **reported** [here](https://discord.gg/sTRg8A7 "here")
- [have a look at upcoming features on GitHub!](https://github.com/dementisimus/MapCreator/projects "have a look at upcoming features on GitHub!")

## default world settings:

+ ### load worlds by adding 'true' to the command to apply these options (GameRules):

  » **autoSave** = _true_;
  <br>
  » **RANDOM_TICK_SPEED** = _0_;
  <br>
  » **DO_FIRE_TICK** = _false_;
  <br>
  » **DO_DAYLIGHT_CYCLE** = _false_;
  <br>
  » **DO_WEATHER_CYCLE** = _false_;
  <br>
  » **DO_MOB_SPAWNING** = _false_;
  <br>
  » **MOB_GRIEFING** = _false_;

## API-Usage

```xml
<!-- dementisimus.dev-Repository -->
<repository>
     <id>dementisimus.dev</id>
     <url>https://repo.dementisimus.dev</url>
</repository>

<!-- MapCreator-1.3.0 by dementisimus -->
<dependency>
     <groupId>dev.dementisimus.mapcreator</groupId>
     <artifactId>MapCreator</artifactId>
     <version>1.3.0</version>
     <scope>provided</scope>
</dependency>
```

```yaml
#this you need to specify in your plugin.yml!
loadbefore: [MapCreator]
```

```java
[example]
MapCreatorAPI.setWorldPoolFolder("/home/minecraft/maps/");
MapCreatorAPI mapCreatorAPI = new MapCreatorAPI(); //use a player as parameter to teleport them to the newly loaded map;
mapCreatorAPI.setMapType("MYMAPS");
mapCreatorAPI.setMapName("myMap");
mapCreatorAPI#load(boolean useDefaultWorldSettings); //(use loadSync for using while loading on server startup, or without any player)
```

## » more information: ([click](https://docs.dementisimus.dev/MapCreator/ "click"))

## ToS
- the plugin **may not** be decompiled, modified, sold, be published as your own.
- the plugin **may** be used in public/private plugins; credits would be nice!

# !!! read this carefully before downloading !!!
> **- in order to use this plugin properly, you need to use Java >11 & [Paper by PaperMC](https://papermc.io/downloads "Paper by PaperMC") >1.16! (Paper is an extension/fork of Spigot by SpigotMC, which implements many useful features!)**

## installation

### » **you can find an example for your defaultWorld [here](https://repo.dementisimus.dev/dev/dementisimus/mapcreator/MapCreator/DEFAULTMAPS/defaultWorld.zip "here")!**

- **[If your server is currently running, make sure you stop him, do NOT reload your server!]**
- **put this plugin** into the **plugin folder** of **your** **server**.
- you can now **start** your **server**!

## install an update
- **download** the **new version** and put it in your **plugin-folder**. **Follow** the **instructions** on the update post, **if given**.
- **restart** or **start** [do **NOT** reload] your **server**.

# » Have fun!
