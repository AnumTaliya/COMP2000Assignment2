import java.util.List;

/**
 * Strategy pattern: how a predator picks which prey to chase.
 *
 * In A1 this was hard coded inside Hawk.act() and Fox.act(), which were
 * basically the same method copied twice. Now a Predator has a HuntStrategy.
 * It's a functional interface, so each strategy is a lambda (see
 * HuntStrategies) instead of a whole subclass.
 *  Keys 1/2/3 swap it.
 */
@FunctionalInterface
public interface HuntStrategy {
    /** Returns the prey to chase, or null if there's nothing worth chasing. */
    Prey choose(Predator hunter, List<Prey> visiblePrey);
}
