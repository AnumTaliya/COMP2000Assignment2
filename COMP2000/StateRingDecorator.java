import java.awt.Color;
import java.awt.Graphics2D;

/** A coloured ring that shows which state the animal is in (yellow = fleeing etc). */
public class StateRingDecorator extends RendererDecorator {
    public StateRingDecorator(Renderer inner) { super(inner); }

    @Override
    public void render(Graphics2D g, Entity e) {
        inner.render(g, e);
        if (e instanceof Animal) {
            Animal a = (Animal) e;
            g.setColor(a.getState().getColour());
            int r = (int) a.getSize() + 3;
            g.drawOval((int) a.getX() - r, (int) a.getY() - r, r * 2, r * 2);
        }
    }
}
