/**
 * How bad the disease is. The D key goes through them.
 * OFF stops it spreading completely and cures everyone.
 */
public enum DiseaseLevel {
    //     name   spread  energy outbreaks
    OFF("Off", 0.0, 1.0, 0.0),
    MILD("Mild", 0.5, 1.3, 0.5),
    NORMAL("Normal", 1.0, 1.7, 1.0),
    DEADLY("Deadly", 1.8, 2.6, 2.0);

    private final String label;
    private final double spreadFactor;
    private final double sickMetabolism;
    private final double outbreakFactor;

    DiseaseLevel(String label, double spreadFactor, double sickMetabolism, double outbreakFactor) {
        this.label = label;
        this.spreadFactor = spreadFactor;
        this.sickMetabolism = sickMetabolism;
        this.outbreakFactor = outbreakFactor;
    }

    public String getLabel() { return label; }
    public double getSpreadFactor() { return spreadFactor; }
    /** How much extra energy a sick animal uses (1.0 = none). */
    public double getSickMetabolism() { return sickMetabolism; }
    public double getOutbreakFactor() { return outbreakFactor; }

    public DiseaseLevel next() {
        if (this == OFF) return MILD;
        if (this == MILD) return NORMAL;
        if (this == NORMAL) return DEADLY;
        return OFF;
    }
}
