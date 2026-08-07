package net.tfminecraft.ArmourShop.pack;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One-shot harness: write all MVP kinds and assert expected outputs.
 *
 * Usage: java -cp target/classes net.tfminecraft.ArmourShop.pack.PackHarnessMain [contentsPath]
 */
public final class PackHarnessMain {

	private static final String DEFAULT_CONTENTS =
		"D:/Documents/TFMC/ItemsAdder Copy/ItemsAdder/contents";

	private PackHarnessMain() {}

	public static void main(String[] args) {
		try {
			Path contents = Path.of(
				args.length > 0 && !args[0].isBlank() ? args[0].trim() : DEFAULT_CONTENTS
			);
			if (!Files.isDirectory(contents)) {
				fail("Contents path is not a directory: " + contents);
			}

			writeAll(contents);
			assertAll(contents);

			System.out.println("OK — pack harness wrote and verified all MVP kinds under:");
			System.out.println("  " + PackPaths.namespaceRoot(contents));
		} catch (AssertionError e) {
			System.err.println("FAIL: " + e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	static void writeAll(Path contents) throws Exception {
		writeArmor(contents);
		writeFlat(contents, "harness_item", "Harness Item", PackKind.ITEM, new Color(0x88, 0x44, 0xcc));
		writeFlat(contents, "harness_handheld", "Harness Handheld", PackKind.HANDHELD, new Color(0xcc, 0x88, 0x22));
		writeLarge(contents, "harness_large_bottom", GripPreset.BOTTOM, new Color(0x22, 0xaa, 0x66));
		writeLarge(contents, "harness_large_middle", GripPreset.MIDDLE, new Color(0xaa, 0xaa, 0x22));
		writeLarge(contents, "harness_large_top", GripPreset.TOP, new Color(0xaa, 0x44, 0x22));
	}

	private static void writeArmor(Path contents) throws Exception {
		Map<String, byte[]> files = new LinkedHashMap<>();
		files.put("helmet", PngUtil.solidPng(16, 16, new Color(0x44, 0xaa, 0xff)));
		files.put("chestplate", PngUtil.solidPng(16, 16, new Color(0x33, 0x88, 0xee)));
		files.put("leggings", PngUtil.solidPng(16, 16, new Color(0x22, 0x66, 0xcc)));
		files.put("boots", PngUtil.solidPng(16, 16, new Color(0x11, 0x44, 0xaa)));
		files.put("layer_1", PngUtil.solidPng(64, 32, new Color(0x55, 0xbb, 0xff)));
		files.put("layer_2", PngUtil.solidPng(64, 32, new Color(0x88, 0xdd, 0xff)));
		List<Path> written = ArmorSetWriter.write(
			contents,
			new PackSubmission("harness_armor", "Harness Armor", PackKind.ARMOR_SET, files)
		);
		System.out.println("Wrote ARMOR_SET (" + written.size() + " files)");
	}

	private static void writeFlat(
		Path contents,
		String slug,
		String name,
		PackKind kind,
		Color color
	) throws Exception {
		Map<String, byte[]> files = new LinkedHashMap<>();
		files.put(FlatItemWriter.TEXTURE_STEM, PngUtil.solidPng(16, 16, color));
		List<Path> written = FlatItemWriter.write(
			contents,
			new PackSubmission(slug, name, kind, files)
		);
		System.out.println("Wrote " + kind + " (" + written.size() + " files)");
	}

	private static void writeLarge(
		Path contents,
		String slug,
		GripPreset grip,
		Color color
	) throws Exception {
		Map<String, byte[]> files = new LinkedHashMap<>();
		files.put(LargeHandheldWriter.TEXTURE_STEM, PngUtil.solidPng(32, 32, color));
		List<Path> written = LargeHandheldWriter.write(
			contents,
			new PackSubmission(
				slug,
				"Harness Large " + grip.id(),
				PackKind.LARGE_HANDHELD,
				grip,
				files
			)
		);
		System.out.println("Wrote LARGE_HANDHELD " + grip.id() + " (" + written.size() + " paths)");
	}

	static void assertAll(Path contents) throws Exception {
		assertArmor(contents);
		assertFlat(contents, "harness_item", "item/generated");
		assertFlat(contents, "harness_handheld", "item/handheld");
		assertLarge(contents, "harness_large_bottom");
		assertLarge(contents, "harness_large_middle");
		assertLarge(contents, "harness_large_top");
		assertFile(PackPaths.itemModelsDir(contents).resolve("grip_bottom.json"));
		assertFile(PackPaths.itemModelsDir(contents).resolve("grip_middle.json"));
		assertFile(PackPaths.itemModelsDir(contents).resolve("grip_top.json"));
	}

	private static void assertArmor(Path contents) throws Exception {
		String yaml = read(PackPaths.configsDir(contents).resolve("harness_armor.yml"));
		assertContains(yaml, "armors_rendering");
		assertContains(yaml, "generate: true");
		String slug = "harness_armor";
		assertFile(PackPaths.armorIconsDir(contents).resolve(slug + "_helmet.png"));
		assertFile(PackPaths.armorIconsDir(contents).resolve(slug + "_chestplate.png"));
		assertFile(PackPaths.armorIconsDir(contents).resolve(slug + "_leggings.png"));
		assertFile(PackPaths.armorIconsDir(contents).resolve(slug + "_boots.png"));
		assertFile(PackPaths.armorLayersDir(contents).resolve(slug + "_layer_1.png"));
		assertFile(PackPaths.armorLayersDir(contents).resolve(slug + "_layer_2.png"));
	}

	private static void assertFlat(Path contents, String slug, String parent) throws Exception {
		String yaml = read(PackPaths.configsDir(contents).resolve(slug + ".yml"));
		assertContains(yaml, "generate: true");
		assertContains(yaml, "parent: " + parent);
		assertFile(PackPaths.itemTexturesDir(contents).resolve(slug + ".png"));
		Path model = PackPaths.itemModelsDir(contents).resolve(slug + ".json");
		if (Files.isRegularFile(model)) {
			fail("Expected no model JSON for flat kind: " + model);
		}
	}

	private static void assertLarge(Path contents, String slug) throws Exception {
		String yaml = read(PackPaths.configsDir(contents).resolve(slug + ".yml"));
		assertContains(yaml, "generate: false");
		assertContains(yaml, "model_path: item/" + slug);
		assertFile(PackPaths.itemTexturesDir(contents).resolve(slug + ".png"));
		assertFile(PackPaths.itemModelsDir(contents).resolve(slug + ".json"));
	}

	private static String read(Path path) throws Exception {
		assertFile(path);
		return Files.readString(path, StandardCharsets.UTF_8);
	}

	private static void assertFile(Path path) {
		if (!Files.isRegularFile(path)) {
			fail("Missing file: " + path);
		}
	}

	private static void assertContains(String haystack, String needle) {
		if (!haystack.contains(needle)) {
			fail("Expected YAML to contain: " + needle);
		}
	}

	private static void fail(String message) {
		throw new AssertionError(message);
	}
}
