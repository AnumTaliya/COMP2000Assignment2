import java.awt.Color;

/**
 * Ring colours: white = foraging, yellow = fleeing, blue = hiding.
 *
 * FORAGING -> FLEEING when a predator is in sight (unless it's desperate)
 * FLEEING -> HIDING when it reaches the safe zone
 * FLEEING -> FORAGING when the predator is out of sight
 * HIDING -> FORAGING after a while with no predator around, or when starving
 *
 * Because of the "desperate" rule, a starving rabbit will leave the safe zone
 * even with a hawk waiting outside.
 */
public enum PreyState implements AnimalState<Prey> {

    FORAGING(new Color(225, 225, 225), 1.0) {
        @Override
        public Action plan(Prey p, World w) {
            if (!p.isDesperate() && p.nearestThreat(w) != null) {
                return p.switchTo(FLEEING, w);
            }
            Action breed = p.breedingAction(w);
            if (breed != null) {
                return breed;
            }
            Action eat = p.foodAction(w.getFood());
            if (eat != null) {
                return eat;
            }
            return x -> p.wander(1.0);
        }
    },

    FLEEING(Color.YELLOW, 1.2) {
        @Override
        public Action plan(Prey p, World w) {
            Predator threat = p.nearestThreat(w);
            if (threat == null) {
                return p.switchTo(FORAGING, w);
            }
            if (w.isInSafeZone(p)) {
                return p.switchTo(HIDING, w);
            }
            if (w.distanceToZone(p) < p.getVision() * 1.5) {
                return x -> p.moveToward(w.getZoneCentreX(), w.getZoneCentreY());
            }
            return x -> p.moveAwayFrom(threat);
        }
    },

    HIDING(new Color(80, 160, 255), 0.6) {
        @Override
        public Action plan(Prey p, World w) {
            if (p.isDesperate()) {
                Action eat = p.foodAction(w.getFood());
                return x -> {
                    p.setState(FORAGING);
                    if (eat != null) {
                        eat.apply(x);
                    } else {
                        p.wander(1.0);
                    }
                };
            }
            if (p.getHiddenTicks() > 40 && p.nearestThreat(w) == null) {
                return p.switchTo(FORAGING, w);
            }
            Action eat = p.foodAction(w.foodInSafeZone());
            if (eat != null) {
                return eat;
            }
            return x -> p.wanderWithinZone(x);
        }
    };

    private final Color colour;
    private final double energyCost;

    PreyState(Color colour, double energyCost) {
        this.colour = colour;
        this.energyCost = energyCost;
    }

    @Override
    public abstract Action plan(Prey p, World w);

    @Override
    public Color getColour() { return colour; }

    @Override
    public double energyCost() { return energyCost; }
}
