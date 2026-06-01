# Agent Instructions — Singularity Launcher

## Your Identity
You are an AI coding assistant. You have full autonomy to read, write, edit, delete files, run bash commands, search the web, and access any directory on this system.

## Permissions & Access
- **Full filesystem access** to `/home/alexquasar/` and all subdirectories
- **Neighboring projects** in `/home/alexquasar/Проекты/` — you may read any of them for reference:
  - `Arc/` — Anuken's libGDX fork (Arc engine). Read it freely for API reference.
  - `MindustryModsFin/` — Mindustry mods
  - `LauncherMindustryIdea20006/` — this project
- **Temporary work**: `/tmp/opencode/` for any scratch files
- **Arc sources**: `/home/alexquasar/Проекты/Arc/` — cloned Arc repo, one directory above the project. Prebuilt JARs are used from `arc-core/build/libs/` and `backends/backend-sdl/build/libs/`.
- **Gradle cache**: `/home/alexquasar/.gradle/` — contains Mindustry core JARs
- **GitHub**: No push access configured

## Workflow Rules
1. **Build** with `./gradlew compileJava` to verify compilation.
2. **Do NOT add code explanations/summaries** unless asked.
3. **Do NOT add comments** to code unless asked.
4. **Minimize output** — be concise, direct, 1-3 sentences when possible.
5. **Commit only when explicitly asked.**

## Project Overview
- **Project**: Singularity Launcher — Mindustry version launcher. Scans `versions/` folder for `.jar` files and launches the selected version.
- **Engine**: [Arc](https://github.com/Anuken/Arc) (SDL backend) + Mindustry core API.
- **Build**: Single-module Gradle project. Java 17 source/target. Mindustry `master-SNAPSHOT` from JitPack.
- **Status**: Early prototype. No UI yet — currently grabs the first JAR from `versions/` and launches it.

## Project Structure
- `core/singlaunch/SingularityLauncher.java` — entry point (`main`), scans `versions/` dir, launches Mindustry via `ProcessBuilder`
- `assets/fonts/` — font assets (empty)
- `assets/sprites/` — sprite assets (empty)
- `TestIdea/Idea.txt` — rough plan/notes in Russian

## Architecture
- **Setup flow**: On startup, lists `.jar` files in `versions/` dir → picks first → launches with `java -jar` → exits.
- **Config**: 400×300 window, title "Singularity Launcher".

## Known Issues
- Arc's `backend-sdl` module must be prebuilt via `./gradlew :backends:backend-sdl:jar` in the Arc project before compiling this project.
- No UI yet — currently grabs the first JAR from `versions/` and launches it.

## Build & Run
- `./gradlew run` — build + run
- `./gradlew compileJava` — quick compile check
