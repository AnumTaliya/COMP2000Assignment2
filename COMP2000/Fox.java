import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/** Foxes are slower and shorter-sighted than hawks but tougher on the ground. They start with the Best meal strategy. */
public class Fox extends Predator {
    private static final Color FUR = new Color(214, 108, 34);
    private static final Color FUR_DARK = new Color(170, 78, 22);
    private static final Color LEGS = new Color(45, 28, 18);
    private static final Color EARS = new Color(60, 32, 18);
    private static final Color WHITE = new Color(245, 240, 230);

    public Fox(double x, double y) {
        this(x, y, new Traits(1.0, 90, 0), HuntStrategies.BEST_MEAL);
    }

    public Fox(double x, double y, Traits traits, HuntStrategy huntStrategy) {
        super(x, y, 100, 2.0, traits, huntStrategy);
    }

    @Override
    protected Animal makeBaby(double x, double y, Traits babyTraits) {
        return new Fox(x, y, babyTraits, huntStrategy);
    }

    @Override
    protected void addTo(World world) {
        world.addFox(this);
    }

    @Override
    public double getSize() { return 14; }

    /** Fox from above: legs that move when it walks, a bushy tail with a white tip, dark ears. */
    @Override
    public void draw(Graphics g) {
        boolean resting = state == PredatorState.RESTING;
        double stride = resting ? 0 : Math.sin(age * 0.6 + phase) * 2;
        drawShadow(g, 34, 11, 2);

        Graphics2D g2 = facing(g);
        g2.setStroke(new BasicStroke(0.6f));

        g2.setColor(LEGS);
        g2.fill(new Ellipse2D.Double(3 + stride, -6.2, 4, 2.4));
        g2.fill(new Ellipse2D.Double(3 - stride, 3.8, 4, 2.4));
        g2.fill(new Ellipse2D.Double(-9 - stride, -6.2, 4, 2.4));
        g2.fill(new Ellipse2D.Double(-9 + stride, 3.8, 4, 2.4));

        Graphics2D t = (Graphics2D) g2.create();
        t.translate(-9, 0);
        t.rotate(Math.sin(age * 0.25 + phase) * (resting ? 0.1 : 0.3));
        Path2D tail = new Path2D.Double();
        tail.moveTo(0, -2.5);
        tail.curveTo(-5, -6.5, -12, -5.5, -15.5, -1.5);
        tail.quadTo(-16.5, 0.5, -14.5, 2);
        tail.curveTo(-10, 5, -4, 4.5, 0, 2.5);
        tail.closePath();
        t.setColor(FUR);
        t.fill(tail);
        Shape oldClip = t.getClip();
        t.clip(tail);
        t.setColor(WHITE);
        t.fill(new Ellipse2D.Double(-18, -3.5, 6, 7));
        t.setColor(FUR_DARK);
        t.fill(new Ellipse2D.Double(-10, -1, 9, 2));
        t.setClip(oldClip);
        t.setColor(FUR_DARK);
        t.draw(tail);
        t.dispose();

        g2.setColor(FUR);
        g2.fill(new Ellipse2D.Double(-11, -4.8, 18, 9.6));
        g2.setColor(FUR_DARK);
        g2.fill(new Ellipse2D.Double(-9, -1.6, 14, 3.2));
        g2.draw(new Ellipse2D.Double(-11, -4.8, 18, 9.6));

        Path2D ear1 = new Path2D.Double();
        ear1.moveTo(3.5, -3.2);
        ear1.lineTo(7.2, -3.8);
        ear1.lineTo(3.8, -8.2);
        ear1.closePath();
        Path2D ear2 = new Path2D.Double();
        ear2.moveTo(3.5, 3.2);
        ear2.lineTo(7.2, 3.8);
        ear2.lineTo(3.8, 8.2);
        ear2.closePath();
        g2.setColor(EARS);
        g2.fill(ear1);
        g2.fill(ear2);

        Path2D head = new Path2D.Double();
        head.moveTo(4, -5);
        head.quadTo(8, -5.5, 10, -2.6);
        head.lineTo(14.5, -0.7);
        head.quadTo(15.4, 0, 14.5, 0.7);
        head.lineTo(10, 2.6);
        head.quadTo(8, 5.5, 4, 5);
        head.quadTo(2, 0, 4, -5);
        head.closePath();
        g2.setColor(FUR);
        g2.fill(head);
        g2.setColor(WHITE);
        g2.fill(new Ellipse2D.Double(8.5, -3.2, 4.5, 1.6));
        g2.fill(new Ellipse2D.Double(8.5, 1.6, 4.5, 1.6));
        g2.setColor(FUR_DARK);
        g2.draw(head);

        g2.setColor(Color.BLACK);
        g2.fill(new Ellipse2D.Double(8.2, -2.6, 1.6, 1.2));
        g2.fill(new Ellipse2D.Double(8.2, 1.4, 1.6, 1.2));
        g2.fill(new Ellipse2D.Double(14, -1, 2, 2));
        g2.dispose();
    }

    @Override
    public Color getColor() {
        return Color.ORANGE;
    }
}
