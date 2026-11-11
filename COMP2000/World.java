import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * World owns every entity and drives the simulation. It stores each kind
 * of entity in its own typed List (List<Hawk>, List<Mouse>, etc.) so
 * animals can query "give me all the mice" without casting, and it also
 * offers allEntities() as a single List<Entity> for generic operations
 * like drawing.
 *
 * A2: update() now has two steps. First every animal makes a plan on a
 * parallel stream (this only reads, so the threads can't clash). Then the
 * plans are run one at a time, which is the only place anything changes.
 * World is also the subject for the Observer pattern (addListener/publish).
 *
 * The simulation runs on its own thread and Swing paints on another, so
 * both of them lock on the World object (see SimulationLoop and SimPanel).
 */
public class World {
    public static final int SEASON_LENGTH = 1500;

    private final int width, height;
    private final List<Hawk> hawks = new ArrayList<>();
    private final List<Fox> foxes = new ArrayList<>();
    private final List<Rabbit> rabbits = new ArrayList<>();
    private final List<Mouse> mice = new ArrayList<>();
    private final List<Food> food = new ArrayList<>();
    private final List<SimListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, Integer> capacity = new HashMap<>();
    private final Ground ground;
    private int tickCount = 0;
    private volatile boolean parallel = true;
    private String huntStrategyName = "Default";

    private Season season = Season.SPRING;
    private Season lockedSeason = null; // null means the seasons change by themselves
    private int seasonTicks = 0;
    private DiseaseLevel diseaseLevel = DiseaseLevel.NORMAL;

    private int foodEvery = 3;
    private int maxFood = 160;
    private int maxZoneFood = 25;

    // A rectangular refuge that Predators are physically barred from entering.
    // Rabbit and Mouse are ordinary Prey, so nothing stops them going in.
    private final double zoneX, zoneY, zoneWidth, zoneHeight;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        this.zoneWidth = width * 0.22;
        this.zoneHeight = height * 0.3;
        this.zoneX = width - zoneWidth - 20;
        this.zoneY = height - zoneHeight - 20;
        capacity.put("Hawk", 40);
        capacity.put("Fox", 40);
        capacity.put("Rabbit", 160);
        capacity.put("Mouse", 220);
        ground = new Ground(width, height);
        addListener(ground);
    }

    public Ground getGround() { return ground; }

    public boolean isInSafeZone(double px, double py) {
        return px >= zoneX && px <= zoneX + zoneWidth
            && py >= zoneY && py <= zoneY + zoneHeight;
    }

    public boolean isInSafeZone(Entity e) {
        return isInSafeZone(e.getX(), e.getY());
    }

    public double distanceToZone(Entity e) {
        double dx = Math.max(Math.max(zoneX - e.getX(), 0), e.getX() - (zoneX + zoneWidth));
        double dy = Math.max(Math.max(zoneY - e.getY(), 0), e.getY() - (zoneY + zoneHeight));
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double getZoneX() { return zoneX; }
    public double getZoneY() { return zoneY; }
    public double getZoneWidth() { return zoneWidth; }
    public double getZoneHeight() { return zoneHeight; }
    public double getZoneCentreX() { return zoneX + zoneWidth / 2; }
    public double getZoneCentreY() { return zoneY + zoneHeight / 2; }

    public List<Hawk> getHawks() { return hawks; }
    public List<Fox> getFoxes() { return foxes; }
    public List<Rabbit> getRabbits() { return rabbits; }
    public List<Mouse> getMice() { return mice; }
    public List<Food> getFood() { return food; }

    public List<Predator> getPredators() {
        List<Predator> predators = new ArrayList<>();
        predators.addAll(hawks);
        predators.addAll(foxes);
        return predators;
    }

    public List<Prey> getPrey() {
        List<Prey> prey = new ArrayList<>();
        prey.addAll(rabbits);
        prey.addAll(mice);
        return prey;
    }

    public List<Animal> getAnimals() {
        return Stream.of(hawks, foxes, rabbits, mice)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<Food> foodInSafeZone() {
        return food.stream().filter(this::isInSafeZone).collect(Collectors.toList());
    }

    /** Food goes first so it's drawn on the ground, and hawks go last because they fly over everything. */
    public List<Entity> allEntities() {
        List<Entity> all = new ArrayList<>(food);
        all.addAll(mice);
        all.addAll(rabbits);
        all.addAll(foxes);
        all.addAll(hawks);
        return all;
    }

    public void addHawk(Hawk h) { hawks.add(h); }
    public void addFox(Fox f) { foxes.add(f); }
    public void addRabbit(Rabbit r) { rabbits.add(r); }
    public void addMouse(Mouse m) { mice.add(m); }
    public void addFood(Food f) { food.add(f); }

    public int countOf(String species) {
        switch (species) {
            case "Hawk": return hawks.size();
            case "Fox": return foxes.size();
            case "Rabbit": return rabbits.size();
            case "Mouse": return mice.size();
            default: return 0;
        }
    }

    public void setCapacity(String species, int cap) { capacity.put(species, cap); }

    public void setFoodSupply(int every, int max) {
        this.foodEvery = every;
        this.maxFood = max;
    }

    /**
     * This used to be spawnMouseNear(). Now any species can use it. It still
     * throws SpawnException, and now also when there are too many of that species.
     */
    public Animal spawnNear(Animal parent, double startingHealth) throws SpawnException {
        String species = parent.getSpecies();
        if (countOf(species) >= capacity.getOrDefault(species, Integer.MAX_VALUE)) {
            throw new SpawnException(species + " population is full");
        }
        double x = parent.getX() + (Math.random() - 0.5) * 20;
        double y = parent.getY() + (Math.random() - 0.5) * 20;
        if (x < 0 || x > width || y < 0 || y > height) {
            throw new SpawnException("Spawn point (" + x + ", " + y + ") is out of bounds");
        }
        Animal baby = parent.makeBaby(x, y, parent.getTraits().mutate());
        baby.health = startingHealth;
        if (baby instanceof Prey && parent instanceof Prey) {
            ((Prey) baby).setCoat(((Prey) parent).getCoat());
        }
        baby.addTo(this);
        publish(EventType.BORN, baby);
        return baby;
    }

    public void setHuntStrategy(String name, HuntStrategy strategy) {
        for (Predator p : getPredators()) {
            p.setHuntStrategy(strategy);
        }
        huntStrategyName = name;
        publish(new SimEvent(EventType.STRATEGY_CHANGED, tickCount, null, 0, 0, name, null));
    }

    public String getHuntStrategyName() { return huntStrategyName; }

    public Season getSeason() { return season; }
    public Season getLockedSeason() { return lockedSeason; }
    public int ticksUntilNextSeason() { return SEASON_LENGTH - seasonTicks; }

    /** W key: Auto -> Spring -> Summer -> Autumn -> Winter -> Auto. */
    public void cycleSeasonMode() {
        if (lockedSeason == null) {
            lockedSeason = Season.SPRING;
        } else if (lockedSeason == Season.WINTER) {
            lockedSeason = null;
        } else {
            lockedSeason = lockedSeason.next();
        }
        if (lockedSeason != null) {
            changeSeason(lockedSeason);
        }
        seasonTicks = 0;
    }

    private void updateSeason() {
        if (lockedSeason != null) {
            return;
        }
        seasonTicks++;
        if (seasonTicks >= SEASON_LENGTH) {
            seasonTicks = 0;
            changeSeason(season.next());
        }
    }

    private void changeSeason(Season next) {
        if (next != season) {
            season = next;
            publish(new SimEvent(EventType.SEASON_CHANGED, tickCount, null, 0, 0, next.getLabel(), null));
        }
    }

    public DiseaseLevel getDiseaseLevel() { return diseaseLevel; }

    /** D key: Off -> Mild -> Normal -> Deadly -> Off. Turning it off cures everyone. */
    public void cycleDiseaseLevel() {
        diseaseLevel = diseaseLevel.next();
        if (diseaseLevel == DiseaseLevel.OFF) {
            for (Animal a : getAnimals()) {
                a.cure();
            }
        }
        publish(new SimEvent(EventType.DISEASE_CHANGED, tickCount, null, 0, 0, diseaseLevel.getLabel(), null));
    }

    /**
     * A healthy animal can catch the disease from sick animals of the same
     * species that are touching it. More sick neighbours = more chance, so
     * crowds spread it the fastest.
     *
     * Working out who gets infected only reads, so I do it on a parallel
     * stream (with ThreadLocalRandom so the threads don't share a Random).
     * Then infect() is called afterwards on this thread.
     */
    private void spreadDisease() {
        if (diseaseLevel == DiseaseLevel.OFF) {
            return;
        }
        List<Animal> animals = getAnimals();
        Map<String, List<Animal>> sickBySpecies = animals.stream()
                .filter(a -> a.isAlive() && a.isSick())
                .collect(Collectors.groupingBy(Entity::getSpecies));
        if (sickBySpecies.isEmpty()) {
            return;
        }

        double chance = Animal.SPREAD_CHANCE * season.getDiseaseFactor() * diseaseLevel.getSpreadFactor();
        chance = Math.min(chance, 0.9);
        final double spreadChance = chance;

        List<Animal> newlyInfected = animals.parallelStream()
                .filter(a -> a.isAlive() && a.canCatchDisease())
                .filter(a -> catchesIt(a, sickBySpecies, spreadChance))
                .collect(Collectors.toList());

        for (Animal a : newlyInfected) {
            a.infect();
            publish(EventType.INFECTED, a);
        }
    }

    private boolean catchesIt(Animal a, Map<String, List<Animal>> sickBySpecies, double chance) {
        List<Animal> sick = sickBySpecies.getOrDefault(a.getSpecies(), Collections.emptyList());
        int contacts = 0;
        for (Animal s : sick) {
            if (a.distanceTo(s) <= Animal.CONTACT_DISTANCE) {
                contacts++;
            }
        }
        if (contacts == 0) {
            return false;
        }
        double p = 1 - Math.pow(1 - chance, contacts);
        return ThreadLocalRandom.current().nextDouble() < p;
    }

    /**
     * Disease isn't added on purpose. Each tick I check 3 random animals, and
     * if one is squashed in with lots of its own kind it might start an
     * outbreak. So it shows up in big crowds, like the safe zone.
     */
    private void maybeStartOutbreak() {
        List<Animal> animals = getAnimals();
        if (animals.isEmpty() || diseaseLevel == DiseaseLevel.OFF) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            Animal a = animals.get((int) (Math.random() * animals.size()));
            if (!a.canCatchDisease()) {
                continue;
            }
            int neighbours = 0;
            for (Animal other : animals) {
                if (other != a && other.getSpecies().equals(a.getSpecies()) && a.distanceTo(other) <= 25) {
                    neighbours++;
                }
            }
            double chance = 0.004 * season.getDiseaseFactor() * diseaseLevel.getOutbreakFactor();
            if (neighbours >= 7 && Math.random() < chance) {
                a.infect();
                publish(new SimEvent(EventType.OUTBREAK, tickCount, a.getSpecies(), a.getX(), a.getY(), "in a crowd", null));
            }
        }
    }

    /** O key: makes 5 of the most common prey sick. Does nothing if the disease is Off. */
    public void triggerOutbreak() {
        if (diseaseLevel == DiseaseLevel.OFF) {
            return;
        }
        List<Prey> group = new ArrayList<>();
        if (mice.size() >= rabbits.size()) {
            group.addAll(mice);
        } else {
            group.addAll(rabbits);
        }
        if (group.isEmpty()) {
            return;
        }
        Collections.shuffle(group);
        int infected = 0;
        for (Prey p : group) {
            if (infected == 5) {
                break;
            }
            if (p.canCatchDisease()) {
                p.infect();
                infected++;
            }
        }
        publish(new SimEvent(EventType.OUTBREAK, tickCount, group.get(0).getSpecies(), 0, 0, "triggered", null));
    }

    public void addListener(SimListener l) { listeners.add(l); }
    public void removeListener(SimListener l) { listeners.remove(l); }

    public void publish(EventType type, Entity e) {
        publish(new SimEvent(type, tickCount, e.getSpecies(), e.getX(), e.getY(), null, null));
    }

    /** If one listener crashes, the others still get the event and the simulation keeps going. */
    public void publish(SimEvent event) {
        for (SimListener l : listeners) {
            try {
                l.onEvent(event);
            } catch (RuntimeException ex) {
                System.err.println("Listener failed on " + event + ": " + ex);
            }
        }
    }

    public boolean isParallel() { return parallel; }
    public void setParallel(boolean parallel) { this.parallel = parallel; }
    public int getTickCount() { return tickCount; }

    public void update() {
        tickCount++;
        List<Animal> animals = getAnimals();

        // step 1: every animal makes a plan (parallel, read only)
        Stream<Animal> stream;
        if (parallel) {
            stream = animals.parallelStream();
        } else {
            stream = animals.stream();
        }
        List<Action> plans = stream
                .filter(Entity::isAlive)
                .map(a -> a.plan(this))
                .collect(Collectors.toList());

        // step 2: run the plans one at a time
        for (Action action : plans) {
            action.apply(this);
        }

        for (Animal a : animals) {
            if (a.isAlive()) {
                a.metabolise(this);
                clampToBounds(a);
                keepPredatorsOutOfZone(a);
            }
        }
        spreadDisease();
        maybeStartOutbreak();
        removeDead();
        ground.decay(tickCount);
        growFood();
        updateSeason();

        publish(new SimEvent(EventType.TICK, tickCount, null, 0, 0, null, census()));
    }

    /** Uses my custom collector on a parallel stream (see Census). */
    public Census census() {
        return getAnimals().parallelStream()
                .filter(Entity::isAlive)
                .collect(Census.collector(tickCount, season));
    }

    /**
     * Food grows faster or slower depending on the season. Half of it grows
     * on fertile ground (where animals died) and that food is rich. The safe
     * zone has its own food as well, so the prey hiding there don't all starve.
     */
    private void growFood() {
        double rate = season.getFoodFactor();
        if (food.size() < maxFood && Math.random() < rate / foodEvery) {
            double[] spot = null;
            if (Math.random() < 0.5) {
                spot = ground.fertileSpot();
            }
            if (spot != null) {
                food.add(new Food(Math.min(spot[0], width), Math.min(spot[1], height), true));
            } else {
                food.add(new Food(Math.random() * width, Math.random() * height));
            }
        }
        if (Math.random() < rate / (foodEvery * 3) && foodInSafeZone().size() < maxZoneFood) {
            food.add(new Food(zoneX + Math.random() * zoneWidth, zoneY + Math.random() * zoneHeight));
        }
    }

    /** setPosition() is protected on Entity, but World is a same-package collaborator, so it can call it. */
    private void clampToBounds(Entity e) {
        double clampedX = Math.max(0, Math.min(e.getX(), width));
        double clampedY = Math.max(0, Math.min(e.getY(), height));
        e.setPosition(clampedX, clampedY);
    }

    /** If a Predator ended its move inside the safe zone, push it back out to the nearest edge. */
    private void keepPredatorsOutOfZone(Entity e) {
        if (!(e instanceof Predator)) return;
        if (!isInSafeZone(e)) return;

        double distLeft = e.getX() - zoneX;
        double distRight = (zoneX + zoneWidth) - e.getX();
        double distTop = e.getY() - zoneY;
        double distBottom = (zoneY + zoneHeight) - e.getY();
        double nearest = Math.min(Math.min(distLeft, distRight), Math.min(distTop, distBottom));

        if (nearest == distLeft) e.setPosition(zoneX - 1, e.getY());
        else if (nearest == distRight) e.setPosition(zoneX + zoneWidth + 1, e.getY());
        else if (nearest == distTop) e.setPosition(e.getX(), zoneY - 1);
        else e.setPosition(e.getX(), zoneY + zoneHeight + 1);
    }

    private void removeDead() {
        hawks.removeIf(h -> !h.isAlive());
        foxes.removeIf(f -> !f.isAlive());
        rabbits.removeIf(r -> !r.isAlive());
        mice.removeIf(m -> !m.isAlive());
        food.removeIf(f -> !f.isAlive());
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
