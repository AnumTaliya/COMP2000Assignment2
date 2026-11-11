import java.awt.Color;

/**
 * Each season changes how fast food grows, how much energy animals use, whether
 * predators can breed and how fast disease spreads. Winter is the hardest:
 * less food, more energy used, no predator babies and more disease.
 *
 * fieldTone is how light the ground looks (0 = dark, 1 = white snow). Prey
 * whose fur is close to it are camouflaged.
 *
 * The seasons change by themselves, or you can pick one with the W key.
 */
public enum Season {
    //       name     food  energy breeding disease field  tint
    SPRING("Spring", 1.6, 1.00, true, 0.8, 0.15, new Color(150, 255, 150, 22)),
    SUMMER("Summer", 1.1, 0.95, true, 1.0, 0.30, new Color(255, 225, 80, 34)),
    AUTUMN("Autumn", 0.75, 1.0, true, 1.1, 0.35, new Color(220, 110, 30, 48)),
    WINTER("Winter", 0.65, 1.08, false, 1.7, 0.90, new Color(225, 235, 255, 85));

    private final String label;
    private final double foodFactor;
    private final double metabolismFactor;
    private final boolean breeding;
    private final double diseaseFactor;
    private final double fieldTone;
    private final Color tint;

    Season(String label, double foodFactor, double metabolismFactor, boolean breeding,
           double diseaseFactor, double fieldTone, Color tint) {
        this.label = label;
        this.foodFactor = foodFactor;
        this.metabolismFactor = metabolismFactor;
        this.breeding = breeding;
        this.diseaseFactor = diseaseFactor;
        this.fieldTone = fieldTone;
        this.tint = tint;
    }

    public String getLabel() { return label; }
    public double getFoodFactor() { return foodFactor; }
    public double getMetabolismFactor() { return metabolismFactor; }
    public boolean allowsBreeding() { return breeding; }
    public double getDiseaseFactor() { return diseaseFactor; }
    public double getFieldTone() { return fieldTone; }
    public Color getTint() { return tint; }

    public Season next() {
        if (this == SPRING) return SUMMER;
        if (this == SUMMER) return AUTUMN;
        if (this == AUTUMN) return WINTER;
        return SPRING;
    }
}
