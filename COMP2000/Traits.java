import java.util.concurrent.ThreadLocalRandom;

/**
 * Traits are passed from parent to baby, with a small random change each
 * time, so a species can slowly evolve. Being fast costs more energy (see
 * Animal.metabolise()), so speed only pays off when there are predators.
 *
 * moultSpeed (0 to 1) is how fast a prey animal changes its coat to match
 * the season, like a snowshoe hare going white in winter. Fast moulting costs
 * a bit of energy, so it only pays off if predators are catching the ones
 * that stand out.
 *
 * Traits never change after they are made (a baby gets a new object), so
 * it is safe for lots of threads to read them at the same time.
 */
public class Traits {
    private static final double MUTATION = 0.06;

    private final double speedFactor;
    private final double vision;
    private final double moultSpeed;

    public Traits(double speedFactor, double vision, double moultSpeed) {
        this.speedFactor = speedFactor;
        this.vision = vision;
        this.moultSpeed = moultSpeed;
    }

    public double getSpeedFactor() { return speedFactor; }
    public double getVision() { return vision; }
    public double getMoultSpeed() { return moultSpeed; }

    /** A copy with a small random change, for a baby. */
    public Traits mutate() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double newSpeed = speedFactor * (1 + r.nextGaussian() * MUTATION);
        double newVision = vision * (1 + r.nextGaussian() * MUTATION);
        newSpeed = Math.max(0.5, Math.min(2.0, newSpeed));
        newVision = Math.max(30, Math.min(220, newVision));
        double newMoult = moultSpeed + r.nextGaussian() * 0.05;
        newMoult = Math.max(0, Math.min(1, newMoult));
        return new Traits(newSpeed, newVision, newMoult);
    }
}
