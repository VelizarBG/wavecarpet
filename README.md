# Wave Carpet

[Carpet Mod](https://github.com/gnembon/fabric-carpet) extension 
developed by the WaveTech TMC server.

## Features
### Stop light recalculation DataFix
Prevents the ChunkDeleteLightFix DataFix from erasing the light data out of <=1.19.4 chunks.\
**Can be enabled just before you load the wanted chunks - no need to restart the server**
- Name: `stopLightRecalculationDataFix`
- Type: `boolean`
- Default: `false`
- Category: `WaveTech`

### Server-side global config
Makes the `config/carpet` directory functional on the server.\
Useful for having a global config for multiple servers if one utilizes symlinks.

### Suppression counter
Registers the scoreboard criterion `suppressionCount`.\
Can be accessed with 
`/scoreboard objectives add {name} suppressionCount`.\
Increases by 1 for the player who triggered an update suppression.

### Player container loading
Server side implementation of TweakerMore's `autoFillContainer` for use with Carpet bots.\
Toggled with `/player {player} loadItems`.\
Whenever a container is opened by the player, it will move as many items as possible in it.

### Enable player's `fallFlying` flag
Makes it possible to enable the player's `fallFlying` flag to make them fly while they are falling with an equipped elytra.
Enabled with `/player {player} startFallFlying`

### Import player statistics into scoreboard objectives
Creates and updates a scoreboard objective for every existing player statistic.\
Use the command `/importstatstoscoreboard`.
- Name: `commandImportStatsToScoreboard`
- Type: `String`
- Default: `ops`
- Category: `WaveTech`, `survival`, `command`

### Obtainable invisible item frames
Makes invisible item frames obtainable by renaming them to "invisible" in an anvil.
- Name: `obtainableInvisibleItemFrames`
- Type: `boolean`
- Default: `false`
- Category: `WaveTech`, `survival`, `feature`

### Disable ops bypass whitelist
Prevents opped players from bypassing the whitelist.
- Name: `disableOpsBypassWhitelist`
- Type: `boolean`
- Default: `false`
- Category: `WaveTech`, `creative`

### Store piston bolt encodings
Use the command `/pistonbolt location` to store location encodings.\
Maps a location name to a number or string.
- Name: `commandPistonBolt`
- Type: `String`
- Default: `false`
- Category: `WaveTech`, `survival`, `command`

### Allow placing master blocks
Makes it possible to place "master" blocks (i.e. command blocks, structure blocks, etc.) in survival.\
- Name: `allowPlacingMasterBlocks`
- Type: `boolean`
- Default: `false`
- Category: `WaveTech`, `survival`, `feature`
