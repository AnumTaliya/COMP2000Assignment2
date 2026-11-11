import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Animal is-a Entity that moves and spends energy.
 * It factors out the behaviour every animal shares 
 * so that Predator and Prey only need to add what makes them different.
 * Assignment 2 changes: update()/act() from A1 became plan()/decide().
 * plan() only looks at the world and returns an Action, so World can call it on a parallel stream. 
 * findNearest() is now a stream.
 * Speed and vision come from Traits.
 */
public abstract class Animal extends Entity {
    protected static final double MAX_HEALTH = 100;

    public static final double CONTACT_DISTANCE = 12;
    public static final double SPREAD_CHANCE = 0.035;
    protected static final int SICK_DURATION = 350;
    protected static final int IMMUNE_DURATION = 900;
    protected static final double SICK_SPEED = 0.6;

    protected double health;
    protected double baseSpeed;
    protected Traits traits;
    protected int breedCooldown;
    protected int age;
    protected int sickTicks;
    protected int immuneTicks;
    protected double heading;
    protected final double phase; // so animals don't all come at the same time

    public Animal(double x, double y, double health, double baseSpeed, Traits traits) {
        super(x, y);
        this.health = health;
        this.baseSpeed = baseSpeed;
        this.traits = traits;
        this.heading = Math.random() * Math.PI * 2;
        this.phase = Math.random() * Math.PI * 2;
    }

    public double getHealth() { return health; }
    public Traits getTraits() { return traits; }
    public double getVision() { return traits.getVision(); }
    public int getAge() { return age; }
    public double getHeading() { return heading; }

    public double getSpeed() {
        double speed = baseSpeed * traits.getSpeedFactor();
        if (isSick()) {
            speed = speed * SICK_SPEED;
        }
        return speed;
    }

    public boolean isSick() { return sickTicks > 0; }
    public boolean canCatchDisease() { return sickTicks == 0 && immuneTicks == 0; }

    public void infect() {
        if (canCatchDisease()) {
            sickTicks = SICK_DURATION;
        }
    }

    /** Used when the disease is turned off. */
    public void cure() {
        sickTicks = 0;
        immuneTicks = 0;
    }

    protected void moveToward(Entity target) {
        moveToward(target.getX(), target.getY());
    }

