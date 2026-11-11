import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.Comparator;

/**
 * Mice still breed when two of them touch (A1 rule), but now a well-fed mouse
 * walks toward a mate it can see. In A1 mice hardly ever met, which was one
 * reason they died out.
 */
public class Mouse extends Prey {
    private static final double CONTACT_DISTANCE = 10;
    private static final double BIRTH_COST = 12;
    private static final int BREED_COOLDOWN = 90;

    private static final Color DARK = new Color(85, 70, 55);
    private static final Color LIGHT = new Color(225, 220, 215);
    private Color fur, furDark; // worked out from the fur gene each time it's drawn
    private static final Color TAIL = new Color(225, 165, 165);
    private static final Color NOSE = new Color(240, 130, 150);

    public Mouse(double x, double y) {
        this(x, y, new Traits(1.0, 90, 0.05 + Math.random() * 0.5));
    }

    public Mouse(double x, double y, Traits traits) {
        super(x, y, 40, 20, traits); // health, nutritionValue (worth to a predator)
    }

    /** Mice are small, so they get by on less food than rabbits. */
    @Override
    protected double baseMetabolism() { return 0.04; }

    private boolean readyToBreed() {
        return breedCooldown == 0 && health > 35 && age > 60;
    }

    @Override
    protected Action breedingAction(World world) {
        if (!readyToBreed() || !inBreedingSeason(world)) return null;
        Mouse mate = world.getMice().stream()
                .filter(m -> m != this && m.isAlive() && m.readyToBreed())
                .filter(m -> distanceTo(m) <= getVision())
                .min(Comparator.comparingDouble(this::distanceTo))
                .orElse(null);
        if (mate == null) return null;

        if (distanceTo(mate) > CONTACT_DISTANCE) {
            return w -> moveToward(mate);
        }
        // both mice might plan this with each other in the same tick, so check again here
        return w -> {
            if (mate.isAlive() && mate.breedCooldown == 0 && reproduce(w, BIRTH_COST, BREED_COOLDOWN)) {
                mate.breedCooldown = BREED_COOLDOWN;
            }
        };
    }

    @Override
    protected Animal makeBaby(double x, double y, Traits babyTraits) {
        return new Mouse(x, y, babyTraits);
    }

    @Override
    protected void addTo(World world) {
        world.addMouse(this);
    }

    @Override
    public double getSize() { return 8; }

    /** Mouse from above: round ears, whiskers and a pink tail that wiggles. */
    @Override
    public void draw(Graphics g) {
        fur = furColour(DARK, LIGHT);
        furDark = fur.darker();
        double wiggle = Math.sin(age * 0.8 + phase) * 2.5;
        drawShadow(g, 12, 7, 1);

        Graphics2D g2 = facing(g);
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D tail = new Path2D.Double();
        tail.moveTo(-5, 0);
        tail.curveTo(-9, wiggle, -13, -wiggle, -17, wiggle * 0.6);
        g2.setColor(TAIL);
        g2.draw(tail);

        g2.setStroke(new BasicStroke(0.4f));
        g2.setColor(TAIL);
        g2.fill(new Ellipse2D.Double(-5, -4, 2.5, 1.5));
        g2.fill(new Ellipse2D.Double(-5, 2.5, 2.5, 1.5));

        g2.setColor(fur);
        g2.fill(new Ellipse2D.Double(-6, -3.5, 10, 7));
        g2.setColor(furDark);
        g2.fill(new Ellipse2D.Double(-4.5, -1, 7, 2));

        Path2D head = new Path2D.Double();
        head.moveTo(2, -3);
        head.quadTo(6, -2.6, 8.2, 0);
        head.quadTo(6, 2.6, 2, 3);
        head.closePath();
        g2.setColor(fur);
        g2.fill(head);

        g2.fill(new Ellipse2D.Double(0.8, -5.6, 3.8, 3.8));
        g2.fill(new Ellipse2D.Double(0.8, 1.8, 3.8, 3.8));
        g2.setColor(TAIL);
        g2.fill(new Ellipse2D.Double(1.5, -4.9, 2.4, 2.4));
        g2.fill(new Ellipse2D.Double(1.5, 2.5, 2.4, 2.4));

        g2.setColor(Color.BLACK);
        g2.fill(new Ellipse2D.Double(4.6, -2.1, 1.3, 1.3));
        g2.fill(new Ellipse2D.Double(4.6, 0.8, 1.3, 1.3));
        g2.setColor(NOSE);
        g2.fill(new Ellipse2D.Double(7.6, -0.7, 1.4, 1.4));

        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(new BasicStroke(0.25f));
        g2.draw(new Line2D.Double(7.5, -0.3, 10.5, -2.4));
        g2.draw(new Line2D.Double(7.5, 0.3, 10.5, 2.4));
        g2.draw(new Line2D.Double(7.5, 0, 11, 0));
        g2.dispose();
    }

    @Override
    public Color getColor() {
        return Color.GRAY;
    }
}
