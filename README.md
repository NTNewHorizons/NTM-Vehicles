# NTM: Vehicles — Minecraft 1.7.10

Backport of [TechTastic's NTM: Vehicles](https://github.com/TechTastic/NTM-Vehicles),
from the [NTNewHorizons fork](https://github.com/NTNewHorizons/NTM-Vehicles).
Requires **Immersive Vehicles: Legacy** (`immersivevehicleslegacy`) and **Hbm's
Nuclear Tech Mod** (`hbm`). This branch targets the local HBM 1.0.27_X5808 API,
not the 1.12.2 NTM fork. No mixins or changes to either dependency are required.

## Build

Use Java 25 to run Gradle/RFG; compilation and Minecraft use an automatically
provisioned Java 8 toolchain. The wrapper uses the same Gradle/RFG generation as IVL.

With the repositories in sibling directories, build their development artifacts first:

```sh
cd ../IVL
VERSION=0.1.0-dev ./gradlew build
cd ../Hbm-s-Nuclear-Tech-GIT
# NTM's older Gradle wrapper itself requires Java 8.
JAVA_HOME=/path/to/jdk8 ./gradlew devJar
cd ../NTM-Vehicles
./gradlew build
```

The default dependencies are:

- `../IVL/build/libs/immersivevehicleslegacy-0.1.0-dev-dev.jar`
- `../Hbm-s-Nuclear-Tech-GIT/build/libs/HBM-NTM-[1.0.27_X5808]-dev.jar`

For other locations or versions:

```sh
./gradlew build -PivlJar=/path/to/ivl-dev.jar -PntmJar=/path/to/ntm-dev.jar
```

Install **`build/libs/ntm_vehicles-1.0.0-1.7.10.jar`** alongside normal
(non-development) IVL and NTM jars in a Forge 10.13.4.1614 instance. Do not install
this alongside the original 1.12.2 bridge; both use the same mod ID.
The bridge jar bundles neither dependency.

`./gradlew runClient` loads the two local development jars, development NEI and
CodeChickenCore, and IVL's audio libraries. Do not duplicate those jars in `run/mods`.
The upstream `libs/Immersive Vehicles-1.12.2-22.18.0.jar` is retained but unused.

## Unchanged pack-creator API

Existing bullet JSON uses the same `CUSTOM` type and `customHitFunctions` IDs:

| Function | Behavior |
| --- | --- |
| `ntm_vehicles:nuke` | NTM MK5 nuclear explosion and standard Torex mushroom cloud |
| `ntm_vehicles:gas` | NTM gas entities with the original numeric type selection |
| `ntm_vehicles:napalm` | 2.5-strength explosion, ignition bounds 9 and 14, five flame bursts |

For nuke and gas, `bullet.blastStrength` is used unless zero, when
`bullet.diameter / 10` is used. The result is truncated to an integer, just as
upstream. For gas this is the **entity count**, not a radius.

Gas constants remain in the top-level `constantValues` object:

- `gasSpreadSpeed`: Gaussian velocity multiplier; default **1.25**.
- `gasType`: **0** chlorine (default), **1** cloud, **2** pink cloud,
  all other values orange cloud. Values are truncated to integers.

```json
{
  "bullet": {
    "diameter": 120,
    "blastStrength": 8,
    "types": ["CUSTOM"],
    "customHitFunctions": ["ntm_vehicles:gas"]
  },
  "constantValues": {
    "gasType": 0,
    "gasSpreadSpeed": 1.25
  }
}
```

This is a fragment of a normal IV bullet definition, not a complete pack item.
Use `ntm_vehicles:nuke` or `ntm_vehicles:napalm` in that list for those effects.
No pack JSON changes are required by the bridge port. This does not translate
unrelated 1.12.2 NTM item IDs or add cross-mod fluid/energy integrations that the
upstream bridge never provided.

Effects execute only on the server. IVL still controls custom-hit dispatch and
its bullet damage/explosion settings. Effect implementation, gas damage,
radiation, tracking, and visuals follow the installed **1.7.10 NTM**; they are
not a backport of the entire 1.12.2 NTM engine or its dimension restrictions.

## Verification and rollback

`./gradlew cleanTest build` checks unchanged JSON/constants, effect IDs, Forge
dependencies, IVL world unwrapping, all gas types, count, position, velocity,
nuclear factory arguments, napalm ignition/bursts, registered server callback,
and client-side suppression against the real dependency classes.

Verified locally: all 13 regression tests pass; the development client loads
the bridge, IVL, and HBM 1.0.27_X5808 together, enters an integrated-server world,
and shuts down successfully. This does not yet verify firing each effect.

For gameplay verification use a disposable world: fire each custom bullet at
blocks and entities, check nuclear damage/cloud/radiation, gas types/protection,
and napalm ignition/bursts. Repeat on a dedicated server. Startup and unit tests
alone do not establish gameplay parity.

The bridge adds no blocks, items, saved data, or migration writes. Roll back by
removing its jar; packs may stay installed, but those custom effects will no
longer run. The original 1.12.2 code remains on `master`; the backport is isolated
on `backport/1.7.10`.
