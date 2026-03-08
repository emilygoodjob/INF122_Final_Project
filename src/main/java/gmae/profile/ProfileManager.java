package gmae.profile;

import gmae.model.PlayerProfile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// Manages player profiles for the GMAE.

public class ProfileManager {

    private final Map<String, PlayerProfile> profiles = new HashMap<>();
    public void loadProfiles() {
    }

    public void saveProfiles() {
    }

    public PlayerProfile getOrCreate(String name) {
        return profiles.computeIfAbsent(name, PlayerProfile::new);
    }

    public PlayerProfile find(String name) {
        return profiles.get(name);
    }

    public Collection<PlayerProfile> getAllProfiles() {
        return profiles.values();
    }
}
