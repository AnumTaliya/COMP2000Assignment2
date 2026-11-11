import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Prey is-a Animal that eats Food and flees Predators, and also implements
 * Edible - since Predator eats Prey. What it does each tick depends on its
 * PreyState (foraging, fleeing or hiding).
 *
 * Speed rule from A1: a well-fed prey moves at FED_SPEED, a hungry one at
 * HUNGRY_SPEED. Now this is also multiplied by its speed trait.
 */
public abstract class Prey extends Animal implements Edible {
    protected static final double HUNGRY_SPEED = 1.0;
    protected static final double FED_SPEED = 2.0;
    protected static final int FED_DURATION = 80;
    protected static final double DESPERATE = 30;
    protected static final double CALM_DISTANCE = 35;

    protected double eatDistance = 6;
    protected double nutritionValue; // how much health a Predator gains from eating this
    protected int fedTicks = 0;
    protected PreyState state = PreyState.FORAGING;
    protected int hiddenTicks = 0;
    protected double coat = 0.15; // the colour it is right now: 0 = dark brown, 1 = white

    public Prey(double x, double y, double health, double nutritionValue, Traits traits) {
        super(x, y, health, HUNGRY_SPEED, traits);
        this.nutritionValue = nutritionValue;
    }

    @Override
    public double getNutritionValue() { return nutritionValue; }

    @Override
    public PreyState getState() { return state; }

    public int getHiddenTicks() { return hiddenTicks; }

    public boolean isDesperate() { return health < DESPERATE; }

    public double getCoat() { return coat; }

    /** Babies are born with the same coat colour as their parent. */
    public void setCoat(double coat) { this.coat = coat; }

    /**
     * Camouflage. 1.0 means a predator can see it from its full vision range.
     * If the coat matches the field it can only be seen from about 20% of that.
     */
    public double detectability(World world) {
        double contrast = Math.abs(coat - world.getSeason().getFieldTone());
        return Math.min(1.0, 0.2 + contrast * 1.6);
    }

    /** Coat colour between dark brown and white, for drawing. */
    protected Color furColour(Color dark, Color light) {
        double t = coat;
        int r = (int) (dark.getRed() + (light.getRed() - dark.getRed()) * t);
        int g = (int) (dark.getGreen() + (light.getGreen() - dark.getGreen()) * t);
        int b = (int) (dark.getBlue() + (light.getBlue() - dark.getBlue()) * t);
        return new Color(r, g, b);
    }

    @Override
    protected Action decide(World world) {
        return state.plan(this, world);
    }

    public void setState(PreyState next) {
        if (next != state) {
            state = next;
            hiddenTicks = 0;
        }
    }

    /** Change state and act in the same tick, so changing state doesn't waste a turn. */
    public Action switchTo(PreyState next, World world) {
        Action action = next.plan(this, world);
        return w -> {
            setState(next);
            action.apply(w);
        };
    }

    /**
     * A predator that is resting (already full) is only scary up close.
     * Before this, prey ran from every predator and starved even with food everywhere.
     */
    public Predator nearestThreat(World world) {
        List<Predator> threats = new ArrayList<>();
        for (Predator p : world.getPredators()) {
            if (p.getState() == PredatorState.HUNTING || distanceTo(p) < CALM_DISTANCE) {
                threats.add(p);
            }
        }
        return findNearest(threats);
    }

    /** Plan to go to the closest food in the list, or null if it can't see any. */
    public Action foodAction(List<Food> options) {
        Food food = findNearest(options);
        if (food == null) {
            return null;
        }
        return w -> {
            moveToward(food);
            tryEatFood(food, w);
        };
    }

    protected boolean tryEatFood(Food food, World world) {
        if (food.isAlive() && distanceTo(food) <= eatDistance) {
            food.kill();
            eat(food);
            fedTicks = FED_DURATION;
            return true;
        }
        return false;
    }

    /** Wander, but go back toward the middle if it walks out of the safe zone. */
    protected void wanderWithinZone(World world) {
        wander(0.5);
        if (!world.isInSafeZone(this)) {
            moveToward(world.getZoneCentreX(), world.getZoneCentreY());
        }
    }

    @Override
    protected double baseMetabolism() { return 0.06; }

    /**
     * Each tick the coat moves a little toward the colour of the field.
     * moultSpeed 1.0 changes most of the way in about 170 ticks, 0.1 takes
     * longer than a whole season. Moulting fast uses a bit more energy.
     */
    @Override
    public void metabolise(World world) {
        double target = world.getSeason().getFieldTone();
        double moult = traits.getMoultSpeed();
        coat = coat + (target - coat) * moult * 0.006;
        health -= baseMetabolism() * moult * 0.25;
        super.metabolise(world);
        if (fedTicks > 0) {
            fedTicks--;
            baseSpeed = FED_SPEED;
        } else {
            baseSpeed = HUNGRY_SPEED;
        }
        if (state == PreyState.HIDING) {
            hiddenTicks++;
        }
    }

    /** Returns a breeding Action if it's ready to breed now, otherwise null. */
    protected abstract Action breedingAction(World world);
}
