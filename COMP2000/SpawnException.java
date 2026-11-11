/**
 * A checked exception thrown when the world cannot find a valid place to
 * spawn a new entity 
 *  Checked because callers should always decide what to do about
 * a failed spawn rather than letting it crash the simulation.
 */
public class SpawnException extends Exception {
    public SpawnException(String message) {
        super(message);
    }
}
