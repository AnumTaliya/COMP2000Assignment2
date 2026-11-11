import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/** All the hunting strategies. Each one is a lambda. */
public class HuntStrategies {

    /** Chase whatever is closest. */
    public static final HuntStrategy NEAREST = (hunter, prey) -> prey.stream()
            .min(Comparator.comparingDouble(hunter::distanceTo))
            .orElse(null);

    /** Go for the weakest one, it will be the slowest. */
    public static final HuntStrategy WEAKEST = (hunter, prey) -> prey.stream()
            .min(Comparator.comparingDouble(Animal::getHealth))
            .orElse(null);

    /** Most food for the distance, so a close rabbit beats a close mouse. */
    public static final HuntStrategy BEST_MEAL = (hunter, prey) -> prey.stream()
            .max(Comparator.comparingDouble((Prey p) -> p.getNutritionValue() / (1 + hunter.distanceTo(p))))
            .orElse(null);

    /** Name -> strategy, in the order of the keys (1, 2, 3). */
    public static final Map<String, HuntStrategy> ALL = new LinkedHashMap<>();

    static {
        ALL.put("Nearest", NEAREST);
        ALL.put("Weakest", WEAKEST);
        ALL.put("Best meal", BEST_MEAL);
    }
}
