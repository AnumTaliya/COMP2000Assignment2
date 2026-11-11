import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/**
 * Food is-a Entity but has no behaviour of its own - it just sits until
 * eaten. It implements Edible so any Prey can ask it how much health it's
 * worth, instead of each species guessing its own value.
 */
public class Food extends Entity implements Edible {
    private static final double NUTRITION_VALUE = 25;
    private static final double RICH_NUTRITION = 40;
    private static final Color LEAF = new Color(80, 175, 65);
    private static final Color LEAF_LIGHT = new Color(140, 215, 110);
    private static final Color STEM = new Color(45, 110, 40);
    private static final Color FLOWER = new Color(250, 235, 120);
    private static final Color RICH_FLOWER = new Color(240, 120, 190);

    private final double angle;
    private final int leaves;
    private final boolean rich; // grew on ground where something died

    public Food(double x, double y) {
        this(x, y, false);
    }

    public Food(double x, double y, boolean rich) {
        super(x, y);
        this.rich = rich;
        this.angle = Math.random() * Math.PI * 2;
        if (rich) {
            this.leaves = 6;
        } else {
            this.leaves = 3 + (int) (Math.random() * 3);
        }
    }

    public boolean isRich() { return rich; }

    @Override
    public double getNutritionValue() {
        if (rich) {
            return RICH_NUTRITION;
        }
        return NUTRITION_VALUE;
    }

    /** A little plant with 3 to 5 leaves. Rich ones are bigger with a pink flower. */
    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(getX(), getY());
        g2.rotate(angle);
        if (rich) {
            g2.scale(1.3, 1.3);
        }
        g2.setStroke(new BasicStroke(0.4f));
        for (int i = 0; i < leaves; i++) {
            Graphics2D leaf = (Graphics2D) g2.create();
            leaf.rotate(i * Math.PI * 2 / leaves);
            leaf.setColor(LEAF);
            leaf.fill(new Ellipse2D.Double(0.5, -1.8, 6.5, 3.6));
            leaf.setColor(LEAF_LIGHT);
            leaf.draw(new Line2D.Double(1, 0, 6, 0));
            leaf.dispose();
        }
        if (rich) {
            g2.setColor(RICH_FLOWER);
            for (int i = 0; i < 5; i++) {
                double a = i * Math.PI * 2 / 5;
                g2.fill(new Ellipse2D.Double(Math.cos(a) * 1.8 - 1.4, Math.sin(a) * 1.8 - 1.4, 2.8, 2.8));
            }
            g2.setColor(FLOWER);
            g2.fill(new Ellipse2D.Double(-1.2, -1.2, 2.4, 2.4));
        } else {
            g2.setColor(leaves == 5 ? FLOWER : STEM);
            g2.fill(new Ellipse2D.Double(-1.5, -1.5, 3, 3));
        }
        g2.dispose();
    }

    @Override
    public Color getColor() {
        return Color.GREEN;
    }
}
