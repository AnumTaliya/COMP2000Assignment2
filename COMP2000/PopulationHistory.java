import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Observer saves a Census every few ticks so SimPanel can draw the
 * population chart. It only keeps every Nth one, because one boom and
 * crash takes thousands of ticks.
 */
public class PopulationHistory implements SimListener {
    private final int capacity;
    private final int sampleEvery;
    private final Deque<Census> history = new ArrayDeque<>();
    private Census latest = new Census(0, Season.SPRING);

    public PopulationHistory(int capacity, int sampleEvery) {
        this.capacity = capacity;
        this.sampleEvery = sampleEvery;
    }

    @Override
    public void onEvent(SimEvent event) {
        if (event.getType() != EventType.TICK) {
            return;
        }
        latest = event.getCensus();
        if (event.getTick() % sampleEvery == 0) {
            history.addLast(latest);
            if (history.size() > capacity) {
                history.removeFirst();
            }
        }
    }

    public List<Census> snapshot() { return new ArrayList<>(history); }

    public Census latest() { return latest; }

    public int getSampleEvery() { return sampleEvery; }

    public int peak(String species) {
        return history.stream().mapToInt(c -> c.count(species)).max().orElse(0);
    }

    /** The biggest count of any species, so the chart knows how tall to be. */
    public int maxCount(List<String> species) {
        return species.stream().mapToInt(this::peak).max().orElse(1);
    }
}
