# Permissions Documentation

- [User permissions](#users-permissions)
  - [Commands](#commands)
    - [Chat / c](#chat)
    - [Chatstyle](#chatstyle)
    - [Claim](#claim)
    - [Clear](#clear)
    - [Delete / Dispose](#delete)
    - [Deny](#deny)
    - [GameMode / gm](#gamemode)
    - [Home / h](#home)
    - [Info / i](#info)
    - [Remove / r](#remove)
    - [Report](#report)
    - [Teleport / tp](#teleport)
    - [Toggle / t](#toggle)
    - [Trust / Add / Permissions](#trust)
    - [Visit / v](#visit)
- [Admin permissions](#admin-permissions)
  - [Bypass](#bypass)
  - [Commands](#admin-commands)
    - [Clear](#admin-clear)
    - [Delete](#admin-delete)
    - [Reload](#reload)
    - [View reports](#view-reports)

## Users permissions

### Commands

#### Chat

| Permission                 | Description                  |
|----------------------------|------------------------------|
| plotcubic.command.chat.use | Allows to use ``/plot chat`` |

#### Chatstyle

| Permission                         | Description                                                                                        |
|------------------------------------|----------------------------------------------------------------------------------------------------|
| plotcubic.command.chatstyle.use    | Allows to use ``/plot chatstyle``                                                                  |
| plotcubic.chatstyle.style.*        | Allows you to use all styles                                                                       |
| plotcubic.chatstyle.style.STYLE_ID | Allows you to use a specific style, for example: ``plotcubic.command.chatstyle.style.window_tile`` |

#### Claim

| Permission                  | Description                   |
|-----------------------------|-------------------------------|
| plotcubic.command.claim.use | Allows to use ``/plot claim`` |

#### Clear

| Permission                  | Description                   |
|-----------------------------|-------------------------------|
| plotcubic.command.clear.use | Allows to use ``/plot clear`` |

#### Delete

| Permission                   | Description                    |
|------------------------------|--------------------------------|
| plotcubic.command.delete.use | Allows to use ``/plot delete`` |

#### Deny

| Permission                        | Description                            |
|-----------------------------------|----------------------------------------|
| plotcubic.command.deny.use        | Allows to use ``/plot deny``           |
| plotcubic.command.deny.add_reason | Allows you to add a reason to the deny |

#### GameMode

| Permission                           | Description                                   |
|--------------------------------------|-----------------------------------------------|
| plotcubic.command.gamemode.use       | Allows to use ``/plot gamemode``              |
| plotcubic.command.gamemode.adventure | Allows you to switch to `adventure` game mode |
| plotcubic.command.gamemode.creative  | Allows you to switch to `creative` game mode  |
| plotcubic.command.gamemode.spectator | Allows you to switch to `spectator` game mode |
| plotcubic.command.gamemode.survival  | Allows you to switch to `survival` game mode  |

#### Home

| Permission                 | Description                  |
|----------------------------|------------------------------|
| plotcubic.command.home.use | Allows to use ``/plot home`` |

#### Info

| Permission                               | Description                                                              |
|------------------------------------------|--------------------------------------------------------------------------|
| plotcubic.command.info.use               | Allows to use ``/plot info``                                             |
| plotcubic.command.info.use_without_owner | It allows you to see the information of the plot without being the owner |

#### Remove

| Permission                   | Description                    |
|------------------------------|--------------------------------|
| plotcubic.command.remove.use | Allows to use ``/plot remove`` |

#### Report

| Permission                    | Description                     |
|-------------------------------|---------------------------------|
| plotcubic.command.report.use  | Allows to use ``/plot report``  |

#### Teleport

| Permission                     | Description                      |
|--------------------------------|----------------------------------|
| plotcubic.command.teleport.use | Allows to use ``/plot teleport`` |

#### Toggle

| Permission                        | Description                         |
|-----------------------------------|-------------------------------------|
| plotcubic.command.toggle.use      | Allows to use ``/plot toggle``      |
| plotcubic.command.toggle.chat.use | Allows to use ``/plot toggle chat`` |

#### Trust

| Permission                       | Description                   |
|----------------------------------|-------------------------------|
| plotcubic.command.trust.use      | Allows to use ``/plot trust`` |

Allows you to give permission to another user with ``/plot trust`` to:

| Permission                                            | Description                                                  |
|-------------------------------------------------------|--------------------------------------------------------------|
| plotcubic.command.trust.permissions.*                 | Be able to give any permission available                     |
| plotcubic.command.trust.permissions.break_blocks      | Break blocks, does not include blocks that may contain items |
| plotcubic.command.trust.permissions.damage_entities   | Hit all types of entity (player included)                    |
| plotcubic.command.trust.permissions.destroy_container | Break blocks that may contain items                          |
| plotcubic.command.trust.permissions.fill_map          | Fill maps (right-click on an empty map)                      |
| plotcubic.command.trust.permissions.open_container    | Open blocks that can contain items                           |
| plotcubic.command.trust.permissions.place_blocks      | Placing blocks, does not include explosives or fluids        |
| plotcubic.command.trust.permissions.place_explosives  | Place tnt and respawn anchor are included                    |
| plotcubic.command.trust.permissions.place_fluids      | Place fluids such as water or lava                           |
| plotcubic.command.trust.permissions.sleep             | Be able to use beds                                          |
| plotcubic.command.trust.permissions.spawn_entities    | Place entities that are in the whitelist                     |
| plotcubic.command.trust.permissions.use_boats         | Being able to get on a boat                                  |
| plotcubic.command.trust.permissions.use_buttons       | Be able to use buttons                                       |
| plotcubic.command.trust.permissions.use_lever         | Be able to use lever                                         |
| plotcubic.command.trust.permissions.use_minecart      | Being able to get on a minecart                              |

#### Visit

| Permission                                | Description                                                                    |
|-------------------------------------------|--------------------------------------------------------------------------------|
| plotcubic.command.visit.use               | Allows to use ``/plot visit``                                                  |
| plotcubic.command.visit.plot_id           | Allows to visit claimed plots by id                                            |
| plotcubic.command.visit.plot_id.unclaimed | Allow to visit plot without claiming by id                                     |
| plotcubic.command.visit.username          | Allows using ``/plot visit <username>`` and ``/plot visit <username> <index>`` |


## Admin permissions

### Bypass

| Permission            | Description                                             |
|-----------------------|---------------------------------------------------------|
| plotcubic.bypass.deny | Allows to visit and enter parcels where you were denied |

### Admin commands

| Permission                  | Description                   |
|-----------------------------|-------------------------------|
| plotcubic.command.admin.use | Allows to use ``/plot admin`` |

#### Admin clear

| Permission                        | Description                         |
|-----------------------------------|-------------------------------------|
| plotcubic.command.admin_clear.use | Allows to use ``/plot admin clear`` |

#### Admin delete

| Permission                         | Description                          |
|------------------------------------|--------------------------------------|
| plotcubic.command.admin_delete.use | Allows to use ``/plot admin delete`` |

#### Reload

| Permission                   | Description                          |
|------------------------------|--------------------------------------|
| plotcubic.command.reload.use | Allows to use ``/plot admin reload`` |

#### View reports

| Permission                         | Description                           |
|------------------------------------|---------------------------------------|
| plotcubic.command.view_reports.use | Allows to use ``/plot admin reports`` |