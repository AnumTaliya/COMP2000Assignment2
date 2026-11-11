/**
 * Observer pattern. World is the subject, and anything that wants to know
 * about births, deaths or the end of a tick adds a SimListener.
 *
 * In A1 SimPanel read the numbers straight out of World's lists. Now World
 * doesn't know who is listening. It's a functional interface, so a
 * listener can also just be a lambda (see Main).
 */
@FunctionalInterface
public interface SimListener {
    void onEvent(SimEvent event);
}
