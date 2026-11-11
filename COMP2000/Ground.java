import java.util.ArrayList;
import java.util.List;

/**
 * The ground remembers where animals died. It's another Observer: it listens
 * for EATEN, STARVED and DIED_SICK events.
 *
 * Every death makes the ground there more fertile, and World grows some of its
 * new food on fertile ground. That food is richer and has a flower. Kills are
 * also kept in their own grid so SimPanel can draw a heatmap (K key).
 * Both grids slowly fade so old deaths stop mattering.
 *
 * Nothing tells predators where to hunt, but the heatmap shows where they end
 * up doing it (e.g. around the edge of the safe meadow).
 */
public class Ground implements SimListener {
    public static final int CELL = 20;
    private static final int REMAINS_TIME = 400;

    /** Where an animal died, so it can be drawn for a while. */
    public static class Remains {
        public final double x, y;
        public final int tick;
        public final boolean eaten;

        Remains(double x, double y, int tick, boolean eaten) {
            this.x = x;
            this.y = y;
            this.tick = tick;
            this.eaten = eaten;
        }
    }

    private final int cols, rows;
    private final double[][] fertility;
    private final double[][] kills;
    private final List<Remains> remains = new ArrayList<>();
    private double totalFertility = 0;

    public Ground(int width, int height) {
        cols = width / CELL + 1;
        rows = height / CELL + 1;
        fertility = new double[cols][rows];
        kills = new double[cols][rows];
    }

    @Override
    public void onEvent(SimEvent e) {
        if (e.getType() == EventType.EATEN) {
            enrich(e.getX(), e.getY(), 1.0);
            kills[col(e.getX())][row(e.getY())] += 1.0;
            remains.add(new Remains(e.getX(), e.getY(), e.getTick(), true));
        } else if (e.getType() == EventType.STARVED || e.getType() == EventType.DIED_SICK) {
            enrich(e.getX(), e.getY(), 0.6);
            remains.add(new Remains(e.getX(), e.getY(), e.getTick(), false));
        }
    }

    /** Adds fertility to the cell and a bit less to the cells around it. */
    private void enrich(double x, double y, double amount) {
        int c = col(x);
        int r = row(y);
        for (int dc = -1; dc <= 1; dc++) {
            for (int dr = -1; dr <= 1; dr++) {
                int nc = c + dc;
                int nr = r + dr;
                if (nc >= 0 && nc < cols && nr >= 0 && nr < rows) {
                    double share = (dc == 0 && dr == 0) ? amount : amount * 0.3;
                    fertility[nc][nr] += share;
                    totalFertility += share;
                }
            }
        }
    }

    /** Called once a tick by World: everything fades a little. */
    public void decay(int tick) {
        totalFertility = 0;
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                fertility[c][r] *= 0.9993;
                kills[c][r] *= 0.9995;
                totalFertility += fertility[c][r];
            }
        }
        remains.removeIf(rm -> tick - rm.tick > REMAINS_TIME);
    }

    /**
     * Picks a random spot, more likely on fertile ground (like a raffle where
     * richer cells have more tickets). Returns null if the ground isn't fertile yet.
     */
    public double[] fertileSpot() {
        if (totalFertility < 1) {
            return null;
        }
        double ticket = Math.random() * totalFertility;
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                ticket -= fertility[c][r];
                if (ticket <= 0) {
                    return new double[]{(c + Math.random()) * CELL, (r + Math.random()) * CELL};
                }
            }
        }
        return null;
    }

    public double fertilityAt(double x, double y) { return fertility[col(x)][row(y)]; }

    public double getKills(int c, int r) { return kills[c][r]; }
    public int getCols() { return cols; }
    public int getRows() { return rows; }
    public List<Remains> getRemains() { return remains; }

    private int col(double x) { return Math.max(0, Math.min(cols - 1, (int) (x / CELL))); }
    private int row(double y) { return Math.max(0, Math.min(rows - 1, (int) (y / CELL))); }
}
