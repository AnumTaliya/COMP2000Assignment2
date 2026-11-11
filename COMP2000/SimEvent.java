/**
 * Something that happened in the world, sent to every SimListener.
 * TICK events carry a Census of the whole population. The other events
 * carry the species and position of the animal it happened to.
 */
public class SimEvent {
    private final EventType type;
    private final int tick;
    private final String species;
    private final double x, y;
    private final String detail;
    private final Census census;

    public SimEvent(EventType type, int tick, String species, double x, double y, String detail, Census census) {
        this.type = type;
        this.tick = tick;
        this.species = species;
        this.x = x;
        this.y = y;
        this.detail = detail;
        this.census = census;
    }

    public EventType getType() { return type; }
    public int getTick() { return tick; }
    public String getSpecies() { return species; }
    public double getX() { return x; }
    public double getY() { return y; }
    public String getDetail() { return detail; }
    public Census getCensus() { return census; }

    @Override
    public String toString() {
        String text = "t" + tick + " " + type;
        if (species != null) {
            text += " " + species;
        }
        if (detail != null) {
            text += " " + detail;
        }
        return text;
    }
}
