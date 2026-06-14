# LifeSteal Server-side

A fabric server side reimplementation of the LifeSteal SMP. This version has many more unique features such as an altar for doing
revival rituals, adds new heart ores as an alternative source of gaining new hearts, and much, much more!

This mod was forked from ModernAdventurer's [LifeSteal Mod](https://github.com/ModernAdventurer/LifeSteal), please make sure to check it out as, although there are quite a few changes, this mod was based off of his.

## Config

Configuration is purely managed through gamerules, here is a list of gamerules and what they do.

| Gamerule                                      | Type                | Description                                                                                        | Default                     |
|-----------------------------------------------|---------------------|----------------------------------------------------------------------------------------------------|-----------------------------|
| lifesteal:death_criteria                      | DeathCriteria       | What counts as a kill: `player_only`, `any_death`, or `any_death_drop_heart`                       | `player_only`               |
| lifesteal:altar_block                         | Block               | The registry for the block to be the center of an altar                                            | `minecraft:netherite_block` |
| lifesteal:enable_anti_heart_dupe              | Boolean             | Whether to prevent players from harvesting infinite hearts from weak players                       | `true`                      |
| lifesteal:revive_method                       | ReviveMethod        | How to revive players: `none`, `command`, `altar`, or `totem`                                      | `altar`                     |
| lifesteal:death_action                        | DeathAction         | Whether to `ban`, `revive` or `spectator` when they reach minimum health                           | `ban`                       |
| lifesteal:gift_method                         | GiftMethod          | How to gift hearts: `manual` or `command`                                                          | `manual`                    |
| lifesteal:steal_amount                        | Integer             | The number of hearts that should be stolen upon death                                              | `1`                         |
| lifesteal:min_player_hearts                   | Integer             | The minimum number of hearts a player can reach before being banned                                | `1`                         |
| lifesteal:max_player_hearts                   | Integer             | The maximum number of hearts a player can reach                                                    | `10`                        |
| lifesteal:withdraw_method                     | WithdrawMethod      | How to withdraw hearts: `none`, `altar`, or `command`                                              | `altar`                     |
| lifesteal:auto_revival_seconds                | Integer             | The number of seconds until a player is automatically revived. Set to 0 to disable.                | `0`                         |
| lifesteal:revival_invulnerability_seconds     | Integer             | The amount of time a player is invulnerable after being revived in seconds. Set to 0 to disable.   | `0`                         |
| lifesteal:heart_stack_size                    | Integer             | The maximum stack size of the heart item                                                           | `1`                         |
| lifesteal:heart_craft_in_crafter              | Boolean             | If a heart item can be crafted in a crafter                                                        | `false`                     |
| lifesteal:limited_heart_crafting_type         | LimitedCraftingType | How limited crafting works: `until_banned`, `forever`, `heart_based`, or `none`                    | `none`                      |
| lifesteal:limited_heart_crafting_amount       | Integer             | The value/limit of limited crafting. It changes meaning depending on the limited crafting gamerule | `0`                         |
| lifesteal:altar_animations                    | Boolean             | If animations should play when interacting with an altar                                           | `true`                      |
| lifesteal:fallback_textures                   | Boolean             | Whether to use Minecraft textures if the resourcepack isn't installed instead of missing texture   | `true`                      |
| lifesteal:new_player_invulnerability_seconds  | Integer             | The number of seconds a player is invulnerable since they started playing                          | `0`                         |

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
- `/gift <player> <hearts>` - Gifts the specified player that amount of health if they can receive it
- `/withdraw <hearts>` - Turns physical hearts into heart items
- `/revive <player>` - Revives a player and consumes a held heart item
- `/admin-revive <player>` - Admin command to revive a player

### Datapack Overriding (WIP):
More info can be found on the [wiki page](https://github.com/Tater-Certified/LifeSteal/wiki/Guides#how-to-configure-ores-with-a-datapack).

## Additional Notes:
- In order to get the textures, run /polymer generate-pack. This will create the resourcepack in the main server's directory, which you can then put in your resourcepack folder
- For more information, visit the **[Wiki](https://github.com/Tater-Certified/LifeSteal/wiki)**
