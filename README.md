# NTM: Vehicles - Minecraft 1.12.2

Integration based on [TechTastic's NTM: Vehicles](https://github.com/TechTastic/NTM-Vehicles)
([NTNewHorizons fork](https://github.com/NTNewHorizons/NTM-Vehicles)).
Requires Immersive Vehicles (MTS) 22.18.0 or later, NTM (`hbm`), and MixinBooter
for Minecraft 1.12.2.

The custom bullet effects and power-armour detection support either NTM
[Community Edition (CE)](https://github.com/Warfactory-Official/Hbm-s-Nuclear-Tech-CE)
or [ExtendedEdition](https://github.com/Alcatergit/Hbm-s-Nuclear-Tech-GIT)
installed alongside MTS. These are alternative NTM installations, not mods to
install together; the same bridge and pack API work with either fork.

## Build

```sh
./gradlew build
```

Pinned development dependencies:

- NTM ExtendedEdition `curse.maven:ntm-708939:7117833` from `https://cursemaven.com/`
- MTS `com.example:Immersive Vehicles-1.12.2:22.18.0`, supplied by
  `libs/Immersive Vehicles-1.12.2-22.18.0.jar`
- MixinBooter `zone.rong:mixinbooter:10.2` from `https://maven.cleanroommc.com/`

The bridge compiles against the `minecrafttransportsimulator.*` classes inside
the MTS jar. Install `build/libs/ntm_vehicles-*.jar` alongside MTS, your chosen
NTM fork, and MixinBooter in Minecraft Forge 1.12.2. The bridge bundles none
of these dependencies. Install it on both client and server for armour-driven
animations and server-side conditions.

## Pack API

### Custom bullet effects

Existing `CUSTOM` bullet functions:

The bridge selects CE or ExtendedEdition effect signatures at startup.
CE uses the standard Torex cloud type and an unattributed detonator, preserving
the original effects without requiring separate bridge builds.

| Function | Behavior |
| --- | --- |
| `ntm_vehicles:nuke` | NTM MK5 explosion with a Torex mushroom cloud |
| `ntm_vehicles:gas` | NTM gas entities; count from blast size |
| `ntm_vehicles:napalm` | 2.5-strength explosion, burning area, flames, and five particle bursts |

For nuclear strength/cloud size and gas count, `blastStrength` is used unless
zero, when `diameter / 10` is used. These values are converted to integers.
Napalm uses fixed effect sizes instead.

Gas tuning lives in `constantValues`:

| Key | Default | Behavior |
| --- | --- | --- |
| `gasType` | `0` | `0`: chlorine, `1`: cloud, `2`: pink cloud, anything else: orange gas |
| `gasSpreadSpeed` | `1.25` | Initial gas velocity spread |

Effects delegate to NTM, so NTM's own configuration and dimension restrictions
still apply. The bridge adds no blocks, items, or saved data; roll back by
removing its jar. Packs using the custom effects or armour variable then lose
those integrations.

### Power armour detection

MTS packs can use the computed animation variable `seat_rider_power_armor`
on a seat, or `seat_rider_power_armor_1` from the vehicle for its first seat
(using MTS's normal numbered-part variable lookup).

- `1`: the rider is a player wearing a complete matching NTM power-armour set.
- `0`: empty seat, non-player rider, ordinary armour, or an incomplete/mixed set.

CE and ExtendedEdition battery-powered suits and CE fuel-powered suits (including their subclasses)
are recognised. Detection follows NTM's full-set rules, including suits that
do not require a helmet. Charge/fuel is deliberately ignored: an empty suit
is still being worn. The value is recomputed as riders and equipment change,
on both client and server; this does not restrict mounting or change armour behaviour.

Example animation condition:

```json
{
  "animationType": "visibility",
  "variable": "seat_rider_power_armor",
  "clampMin": 1,
  "clampMax": 1
}
```
