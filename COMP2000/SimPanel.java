import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The visible surface. It draws the world, a sidebar with the live numbers
 * and a population chart at the bottom. It repaints on its own Swing Timer,
 * and the simulation runs on a different thread (see SimulationLoop).
 */
public class SimPanel extends JPanel {
    private static final int SIDEBAR = 360;
    private static final int CHART = 160;
    private static final List<String> SPECIES = Arrays.asList("Hawk", "Fox", "Rabbit", "Mouse");
    private static final Color[] CHART_COLOURS = {new Color(255, 90, 90), new Color(255, 165, 40),
                                                  Color.WHITE, new Color(150, 160, 255)};

    private final World world;
    private final SimulationLoop loop;
    private final PopulationHistory history;
    private final EventLog log;

    private boolean showVision = false;
    private boolean showStates = true;
    private boolean showHealth = false;
    private boolean showKills = false;
    private boolean showFurChart = false;
    private Renderer renderer;
    private BufferedImage background;

    public SimPanel(World world, SimulationLoop loop, PopulationHistory history, EventLog log) {
        this.world = world;
        this.loop = loop;
        this.history = history;
        this.log = log;
        setBackground(new Color(30, 30, 30));
        setPreferredSize(new Dimension(world.getWidth() + SIDEBAR, world.getHeight() + CHART));
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKey(e);
            }
        });
        rebuildRenderer();
        new Timer(33, e -> repaint()).start();
    }

    /** Builds the decorator chain from whatever is switched on. */
    private void rebuildRenderer() {
        Renderer r = Renderer.BASIC;
        if (showVision) r = new VisionDecorator(r);
        if (showStates) r = new StateRingDecorator(r);
        if (showHealth) r = new HealthBarDecorator(r);
        r = new SicknessDecorator(r);
        renderer = r;
    }

    private void handleKey(KeyEvent e) {
        char c = Character.toUpperCase(e.getKeyChar());
        if (c >= '1' && c <= '9') {
            List<String> names = new ArrayList<>(HuntStrategies.ALL.keySet());
            int i = c - '1';
            if (i < names.size()) {
                synchronized (world) {
                    world.setHuntStrategy(names.get(i), HuntStrategies.ALL.get(names.get(i)));
                }
            }
            return;
        }
        switch (c) {
            case ' ': loop.togglePause(); break;
            case 'V': showVision = !showVision; rebuildRenderer(); break;
            case 'K': showKills = !showKills; break;
            case 'G': showFurChart = !showFurChart; break;
            case 'S': showStates = !showStates; rebuildRenderer(); break;
            case 'H': showHealth = !showHealth; rebuildRenderer(); break;
            case 'P': world.setParallel(!world.isParallel()); break;
            case 'W': synchronized (world) { world.cycleSeasonMode(); } break;
            case 'D': synchronized (world) { world.cycleDiseaseLevel(); } break;
            case 'O': synchronized (world) { world.triggerOutbreak(); } break;
            case '+': case '=': loop.faster(); break;
            case '-': case '_': loop.slower(); break;
            default: break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        synchronized (world) {
            drawWorld(g2);
            drawSidebar(g2);
            drawChart(g2);
        }
    }

    private void drawWorld(Graphics2D g) {
        if (background == null) background = buildBackground();
        g.drawImage(background, 0, 0, null);
        g.setColor(new Color(235, 240, 220));
        g.drawString("Safe meadow", (int) world.getZoneX() + 8, (int) world.getZoneY() + 16);

        g.setColor(world.getSeason().getTint());
        g.fillRect(0, 0, world.getWidth(), world.getHeight());

        g.setClip(0, 0, world.getWidth(), world.getHeight());
        drawGround(g);
        for (Entity e : world.allEntities()) {
            if (e.isAlive()) renderer.render(g, e);
        }
        drawWeather(g);
        g.setClip(null);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(0, 0, world.getWidth(), world.getHeight());
    }

    /**
     * Draws the grass, dirt, pebbles and the hedged meadow with burrows once
     * into an image, so each frame just copies it. The fixed seed makes the
     * field look the same every time.
     */
    private BufferedImage buildBackground() {
        int w = world.getWidth(), h = world.getHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Random r = new Random(2000);

        g.setColor(new Color(46, 70, 38));
        g.fillRect(0, 0, w, h);
        for (int i = 0; i < 40; i++) {
            int s = 40 + r.nextInt(90);
            g.setColor(new Color(40 + r.nextInt(15), 62 + r.nextInt(18), 32 + r.nextInt(10), 120));
            g.fill(new Ellipse2D.Double(r.nextInt(w) - s / 2.0, r.nextInt(h) - s / 2.0, s * 1.6, s));
        }
        for (int i = 0; i < 18; i++) {
            int s = 15 + r.nextInt(35);
            g.setColor(new Color(85 + r.nextInt(20), 70 + r.nextInt(15), 45, 90));
            g.fill(new Ellipse2D.Double(r.nextInt(w), r.nextInt(h), s * 1.4, s));
        }

        int zx = (int) world.getZoneX(), zy = (int) world.getZoneY();
        int zw = (int) world.getZoneWidth(), zh = (int) world.getZoneHeight();
        g.setColor(new Color(70, 105, 55));
        g.fillRoundRect(zx, zy, zw, zh, 18, 18);

        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 2600; i++) {
            double x = r.nextDouble() * w, y = r.nextDouble() * h;
            boolean inZone = x >= zx && x <= zx + zw && y >= zy && y <= zy + zh;
            int shade = r.nextInt(40);
            g.setColor(inZone ? new Color(95 + shade, 145 + shade, 70) : new Color(55 + shade, 90 + shade, 45));
            double len = 3 + r.nextDouble() * 4, lean = (r.nextDouble() - 0.5) * 4;
            g.draw(new QuadCurve2D.Double(x, y, x + lean / 2, y - len / 2, x + lean, y - len));
        }
        for (int i = 0; i < 70; i++) {
            int s = 2 + r.nextInt(3);
            g.setColor(new Color(120 + r.nextInt(40), 115 + r.nextInt(30), 105));
            g.fill(new Ellipse2D.Double(r.nextInt(w), r.nextInt(h), s, s * 0.8));
        }

        for (int i = 0; i < 5; i++) {
            double bx = zx + 25 + r.nextDouble() * (zw - 50), by = zy + 30 + r.nextDouble() * (zh - 50);
            g.setColor(new Color(110, 85, 55));
            g.fill(new Ellipse2D.Double(bx - 9, by - 5, 18, 11));
            g.setColor(new Color(25, 18, 12));
            g.fill(new Ellipse2D.Double(bx - 5, by - 3, 10, 6));
        }

        for (int side = 0; side < 4; side++) {
            int len = (side % 2 == 0) ? zw : zh;
            for (int i = 0; i <= len; i += 7) {
                double x = side == 0 ? zx + i : side == 1 ? zx + zw : side == 2 ? zx + i : zx;
                double y = side == 0 ? zy : side == 1 ? zy + i : side == 2 ? zy + zh : zy + i;
                double s = 7 + r.nextDouble() * 4;
                g.setColor(new Color(28 + r.nextInt(15), 70 + r.nextInt(25), 30));
                g.fill(new Ellipse2D.Double(x - s / 2, y - s / 2, s, s));
                if (r.nextInt(6) == 0) {
                    g.setColor(new Color(200, 60, 80));
                    g.fill(new Ellipse2D.Double(x - 1, y - 1, 2.4, 2.4));
                }
            }
        }
        g.dispose();
        return img;
    }

    /**
     * Fertile ground shows as darker, lusher patches, and remains are drawn
     * where animals died (feathers/fur for eaten ones, bones for the rest).
     * They fade out over time. With K on, kills are shown as a red heatmap.
     */
    private void drawGround(Graphics2D g) {
        Ground ground = world.getGround();
        int cell = Ground.CELL;
        for (int c = 0; c < ground.getCols(); c++) {
            for (int r = 0; r < ground.getRows(); r++) {
                double f = ground.fertilityAt(c * cell + 1, r * cell + 1);
                if (f > 0.2) {
                    int alpha = (int) Math.min(70, f * 18);
                    g.setColor(new Color(25, 85, 15, alpha));
                    g.fillOval(c * cell - 4, r * cell - 4, cell + 8, cell + 8);
                }
            }
        }

        int now = world.getTickCount();
        for (Ground.Remains rm : ground.getRemains()) {
            double fade = 1 - (now - rm.tick) / 400.0;
            if (fade <= 0) {
                continue;
            }
            int alpha = (int) (200 * fade);
            int x = (int) rm.x;
            int y = (int) rm.y;
            if (rm.eaten) {
                g.setColor(new Color(90, 20, 20, (int) (90 * fade)));
                g.fillOval(x - 4, y - 3, 8, 6);
                g.setColor(new Color(235, 230, 220, alpha));
                g.drawLine(x - 5, y - 2, x - 1, y - 4);
                g.drawLine(x + 2, y + 3, x + 6, y + 1);
                g.drawLine(x - 2, y + 4, x + 1, y + 1);
            } else {
                g.setColor(new Color(225, 220, 205, alpha));
                g.setStroke(new BasicStroke(1.5f));
                g.drawLine(x - 4, y, x + 4, y);
                g.fillOval(x - 6, y - 2, 3, 3);
                g.fillOval(x + 3, y - 2, 3, 3);
                g.setStroke(new BasicStroke(1f));
            }
        }

        if (showKills) {
            for (int c = 0; c < ground.getCols(); c++) {
                for (int r = 0; r < ground.getRows(); r++) {
                    double k = ground.getKills(c, r);
                    if (k > 0.05) {
                        int alpha = (int) Math.min(170, k * 70);
                        g.setColor(new Color(255, 40, 30, alpha));
                        g.fillRect(c * cell, r * cell, cell, cell);
                    }
                }
            }
            g.setFont(BOLD);
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRoundRect(8, world.getHeight() - 30, 300, 22, 10, 10);
            g.setColor(Color.WHITE);
            g.drawString("Kill map: red = where predators made kills", 16, world.getHeight() - 14);
        }
    }

    /** One snowflake, leaf or petal. */
    private static class Particle {
        double x, y, drift, spin;
        boolean started = false;
    }

    private final Random weatherRandom = new Random();
    private final Particle[] particles = new Particle[140];

    /** Snow in winter, leaves in autumn, petals in spring and nothing in summer. Only for looks. */
    private void drawWeather(Graphics2D g) {
        Season s = world.getSeason();
        int count = 0;
        if (s == Season.WINTER) count = 140;
        if (s == Season.AUTUMN) count = 45;
        if (s == Season.SPRING) count = 30;

        for (int i = 0; i < count; i++) {
            if (particles[i] == null) {
                particles[i] = new Particle();
            }
            Particle p = particles[i];
            boolean offScreen = p.y > world.getHeight() || p.x < -10 || p.x > world.getWidth() + 10;
            if (!p.started || offScreen) {
                p.x = weatherRandom.nextDouble() * world.getWidth();
                if (p.started) {
                    p.y = 1; // start again at the top
                } else {
                    p.y = weatherRandom.nextDouble() * world.getHeight();
                }
                p.drift = weatherRandom.nextDouble() - 0.5;
                p.spin = weatherRandom.nextDouble() * Math.PI * 2;
                p.started = true;
            }
            p.spin += 0.05;

            if (s == Season.WINTER) {
                p.x += p.drift * 0.6 + Math.sin(p.spin) * 0.3;
                p.y += 1.2 + Math.abs(p.drift);
                g.setColor(new Color(255, 255, 255, 210));
                int size = 2 + (int) (Math.abs(p.drift) * 3);
                g.fillOval((int) p.x, (int) p.y, size, size);
            } else if (s == Season.AUTUMN) {
                p.x += Math.sin(p.spin) * 1.2 + p.drift;
                p.y += 0.9;
                Graphics2D leaf = (Graphics2D) g.create();
                leaf.translate(p.x, p.y);
                leaf.rotate(p.spin);
                if (i % 3 == 0) {
                    leaf.setColor(new Color(200, 80, 30));
                } else if (i % 3 == 1) {
                    leaf.setColor(new Color(220, 150, 40));
                } else {
                    leaf.setColor(new Color(160, 60, 30));
                }
                leaf.fill(new Ellipse2D.Double(-3.5, -1.5, 7, 3));
                leaf.dispose();
            } else {
                p.x += Math.sin(p.spin) * 0.8 + p.drift * 0.5;
                p.y += 0.6;
                g.setColor(new Color(255, 190, 215, 220));
                g.fill(new Ellipse2D.Double(p.x, p.y, 3, 2));
            }
        }
    }

    // ----- sidebar -----

    private static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    private static final Font TEXT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private static final Font BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    private static final Font BIG = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    private static final Font SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    private static final Color CARD = new Color(44, 46, 50);
    private static final Color ROW = new Color(52, 55, 60);
    private static final Color SOFT = new Color(170, 175, 185);
    private static final Color SICK = new Color(170, 255, 60);

    // little animals used as icons in the population table
    private final Animal[] icons = {new Hawk(0, 0), new Fox(0, 0), new Rabbit(0, 0), new Mouse(0, 0)};

    private void drawSidebar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int x = world.getWidth() + 10;
        int w = SIDEBAR - 20;
        int y = 10;
        y = drawStatusCard(g, x, y, w) + 8;
        y = drawPopulationCard(g, x, y, w) + 8;
        y = drawTotalsCard(g, x, y, w) + 8;
        int controlsHeight = 220;
        int bottom = world.getHeight() + CHART - 10;
        drawEventsCard(g, x, y, w, bottom - controlsHeight - 8 - y);
        drawControlsCard(g, x, bottom - controlsHeight, w, controlsHeight);
    }

    private void card(Graphics2D g, int x, int y, int w, int h, String title) {
        g.setColor(CARD);
        g.fillRoundRect(x, y, w, h, 12, 12);
        if (title != null) {
            g.setFont(TITLE);
            g.setColor(Color.WHITE);
            g.drawString(title, x + 10, y + 18);
        }
    }

    /** A rounded coloured label, like a tag. Returns how wide it was. */
    private int chip(Graphics2D g, int x, int y, String text, Color background, Color textColour) {
        g.setFont(BOLD);
        int w = g.getFontMetrics().stringWidth(text) + 16;
        g.setColor(background);
        g.fillRoundRect(x, y, w, 20, 10, 10);
        g.setColor(textColour);
        g.drawString(text, x + 8, y + 14);
        return w;
    }

    private int drawStatusCard(Graphics2D g, int x, int y, int w) {
        int h = 104;
        card(g, x, y, w, h, null);
        g.setFont(BIG);
        g.setColor(Color.WHITE);
        g.drawString("Tick " + world.getTickCount(), x + 10, y + 26);

        int cx = x + 10;
        if (loop.isPaused()) {
            cx += chip(g, cx, y + 36, "PAUSED", new Color(200, 60, 60), Color.WHITE) + 6;
        } else {
            cx += chip(g, cx, y + 36, "Running x" + loop.getTicksPerStep(), new Color(60, 120, 70), Color.WHITE) + 6;
        }
        String planning = world.isParallel() ? "Parallel" : "Sequential";
        chip(g, cx, y + 36, planning, new Color(70, 80, 110), Color.WHITE);

        Season season = world.getSeason();
        String seasonText = season.getLabel();
        if (world.getLockedSeason() == null) {
            seasonText += " (next in " + world.ticksUntilNextSeason() + ")";
        } else {
            seasonText += " (locked)";
        }
        cx = x + 10;
        cx += chip(g, cx, y + 62, seasonText, seasonChipColour(season), Color.BLACK) + 6;
        chip(g, cx, y + 62, "Disease: " + world.getDiseaseLevel().getLabel(), diseaseChipColour(), Color.BLACK);

        g.setFont(TEXT);
        g.setColor(SOFT);
        String extra = "Hunting: " + world.getHuntStrategyName();
        if (!season.allowsBreeding()) {
            extra += "   |   No predator babies in winter";
        }
        g.drawString(extra, x + 10, y + 98);
        return y + h;
    }

    private int drawPopulationCard(Graphics2D g, int x, int y, int w) {
        Census now = history.latest();
        int h = 152;
        card(g, x, y, w, h, "Population");

        int[] cols = {x + 102, x + 138, x + 170, x + 212, x + 268, x + 322};
        String[] headers = {"Now", "Peak", "Sick", "Speed", "Coat", "Moult"};
        g.setFont(SMALL);
        g.setColor(SOFT);
        for (int i = 0; i < headers.length; i++) {
            rightText(g, headers[i], cols[i], y + 36);
        }

        int rowY = y + 42;
        for (int i = 0; i < SPECIES.size(); i++) {
            String s = SPECIES.get(i);
            if (i % 2 == 0) {
                g.setColor(ROW);
                g.fillRect(x + 4, rowY, w - 8, 22);
            }
            Graphics2D icon = (Graphics2D) g.create();
            icon.translate(x + 22, rowY + 11);
            icon.scale(0.55, 0.55);
            icons[i].draw(icon);
            icon.dispose();

            g.setFont(BOLD);
            g.setColor(Color.WHITE);
            g.drawString(s, x + 44, rowY + 15);
            g.setFont(TEXT);
            rightText(g, "" + now.count(s), cols[0], rowY + 15);
            rightText(g, "" + history.peak(s), cols[1], rowY + 15);
            g.setColor(now.sick(s) > 0 ? SICK : SOFT);
            rightText(g, "" + now.sick(s), cols[2], rowY + 15);
            g.setColor(Color.WHITE);
            rightText(g, String.format("%.2f", now.averageSpeed(s)), cols[3], rowY + 15);
            if (i >= 2) {
                double fur = now.averageFur(s);
                g.setColor(furSwatch(fur));
                g.fillRoundRect(cols[4] - 42, rowY + 5, 12, 12, 4, 4);
                g.setColor(Color.WHITE);
                rightText(g, String.format("%.2f", fur), cols[4], rowY + 15);
                rightText(g, String.format("%.2f", now.averageMoult(s)), cols[5], rowY + 15);
            } else {
                g.setColor(SOFT);
                rightText(g, "-", cols[4], rowY + 15);
                rightText(g, "-", cols[5], rowY + 15);
            }
            rowY += 22;
        }
        g.setFont(TEXT);
        g.setColor(SOFT);
        g.drawString("Food on the ground: " + world.getFood().size()
                + "      Sick right now: " + now.totalSick(), x + 10, rowY + 16);
        return y + h;
    }

    private int drawTotalsCard(Graphics2D g, int x, int y, int w) {
        int h = 136;
        card(g, x, y, w, h, "Totals (last " + log.size() + " events)");
        Map<EventType, Map<String, Long>> totals = log.breakdown();
        EventType[] types = {EventType.BORN, EventType.EATEN, EventType.STARVED, EventType.DIED_SICK, EventType.INFECTED};
        String[] headers = {"Born", "Eaten", "Starved", "Died sick", "Got sick"};
        int[] cols = {x + 116, x + 160, x + 212, x + 270, x + 326};

        g.setFont(SMALL);
        g.setColor(SOFT);
        for (int i = 0; i < headers.length; i++) {
            rightText(g, headers[i], cols[i], y + 36);
        }
        int rowY = y + 42;
        for (int i = 0; i < SPECIES.size(); i++) {
            String s = SPECIES.get(i);
            if (i % 2 == 0) {
                g.setColor(ROW);
                g.fillRect(x + 4, rowY, w - 8, 20);
            }
            g.setFont(BOLD);
            g.setColor(Color.WHITE);
            g.drawString(s, x + 10, rowY + 14);
            g.setFont(TEXT);
            for (int c = 0; c < types.length; c++) {
                long n = 0;
                if (totals.containsKey(types[c]) && totals.get(types[c]).containsKey(s)) {
                    n = totals.get(types[c]).get(s);
                }
                if (n > 0 && (types[c] == EventType.DIED_SICK || types[c] == EventType.INFECTED)) {
                    g.setColor(SICK);
                } else {
                    g.setColor(n > 0 ? Color.WHITE : SOFT);
                }
                rightText(g, "" + n, cols[c], rowY + 14);
            }
            rowY += 20;
        }
        return y + h;
    }

    private void drawEventsCard(Graphics2D g, int x, int y, int w, int h) {
        card(g, x, y, w, h, "What just happened");
        int lines = Math.max(1, (h - 30) / 18);
        int rowY = y + 38;
        g.setFont(TEXT);
        for (SimEvent e : log.latestEvents(lines)) {
            g.setColor(eventColour(e.getType()));
            g.fillOval(x + 12, rowY - 9, 8, 8);
            g.setColor(SOFT);
            g.drawString("t" + e.getTick(), x + 26, rowY);
            g.setColor(Color.WHITE);
            g.drawString(describe(e), x + 76, rowY);
            rowY += 18;
        }
    }

    private void drawControlsCard(Graphics2D g, int x, int y, int w, int h) {
        card(g, x, y, w, h, "Controls");
        String[][] keys = {
            {"SPACE", "Pause"}, {"+ / -", "Speed"},
            {"1 2 3", "Hunt strategy"}, {"P", "Parallel on/off"},
            {"W", "Season"}, {"D", "Disease level"},
            {"O", "Start outbreak"}, {"S", "State rings"},
            {"H", "Health bars"}, {"V", "Vision circles"},
            {"K", "Kill map"}, {"G", "Chart: pop / coat"},
        };
        for (int i = 0; i < keys.length; i++) {
            int kx = x + 10 + (i % 2) * (w / 2);
            int ky = y + 28 + (i / 2) * 24;
            g.setFont(BOLD);
            int kw = g.getFontMetrics().stringWidth(keys[i][0]) + 12;
            g.setColor(new Color(85, 90, 100));
            g.fillRoundRect(kx, ky, kw, 18, 6, 6);
            g.setColor(Color.WHITE);
            g.drawString(keys[i][0], kx + 6, ky + 13);
            g.setFont(TEXT);
            g.setColor(SOFT);
            g.drawString(keys[i][1], kx + kw + 6, ky + 13);
        }

        // ring legend
        int ly = y + h - 22;
        String[] names = {"Forage", "Flee", "Hide/rest", "Hunt", "Sick"};
        Color[] colours = {PreyState.FORAGING.getColour(), PreyState.FLEEING.getColour(),
                           PreyState.HIDING.getColour(), PredatorState.HUNTING.getColour(), SICK};
        int lx = x + 10;
        g.setFont(SMALL);
        for (int i = 0; i < names.length; i++) {
            g.setColor(colours[i]);
            g.setStroke(new BasicStroke(i == 4 ? 2.5f : 1.5f));
            g.drawOval(lx, ly - 10, 12, 12);
            g.setStroke(new BasicStroke(1f));
            g.setColor(SOFT);
            g.drawString(names[i], lx + 16, ly);
            lx += 18 + g.getFontMetrics().stringWidth(names[i]) + 10;
        }
    }

    /** The colour a coat with this fur tone would be (same as Rabbit uses). */
    private Color furSwatch(double t) {
        int r = (int) (120 + (240 - 120) * t);
        int gr = (int) (85 + (237 - 85) * t);
        int b = (int) (55 + (230 - 55) * t);
        return new Color(r, gr, b);
    }

    private void rightText(Graphics2D g, String text, int rightX, int y) {
        g.drawString(text, rightX - g.getFontMetrics().stringWidth(text), y);
    }

    /** Turns an event into a normal sentence for the feed. */
    private String describe(SimEvent e) {
        String s = e.getSpecies();
        switch (e.getType()) {
            case BORN: return s + " was born";
            case EATEN: return s + " was eaten";
            case STARVED: return s + " starved";
            case DIED_SICK: return s + " died of disease";
            case INFECTED: return s + " got sick";
            case RECOVERED: return s + " got better";
            case OUTBREAK: return "Outbreak in the " + s + "s (" + e.getDetail() + ")";
            case SEASON_CHANGED: return "It is now " + e.getDetail();
            case DISEASE_CHANGED: return "Disease set to " + e.getDetail();
            case STRATEGY_CHANGED: return "Predators now hunt: " + e.getDetail();
            default: return e.toString();
        }
    }

    private Color eventColour(EventType type) {
        switch (type) {
            case BORN: return new Color(120, 200, 255);
            case EATEN: return new Color(240, 80, 80);
            case STARVED: return new Color(200, 150, 90);
            case DIED_SICK:
            case INFECTED:
            case OUTBREAK: return SICK;
            case RECOVERED: return new Color(120, 230, 160);
            default: return Color.WHITE;
        }
    }

    private Color seasonChipColour(Season s) {
        switch (s) {
            case SPRING: return new Color(150, 230, 140);
            case SUMMER: return new Color(250, 215, 90);
            case AUTUMN: return new Color(235, 150, 70);
            default: return new Color(200, 220, 250);
        }
    }

    private Color diseaseChipColour() {
        switch (world.getDiseaseLevel()) {
            case OFF: return new Color(170, 175, 185);
            case MILD: return new Color(200, 240, 150);
            case NORMAL: return SICK;
            default: return new Color(255, 120, 90);
        }
    }

    // ----- chart -----

    private void drawChart(Graphics2D g) {
        if (showFurChart) {
            drawFurChart(g);
            return;
        }
        int top = world.getHeight() + 6;
        int h = CHART - 12;
        int w = world.getWidth();
        g.setColor(CARD);
        g.fillRect(0, top, w, h);

        List<Census> data = history.snapshot();
        int max = Math.max(10, history.maxCount(SPECIES));
        int plotTop = top + 24;
        int plotH = h - 30;

        if (data.size() >= 2) {
            // coloured bands for the seasons, so you can see what each season does
            for (int i = 0; i < data.size(); i++) {
                int x0 = i * w / (data.size() - 1);
                int x1 = (i + 1) * w / (data.size() - 1);
                Color t = data.get(i).getSeason().getTint();
                g.setColor(new Color(t.getRed(), t.getGreen(), t.getBlue(), 40));
                g.fillRect(x0, plotTop, Math.max(1, x1 - x0), plotH);
            }
            g.setStroke(new BasicStroke(2f));
            for (int s = 0; s < SPECIES.size(); s++) {
                int[] xs = new int[data.size()];
                int[] ys = new int[data.size()];
                for (int i = 0; i < data.size(); i++) {
                    xs[i] = i * w / (data.size() - 1);
                    ys[i] = plotTop + plotH - data.get(i).count(SPECIES.get(s)) * plotH / max;
                }
                g.setColor(CHART_COLOURS[s]);
                g.drawPolyline(xs, ys, data.size());
            }
            int[] xs = new int[data.size()];
            int[] ys = new int[data.size()];
            for (int i = 0; i < data.size(); i++) {
                xs[i] = i * w / (data.size() - 1);
                ys[i] = plotTop + plotH - data.get(i).totalSick() * plotH / max;
            }
            g.setColor(SICK);
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{5f, 4f}, 0f));
            g.drawPolyline(xs, ys, data.size());
            g.setStroke(new BasicStroke(1f));
        }

        // title and legend
        g.setFont(BOLD);
        g.setColor(Color.WHITE);
        g.drawString("Population (last " + data.size() * history.getSampleEvery() + " ticks)", 10, top + 16);
        int lx = 260;
        g.setFont(SMALL);
        for (int s = 0; s < SPECIES.size(); s++) {
            g.setColor(CHART_COLOURS[s]);
            g.fillRect(lx, top + 9, 16, 3);
            g.setColor(SOFT);
            g.drawString(SPECIES.get(s), lx + 20, top + 15);
            lx += 26 + g.getFontMetrics().stringWidth(SPECIES.get(s)) + 8;
        }
        g.setColor(SICK);
        g.drawString("- - Sick", lx, top + 15);
        lx += 60;
        g.setColor(SOFT);
        g.drawString("Bands = seasons", lx, top + 15);
        rightText(g, "max " + max, w - 8, top + 15);
    }

    /**
     * Average coat colour of rabbits and mice over time (thick lines). The
     * dashed line is the colour of the field, which is perfect camouflage for
     * that season. The thin lines are the moult speed gene, which evolves.
     */
    private void drawFurChart(Graphics2D g) {
        int top = world.getHeight() + 6;
        int h = CHART - 12;
        int w = world.getWidth();
        g.setColor(CARD);
        g.fillRect(0, top, w, h);
        List<Census> data = history.snapshot();
        int plotTop = top + 24;
        int plotH = h - 30;

        if (data.size() >= 2) {
            for (int i = 0; i < data.size(); i++) {
                int x0 = i * w / (data.size() - 1);
                int x1 = (i + 1) * w / (data.size() - 1);
                Color t = data.get(i).getSeason().getTint();
                g.setColor(new Color(t.getRed(), t.getGreen(), t.getBlue(), 40));
                g.fillRect(x0, plotTop, Math.max(1, x1 - x0), plotH);
            }
            int[] xs = new int[data.size()];
            int[] ys = new int[data.size()];
            for (int i = 0; i < data.size(); i++) {
                xs[i] = i * w / (data.size() - 1);
                ys[i] = plotTop + plotH - (int) (data.get(i).getSeason().getFieldTone() * plotH);
            }
            g.setColor(new Color(255, 255, 255, 150));
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{5f, 4f}, 0f));
            g.drawPolyline(xs, ys, data.size());

            String[] prey = {"Rabbit", "Mouse"};
            Color[] colours = {CHART_COLOURS[2], CHART_COLOURS[3]};
            for (int s = 0; s < prey.length; s++) {
                for (int i = 0; i < data.size(); i++) {
                    ys[i] = plotTop + plotH - (int) (data.get(i).averageFur(prey[s]) * plotH);
                }
                g.setColor(colours[s]);
                g.setStroke(new BasicStroke(2.5f));
                g.drawPolyline(xs, ys, data.size());
                for (int i = 0; i < data.size(); i++) {
                    ys[i] = plotTop + plotH - (int) (data.get(i).averageMoult(prey[s]) * plotH);
                }
                g.setStroke(new BasicStroke(1f));
                g.drawPolyline(xs, ys, data.size());
            }
            g.setStroke(new BasicStroke(1f));
        }

        g.setFont(BOLD);
        g.setColor(Color.WHITE);
        g.drawString("Coat colour (low = dark, high = white)", 10, top + 16);
        g.setFont(SMALL);
        int lx = 268;
        g.setColor(CHART_COLOURS[2]);
        g.fillRect(lx, top + 9, 16, 3);
        g.setColor(SOFT);
        g.drawString("Rabbit", lx + 20, top + 15);
        lx += 70;
        g.setColor(CHART_COLOURS[3]);
        g.fillRect(lx, top + 9, 16, 3);
        g.setColor(SOFT);
        g.drawString("Mouse", lx + 20, top + 15);
        lx += 66;
        g.setColor(Color.WHITE);
        g.drawString("- - Field colour", lx, top + 15);
        g.setColor(SOFT);
        g.drawString("thin = moult speed gene", lx + 100, top + 15);
        rightText(g, "G = population", w - 8, top + 15);
    }
}
