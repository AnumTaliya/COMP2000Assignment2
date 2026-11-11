/**
 * Timing test for Week 12 ("measure the speedup"). Run it with: java Benchmark
 * It makes a big world with no window and times the same number of ticks with
 * normal planning and then parallel planning.
 */
public class Benchmark {
    public static void main(String[] args) {
        int ticks = 300;
        if (args.length > 0) {
            ticks = Integer.parseInt(args[0]);
        }
        System.out.println("Cores: " + Runtime.getRuntime().availableProcessors());
        for (int round = 0; round < 2; round++) {
            for (boolean parallel : new boolean[]{false, true}) {
                World w = bigWorld();
                w.setParallel(parallel);
                for (int i = 0; i < 20; i++) w.update(); // warm up first so the timing is fair
                long start = System.nanoTime();
                for (int i = 0; i < ticks; i++) w.update();
                double ms = (System.nanoTime() - start) / 1e6;
                String mode = parallel ? "parallel" : "sequential";
                System.out.printf("round %d  %-10s %8.1f ms total  %6.2f ms/tick  (%d animals at end)%n",
                        round + 1, mode, ms, ms / ticks, w.getAnimals().size());
            }
        }
    }

    private static World bigWorld() {
        World w = new World(2000, 1500);
        w.setCapacity("Hawk", 200);
        w.setCapacity("Fox", 200);
        w.setCapacity("Rabbit", 1500);
        w.setCapacity("Mouse", 2500);
        w.setFoodSupply(1, 3000);
        for (int i = 0; i < 60; i++) w.addHawk(new Hawk(Math.random() * 1400, Math.random() * 1000));
        for (int i = 0; i < 80; i++) w.addFox(new Fox(Math.random() * 1400, Math.random() * 1000));
        for (int i = 0; i < 800; i++) w.addRabbit(new Rabbit(Math.random() * 2000, Math.random() * 1500));
        for (int i = 0; i < 1500; i++) w.addMouse(new Mouse(Math.random() * 2000, Math.random() * 1500));
        for (int i = 0; i < 2000; i++) w.addFood(new Food(Math.random() * 2000, Math.random() * 1500));
        return w;
    }
}
