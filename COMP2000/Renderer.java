import java.awt.Graphics2D;

/**
 * Decorator pattern for drawing. BASIC just calls e.draw(g), and the extras
 * (vision circle, state ring, health bar, sickness glow) wrap around it.
 * They can be turned on and off with keys.
 */
@FunctionalInterface
public interface Renderer {
    void render(Graphics2D g, Entity e);

    Renderer BASIC = (g, e) -> e.draw(g);
}
