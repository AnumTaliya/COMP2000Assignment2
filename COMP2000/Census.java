import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collector;

/**
 * A count of the population at one tick: for each species, how many there
 * are, how many are sick, and their average health, speed, vision, coat
 * colour and moult speed.
 *
 * It's made with my own Collector (see collector()). This gets all the numbers
 * in one go, where groupingBy + summarizingDouble would need a pass for each
 * number. The merge() step is what makes it work on a parallel stream: each
 * thread fills its own Census and then they get added together.
 */
public class Census {

    /** The running totals for one species. */
    private static class Tally {
        int count;
        int sick;
        double health;
        double speed;
        double vision;
        double fur;
        double moult;
    }

    private final int tick;
    private final Season season;
    private final Map<String, Tally> tallies = new TreeMap<>();

    public Census(int tick, Season season) {
        this.tick = tick;
        this.season = season;
    }

    public void add(Animal a) {
        Tally t = tallies.get(a.getSpecies());
        if (t == null) {
            t = new Tally();
            tallies.put(a.getSpecies(), t);
        }
        t.count++;
        if (a.isSick()) {
            t.sick++;
        }
        t.health += a.getHealth();
        t.speed += a.getTraits().getSpeedFactor();
        t.vision += a.getTraits().getVision();
        if (a instanceof Prey) {
            t.fur += ((Prey) a).getCoat();
        }
        t.moult += a.getTraits().getMoultSpeed();
    }

    public Census merge(Census other) {
        for (String species : other.tallies.keySet()) {
            Tally theirs = other.tallies.get(species);
            Tally mine = tallies.get(species);
            if (mine == null) {
                mine = new Tally();
                tallies.put(species, mine);
            }
            mine.count += theirs.count;
            mine.sick += theirs.sick;
            mine.health += theirs.health;
            mine.speed += theirs.speed;
            mine.vision += theirs.vision;
            mine.fur += theirs.fur;
            mine.moult += theirs.moult;
        }
        return this;
    }

    public int getTick() { return tick; }
    public Season getSeason() { return season; }

    public int count(String species) {
        Tally t = tallies.get(species);
        return t == null ? 0 : t.count;
    }

    public int sick(String species) {
        Tally t = tallies.get(species);
        return t == null ? 0 : t.sick;
    }

    public double averageHealth(String species) {
        Tally t = tallies.get(species);
        return (t == null || t.count == 0) ? 0 : t.health / t.count;
    }

    public double averageSpeed(String species) {
        Tally t = tallies.get(species);
        return (t == null || t.count == 0) ? 0 : t.speed / t.count;
    }

    public double averageVision(String species) {
        Tally t = tallies.get(species);
        return (t == null || t.count == 0) ? 0 : t.vision / t.count;
    }

    /** Average moult speed gene (0 to 1). */
    public double averageMoult(String species) {
        Tally t = tallies.get(species);
        return (t == null || t.count == 0) ? 0 : t.moult / t.count;
    }

    /** Average coat colour right now (0 = dark, 1 = white). */
    public double averageFur(String species) {
        Tally t = tallies.get(species);
        return (t == null || t.count == 0) ? 0 : t.fur / t.count;
    }

    public int total() {
        return tallies.values().stream().mapToInt(t -> t.count).sum();
    }

    public int totalSick() {
        return tallies.values().stream().mapToInt(t -> t.sick).sum();
    }

    /** My custom collector: supplier, accumulator, combiner. */
    public static Collector<Animal, Census, Census> collector(int tick, Season season) {
        return Collector.of(
                () -> new Census(tick, season),
                Census::add,
                Census::merge,
                Collector.Characteristics.UNORDERED,
                Collector.Characteristics.IDENTITY_FINISH);
    }
}
