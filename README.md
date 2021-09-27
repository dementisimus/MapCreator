# MapCreator [![main build status](https://github.com/dementisimus/MapCreator/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/dementisimus/MapCreator) [![develop build status](https://github.com/dementisimus/MapCreator/actions/workflows/build.yml/badge.svg?branch=develop)](https://github.com/dementisimus/MapCreator/tree/develop)
## _Build maps better. Together._
  
MapCreator is a **lightweight**, **inventory based** map management tool which allows users to **create**, **load**, **delete**, **import** and **clone** custom Minecraft maps. **For your custom needs**!

## **Features**

- **Create** new maps **from templates**
- **Manage** (**load**, **delete**, **clone**) already **existing worlds**
- **Easy import** of **traditionally** saved **maps**
- **Lightweight** & **asynchronous** map management
- **Ability** to **store** custom maps **permanently** in **databases** such as **MongoDB**, **MariaDB** (**MySQL**), **SQLite**, or on your **file system**
- **Sort** your **maps** by creating **unique categories**
- Ability to **manage all maps** and **categories** through an **inventory**
- **Multilingual** console messages (languages currently available: **English**, **German**)
- Each **player may choose** a suitable language via `/language`, otherwise the language will be **picked automatically**
- Uses **less disk space** and **provides more performance** than **traditional maps**
- **No need** to **install anything** by yourself - **MapCreator** does **everything** for you (except for **MapCreator itself** ¬‿¬ )!
- **Powerful API** for **Java Developers**
- **Continuous development** with many **[planned features]**
- **24/7-Support** at our **[Discord-Server]**

## **Requirements**

1. **Java 16**
2. **Spigot 1.17.1** **([PaperMC] recommended)**
3. **Access** to the **server console** (for the **automated setup**)
4. **Access** to a **database** **(MongoDB, MariaDB (MySQL), SQLite)**

## **Installation**

1. Make sure your **server** is **stopped**, or will be **restarted**. Do **not reload** your server!
2. **Download** the **latest version** of _**CoreAPI**_ & _**MapCreator**_ from **[GitHub Releases]**
3. **Move** the **downloaded jar-file** to the **`plugins`-folder**
4. **Start** (or **restart**) your **server**
5. **Go through** the **installation process** (**setu**p) in your **console** by **answering** the **prompted questions** with **commands** (**commands** represent the **data**, may be **infinitely long**)

## **Development**

```java
// Docs: https://docs.dementisimus.dev/development/MapCreator/1.4.0/dev/dementisimus/mapcreator/creator/api/package-summary.html
MapCreator mapCreator = new CustomMapCreator();
MapCreatorMap mapCreatorMap = MapCreatorMap.of("myMapName", "myMapCategory");

// MapCreator.Action.LOAD loads 'mapCreatorMap', for more Actions see 
// https://docs.dementisimus.dev/development/MapCreator/1.4.0/dev/dementisimus/mapcreator/creator/api/MapCreator.Action.html
mapCreator.perform(MapCreator.Action.LOAD, mapCreatorMap, performance -> {
    // do something with MapCreator.Performance
});
```

## **Images**

<p align="center">
  <img src="https://dementisimus.dev/img/MapCreator/overview.jpg" />
  <img src="https://dementisimus.dev/img/MapCreator/map_overview.jpg" />
  <img src="https://dementisimus.dev/img/MapCreator/choose_template.jpg" />
  <img src="https://dementisimus.dev/img/MapCreator/world_import.jpg" />
  <img src="https://dementisimus.dev/img/MapCreator/name.jpg" />
  <img src="https://dementisimus.dev/img/MapCreator/load.jpg" />
  <img src="https://dementisimus.dev/img/MapCreator/teleport.jpg" />
  <img src="https://dementisimus.dev/img/MapCreator/save.jpg" />
  <img src="https://dementisimus.dev/img/MapCreator/leave-without-saving.jpg" />
  <img src="https://dementisimus.dev/img/MapCreator/delete.jpg" />
</p>


## **Credits**

- [Advanced-Slime-World-Manager] for **creating** and **maintaining** the **software used** to **store maps** in the **Slime-Format**

## **Special thanks to**

- [@TearingBooch482] for **helping** me **stress-testing** every **new version** of my **plugins**

## **License**

» [Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License]

## find us on

[<img src="https://discordapp.com/assets/e4923594e694a21542a489471ecffa50.svg" alt="" height="55" />](https://discord.gg/sTRg8A7)

# **Happy map creating!**

   [planned features]: <https://github.com/dementisimus/MapCreator/projects/2>
   [Discord-Server]: <https://discord.gg/sTRg8A7>
   
   [PaperMC]: <https://papermc.io/downloads>
   
   [GitHub Releases]: <https://github.com/dementisimus/MapCreator/releases>
   
   [Advanced-Slime-World-Manager]: <https://github.com/Paul19988/Advanced-Slime-World-Manager>
   [@TearingBooch482]: <https://github.com/TearingBooch482>
   
   [Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License]: <https://creativecommons.org/licenses/by-nc-nd/4.0/>
