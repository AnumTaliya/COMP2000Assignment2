import java.awt.Color;
import java.awt.Graphics2D;

/** A faint circle showing how far an animal can see. */
public class VisionDecorator extends RendererDecorator {
    public VisionDecorator(Renderer inner) { super(inner); }

    @Override
    public void render(Graphics2D g, Entity e) {
        if (e instanceof Animal) {
            Animal a = (Animal) e;
            int r = (int) a.getVision();
            Color c = a.getColor();
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 25));
            g.drawOval((int) a.getX() - r, (int) a.getY() - r, r * 2, r * 2);
        }
        inner.render(g, e);
    }
}
