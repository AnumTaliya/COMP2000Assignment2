import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Makes sick animals easy to spot, a green tint over the body, a thick
 * lime ring that pulses, a little virus icon, and bubbles coming off them.
 */
public class SicknessDecorator extends RendererDecorator {
    private static final Color TINT = new Color(140, 255, 40, 80);
    private static final Color RING = new Color(170, 255, 60);
    private static final Color VIRUS = new Color(120, 220, 30);

    public SicknessDecorator(Renderer inner) { super(inner); }

    @Override
    public void render(Graphics2D g, Entity e) {
        inner.render(g, e);
        if (!(e instanceof Animal) || !((Animal) e).isSick()) {
            return;
        }
        Animal a = (Animal) e;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int x = (int) a.getX();
        int y = (int) a.getY();
        int size = (int) a.getSize();

        // green tint over the body
        g2.setColor(TINT);
        g2.fillOval(x - size, y - size, size * 2, size * 2);

        // pulsing ring
        double pulse = 0.5 + 0.5 * Math.sin(a.getAge() * 0.3);
        int r = size + 4 + (int) (pulse * 3);
        g2.setColor(new Color(RING.getRed(), RING.getGreen(), RING.getBlue(), 150 + (int) (pulse * 100)));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(x - r, y - r, r * 2, r * 2);

        // virus icon (a ball with spikes) at the top right
        int vx = x + size + 2;
        int vy = y - size - 2;
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(VIRUS);
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            int sx = vx + (int) Math.round(Math.cos(angle) * 5);
            int sy = vy + (int) Math.round(Math.sin(angle) * 5);
            g2.drawLine(vx, vy, sx, sy);
            g2.fillOval(sx - 1, sy - 1, 3, 3);
        }
        g2.fillOval(vx - 3, vy - 3, 7, 7);
        g2.setColor(new Color(40, 90, 10));
        g2.fillOval(vx - 1, vy - 1, 2, 2);

        // bubbles floating up
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(200, 255, 120, 200));
        for (int i = 0; i < 3; i++) {
            double t = ((a.getAge() + i * 12) % 36) / 36.0;
            int bx = x - 5 + i * 5;
            int by = (int) (y - size * 0.5 - t * 14);
            int s = (int) (1 + t * 3);
            g2.drawOval(bx, by, s, s);
        }
        g2.dispose();
    }
}
