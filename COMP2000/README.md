# Predator and Prey Simulation – COMP2000 Assignment 2

Hawks and foxes hunt rabbits and mice in a grassy field. The prey eat plants, hide in a safe meadow, and breed. Seasons change, disease spreads through crowds, every death feeds the soil, and rabbits and mice change their coats with the seasons (white in winter, brown in summer) to hide from predators.

Assignment 2 builds on the A1 simulation (see **Changes from A1** below).

## Controls

Click on the window first so it picks up your key presses.

| Key | What it does |
|---|---|
| **SPACE** | Pause / resume |
| **+** / **-** | Speed up / slow down (x1 up to x32) |
| **1** | Predators chase the **nearest** prey |
| **2** | Predators chase the **weakest** prey |
| **3** | Predators chase the **best meal** (most food for the distance) |
| **W** | Season: Auto → Spring → Summer → Autumn → Winter → Auto |
| **D** | Disease: Off → Mild → Normal → Deadly (Off cures everyone) |
| **O** | Start a disease outbreak right now |
| **S** | Show / hide the state rings |
| **H** | Show / hide health bars |
| **V** | Show / hide vision circles |
| **K** | Show / hide the kill map (red = where predators made kills) |
| **G** | Switch the chart between population and coat colour |
| **P** | Turn parallel planning on / off |

**Tip:** press **+** a few times to watch a full boom-and-bust cycle. One takes a few thousand ticks.

---

## What you are looking at

**The field**
- Green sprouts are food. Bigger sprouts with a **pink flower** are rich food that grew where an animal died.
- **Darker green patches** are fertile ground, from past deaths.
- Small **feathers/fur** mark where something was eaten, and small **bones** mark where something starved or died of disease. They fade away after a while.
- Rabbits and mice **change coat colour with the seasons**: they go white when winter comes and dark again in spring. Some change faster than others.
- The hedged area in the bottom right is the **safe meadow**. Predators can't go in, so prey hide there. It also has its own food.
- The field changes colour with the season. Snow falls in winter, leaves in autumn and petals in spring.

**The rings around animals (S key)**

| Ring | Meaning |
|---|---|
| White | Prey is foraging (looking for food or a mate) |
| Yellow | Prey is fleeing |
| Blue | Prey is hiding, or a predator is resting |
| Red | Predator is hunting |
| Pulsing lime, with a virus icon | The animal is sick |

**The sidebar**
- **Status:** tick number, speed, season and disease level.
- **Population:** how many of each species there are now, the highest it has been, how many are sick, their average speed, their average coat colour (with a colour swatch), and their average moult speed gene.
- **Totals:** how many of each species were born, eaten, starved, died of disease, or got sick.
- **What just happened:** the latest events in plain words.
- **Controls:** a reminder of the keys.

**The chart (bottom)** shows each species' population over time. The dashed lime line is the number of sick animals, and the coloured bands behind it show the seasons. Press **G** to switch to the **coat colour chart**. The thick lines are the average coat colour of rabbits and mice, the dashed line is the field colour (perfect camouflage), and the thin lines are the moult speed gene.

---

## How the simulation works

- **Prey** (rabbits and mice) look for food. If a hunting predator gets close they run, and if the safe meadow is close they run there to hide. A prey animal that is starving will leave cover anyway.
- **Predators** (hawks and foxes) hunt when they're hungry and rest when they're full. Prey ignore a resting predator unless it's very close.
- **Breeding:** rabbits breed when they're well fed. Mice find a mate and breed on contact. Predators breed when they're very full, but not in winter. The energy the parent spends becomes the baby's health.
- **Evolution:** babies get their parent's speed and vision with a small random change. Being fast costs a lot of energy, so speed only pays off when there are lots of predators. You can watch the average speed change in the sidebar.
- **Seasons:** each season changes how fast food grows, how much energy animals use, and how fast disease spreads. Winter is the hardest season.
- **Disease** starts by itself when animals are packed together, then spreads on contact within a species. Sick animals are slower and use more energy. They either die or recover and are immune for a while.
- **Camouflage and moulting:** each season has a field colour, dark in spring, summer and autumn, and white snow in winter. Rabbits and mice slowly change their own coat toward the field colour, like real snowshoe hares. How fast they change is an inherited **moult speed** gene. Prey whose coat matches the field are hard to spot: a predator has to get within about a fifth of its normal vision before it notices a perfectly camouflaged one. Moulting fast costs a little energy, so it only pays off if predators are around.
- **Death feeds new growth:** every death makes the ground there more fertile. Half of all new food grows on fertile ground, and that food is rich (worth 40 instead of 25). Fertility slowly fades over time.

