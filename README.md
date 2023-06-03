# LifeSteal Server-side

A fabric server side implementation of the LifeSteal SMP.

This mod was forked from [ModernAdventurer's](https://github.com/ModernAdventurer) LifeSteal [Mod](https://github.com/ModernAdventurer/LifeSteal), please make sure to check it out as although there are quite a few changes this mod was based off of his.

## Config

Configuration is purely managed through gamerules, here is a list of gamerules and what they do.

|Gamerule|Type|Description| Default |
|-----|----|-----------|---------|
|lifeSteal:playerKillOnly|Boolean|If a player should lose hearts when dying in any way other than to a player| true    |
|lifeSteal:banWhenMinHealth|Boolean|If a player should be banned when they reach the minimum health value| true    |
|lifeSteal:giftHearts|Boolean|If a player can trade a heart for a heart item| true|
|lifeSteal:stealAmount|Integer|The amount of health that should be stolen upon death| 2       |
|lifeSteal:minPlayerHealth|Integer|The minimum health a player can reach before being banned *if the value is below 1 it is automatically corrected to 1*| 1       |
|lifeSteal:maxPlayerHealth|Integer|The maximum health a player can reach *set to a value of 0 or below to disable*| 40      |
```yaml
Additional Config options:
- Ritual Block: What block to use as the ritual block
```

```yaml
Datapack Overriding:
  - Override "lifesteal\worldgen\configured_feature\heart_ore.json" to change amount of ores per vein
  - Override "lifesteal\worldgen\placed_feature\heart_ore.json" to change amount of veins per chunk
```

## Aditional Notes:
- In order to get the textures, run /polymer generate-pack. This will create the resourcepack in the main server's directory, which you can then put in your resourcepack folder
- To revive a player, put lit candles in all 4 directions with a netherite block in the center. Then rename a heart to the player's name and shift right-click on the ritual block
- To trade a heart for a heart item, rename it to your name, then shift right-click the ritual block