    /** Overload: move toward a raw coordinate instead of a whole Entity. */
    protected void moveToward(double targetX, double targetY) {
        double dx = targetX - getX();
        double dy = targetY - getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0.001) {
            double step = Math.min(getSpeed(), dist);
            stepTo(getX() + (dx / dist) * step, getY() + (dy / dist) * step);
        }
    }

    protected void moveAwayFrom(Entity threat) {
        moveAwayFrom(threat.getX(), threat.getY());
    }

    /** Overload: flee a raw coordinate instead of a whole Entity. */
    protected void moveAwayFrom(double threatX, double threatY) {
        double dx = getX() - threatX;
        double dy = getY() - threatY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0.001) {
            stepTo(getX() + (dx / dist) * getSpeed(), getY() + (dy / dist) * getSpeed());
        }
    }

    /**
     * Keeps going roughly the same way and turns a bit each tick, instead of
 jumping around randomly like in Assignment 1. scale < 1 is a slow walk.
     */
    protected void wander(double scale) {
        double dir = heading + ThreadLocalRandom.current().nextGaussian() * 0.5;
        double step = getSpeed() * scale * 0.7;
        stepTo(getX() + Math.cos(dir) * step, getY() + Math.sin(dir) * step);
    }

    /** Moves and slowly turns the animal to face the way it moved. */
    protected void stepTo(double newX, double newY) {
        double dx = newX - getX();
        double dy = newY - getY();
        if (dx * dx + dy * dy > 0.0001) {
            double target = Math.atan2(dy, dx);
            double turn = Math.atan2(Math.sin(target - heading), Math.cos(target - heading));
            heading = heading + turn * 0.35;
        }
        setPosition(newX, newY);
    }

    /** Generic search: it finds the closest living candidate within vision range. */
    public <T extends Entity> T findNearest(List<T> candidates) {
        return candidates.stream()
                .filter(c -> c != this && c.isAlive())
                .filter(c -> distanceTo(c) <= getVision())
                .min(Comparator.comparingDouble(this::distanceTo))
                .orElse(null);
    }

    /** Every living object this animal can see. */
    public <T extends Entity> List<T> visible(List<T> candidates) {
        return candidates.stream()
                .filter(c -> c != this && c.isAlive())
                .filter(c -> distanceTo(c) <= getVision())
                .collect(Collectors.toList());
    }

    /**
     * Called from a parallel stream, so it must only look at the world, not
     * change it. The isAlive() check is because the animal might get eaten
     * after it made its plan but before the plan runs.
     */
    public final Action plan(World world) {
        Action chosen = decide(world);
        return w -> {
            if (isAlive()) {
                chosen.apply(w);
            }
        };
    }

    /** Each animal passes this on to its current state. */
    protected abstract Action decide(World world);

    /** End of each tick: get older, use energy, get better or die. */
    public void metabolise(World world) {
        age++;
        if (breedCooldown > 0) {
            breedCooldown--;
        }

        double speedFactor = traits.getSpeedFactor();
        double cost = baseMetabolism() * speedFactor * speedFactor; // being fast costs a lot more energy 
        cost = cost * getState().energyCost();
        cost = cost * world.getSeason().getMetabolismFactor();
        if (isSick()) {
            cost = cost * world.getDiseaseLevel().getSickMetabolism();
        }
        health -= cost;

        if (sickTicks > 0) {
            sickTicks--;
            if (sickTicks == 0 && health > 0) {
                immuneTicks = IMMUNE_DURATION;
                world.publish(EventType.RECOVERED, this);
            }
        } else if (immuneTicks > 0) {
            immuneTicks--;
        }

        if (health <= 0) {
            kill();
            if (isSick()) {
                world.publish(EventType.DIED_SICK, this);
            } else {
                world.publish(EventType.STARVED, this);
            }
        }
    }

    protected double baseMetabolism() { return 0.1; }

    /** Prey can breed all year. Predator changes this so it can't breed in winter. */
    protected boolean inBreedingSeason(World world) {
        return true;
    }

    protected void eat(Edible meal) {
        health = Math.min(health + meal.getNutritionValue(), MAX_HEALTH);
    }

    /**
     * Makes a baby. The health the parent gives up becomes the baby's health.
     * Checks the cooldown again because things might have changed since the
     * plan was made.
     */
    protected boolean reproduce(World world, double cost, int cooldown) {
        if (breedCooldown > 0 || health <= cost || !inBreedingSeason(world)) {
            return false;
        }
        try {
            world.spawnNear(this, cost);
            health -= cost;
            breedCooldown = cooldown;
            return true;
        } catch (SpawnException e) {
            breedCooldown = 20; // try again soon
            world.publish(EventType.SPAWN_FAILED, this);
            return false;
        }
    }

    public abstract AnimalState<?> getState();

    /** Factory method: World.spawnNear() calls this so it doesn't need to know the species. */
    protected abstract Animal makeBaby(double x, double y, Traits traits);

    /** Adds this animal to the right list in World. */
    protected abstract void addTo(World world);

    /** Copy of g moved to this animal and turned the way it's facing. Draw facing right (+x). */
    protected Graphics2D facing(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(getX(), getY());
        g2.rotate(heading);
        return g2;
    }

    /** Shadow under the animal. A bigger offset makes it look higher off the ground. */
    protected void drawShadow(Graphics g, double length, double width, double offset) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(getX() + offset, getY() + offset);
        g2.rotate(heading);
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fill(new Ellipse2D.Double(-length / 2, -width / 2, length, width));
        g2.dispose();
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", health %.0f, %s", health, getState());
    }
}
