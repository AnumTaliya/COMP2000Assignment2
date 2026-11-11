import java.awt.Color;
import java.awt.Graphics;

/**
 * Entity is the root of the whole hierarchy. Anything that exists in the
 * world (an animal or a piece of food) is a Entity.
 * It has a position and can be alive or dead. 
 * Putting position /alive here means World can treat
 * every kind of thing in this simulation the same way (see World.allEntities()).
 * Movement goes through setPosition(),
 * which is protected so Animal can call it, but nothing else can silently move an entity around.
 * A2: I removed update(World) from here. Only animals act now, and they do
 * it through plan() in Animal.
 */
public abstract class Entity {
    private double x, y;
    private boolean alive = true;

    public Entity(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isAlive() { return alive; }
    public void kill() { alive = false; }

    /** The only way to move an entity is for the subclasses to use this instead of touching fields. */
    protected void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    
    public String getSpecies() { return getClass().getSimpleName(); }

    public double distanceTo(Entity other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** Draws this entity onto the panel. */
    public abstract void draw(Graphics g);

    public abstract Color getColor();

    /** Roughly how big it is drawn, so the rings and health bars fit around it. */
    public double getSize() { return 6; }

    @Override
    public String toString() {
        return String.format("%s at (%.0f, %.0f)", getClass().getSimpleName(), x, y);
    }
}