---

## Findings (what the simulation showed in testing)

None of these are programmed in directly. They come from the rules interacting. Numbers are from my own test runs, and every run is a bit different.

1. **Boom-and-bust cycles.** Prey boom, then predators boom, then prey crash, then predators starve, then prey recover. A1 died out within about 2,000 ticks in every test run. A2 keeps going for 12,000+ ticks.
2. **A "deadly ring" around the safe meadow.** Turn on the kill map (K). In testing, **26–52% of all kills happened in a thin band just outside the meadow, which is only 8% of the field.** Nobody told the predators to wait there. Prey run to the meadow, starving prey have to come back out, and predators end up at the edge. Those kills then fertilise the ground, so rich food grows right next to the danger zone and pulls even more prey into it.
3. **Coats change with the seasons, and moulting evolves.** Every winter the average coat goes from about **0.34 to 0.75–0.88 (white)**, then back to dark in spring. The interesting part is spring. Rabbits that are slow to lose their white coat stand out on the green field just as the predators are breeding again, so they get caught. In some runs the moult speed gene **evolved from about 0.35 to 0.50 (rabbits) and 0.55 (mice)**. In other runs it barely moved: if predators are scarce, moulting fast is just wasted energy. **Evolution follows the predators.**
4. **Speed evolves with predator numbers.** Rabbits got faster (1.00 → 1.19) during predator booms, then slower again (0.94) once predators were gone, because speed costs energy.
5. **The refuge is also a trap for disease.** The safe meadow protects prey from predators but crowds them together, so outbreaks often start there.
6. **Prey starving from fear.** Before prey were allowed to ignore resting predators, they starved even with food at its maximum, because they spent all their time running. Ecologists call this a "landscape of fear".

**Try this in a demo:**
- Press **+** a few times, then **K**, and watch the red ring form around the meadow.
- Press **W** until it says Winter, then **D** twice (Deadly) and **O**. The disease tears through the prey crowded in the meadow.
- Press **G** and wait for winter. The coat line rises as the animals turn white, then drops again in spring.

---

## Changes from A1

### Problems in A1 that are now fixed
- **Everything died out** within about 2,000 ticks in every test run, because there was no breeding apart from mice, and mice almost never met. Now every species breeds, so the ecosystem keeps going.
- **Prey just inside the safe zone could be eaten** by a predator standing just outside it. `tryEat()` now checks the safe zone.
- **`Hawk.act()` and `Fox.act()` were the same code copied twice.** That code is gone. Both now share one hunting system.
- **Animals jittered on the spot when wandering.** They now walk in smooth curves and turn to face the way they're going.
- **"Reproduction skipped" filled up the console.** Failed births are now counted in the sidebar instead.

### Design patterns added
| Pattern | Where | What it does |
|---|---|---|
| **Strategy** | `HuntStrategy`, `HuntStrategies` | How a predator picks its target. Each strategy is a lambda, and you can swap them with keys 1/2/3. |
| **State** | `AnimalState`, `PreyState`, `PredatorState` | Prey are foraging, fleeing or hiding; predators are hunting or resting. Each state decides what the animal does and when it changes state. |
| **Observer** | `SimListener`, `SimEvent`, `EventType` | World announces births, deaths, season changes and so on. The chart, the event log and the `Ground` listen without World knowing about them. |
| **Decorator** | `Renderer`, `RendererDecorator` and the `…Decorator` classes | Rings, health bars, vision circles and the sickness glow are drawn as layers you can turn on and off. |
| **Factory method** | `Animal.makeBaby()` | Lets `World.spawnNear()` make a baby of any species. |

