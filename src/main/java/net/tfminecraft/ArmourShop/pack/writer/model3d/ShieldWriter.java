package net.tfminecraft.ArmourShop.pack.writer.model3d;


import net.tfminecraft.ArmourShop.pack.model.PackKind;
import net.tfminecraft.ArmourShop.pack.model.PackPaths;
import net.tfminecraft.ArmourShop.pack.model.PackSubmission;
import net.tfminecraft.ArmourShop.pack.util.Model3dUtil;
import net.tfminecraft.ArmourShop.pack.util.YamlUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Writes shield skins: idle model + blocking clone with locked round display Δ.
 */
public final class ShieldWriter {

	/**
	 * Round-shield idle→blocking display deltas (absolute replacement tabs from
	 * wooden_round_shield → wooden_round_shield_blocking). Applied onto donor
	 * display after clone; tabs not listed are left as in the idle model.
	 */
	private static final Map<String, float[][]> ROUND_BLOCKING_DISPLAY = Map.of(
		"thirdperson_righthand", new float[][]{
			{30, -35, 0}, {1, -1, -1}, {1.01f, 1.01f, 1.01f}
		},
		"thirdperson_lefthand", new float[][]{
			{30, -35, 0}, {1, -1, -1}, {1.01f, 1.01f, 1.01f}
		},
		"firstperson_righthand", new float[][]{
			{10, 0, 15}, {-3, 0, 1}, {0.78f, 0.78f, 0.78f}
		},
		"firstperson_lefthand", new float[][]{
			{10, 0, 15}, {-3, 0, 1}, {0.78f, 0.78f, 0.78f}
		}
	);

	private ShieldWriter() {}

	public static List<Path> write(Path contentsRoot, PackSubmission submission)
		throws IOException
	{
		if (submission.kind() != PackKind.SHIELD) {
			throw new IllegalArgumentException(
				"ShieldWriter requires SHIELD, got " + submission.kind()
			);
		}

		String slug = submission.slug();
		YamlUtil.validateSlug(slug);

		Path modelsDir = PackPaths.itemModelsDir(contentsRoot);
		Path itemTexDir = PackPaths.itemTexturesDir(contentsRoot);
		Path configsDir = PackPaths.configsDir(contentsRoot);
		Files.createDirectories(modelsDir);
		Files.createDirectories(itemTexDir);
		Files.createDirectories(configsDir);

		List<Path> written = new ArrayList<>();

		Path pngPath = itemTexDir.resolve(slug + ".png");
		Files.write(pngPath, submission.requireFile(Model3dUtil.TEXTURE_STEM));
		written.add(pngPath);

		byte[] idleNormalized = Model3dUtil.normalizeModel(
			submission.requireFile(Model3dUtil.MODEL_STEM),
			slug
		);
		JsonObject idle = Model3dUtil.parseObject(idleNormalized);
		applyBlockingOverrides(idle, slug);

		Path idlePath = modelsDir.resolve(slug + ".json");
		Files.write(idlePath, Model3dUtil.toBytes(idle));
		written.add(idlePath);

		JsonObject blocking = Model3dUtil.parseObject(idleNormalized);
		applyRoundBlockingDisplay(blocking);
		blocking.remove("overrides");
		Path blockingPath = modelsDir.resolve(slug + "_blocking.json");
		Files.write(blockingPath, Model3dUtil.toBytes(blocking));
		written.add(blockingPath);

		Path yamlPath = configsDir.resolve(slug + ".yml");
		Files.writeString(yamlPath, buildYaml(submission), StandardCharsets.UTF_8);
		written.add(yamlPath);
		return written;
	}

	static void applyBlockingOverrides(JsonObject idle, String slug) {
		JsonArray overrides = new JsonArray();
		JsonObject entry = new JsonObject();
		JsonObject predicate = new JsonObject();
		predicate.addProperty("blocking", 1);
		entry.add("predicate", predicate);
		entry.addProperty(
			"model",
			PackPaths.NAMESPACE + ":item/" + slug + "_blocking"
		);
		overrides.add(entry);
		idle.add("overrides", overrides);
	}

	static void applyRoundBlockingDisplay(JsonObject model) {
		JsonObject display = model.has("display") && model.get("display").isJsonObject()
			? model.getAsJsonObject("display")
			: new JsonObject();
		for (Map.Entry<String, float[][]> e : ROUND_BLOCKING_DISPLAY.entrySet()) {
			float[][] rtsv = e.getValue();
			JsonObject tab = new JsonObject();
			tab.add("rotation", floatArray(rtsv[0]));
			tab.add("translation", floatArray(rtsv[1]));
			tab.add("scale", floatArray(rtsv[2]));
			display.add(e.getKey(), tab);
		}
		model.add("display", display);
	}

	private static JsonArray floatArray(float[] values) {
		JsonArray arr = new JsonArray();
		for (float v : values) {
			arr.add(v);
		}
		return arr;
	}

	static String buildYaml(PackSubmission submission) {
		String slug = submission.slug();
		String name = YamlUtil.escapeDoubleQuoted(submission.displayName());
		StringBuilder sb = new StringBuilder();
		sb.append("info:\n");
		sb.append("  namespace: ").append(PackPaths.NAMESPACE).append('\n');
		sb.append("items:\n");
		sb.append("  ").append(slug).append(":\n");
		sb.append("    display_name: \"").append(name).append("\"\n");
		sb.append("    permission: ").append(slug).append('\n');
		sb.append("    resource:\n");
		sb.append("      material: SHIELD\n");
		sb.append("      generate: false\n");
		sb.append("      model_path: item/").append(slug).append('\n');
		return sb.toString();
	}
}
