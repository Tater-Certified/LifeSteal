# LifeSteal Server-side

A fabric server side implementation of the LifeSteal SMP.

This mod was forked from [ModernAdventurer's](https://github.com/ModernAdventurer) LifeSteal [Mod](https://github.com/ModernAdventurer/LifeSteal), please make sure to check it out as, although there are quite a few changes, this mod was based off of his.

## Config

Configuration is purely managed through gamerules, here is a list of gamerules and what they do.

| Gamerule                                | Type    | Description                                                                                                            | Default                     |
|-----------------------------------------|---------|------------------------------------------------------------------------------------------------------------------------|-----------------------------|
| lifesteal:enableAltars                  | Boolean | Whether to allow the creation of altars instead of commands                                                            | `true`                      |
| lifesteal:altarBlock                    | Block   | The registry for the block to be the center of an altar                                                                | `minecraft:netherite_block` |
| lifesteal:enableAntiHeartDupe           | Boolean | Whether to prevent players from harvesting infinite hearts from weak players                                           | `true`                      |
| lifesteal:playerKillOnly                | Boolean | If a player should lose hearts when dying in any way other than to a player                                            | `true`                      |
| lifesteal:deathAction                   | Enum    | Whether to `ban`, `revive` or `spectator` when they reach minimum health                                               | `ban`                       |
| lifesteal:giftHearts                    | Boolean | If a player can trade a heart for a heart item                                                                         | `true`                      |
| lifesteal:stealAmount                   | Integer | The amount of health that should be stolen upon death                                                                  | `2`                         |
| lifesteal:minPlayerHealth               | Integer | The minimum health a player can reach before being banned *if the value is below 1 it is automatically corrected to 1* | `2`                         |
| lifesteal:maxPlayerHealth               | Integer | The maximum health a player can reach *set to a value of 0 or below to disable*                                        | `40`                        |
| lifesteal:healthFromHeart               | Integer | The amount of health to receive from a heart item                                                                      | `2`                         |
| lifesteal:autoRevivalSeconds            | Integer | The number of seconds until a player is automatically revived. Set to 0 to disable.                                    | `0`                         |
| lifesteal:revivalInvulnerabilitySeconds | Integer | The amount of time a player is invulnerable after being revived in seconds. Set to 0 to disable.                       | `0`                         |
| lifesteal:heartStackSize                | Integer | The maximum stack size of the heart item                                                                               | `1`                         |

### Dead Player Json:
Located in `config/lifesteal-deaths.json`
```json
[
  {
    "deadPlayerID": "uuid",
    "deathTime": 100
  }
]
 ```

### Commands:
- `/gift <Player> <Health>` - Gifts the specified player that amount of health if they can receive it
- `/withdraw <Hearts>` - Turns physical hearts into heart items
- `/revive <Player>` - Admin command to revive a player

### Datapack Overriding (WIP):
More info can be found on the [wiki page](https://github.com/Tater-Certified/LifeSteal/wiki/Guides#how-to-configure-ores-with-a-datapack).

## Aditional Notes:
- In order to get the textures, run /polymer generate-pack. This will create the resourcepack in the main server's directory, which you can then put in your resourcepack folder
- For more information, visit the **[Wiki](https://github.com/Tater-Certified/LifeSteal/wiki)**
