/**
 * Parent class for the extras. It is a Renderer so it can be used like one,
 * and it has a Renderer which is the thing it wraps. The chain always ends
 * with Renderer.BASIC.
 */
public abstract class RendererDecorator implements Renderer {
    protected final Renderer inner;

    protected RendererDecorator(Renderer inner) {
        this.inner = inner;
    }
}
