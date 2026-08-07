package net.tfminecraft.ArmourShop.pack;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dry-run large_handheld writer for all three grips (no Bukkit).
 *
 * Usage: java -cp target/classes net.tfminecraft.ArmourShop.pack.LargeHandheldPackFixtureMain [contentsPath]
 */
public final class LargeHandheldPackFixtureMain {

	private static final String DEFAULT_CONTENTS =
		"D:/Documents/TFMC/ItemsAdder Copy/ItemsAdder/contents";

	private LargeHandheldPackFixtureMain() {}

	public static void main(String[] args) {
		try {
			Path contents = Path.of(
				args.length > 0 && !args[0].isBlank() ? args[0].trim() : DEFAULT_CONTENTS
			);
			if (!Files.isDirectory(contents)) {
				System.err.println("Contents path is not a directory: " + contents);
				System.exit(1);
			}

			writeOne(contents, "fixture_large_bottom", GripPreset.BOTTOM, new Color(0x22, 0xaa, 0x66));
			writeOne(contents, "fixture_large_middle", GripPreset.MIDDLE, new Color(0xaa, 0xaa, 0x22));
			writeOne(contents, "fixture_large_top", GripPreset.TOP, new Color(0xaa, 0x44, 0x22));
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private static void writeOne(
		Path contents,
		String slug,
		GripPreset grip,
		Color color
	) throws Exception {
		Map<String, byte[]> files = new LinkedHashMap<>();
		files.put(LargeHandheldWriter.TEXTURE_STEM, PngUtil.solidPng(32, 32, color));
		PackSubmission submission = new PackSubmission(
			slug,
			"Fixture Large " + grip.id(),
			PackKind.LARGE_HANDHELD,
			grip,
			files
		);
		List<Path> written = LargeHandheldWriter.write(contents, submission);
		System.out.println("Wrote LARGE_HANDHELD " + grip.id() + " (" + written.size() + " paths):");
		for (Path p : written) {
			System.out.println("  " + p);
		}
	}
}
