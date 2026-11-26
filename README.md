<h1 align="center">Rat-ons</h1>

<div align="center">
    
[![Made with Kotlin](https://img.shields.io/badge/Made%20With-Kotlin-purple?style=for-the-badge&logo=kotlin&logocolor=white&color=7F52FF)](https://kotlinlang.org/)
[![Downloads](https://img.shields.io/badge/Downloads-0-green?style=for-the-badge&logo=github)](https://github.com/Jordyrat/Rat-ons-fabric/releases)
[![Version](https://img.shields.io/badge/Version-v2.0.0-blue?logo=github&style=for-the-badge)](https://github.com/Jordyrat/Rat-ons-fabric/releases)
[![License](https://img.shields.io/badge/License-LGPL%202.1-blue?style=for-the-badge&logo=gnu)](LICENSE)

</div>

A Fabric client mod for Minecraft **26.2** made for Hypixel SkyBlock. Rat-ons focuses on trying to reduce your modlist with features considered UAYOR (Use At Your Own Risk).

If you like Rat-ons, please consider Starring!

## Installation
1. **Install** [Fabric](https://fabricmc.net/use/) for Minecraft **26.2**, the [Fabric API](https://modrinth.com/mod/fabric-api) and [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin). The last two are prerequisites; Rat-ons does not bundle them.
2. **Download** the latest release from [Releases](https://github.com/Jordyrat/Rat-ons-fabric/releases) into your mods folder.
3. **Configure** the mod with `/rat`, or toggle a feature with `/rat toggle <feature>`.

Rat-ons used to be an addon for SkyHanni on 1.8.9. This version targets 26.2 and bundles
[SkyblockAPI](https://github.com/SkyblockAPI/SkyblockAPI), so no companion mod is required.

## AI Usage

Parts of Rat-ons, including this Fabric port and much of its documentation, were written with the help of AI coding tools under human direction and review. The same standard applies to that code as to anything hand-written: it is reviewed, built and run before release, but it is still code you should read before you trust it.

Nothing in the running mod uses an AI model. It makes no AI calls and sends nothing about your gameplay anywhere; the only network request is the update check against GitHub, which you can switch off in `/rat` under Update Mode.

## Building
`./gradlew build` produces the mod jar in `build/libs`. The build needs JDK 25 and Gradle 9.8+
(the wrapper supplies Gradle); Minecraft 26.1 and later ship unobfuscated, so there are no
mappings to configure. `./gradlew runClient` starts the development client.

## Support
Suggest features and report issues using the **Issues** tab above. We do not currently have a discord for support but please feel free to use the **Discussions** tab to talk to other members and ask for support.

### Issues

Please follow the formatting rules below when creating a new issue.

* Start your issue with its type, these include:
    - **Feature:** to suggest any new features for the mod.
    - **Improvement:** to suggest an improvement to an existing feature.
    - **Bug:** to report a bug with the mod.
    - **Docs:** to suggest any changes to the mod's documentation.
    - **Backend:** to suggest any changes to how the backend code works.
      
* Explain the issue in its description.  
* Include any useful pictures.

### Support

No specific formatting is required in the **Discussions** tab but please ensure to use the appropriate category so that any support isn't missed.

## Contributing

Rat-ons is an open-source mod and is open to contributions from anyone. If you would like to contribute to Rat-ons, please follow the [Contributors Guide](docs/CONTRIBUTING.md), create a feature and make a pull request when you're ready!
