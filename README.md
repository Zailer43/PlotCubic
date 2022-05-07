# PlotCubic (WIP)
A Fabric Server Side mod for managing plots in a plot world.

### Info
This mod takes care of creating and managing a plot world in which users can claim their own plots, plots are protected and other users can't interact without permission, users can also set flags that changes the plot behaviour for other users such as weather, pvp, time, custom messages etc...

### Disclamer
This mod is still a work in progress, as of now the mod is full of dangerous bugs so we don't recommend at all using this mod in a production environment. Stay updated for future changes and a possible release date.

### Requirements
You will need [Fabric](https://fabricmc.net/use/installer/) mod loader installed on your server, you don't necessarily need a server to run PlotCubic, you could just install it as a normal mod inside the client but having a dedicated server is our recommended option.

Other mod loaders such as Forge or Bukkit will not be supported.

You will also need a MySQL database to store Plot and user information, without this PlotCubic will refuse to initialize. Open the configuration file on `./config/PlotCubic.json` to set your database credentials.

### Dependencies
[Fabric API](https://modrinth.com/mod/fabric-api) - _You know the drill._

### Compiling
I hope you are ready for some trouble.

1. Clone the repo`git clone https://github.com/Zailer43/PlotCubic.git`
2. `./gradlew build`

### Credits

Credits to [NucleoidMC](https://github.com/NucleoidMC)'s [Stimuli](https://github.com/NucleoidMC/stimuli) and [Fantasy](https://github.com/NucleoidMC/fantasy) libraries, and to [Patbox](https://github.com/Patbox)'s [SGUI](https://github.com/Patbox/sgui) library, these libraries make Fabric Server Side development less painful and much more enjoyable.