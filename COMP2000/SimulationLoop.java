import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Runs the simulation on its own thread so the window doesn't freeze. In A1
 * world.update() ran in the Swing Timer, on the same thread as the painting.
 *
 * When I split them, the panel could paint in the middle of a tick and get a
 * ConcurrentModificationException while removeDead() was changing the lists.
 * To fix it, update() and paintComponent() both lock on the World object.
 *
 * paused and ticksPerStep are volatile because the key presses change them on
 * the Swing thread and this thread reads them.
 */
public class SimulationLoop {
    private final World world;
    private final long periodMillis;
    private final ScheduledExecutorService executor;
    private volatile boolean paused = false;
    private volatile int ticksPerStep = 1;

    public SimulationLoop(World world, long periodMillis) {
        this.world = world;
        this.periodMillis = periodMillis;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "simulation");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        executor.scheduleAtFixedRate(this::step, 0, periodMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * The try/catch is important.
     * If this throws, the executor stops running, it forever without printing anything, and the simulation just freezes.
     */
    private void step() {
        if (paused) return;
        try {
            for (int i = 0; i < ticksPerStep; i++) {
                synchronized (world) {
                    world.update();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public boolean isPaused() { return paused; }
    public void togglePause() { paused = !paused; }

    public int getTicksPerStep() { return ticksPerStep; }
    public void faster() { ticksPerStep = Math.min(ticksPerStep * 2, 32); }
    public void slower() { ticksPerStep = Math.max(ticksPerStep / 2, 1); }
}
