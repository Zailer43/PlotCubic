# Configuration Documentation


## Config structure
```json5
{
  "CONFIG_VERSION_DONT_TOUCH_THIS": 1,
  
  "general": {
    
    // Teleports the player to the plot world automatically on respawn
    "autoTeleport": true, // boolean
    
    // Displays a warning in case the player sends a message in the plot chat and no one else is on the plot
    "warningUseOfPlotChatInEmptyPlot": true, // boolean
    
    // Show plot chat messages in the logs
    "logPlotChat": true, // boolean

    // Coordinates that are added to the spawn (where you go with /plot visit) of each plot 
    // if the spawn has not changed (with /plot setspawn)
    "defaultPlotSpawn": { // Block Pos
      "x": 0, // int
      "y": 0, // int
      "z": 0 // int
    },
    
    // List of entities that are allowed to spawn, any other entity in the world will be removed
    "entityWhitelist": [ // Entity ID
      "armor_stand"
    ],
    
    // List of entities that are allowed to spawn on a road or to leave a plot
    "entityRoadWhitelist": [ // Entity ID
      "item"
    ]
  },

  // Settings related to world generation, it is not recommended to modify them once you have the server in production 
  // as it can cause inconsistencies
  "PlotWorld": {
    
    // Distance between the borders of the plots, border is not counted
    "roadSize": 8, // int
    
    // Plot size between borders, border is not counted
    "plotSize": 200, // int
    
    // Minimum height of the world
    // It only modifies the real height of the world when generating it for the first time
    // Modifying this will then cause the plots to change their height
    
    "minHeight": -64, // int
    
    // Maximum height of the world
    // It only modifies the real height of the world when generating it for the first time
    "maxHeight": 320, // int
    
    // ID of the biome to use, if not found plains is used
    // It only changes when the server is restarted
    "biomeId": "minecraft:plains",
    
    // The block that is one higher up between the road and the plot when the plot does not have an owner
    "unclaimedBorderBlock": { // Block State
      "id": "stone_slab",
      "states": []
    },
    
    // The block that is one higher up between the road and the plot when the plot has an owner
    "claimedBorderBlock": { // Block State
      "id": "deepslate_tile_slab",
      "states": []
    },
    
    // The block that is between the road and the plot
    "borderBlock": { // Block State
      "id": "deepslate_tiles",
      "states": []
    },
    
    // The block used on roads
    "roadBlock": { // Block State
      "id": "white_concrete",
      "states": []
    },
    
    // Layers of blocks that are in the plots, ordered from bottom to top
    "layers": [
      {
        
        // The number of layers of this block
        "thickness": 1,
        
        "block": { // Block State
          "id": "barrier",
          "states": []
        }
      }
    ]
  },
  
  "database": {
    
    // Database type
    "type": "mariadb", // mariadb (soon more options)
    
    // The host on which the database is hosted
    // "localhost" for local database
    "host": "localhost", // string
    
    // The port on which the database is hosted
    "port": 3306, // unsigned short (0-65535)
    
    // Username to enter the database
    "user": "root", // string
    
    // Password to enter the database user
    // Leave the field empty if a password is not required
    "password": "", // string
    
    // The name of the database
    "database": "plotcubic", // string
    
    // The table prefix, for example "p3." for "p3.table"
    // currently not working
    "table_name": "p3" // string
  },

  // Chat style affects "/plot chat" for everyone within a plot
  "plotChatStyles": [
    {
      
      // The ID is used to differentiate one chat style from another.
      // You can give a user permission to use a chat style with plotcubic.chatstyle.style.ID
      "id": "frozen_berries", // string preferably with only lowercase letters and underscores
      
      // Chat style name
      "name": "Frozen Berries", // string
      
      // ID of the chat style item that the user sees in the GUI
      "itemId": "sweet_berries", // Minecraft item id
      
      // Message displayed when using plot chat with this chat style enabled
      // Support PlaceHolder API
      // Placeholders:
      // To show the ID of the plot: %plot_id% for example: 3;50
      // To show the user who sent the message: %username%
      // To display the message: %message%
      "message": "<color:#595e60>[<color:#707370>%plot_id%<color:#595e60>] <color:#b0b7c0>%username% <color:#707370>» <color:#b0b7c0>%message%" // string
    }
  ],
  
  // Colors used by PlotCubic
  "customColors": {
    
    // Color of the parameters in the messages
    "highlight": "2C8395", // hex color
    
    // They are used to avoid having hardcode colors in translations.
    "others": [
      {
        
        // Color name, used with <NAME> in translation files
        "name": "p3_normal",  // string preferably with only lowercase letters and underscores
        
        // Color value as hexadecimal number
        "color": "61C2A2" // hex color
      }
    ]
  },
  
  // Possible reporting reasons
  "reportReasons": [
    {
      // Report Reason ID
      // The translation of the name goes in text.plotcubic.report_reason.ID.name
      // The translation of the descriptions goes in text.plotcubic.report_reason.ID.description.INDEX
      "id": "griffed", // string preferably with only lowercase letters and underscores
 
      // Report descriptions go below the name
      // The report descriptions go below the name, this value indicates the number of lines, starts at 0 and increases
      // 0 if there is no description
      "descriptionCount": 0, // int
      
      // The item used in the GUI to report
      "item": { // GUI Item 
        "itemId": "lava_bucket",
        "count": 1,
        "hideAttributes": false,
        "glow": false,
        "headValue": ""
      }
    }
  ]
}
```

## Reload

You can reload the configuration without restarting the server with `/plot admin reload`

## Objects structure

### Block state
You can find more information in https://minecraft.fandom.com/wiki/Block_states

```json5
{
  // Block ID, can contain namespace
  "id": "stone_slab",
  
  // Block states are properties that usually change the texture and/or hitbox of the block.
  // You can see the block state of a block by looking at it in F3, in Targeted Block on the right,
  // they are the first after the block ID and are divided into <key>: <value>
  // Note: not all blocks have states, and if they are not specified, the default ones are used.
  "states": [
    {
      "key": "waterlogged",
      "value": "true"
    }
  ]
}
```

### GUI Item


```json5
{
    
    "itemId": "lava_bucket", // item ID
    "count": 1, // int
    
    // Hide attributes of an item, it is recommended to activate in tools and armor
    "hideAttributes": false, // boolean
    
    // Activate enchantment glow
    "glow": false, // boolean
    
    // The Value tag contains the skin of a head, it is found in SkullOwner.Properties.textures[0].Value
    // Or you can upload a skin to get the value at https://mineskin.org
    // You can leave the value empty in case it is not a head
    "headValue": "" // string
}
```