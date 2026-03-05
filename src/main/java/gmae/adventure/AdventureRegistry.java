package gmae.adventure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Central registry for all available mini-adventures.
public class AdventureRegistry {

    private static final List<MiniAdventure> adventures = new ArrayList<>();

    private AdventureRegistry() {}

    public static void register(MiniAdventure adventure) {
        if (adventure == null) throw new IllegalArgumentException("Adventure must not be null");
        adventures.add(adventure);
    }

    public static List<MiniAdventure> getAll() {
        return Collections.unmodifiableList(adventures);
    }

    public static int count() {
        return adventures.size();
    }
}
