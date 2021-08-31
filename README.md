![](https://dementisimus.dev/img/MapCreator/MapCreator.jpg)

### *an easy && lightweight Plugin / and or API // for creating/loading custom maps for your custom needs!*
------------

## some impressions first (outdated)

![](https://dementisimus.dev/img/MapCreator/commandOutput.jpg)

![](https://dementisimus.dev/img/MapCreator/treeCommandOutput.jpg)

![](https://dementisimus.dev/img/MapCreator/loadExample.jpg)

![](https://dementisimus.dev/img/MapCreator/newlyCreatedMap.jpg)

![](https://dementisimus.dev/img/MapCreator/loadedSuccessfully.jpg)

![](https://repo.dementisimus.dev/dev/dementisimus/mrs/MapRatingSystem/images/server.png)

![](https://dementisimus.dev/img/MapCreator/savedSuccessfully.jpg)

![](https://dementisimus.dev/img/MapCreator/treeCommandOutputAfterSaving.jpg)

## Features (outdated)

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

## API-Usage (outdated)

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
> **- in order to use this plugin properly, you need to use Java >16 & [Paper by PaperMC](https://papermc.io/downloads "Paper by PaperMC") >1.17! (Paper is an extension/fork of Spigot by SpigotMC, which implements many useful features!)**

## Credits

**special thanks** goes to
  - the **team & community** of [Advanced-Slime-World-Manager](https://github.com/Paul19988/Advanced-Slime-World-Manager)
    - for creating such awesome software!

## installation

###  (outdated) » **you can find an example for your defaultWorld [here](https://repo.dementisimus.dev/dev/dementisimus/mapcreator/MapCreator/DEFAULTMAPS/defaultWorld.zip "here")!**

- **[If your server is currently running, make sure you stop him, do NOT reload your server!]**
- **put this plugin** into the **plugin folder** of **your** **server**.
- you can now **start** your **server**!

## install an update
- **download** the **new version** and put it in your **plugin-folder**. **Follow** the **instructions** on the update post, **if given**.
- **restart** or **start** [do **NOT** reload] your **server**.

# » Have fun!
