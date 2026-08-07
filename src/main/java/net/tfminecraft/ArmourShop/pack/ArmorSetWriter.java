package net.tfminecraft.ArmourShop.pack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes an armor_set into tfmc_submissions (YAML + six PNGs).
 */
public final class ArmorSetWriter {

	private static final String[] ICON_STEMS = {
		"helmet", "chestplate", "leggings", "boots"
	};
	private static final String[] ICON_SLOTS = {
		"head", "chest", "legs", "feet"
	};
	private static final String[] ICON_SUFFIXES = {
		"Helmet", "Chestplate", "Leggings", "Boots"
	};
	private static final String[] LAYER_STEMS = {
		"layer_1", "layer_2"
	};

	private ArmorSetWriter() {}

	/**
	 * @return list of paths written (relative-friendly absolute paths)
	 */
	public static List<Path> write(Path contentsRoot, PackSubmission submission)
		throws IOException
	{
		if (submission.kind() != PackKind.ARMOR_SET) {
			throw new IllegalArgumentException(
				"ArmorSetWriter requires ARMOR_SET, got " + submission.kind()
			);
		}

		String slug = submission.slug();
		YamlUtil.validateSlug(slug);

		Path iconsDir = PackPaths.armorIconsDir(contentsRoot);
		Path layersDir = PackPaths.armorLayersDir(contentsRoot);
		Path configsDir = PackPaths.configsDir(contentsRoot);
		Files.createDirectories(iconsDir);
		Files.createDirectories(layersDir);
		Files.createDirectories(configsDir);

		List<Path> written = new ArrayList<>();

		for (String stem : ICON_STEMS) {
			Path out = iconsDir.resolve(slug + "_" + stem + ".png");
			Files.write(out, submission.requireFile(stem));
			written.add(out);
		}
		for (String stem : LAYER_STEMS) {
			Path out = layersDir.resolve(slug + "_" + stem + ".png");
			Files.write(out, submission.requireFile(stem));
			written.add(out);
		}

		Path yamlPath = configsDir.resolve(slug + ".yml");
		Files.writeString(
			yamlPath,
			buildYaml(submission),
			StandardCharsets.UTF_8
		);
		written.add(yamlPath);
		return written;
	}

	static String buildYaml(PackSubmission submission) {
		String slug = submission.slug();
		String name = YamlUtil.escapeDoubleQuoted(submission.displayName());

		StringBuilder sb = new StringBuilder();
		sb.append("info:\n");
		sb.append("  namespace: ").append(PackPaths.NAMESPACE).append('\n');
		sb.append("armors_rendering:\n");
		sb.append("  ").append(slug).append(":\n");
		sb.append("    color: '#ffffff'\n");
		sb.append("    layer_1: armor_layers/").append(slug).append("_layer_1\n");
		sb.append("    layer_2: armor_layers/").append(slug).append("_layer_2\n");
		sb.append("    use_color: false\n");
		sb.append("items:\n");

		for (int i = 0; i < ICON_STEMS.length; i++) {
			String stem = ICON_STEMS[i];
			String itemId = slug + "_" + stem;
			sb.append("  ").append(itemId).append(":\n");
			sb.append("    display_name: \"").append(name).append(' ')
				.append(ICON_SUFFIXES[i]).append("\"\n");
			sb.append("    permission: ").append(slug).append('\n');
			sb.append("    resource:\n");
			sb.append("      generate: true\n");
			sb.append("      textures:\n");
			sb.append("      - armor_icons/").append(slug).append('_').append(stem).append('\n');
			sb.append("    specific_properties:\n");
			sb.append("      armor:\n");
			sb.append("        slot: ").append(ICON_SLOTS[i]).append('\n');
			sb.append("        custom_armor: ").append(slug).append('\n');
		}
		return sb.toString();
	}
}
