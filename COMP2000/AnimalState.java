import java.awt.Color;

/**
 * State pattern. An animal asks its current state what to do each tick, and
 * the state can switch the animal into a different state.
 *
 * The states are enums (PreyState, PredatorState) and hold no data about any
 * one animal, so every rabbit shares the same FLEEING object. Anything that
 * belongs to one animal (like how long it has been hiding) stays on the animal.
 */
public interface AnimalState<A extends Animal> {
    Action plan(A self, World world);

    Color getColour();

    /** How much energy this state uses, 1.0 = normal. */
    double energyCost();
}
