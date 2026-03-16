# LegitMod v1.0.0

**Minecraft 1.21.4 · Fabric Loader 0.16.9+**

A lightweight client-side PvP mod that provides several utility features designed for "legit" gameplay.

Keybindings do **not appear in the Controls menu** — all keys are hardcoded and cannot be changed.

---

# Installation

1. Install **Fabric Loader 0.16.9+** for **Minecraft 1.21.4**
2. Install **Fabric API**
3. Copy `legitmod-1.0.0.jar` into your `.minecraft/mods` folder
4. Launch the game

---

# Building from Source

Windows:

```
.\gradlew.bat build
```

Linux / macOS:

```
./gradlew build
```

The compiled `.jar` file will be located at:

```
build/libs/legitmod-1.0.0.jar
```

---

# Keybinds

```
Y  — Toggle TriggerBot
G  — Toggle AimBot
R  — Combo AimBot + TriggerBot

Z  — Toggle TargetStrafe
B  — Toggle Hitbox
=  — Increase Hitbox size (+0.05 blocks)
-  — Decrease Hitbox size (-0.05 blocks)

[  — Toggle ESP
I  — Toggle AutoSprint
H  — Toggle ShiftTap
N  — Toggle NameProtect

V  — ElytraSwap (equip / unequip Elytra)

→  — Toggle AutoAuth
↑  — Toggle AutoRegister

←  — UnHook (disable and hide risky features)
```

---

# Features

## Combat

### TriggerBot [Y]

Automatically attacks any living entity you are aiming at.

Features:

* Attacks exactly at weapon cooldown
* Random delay of **1–10 ms** to simulate human clicks
* Requires holding a **sword**
* Range: **3.0 blocks**
* Synchronizes with **Hitbox**

---

### AimBot [G]

Smoothly aims at the chest of the nearest player with the **lowest HP within 6 blocks**.

Features:

* Smooth aim speed depending on distance
* Target locking after the first acquisition
* Micro aim deviation every **3–6 ticks** for realism
* Reset target by toggling AimBot

---

### TargetStrafe [Z]

Automatically circles around the selected player and performs attacks.

Features:

* Selects player with the lowest HP within **24 blocks**
* Strafes around the target at **0.5 block distance**
* Automatically jumps for **critical hits**
* Built-in TriggerBot
* Player head and body track the target
* Camera remains free (you can look anywhere)

Visual indicators include:

* Rotating ring around the target
* Corner brackets around the player
* Vertical beam above the target

Displays **BPS (Blocks Per Second)** in the HUD.

---

## Movement

### AutoSprint [I]

Automatically enables sprint when moving forward.

Includes a **WTap system**:

* Stops sprint for **3 ticks on hit**
* Reduces enemy knockback
* Automatically restores sprint

Requirements:

* Hunger above 6
* Not sneaking
* Not blocking

---

### ShiftTap [H]

Presses **Shift for 2–3 ticks on every hit**.

Adds a slight downward momentum often used in PvP mechanics.

Does not interfere with AutoSprint.

---

### ElytraSwap [V]

Instantly swaps between **chestplate and Elytra**.

Behavior:

* First press → equip Elytra
* Second press → equip chestplate
* If no chestplate exists → simply equips / removes Elytra

---

## Visual

### Hitbox [B] [=] [-]

Expands the horizontal hitbox of players.

* `=` increase by **0.05 blocks**
* `-` decrease by **0.05 blocks**
* No maximum expansion limit
* Synchronizes with ESP boxes

---

### ESP [[ ]

Displays a **3D box around players through walls**.

Features:

* Semi-transparent blue box
* Glow effect
* Wireframe outline
* Synchronizes with Hitbox

Disabled automatically when **UnHook** is active.

---

## Utilities

### AutoTotem (Always enabled)

Automatically equips a **Totem of Undying** in the offhand:

* When joining the server if the offhand is empty
* When HP drops below **4.5**
* Does nothing if a totem is already equipped

---

### AutoAuth [→]

Automatically sends:

```
/login IRBBY123
```

Runs **2 seconds after joining** the server.

---

### AutoRegister [↑]

Automatically sends:

```
/register IRBBY123 IRBBY123
```

Runs **2 seconds after joining** the server.

---

## Misc

### NameProtect [N]

Replaces your nickname with **"Ezz"** locally.

* Chat
* Tab list

Other players and server administrators still see your **real username**.

---

### UnHook [←]

Emergency system for disabling suspicious modules.

First press disables and hides:

* TriggerBot
* AimBot
* TargetStrafe
* Hitbox
* ESP

Second press restores them.

While active, these keybinds will **not respond**.

---

# HUD

### Top Right

Displays a list of active modules.

* **Green** = enabled
* **Red** = disabled

When UnHook is active, only **AutoSprint** and **ShiftTap** are shown.

---

### Bottom Right

Displays performance information:

* **Ping** — server latency

  * green < 80
  * yellow < 150
  * red > 150

* **TPS** — server ticks per second

  * green > 18
  * yellow > 14
  * red < 14

* **BPS** — blocks per second (only when TargetStrafe is active)

---

# Technical Information

```
Minecraft:       1.21.4
Fabric Loader:   0.16.9+
Fabric API:      0.116.0+1.21.4
Java:            21+
```

Mixins used:

* EntityMixin
* ChatHudMixin
* PlayerListHudMixin

Notes:

* Keybinds do not appear in **Options → Controls**
* The mod has **no configuration file**
* All parameters are **hardcoded**

---

# Notes

* `TargetStrafe` uses `setVelocity()` for movement.
  On servers with **Grim / Polar anticheat** it may trigger speed flags.

* `AutoTotem` and `ElytraSwap` use **client-side inventory clicks**.
  Some servers may block them if inventory actions are restricted.

* `NameProtect` works **locally only**.
  The server and other players always see your real username.
