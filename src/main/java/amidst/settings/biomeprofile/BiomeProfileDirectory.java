package amidst.settings.biomeprofile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import amidst.documentation.Immutable;
import amidst.logging.AmidstLogger;
import amidst.parsing.FormatException;
import amidst.parsing.json.JsonReader;

@Immutable
public class BiomeProfileDirectory {
	public static BiomeProfileDirectory create(Path biomeProfilesDirectory) {
	    if (biomeProfilesDirectory == null) {
	        biomeProfilesDirectory = DEFAULT_ROOT_DIRECTORY;
	    }
		BiomeProfileDirectory result = new BiomeProfileDirectory(biomeProfilesDirectory);
		AmidstLogger.info("using biome profiles at: '" + result.getRoot() + "'");
		return result;
	}

	private static final Path DEFAULT_ROOT_DIRECTORY = Paths.get("biome");

	private final Path root;
	private final Path defaultProfile;

	public BiomeProfileDirectory(Path root) {
		this.root = root;
		this.defaultProfile = root.resolve("default.json");
	}

	public Path getRoot() {
		return root;
	}

	public Path getDefaultProfile() {
		return defaultProfile;
	}

	public boolean isValid() {
		return Files.isDirectory(root);
	}

	public void saveDefaultProfileIfNecessary() {
		if (!isValid()) {
			AmidstLogger.info("Unable to find biome profile directory.");
		} else {
			AmidstLogger.info("Found biome profile directory.");
			if (Files.isRegularFile(defaultProfile)) {
				AmidstLogger.info("Found default biome profile.");
			} else if (BiomeProfile.getDefaultProfile().save(defaultProfile)) {
				AmidstLogger.info("Saved default biome profile.");
			} else {
				AmidstLogger.info("Attempted to save default biome profile, but encountered an error.");
			}
		}
	}

	public void visitProfiles(BiomeProfileVisitor visitor) {
		visitProfiles(root, visitor);
	}

	private void visitProfiles(Path directory, BiomeProfileVisitor visitor) {
		boolean[] entered = new boolean[]{ false };

		try {
			Files.list(directory).forEachOrdered(file -> {
				if (Files.isRegularFile(file)) {
					BiomeProfile profile = createFromFile(file);
					if (profile != null) {
						if (!entered[0]) {
							entered[0] = true;
							visitor.enterDirectory(directory.getFileName().toString());
						}
						visitor.visitProfile(profile);
					}
				} else {
					visitProfiles(file, visitor);
				}
			});
		} catch (IOException e) {
			AmidstLogger.error(e, "Unexpected IO error while visiting biomes profiles.");
		}

		if (entered[0]) {
			visitor.leaveDirectory();
		}
	}

	private BiomeProfile createFromFile(Path file) {
		try {
			BiomeProfile profile = JsonReader.readLocation(file, BiomeProfile.class);
			if(profile.validate()) {
				return profile;
			}
			AmidstLogger.warn("Profile invalid, ignoring: {}", file);
		} catch (IOException | FormatException e) {
			AmidstLogger.warn(e, "Unable to load file: {}", file);
		}
		return null;
	}
}
