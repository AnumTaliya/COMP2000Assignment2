import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        World world = new World(800, 600);
        populate(world);

        // observers - the last one is just a lambda
        PopulationHistory history = new PopulationHistory(800, 5);
        EventLog log = new EventLog(400);
        world.addListener(history);
        world.addListener(log);
        world.addListener(e -> {
            if (e.getType() == EventType.STRATEGY_CHANGED) {
                System.out.println("Predators switched to: " + e.getDetail());
            }
        });

        SimulationLoop loop = new SimulationLoop(world, 40);

        SwingUtilities.invokeLater(() -> {
            SimPanel panel = new SimPanel(world, loop, history, log);
            JFrame frame = new JFrame("Predator and Prey Simulation - COMP2000 A2");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();
            loop.start();
        });
    }

    static void populate(World world) {
        int width = world.getWidth(), height = world.getHeight();
        for (int i = 0; i < 2; i++) {
            double[] p = randomPointOutsideZone(world);
            world.addHawk(new Hawk(p[0], p[1]));
        }
        for (int i = 0; i < 3; i++) {
            double[] p = randomPointOutsideZone(world);
            world.addFox(new Fox(p[0], p[1]));
        }
        for (int i = 0; i < 20; i++) world.addRabbit(new Rabbit(Math.random() * width, Math.random() * height));
        for (int i = 0; i < 30; i++) world.addMouse(new Mouse(Math.random() * width, Math.random() * height));
        for (int i = 0; i < 60; i++) world.addFood(new Food(Math.random() * width, Math.random() * height));
    }

   
    private static double[] randomPointOutsideZone(World world) {
        double x, y;
        do {
            x = Math.random() * world.getWidth();
            y = Math.random() * world.getHeight();
        } while (world.isInSafeZone(x, y));
        return new double[]{x, y};
    }
}
