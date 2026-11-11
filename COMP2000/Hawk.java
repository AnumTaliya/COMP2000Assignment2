import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

/** Hawks are fast, long-sighted hunters. They start with the Nearest strategy. */
public class Hawk extends Predator {
    private static final Color WING = new Color(120, 78, 42);
    private static final Color WING_LIGHT = new Color(170, 125, 80);
    private static final Color WING_TIP = new Color(55, 35, 22);
    private static final Color BODY = new Color(105, 68, 38);
    private static final Color HEAD = new Color(85, 55, 32);
    private static final Color TAIL = new Color(190, 85, 40);
    private static final Color TAIL_BAND = new Color(60, 30, 15);
    private static final Color BEAK = new Color(230, 200, 80);
    private static final Color EYE = new Color(250, 210, 40);

    public Hawk(double x, double y) {
        this(x, y, new Traits(1.0, 120, 0), HuntStrategies.NEAREST);
    }

    public Hawk(double x, double y, Traits traits, HuntStrategy huntStrategy) {
        super(x, y, 100, 2.4, traits, huntStrategy);
    }

    @Override
    protected Animal makeBaby(double x, double y, Traits babyTraits) {
        return new Hawk(x, y, babyTraits, huntStrategy);
    }

    @Override
    protected void addTo(World world) {
        world.addHawk(this);
    }

    @Override
    public double getSize() { return 15; }

    /** Hawk from above. Wings flap while it hunts and fold in when it's resting. */
    @Override
    public void draw(Graphics g) {
        boolean perched = state == PredatorState.RESTING;
        double flap = perched ? 0.4 : 0.78 + 0.22 * Math.sin(age * 0.45 + phase);
        drawShadow(g, 16, 30 * flap + 4, perched ? 2 : 9);

        Graphics2D g2 = facing(g);
        g2.setStroke(new BasicStroke(0.6f));

        Path2D tail = new Path2D.Double();
        tail.moveTo(-6, -2);
        tail.lineTo(-14, -4.8);
        tail.quadTo(-16.5, 0, -14, 4.8);
        tail.lineTo(-6, 2);
        tail.closePath();
        g2.setColor(TAIL);
        g2.fill(tail);
        g2.setColor(TAIL_BAND);
        g2.draw(new Line2D.Double(-13.2, -4.2, -13.2, 4.2));
        g2.draw(tail);

        drawWing(g2, 1, flap);
        drawWing(g2, -1, flap);

        g2.setColor(BODY);
        g2.fill(new Ellipse2D.Double(-8, -3.2, 14, 6.4));
        g2.setColor(WING_LIGHT);
        for (int i = 0; i < 3; i++) {
            g2.draw(new Line2D.Double(-5 + i * 3, -1.2, -3.5 + i * 3, 0));
            g2.draw(new Line2D.Double(-5 + i * 3, 1.2, -3.5 + i * 3, 0));
        }

        g2.setColor(HEAD);
        g2.fill(new Ellipse2D.Double(3.5, -3, 6, 6));
        Path2D beak = new Path2D.Double();
        beak.moveTo(8.8, -1.4);
        beak.quadTo(12.6, -0.6, 11.2, 1.5);
        beak.lineTo(8.8, 1.4);
        beak.closePath();
        g2.setColor(BEAK);
        g2.fill(beak);
        g2.setColor(TAIL_BAND);
        g2.draw(beak);

        g2.setColor(EYE);
        g2.fill(new Ellipse2D.Double(6.4, -2.9, 1.8, 1.8));
        g2.fill(new Ellipse2D.Double(6.4, 1.1, 1.8, 1.8));
        g2.setColor(Color.BLACK);
        g2.fill(new Ellipse2D.Double(7.0, -2.5, 0.9, 0.9));
        g2.fill(new Ellipse2D.Double(7.0, 1.5, 0.9, 0.9));
        g2.dispose();
    }

    /** Draws one wing. side is 1 or -1 so the same code draws the left and right wing. */
    private void drawWing(Graphics2D g2, int side, double flap) {
        Graphics2D w = (Graphics2D) g2.create();
        w.scale(1, side * flap);
        Path2D wing = new Path2D.Double();
        wing.moveTo(4, 1.8);
        wing.curveTo(7, 6, 5.5, 13, 1, 18);
        wing.lineTo(-1.2, 16.6);
        wing.lineTo(-1.6, 19);
        wing.lineTo(-3.6, 16.4);
        wing.lineTo(-4.4, 18.4);
        wing.lineTo(-6, 15.4);
        wing.lineTo(-7, 16.8);
        wing.lineTo(-8.4, 13);
        wing.quadTo(-10, 7, -6, 2);
        wing.closePath();
        w.setColor(WING);
        w.fill(wing);

        Shape oldClip = w.getClip();
        w.clip(wing);
        w.setColor(WING_TIP);
        w.fill(new Ellipse2D.Double(-10, 13, 13, 8));
        w.setColor(WING_LIGHT);
        w.setStroke(new BasicStroke(1.6f));
        w.draw(new Line2D.Double(4.5, 4, 2, 13));
        w.setClip(oldClip);

        w.setStroke(new BasicStroke(0.5f));
        w.setColor(WING_TIP);
        w.draw(new Line2D.Double(0, 5, -7, 9));
        w.draw(new Line2D.Double(0, 9, -7.5, 12.5));
        w.draw(new Line2D.Double(1, 12.5, -5, 15));
        w.draw(wing);
        w.dispose();
    }

    @Override
    public Color getColor() {
        return Color.RED;
    }
}
