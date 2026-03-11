package gmae.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class RealmMap {

    private final Map<String, Realm> realmsById = new LinkedHashMap<>();
    private final Map<Realm, Set<Realm>> adjacency = new LinkedHashMap<>();

    public void addRealm(Realm realm) {
        if (realm == null) {
            throw new IllegalArgumentException("Realm must not be null");
        }
        realmsById.put(realm.getId(), realm);
        adjacency.computeIfAbsent(realm, ignored -> new LinkedHashSet<>());
    }

    public Realm getRealm(String id) {
        return realmsById.get(id);
    }

    public Collection<Realm> getRealms() {
        return Collections.unmodifiableCollection(realmsById.values());
    }

    public List<Realm> neighborsOf(Realm realm) {
        if (realm == null) {
            return List.of();
        }
        Set<Realm> neighbors = adjacency.get(realm);
        if (neighbors == null) {
            return List.of();
        }
        return List.copyOf(neighbors);
    }

    public boolean isAdjacent(Realm first, Realm second) {
        if (first == null || second == null) {
            return false;
        }
        return adjacency.getOrDefault(first, Set.of()).contains(second);
    }

    public void connect(String firstId, String secondId) {
        Realm first = getRealm(firstId);
        Realm second = getRealm(secondId);
        if (first == null || second == null) {
            throw new IllegalArgumentException("Both realms must exist before connecting them");
        }
        connect(first, second);
    }

    public void connect(Realm first, Realm second) {
        Objects.requireNonNull(first, "First realm must not be null");
        Objects.requireNonNull(second, "Second realm must not be null");
        addRealm(first);
        addRealm(second);
        adjacency.get(first).add(second);
        adjacency.get(second).add(first);
    }

    public Realm randomRealm(Random rng) {
        if (realmsById.isEmpty()) {
            return null;
        }
        List<Realm> realms = new ArrayList<>(realmsById.values());
        return realms.get(rng.nextInt(realms.size()));
    }

    public static RealmMap createGrid(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be positive");
        }

        RealmMap map = new RealmMap();
        Realm[][] grid = new Realm[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Realm realm = new Realm(
                        "R" + row + "C" + col,
                        "Realm (" + row + "," + col + ")",
                        "Grid cell at row " + row + ", column " + col
                );
                grid[row][col] = realm;
                map.addRealm(realm);
            }
        }

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (row + 1 < rows) {
                    map.connect(grid[row][col], grid[row + 1][col]);
                }
                if (col + 1 < cols) {
                    map.connect(grid[row][col], grid[row][col + 1]);
                }
            }
        }

        return map;
    }
}
