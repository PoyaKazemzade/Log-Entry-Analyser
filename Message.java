import java.util.Comparator;

public record Message(String logMessage, int count) implements Comparable<Message> {

    private static final Comparator<Message> comp = Comparator.comparingInt(Message::count).reversed();

    @Override
    public int compareTo(Message o) {
        return comp.compare(this, o);
    }
}