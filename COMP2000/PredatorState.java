import java.awt.Color;

/**
 * HUNTING -> RESTING when full (health over 85)
 * RESTING -> HUNTING when hungry again (health under 60)
 * In either state a predator will breed if it's ready.
 *
 * A resting predator hardly moves and uses less energy, which gives the prey
 * a break after it has eaten. This is a big reason the populations go up and
 * down now instead of everything dying like in A1.
 */
public enum PredatorState implements AnimalState<Predator> {

    HUNTING(Color.RED, 1.0) {
        @Override
        public Action plan(Predator p, World w) {
            if (p.readyToBreed(w)) {
                return x -> p.reproduce(x, Predator.BIRTH_COST, Predator.BREED_COOLDOWN);
            }
            if (p.getHealth() > 85) {
                return p.switchTo(RESTING, w);
            }
            Action hunt = p.huntAction(w);
            if (hunt != null) {
                return hunt;
            }
            return x -> p.wander(1.0);
        }
    },

    RESTING(new Color(150, 150, 210), 0.4) {
        @Override
        public Action plan(Predator p, World w) {
            if (p.readyToBreed(w)) {
                return x -> p.reproduce(x, Predator.BIRTH_COST, Predator.BREED_COOLDOWN);
            }
            if (p.getHealth() < 60) {
                return p.switchTo(HUNTING, w);
            }
            return x -> p.wander(0.3);
        }
    };

    private final Color colour;
    private final double energyCost;

    PredatorState(Color colour, double energyCost) {
        this.colour = colour;
        this.energyCost = energyCost;
    }

    @Override
    public abstract Action plan(Predator p, World w);

    @Override
    public Color getColour() { return colour; }

    @Override
    public double energyCost() { return energyCost; }
}
