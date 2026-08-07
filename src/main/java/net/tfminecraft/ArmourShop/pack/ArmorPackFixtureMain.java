package net.tfminecraft.ArmourShop.pack;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dry-run armor_set writer into ItemsAdder contents (no Bukkit).
 *
 * Usage: java -cp target/classes net.tfminecraft.ArmourShop.pack.ArmorPackFixtureMain [contentsPath]
 */
public final class ArmorPackFixtureMain {

	private static final String DEFAULT_CONTENTS =
		"D:/Documents/TFMC/ItemsAdder Copy/ItemsAdder/contents";

	private ArmorPackFixtureMain() {}

	public static void main(String[] args) {
		try {
			Path contents = Path.of(
				args.length > 0 && !args[0].isBlank() ? args[0].trim() : DEFAULT_CONTENTS
			);
			if (!Files.isDirectory(contents)) {
				System.err.println("Contents path is not a directory: " + contents);
				System.exit(1);
			}

			String slug = "fixture_armor";
			Map<String, byte[]> files = new LinkedHashMap<>();
			files.put("helmet", PngUtil.solidPng(16, 16, new Color(0x44, 0xaa, 0xff)));
			files.put("chestplate", PngUtil.solidPng(16, 16, new Color(0x33, 0x88, 0xee)));
			files.put("leggings", PngUtil.solidPng(16, 16, new Color(0x22, 0x66, 0xcc)));
			files.put("boots", PngUtil.solidPng(16, 16, new Color(0x11, 0x44, 0xaa)));
			files.put("layer_1", PngUtil.solidPng(64, 32, new Color(0x55, 0xbb, 0xff)));
			files.put("layer_2", PngUtil.solidPng(64, 32, new Color(0x88, 0xdd, 0xff)));

			PackSubmission submission = new PackSubmission(
				slug,
				"Fixture Armor",
				PackKind.ARMOR_SET,
				files
			);

			List<Path> written = ArmorSetWriter.write(contents, submission);
			System.out.println("Wrote " + written.size() + " files under " + contents);
			for (Path p : written) {
				System.out.println("  " + p);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
