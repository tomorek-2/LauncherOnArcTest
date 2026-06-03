# Singularity Launcher

A Mindustry version launcher built with the [Arc engine](https://github.com/Anuken/Arc). Scans a `versions/` directory for `.jar` files and launches the selected Mindustry version.

## Features

- Lists all Mindustry `.jar` files placed in `versions/`
- Launches the selected version as a separate process
- Game-styled UI with Arc Scene2D

## Usage

1. Place Mindustry `.jar` files in the `versions/` directory
2. Run the launcher:

```bash
./gradlew run
```

Or build a standalone JAR:

```bash
./gradlew jar
java -jar build/libs/SingularityLauncher.jar
```

## Build

Requires **Java 17+**.

```bash
./gradlew compileJava
```

## Dependencies

- **Arc** — game framework (arc-core, backend-sdl, natives-desktop via JitPack)
- **Mindustry** — core API for version scanning (JitPack, `master-SNAPSHOT`)

## License

MIT
