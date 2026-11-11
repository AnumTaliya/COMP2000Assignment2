/**
 * Anything that provides nutrition when eaten. Food implements this (eaten
 * by Prey), and Prey implements it too (eaten by Predator) 
 */
public interface Edible {
    double getNutritionValue();
}
