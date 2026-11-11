/**
 * Something an animal has decided to do but hasn't done yet.
 * Each tick, World asks every animal for an Action first (this part runs in
 * parallel and only reads the world), then runs all the Actions one by one.
 * It's a functional interface, so an Action is just a lambda like
 * w -> moveToward(food).
 */
@FunctionalInterface
public interface Action {
    void apply(World world);
}
