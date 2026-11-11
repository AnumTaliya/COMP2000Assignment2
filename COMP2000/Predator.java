import java.util.ArrayList;
import java.util.List;

/**
 * Predator is-a Animal that can eat Prey. Hawk and Fox both extend this.
 *
 * Two patterns are used here: the State (HUNTING or RESTING) decides WHETHER
 * to hunt, and the HuntStrategy decides WHO to hunt. Hawk and Fox don't have
 * their own act() any more - in A1 that was the same code copied twice.
 */
public abstract class Predator extends Animal {
    public static final double BIRTH_COST = 45;
    public static final int BREED_COOLDOWN = 300;

    protected double eatDistance = 8;
    protected PredatorState state = PredatorState.HUNTING;
    protected HuntStrategy huntStrategy;

    public Predator(double x, double y, double health, double speed, Traits traits, HuntStrategy huntStrategy) {
        super(x, y, health, speed, traits);
        this.huntStrategy = huntStrategy;
    }

    @Override
    public PredatorState getState() { return state; }

    public void setState(PredatorState next) { state = next; }

    public HuntStrategy getHuntStrategy() { return huntStrategy; }

    public void setHuntStrategy(HuntStrategy huntStrategy) { this.huntStrategy = huntStrategy; }

    @Override
    protected Action decide(World world) {
        return state.plan(this, world);
    }

    public Action switchTo(PredatorState next, World world) {
        Action action = next.plan(this, world);
        return w -> {
            setState(next);
            action.apply(w);
        };
    }

    /** Predators can't raise babies in winter. */
    @Override
    protected boolean inBreedingSeason(World world) {
        return world.getSeason().allowsBreeding();
    }

    public boolean readyToBreed(World world) {
        return health >= 95 && breedCooldown == 0 && age > 200 && inBreedingSeason(world);
    }

    /**
     * Prey it can actually spot that aren't hiding in the safe zone.
     * Camouflaged prey have to be a lot closer before they get spotted.
     */
    public List<Prey> reachablePrey(World world) {
        List<Prey> result = new ArrayList<>();
        for (Prey p : visible(world.getPrey())) {
            boolean spotted = distanceTo(p) <= getVision() * p.detectability(world);
            if (spotted && !world.isInSafeZone(p)) {
                result.add(p);
            }
        }
        return result;
    }

    public Action huntAction(World world) {
        Prey target = huntStrategy.choose(this, reachablePrey(world));
        if (target == null) {
            return null;
        }
        return w -> {
            moveToward(target);
            tryEat(target, w);
        };
    }

    /**
     * Two hawks can plan to eat the same mouse in one tick, so this checks the
     * prey is still alive - only the first one gets it. Also checks the safe
     * zone, which fixes the A1 bug where prey just inside the edge got eaten.
     */
    protected boolean tryEat(Prey prey, World world) {
        if (prey.isAlive() && distanceTo(prey) <= eatDistance && !world.isInSafeZone(prey)) {
            prey.kill();
            eat(prey);
            world.publish(EventType.EATEN, prey);
            return true;
        }
        return false;
    }
}
