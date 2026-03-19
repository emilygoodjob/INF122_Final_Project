package gmae.profile;

import gmae.model.PlayerProfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

// Manages player profiles for the GMAE.

public class ProfileManager {

    private static final String SAVE_FILE = "gmae_profiles.properties";

    private final Map<String, PlayerProfile> profiles = new HashMap<>();

    public void loadProfiles() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            return;
        }
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
        } catch (IOException e) {
            System.err.println("Failed to load profiles: " + e.getMessage());
            return;
        }
        for (String key : props.stringPropertyNames()) {
            if (!key.endsWith(".gamesPlayed")) continue;
            String name = key.substring(0, key.length() - ".gamesPlayed".length());
            int played = Integer.parseInt(props.getProperty(name + ".gamesPlayed", "0"));
            int wins   = Integer.parseInt(props.getProperty(name + ".gamesWin",    "0"));
            PlayerProfile profile = profiles.computeIfAbsent(name, PlayerProfile::new);
            for (int i = 0; i < played; i++) profile.incrementGamesPlayed();
            for (int i = 0; i < wins;   i++) profile.incrementGamesWin();
        }
    }

    public void saveProfiles() {
        Properties props = new Properties();
        for (PlayerProfile profile : profiles.values()) {
            String name = profile.getPlayerName();
            props.setProperty(name + ".gamesPlayed", String.valueOf(profile.getGamesPlayed()));
            props.setProperty(name + ".gamesWin",    String.valueOf(profile.getGamesWin()));
        }
        try (FileOutputStream out = new FileOutputStream(SAVE_FILE)) {
            props.store(out, "GMAE Player Profiles");
        } catch (IOException e) {
            System.err.println("Failed to save profiles: " + e.getMessage());
        }
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
