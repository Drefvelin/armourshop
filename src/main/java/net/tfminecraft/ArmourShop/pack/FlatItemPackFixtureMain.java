package net.tfminecraft.ArmourShop.pack;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dry-run item + handheld writers into ItemsAdder contents (no Bukkit).
 *
 * Usage: java -cp target/classes net.tfminecraft.ArmourShop.pack.FlatItemPackFixtureMain [contentsPath]
 */
public final class FlatItemPackFixtureMain {

	private static final String DEFAULT_CONTENTS =
		"D:/Documents/TFMC/ItemsAdder Copy/ItemsAdder/contents";

	private FlatItemPackFixtureMain() {}

	public static void main(String[] args) {
		try {
			Path contents = Path.of(
				args.length > 0 && !args[0].isBlank() ? args[0].trim() : DEFAULT_CONTENTS
			);
			if (!Files.isDirectory(contents)) {
				System.err.println("Contents path is not a directory: " + contents);
				System.exit(1);
			}

			writeOne(
				contents,
				"fixture_item",
				"Fixture Item",
				PackKind.ITEM,
				new Color(0xaa, 0x44, 0xff)
			);
			writeOne(
				contents,
				"fixture_handheld",
				"Fixture Handheld",
				PackKind.HANDHELD,
				new Color(0xff, 0x88, 0x22)
			);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private static void writeOne(
		Path contents,
		String slug,
		String displayName,
		PackKind kind,
		Color color
	) throws Exception {
		Map<String, byte[]> files = new LinkedHashMap<>();
		files.put(FlatItemWriter.TEXTURE_STEM, PngUtil.solidPng(16, 16, color));
		PackSubmission submission = new PackSubmission(slug, displayName, kind, files);
		List<Path> written = FlatItemWriter.write(contents, submission);
		System.out.println("Wrote " + kind + " (" + written.size() + " files):");
		for (Path p : written) {
			System.out.println("  " + p);
		}
	}
}
