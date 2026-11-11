import java.awt.Color;
import java.awt.Graphics2D;

/** A small health bar above each animal. */
public class HealthBarDecorator extends RendererDecorator {
    public HealthBarDecorator(Renderer inner) { super(inner); }

    @Override
    public void render(Graphics2D g, Entity e) {
        inner.render(g, e);
        if (e instanceof Animal) {
            Animal a = (Animal) e;
            int full = (int) a.getSize() + 4;
            int w = (int) (full * a.getHealth() / Animal.MAX_HEALTH);
            int x = (int) a.getX() - full / 2;
            int y = (int) (a.getY() - a.getSize() - 7);
            g.setColor(Color.DARK_GRAY);
            g.fillRect(x, y, full, 3);
            g.setColor(a.getHealth() < 30 ? Color.RED : Color.GREEN);
            g.fillRect(x, y, w, 3);
        }
    }
}