### Streams, lambdas and parallelism added
- **Two-step tick** (`World.update()`): first every animal makes a plan on a **parallel stream**, which only reads the world. Then the plans are carried out one at a time. This stops two threads changing things at once. For example, if two hawks plan to eat the same mouse, only the first one gets it.
- **Custom collector** (`Census.collector()`): counts every species and averages their health, speed and vision in one pass. It works safely on a parallel stream.
- **Built-in collectors:** `EventLog.breakdown()` uses `groupingBy` with `counting()` to make the Totals table.
- **Streams instead of loops:** `findNearest()`, `getAnimals()` (with `flatMap`), `PopulationHistory`, and the mouse mate search.
- **Disease spread** also uses the same "work it out in parallel, then apply on one thread" idea.
- **Separate simulation thread** (`SimulationLoop`): the simulation no longer runs on the same thread as the window. Both lock on the `World` so the window never draws half a tick.

### New features
- Breeding for every species, with heritable traits (evolution).
- Seasons, which you can pick with W.
- Disease, which you can control with D and O.
- Camouflage and seasonal moulting, with an inherited moult speed gene that evolves, and a coat colour chart (G).
- Death feeds the soil, and rich food grows there. A kill heatmap (K) and remains show where animals died.
- The safe meadow has its own food supply.
- Predators rest when full, and prey only fear hunting predators.
- Detailed drawings of every animal (flapping hawks, trotting foxes, hopping rabbits, mice with wiggling tails), a grass field background, and seasonal weather.
- A live population chart, a stats sidebar and an event feed.
- Pause, speed control, strategy switching and overlay toggles.

### Numbers I changed
| | A1 | A2 |
|---|---|---|
| Starting rabbits / mice / food | 15 / 20 / 30 | 20 / 30 / 60 |
| Hawk speed | 3.0 | 2.4 |
| Fox speed | 2.2 | 2.0 |
| Food regrowth | 1 every 30 ticks | about 1 every 3 ticks (changes with season) |
| Window | 800 × 600 | 800 × 600 world, plus a sidebar and a chart |

### A1 code that was removed
- `Entity.update()` and `Food.update()`, because only animals act now, through `plan()`.
- `World.spawnMouseNear()`, which became `World.spawnNear()` so every species can breed.

---

## Files

| File | What it is |
|---|---|
| `Main` | Starts everything |
| `World` | Holds all the entities and runs each tick |
| `Entity`, `Animal`, `Predator`, `Prey` | The class hierarchy |
| `Hawk`, `Fox`, `Rabbit`, `Mouse`, `Food` | The things in the world, and how each one is drawn |
| `Edible` | Anything that can be eaten |
| `Traits` | Speed, vision and moult speed that babies inherit |
| `Action` | A plan an animal makes for this tick |
| `AnimalState`, `PreyState`, `PredatorState` | State pattern |
| `HuntStrategy`, `HuntStrategies` | Strategy pattern |
| `SimListener`, `SimEvent`, `EventType` | Observer pattern |
| `PopulationHistory`, `EventLog` | Observers that feed the chart and the sidebar |
| `Census` | The custom collector |
| `Season`, `DiseaseLevel` | Seasons and disease settings |
| `Ground` | Observer that tracks fertile ground, the kill map and remains |
| `Renderer`, `RendererDecorator`, `VisionDecorator`, `StateRingDecorator`, `HealthBarDecorator`, `SicknessDecorator` | Decorator pattern for drawing |
| `SimulationLoop` | Runs the simulation on its own thread |
| `SimPanel` | The window: world, sidebar and chart |
| `SpawnException` | Thrown when a baby can't be placed |
| `Benchmark` | Speed test, sequential vs parallel |

---

## Credits

- **A2:** built on top of the A1 code. And some AI assistance was used.
