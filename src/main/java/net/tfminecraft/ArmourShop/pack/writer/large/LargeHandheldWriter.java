package net.tfminecraft.ArmourShop.pack.writer.large;

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

/**
 * Writes large_handheld skins from web-built thin model JSON (passthrough).
 */
public final class LargeHandheldWriter {

	public static final String TEXTURE_STEM = "texture";

	private LargeHandheldWriter() {}

	public static List<Path> write(Path contentsRoot, PackSubmission submission)
		throws IOException
	{
		if (submission.kind() != PackKind.LARGE_HANDHELD) {
			throw new IllegalArgumentException(
				"LargeHandheldWriter requires LARGE_HANDHELD, got " + submission.kind()
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
		Files.write(pngPath, submission.requireFile(TEXTURE_STEM));
		written.add(pngPath);

		Path thinModel = modelsDir.resolve(slug + ".json");
		Files.write(thinModel, submission.requireFile(Model3dUtil.MODEL_STEM));
		written.add(thinModel);

		Path yamlPath = configsDir.resolve(slug + ".yml");
		Files.writeString(yamlPath, buildYaml(submission), StandardCharsets.UTF_8);
		written.add(yamlPath);
		return written;
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
		sb.append("      material: PAPER\n");
		sb.append("      generate: false\n");
		sb.append("      model_path: item/").append(slug).append('\n');
		return sb.toString();
	}
}
