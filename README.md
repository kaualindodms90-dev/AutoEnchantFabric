# Auto Enchant (Fabric 1.21.1)

A source project for a server-side Fabric mod that checks a player's main-hand item every 10 ticks and applies the configured enchantment levels. Intended for Minecraft Java 1.21.1, Fabric Loader, Fabric API, and Java 21.

## Build locally

1. Install Java 21 and Gradle 8.8 or a compatible Gradle 8.x release.
2. In this folder, run `gradle build`.
3. The built JAR will be in `build/libs/`.
4. Put the main mod JAR in the Minecraft `mods` folder with Fabric API for 1.21.1.

## Build without installing Gradle (GitHub Actions)

1. Create a GitHub repository and upload the contents of this project (including `.github/workflows/build.yml`).
2. Open the repository's **Actions** tab.
3. Select **Build Auto Enchant** and click **Run workflow** (or push to the `main` branch).
4. When the workflow finishes successfully, open its run and download the artifact named `auto-enchant-fabric-1.21.1`.
5. Extract the artifact and copy the mod JAR into the `mods` folder.

## Important

This is source code and a build workflow, not a precompiled or verified JAR. Build it and test it in a backup world first. The mod applies enchantments directly to the held stack; this can bypass the normal anvil/enchanting-table compatibility restrictions. The chosen levels are ordinary vanilla maximum levels, but some combinations may be mutually exclusive in normal gameplay. It currently checks the main hand only, not the offhand or armor slots.
