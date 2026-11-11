import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Observer remembers the recent births, deaths and so on. breakdown() uses
 * groupingBy twice with counting() to give something like
 * {EATEN={Mouse=12, Rabbit=4}, STARVED={Fox=1}}, which the sidebar shows.
 */
public class EventLog implements SimListener {
    private final int capacity;
    private final List<SimEvent> events = new ArrayList<>();

    public EventLog(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public void onEvent(SimEvent event) {
        if (event.getType() == EventType.TICK) {
            return;
        }
        events.add(event);
        if (events.size() > capacity) {
            events.remove(0);
        }
    }

    /** The last n events, without the failed spawns because there are too many of them. */
    public List<String> latest(int n) {
        List<String> lines = events.stream()
                .filter(e -> e.getType() != EventType.SPAWN_FAILED)
                .map(SimEvent::toString)
                .collect(Collectors.toList());
        return lines.subList(Math.max(0, lines.size() - n), lines.size());
    }

    /** The same as latest() but gives back the events themselves, for the sidebar feed. */
    public List<SimEvent> latestEvents(int n) {
        List<SimEvent> list = events.stream()
                .filter(e -> e.getType() != EventType.SPAWN_FAILED)
                .collect(Collectors.toList());
        return new ArrayList<>(list.subList(Math.max(0, list.size() - n), list.size()));
    }

    public Map<EventType, Map<String, Long>> breakdown() {
        return events.stream()
                .filter(e -> e.getSpecies() != null)
                .collect(Collectors.groupingBy(SimEvent::getType,
                        () -> new EnumMap<>(EventType.class),
                        Collectors.groupingBy(SimEvent::getSpecies, TreeMap::new, Collectors.counting())));
    }

    public int size() { return events.size(); }
}
