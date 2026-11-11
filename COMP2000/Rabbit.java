import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/** Rabbits flee from predators, eat food, and breed on their own once they're well fed. */
public class Rabbit extends Prey {
    private static final double BIRTH_COST = 25;
    private static final int BREED_COOLDOWN = 160;

    private static final Color DARK = new Color(120, 85, 55);
    private static final Color LIGHT = new Color(240, 237, 230);
    private Color fur, furShade, furBack; // worked out from the fur gene each time it's drawn

    private static final Color PINK = new Color(235, 160, 170);
    private static final Color EYE = new Color(40, 25, 20);

    public Rabbit(double x, double y) {
        this(x, y, new Traits(1.0, 100, 0.05 + Math.random() * 0.5));
    }

    public Rabbit(double x, double y, Traits traits) {
        super(x, y, 60, 35, traits); // health, nutritionValue (worth to a predator)
    }

    @Override
    protected Action breedingAction(World world) {
        if (breedCooldown > 0 || health < 70 || age < 100 || !inBreedingSeason(world)) return null;
        return w -> reproduce(w, BIRTH_COST, BREED_COOLDOWN);
    }

    @Override
    protected Animal makeBaby(double x, double y, Traits babyTraits) {
        return new Rabbit(x, y, babyTraits);
    }

    @Override
    protected void addTo(World world) {
        world.addRabbit(this);
    }

    @Override
    public double getSize() { return 10; }

    /** Rabbit from above: long ears, fluffy tail, back feet, and it bounces when it hops. */
    @Override
    public void draw(Graphics g) {
        fur = furColour(DARK, LIGHT);
        furShade = fur.darker();
        furBack = furColour(DARK.brighter(), Color.WHITE);
        boolean hiding = state == PreyState.HIDING;
        double hop = hiding ? 0 : Math.max(0, Math.sin(age * 0.5 + phase));
        double stretch = 1 + 0.18 * hop;
        drawShadow(g, 20, 11, 1 + hop * 2.5);

        Graphics2D g2 = facing(g);
        g2.scale(stretch, 1);
        g2.setStroke(new BasicStroke(0.5f));

        g2.setColor(furShade);
        g2.fill(new Ellipse2D.Double(-7.5, -6, 6.5, 2.8));
        g2.fill(new Ellipse2D.Double(-7.5, 3.2, 6.5, 2.8));

        g2.setColor(fur);
        g2.fill(new Ellipse2D.Double(-8, -5.5, 13, 11));
        g2.setColor(furBack);
        g2.fill(new Ellipse2D.Double(-6, -3, 9, 6));
        g2.setColor(furShade);
        g2.draw(new Ellipse2D.Double(-8, -5.5, 13, 11));

        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(-10.5, -2.2, 4.4, 4.4));
        g2.setColor(furShade);
        g2.draw(new Ellipse2D.Double(-10.5, -2.2, 4.4, 4.4));

        g2.setColor(fur);
        g2.fill(new Ellipse2D.Double(2.5, -4, 8, 8));
        g2.setColor(furShade);
        g2.draw(new Ellipse2D.Double(2.5, -4, 8, 8));

        drawEar(g2, 4, -2.2, Math.PI + 0.28);
        drawEar(g2, 4, 2.2, Math.PI - 0.28);

        g2.setColor(EYE);
        g2.fill(new Ellipse2D.Double(6.3, -3.9, 2, 1.5));
        g2.fill(new Ellipse2D.Double(6.3, 2.4, 2, 1.5));
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(7.2, -3.7, 0.6, 0.6));
        g2.fill(new Ellipse2D.Double(7.2, 2.6, 0.6, 0.6));

        g2.setColor(PINK);
        g2.fill(new Ellipse2D.Double(9.8, -0.8, 1.6, 1.6));
        g2.setColor(new Color(150, 150, 150));
        g2.setStroke(new BasicStroke(0.3f));
        g2.draw(new Line2D.Double(10.5, -0.3, 13.5, -2.6));
        g2.draw(new Line2D.Double(10.5, 0.3, 13.5, 2.6));
        g2.draw(new Line2D.Double(10.5, 0, 13.8, 0));
        g2.dispose();
    }

    private void drawEar(Graphics2D g2, double x, double y, double angle) {
        Graphics2D e = (Graphics2D) g2.create();
        e.translate(x, y);
        e.rotate(angle);
        e.setColor(fur);
        e.fill(new Ellipse2D.Double(0, -1.7, 10, 3.4));
        e.setColor(PINK);
        e.fill(new Ellipse2D.Double(1.8, -0.8, 6.8, 1.6));
        e.setColor(furShade);
        e.draw(new Ellipse2D.Double(0, -1.7, 10, 3.4));
        e.dispose();
    }

    @Override
    public Color getColor() {
        return Color.WHITE;
    }
}
