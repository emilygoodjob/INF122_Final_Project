# GMAE — GuildQuest Mini-Adventure Environment

**Group 18 | INF122 Final Project**

**Team:** Emily Gao Wang, Zhouheng Tao, Hiu Yau Lai, Junhao Che, Yatong Xing

A two-player local mini-adventure framework set in the GuildQuest world. Players create profiles, pick a mini-adventure from the menu, and compete turn-by-turn through a JavaFX GUI.

---

## Requirements

* Java 17+
* Maven 3.6+
* JavaFX 21+

---

## Setup (If Maven is not installed)

### 1. Install Homebrew (Mac only)

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

Then run:

```bash
echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zshrc
eval "$(/opt/homebrew/bin/brew shellenv)"
```

---

### 2. Install Maven

Check if Maven is installed:

```bash
mvn -version
```

If you see `command not found`, install it:

```bash
brew install maven
```

Verify installation:

```bash
mvn -version
```

---

### 3. Build the project

Before running, build the project:

```bash
mvn clean install
```

---

Verify your versions:

```bash
java -version
mvn -version
```

---

## How to Run

Clone the repository and run from the project root:

```bash
git clone https://github.com/emilygoodjob/INF122_Final_Project
cd INF122_Final_Project
mvn javafx:run
```

Maven will download all dependencies (including JavaFX 21) automatically on first run.

To run unit tests:

```bash
mvn test
```

---

## How to Play

### Step 1 — Enter Player Names

When the application launches you will see the **Main Menu**.

* Type **Player 1's name** in the first text field.
* Type **Player 2's name** in the second text field.
* Names must be non-blank and different from each other.
* If a name matches a previously saved profile, that profile's stats (games played, wins) are loaded automatically.

### Step 2 — View the Adventurer Roster

Below the name fields, the **Adventurer Roster** shows all known player profiles with their win statistics. This acts as a persistent leaderboard across sessions.

### Step 3 — Choose a Mini-Adventure

The menu lists all available mini-adventures with a one-line description.
Click an adventure to highlight it, then press **Start Adventure**.

### Step 4 — Understand the Game Screen

Once the game starts, the **GameView** displays:

| UI Panel                         | What it shows                                                          |
| -------------------------------- | ---------------------------------------------------------------------- |
| **Round / Status bar**           | Current round number and status message                                |
| **Player 1 info panel** (left)   | Name, score/gold/relics, current realm, inventory                      |
| **Player 2 info panel** (right)  | Same for Player 2                                                      |
| **Map grid** (center)            | 6×8 realm grid — your position and opponent's position are highlighted |
| **Action panel** (bottom-center) | Buttons for each valid action this turn                                |
| **Game log** (bottom)            | Scrolling history of what happened each turn                           |

### Step 5 — Take Your Turn

Players alternate turns. On your turn, the action panel shows only the actions available to you right now (these change based on your realm, inventory, and game state).

**Click an action button** to select it. Some actions open a follow-up dialog:

* **MOVE** → a list of adjacent realms appears; click one to travel there.
* **BUY** → a list of goods available at the current realm's market appears with prices; click one to purchase.
* **SELL** → shows goods you are carrying; click one and choose a quantity to sell.
* **TRADE** → shows fulfillable trade orders at the current realm; click one to complete it.
* **USE ITEM** → shows items in your inventory; click one to activate it.
* **DEFEND** → no follow-up needed; activates immediately.
* **PASS** → ends your turn with no action.

After you confirm an action, the game log updates and play passes to the other player.

### Step 6 — End of Round

After both players have taken their turns, the round ends automatically. The round counter increments, any expiring effects are cleared, and trade orders may tick down.

### Step 7 — Game Over

When the finish condition is met (win target reached or max rounds elapsed) the **result screen** appears showing:

* The winner's name (or "Tie")
* A summary of the final outcome (e.g., final gold totals or relic counts)

Player profiles are updated automatically — games played and wins are saved to `gmae_profiles.properties` in the project root and will be visible in the Adventurer Roster next time.

### Step 8 — Play Again or Return to Menu

After the result screen, press **Play Again** to restart the same adventure with the same players, or **Back to Menu** to return to the main menu and choose a different adventure or new players.

---

## Mini-Adventures

### Caravan Trade Run

Travel between realms, buy and sell goods, and deliver trade orders to build the richest caravan.

**Objective:** Be the first player to accumulate **30 gold**, or have the most gold after **10 rounds**.

**Setup:**

* Each player starts with **12 gold** and an empty inventory.
* 6 trade orders are placed across the realm map at the start.
* Each realm has a market with unique buy and sell prices for four goods: **Spice, Silk, Ore, Tea**.

**Actions:**

| Action    | Description                                                        |
| --------- | ------------------------------------------------------------------ |
| **MOVE**  | Travel to an adjacent realm on the map                             |
| **BUY**   | Purchase a good at the current realm's market price                |
| **SELL**  | Sell a good you are carrying for the current realm's sell price    |
| **TRADE** | Fulfill an open trade order whose destination is the current realm |
| **PASS**  | End your turn without acting                                       |

**Trade Orders:**

* Each order specifies an item, a quantity, a destination realm, and a gold reward.
* Press **View Open Orders** (shown in the action panel) to inspect all active orders and their remaining turns.
* Orders expire after a fixed number of turns — complete them before they disappear.

**Winning:**

* Reaching **30 gold** at any point instantly wins the game.
* If neither player hits 30 after 10 rounds, the player with more gold wins.

**Tips:**

* Buy cheap in one realm, sell high in another — price spreads are your main profit engine.
* Trade orders pay a fixed bonus; prioritize orders whose destination aligns with your travel route.

---

### Relic Hunt

Race across the realm map, collect scattered relics, and overcome your rival with defense and power-ups.

**Objective:** Be the first player to collect **5 relics**, or hold the most relics after **10 rounds**.

**Setup:**

* Relics are scattered randomly across the **6×8 realm grid**.
* Both players start at different positions on the map.

**Actions:**

| Action       | Description                                                                |
| ------------ | -------------------------------------------------------------------------- |
| **MOVE**     | Travel to an adjacent realm; collect a relic automatically if one is there |
| **DEFEND**   | Enter a defensive stance — blocks one steal attempt against you this round |
| **USE ITEM** | Activate a power-up from your inventory                                    |
| **PASS**     | End your turn without acting                                               |

**Stealing:**
Moving into the same realm as your opponent while they are **not** defending steals one of their relics.

**Power-ups** (35% drop chance on collection or steal):

| Power-up             | Effect                                                      |
| -------------------- | ----------------------------------------------------------- |
| **Twin Relic Charm** | Your next relic collection counts as **two** relics         |
| **Ward Sigil**       | Automatically blocks the **next** steal attempt against you |

**Winning:**

* First to **5 relics** wins instantly.
* If neither player reaches 5 after 10 rounds, the player with more relics wins.

---

## Player Profiles & Persistence

Profiles are stored in `gmae_profiles.properties` (auto-created in the project root on first run).

Each profile tracks:

* Player name
* Games played
* Games won / lost
* Win rate (shown in the Adventurer Roster)
* Inventory snapshot

Profiles load automatically when a player enters a name they have used before. No manual setup is required.
