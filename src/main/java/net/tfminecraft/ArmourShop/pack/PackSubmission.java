package net.tfminecraft.ArmourShop.pack;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Input for pack writers (Bukkit-free).
 */
public final class PackSubmission {

	private final String slug;
	private final String displayName;
	private final PackKind kind;
	private final GripPreset gripPreset;
	/** File stem → PNG bytes (e.g. helmet, chestplate, layer_1, texture). */
	private final Map<String, byte[]> files;

	public PackSubmission(
		String slug,
		String displayName,
		PackKind kind,
		Map<String, byte[]> files
	) {
		this(slug, displayName, kind, null, files);
	}

	public PackSubmission(
		String slug,
		String displayName,
		PackKind kind,
		GripPreset gripPreset,
		Map<String, byte[]> files
	) {
		this.slug = Objects.requireNonNull(slug, "slug").trim();
		this.displayName = Objects.requireNonNull(displayName, "displayName").trim();
		this.kind = Objects.requireNonNull(kind, "kind");
		if (this.slug.isEmpty()) {
			throw new IllegalArgumentException("slug is required");
		}
		if (this.displayName.isEmpty()) {
			throw new IllegalArgumentException("displayName is required");
		}
		if (kind == PackKind.LARGE_HANDHELD) {
			if (gripPreset == null) {
				throw new IllegalArgumentException("gripPreset is required for LARGE_HANDHELD");
			}
			this.gripPreset = gripPreset;
		} else {
			this.gripPreset = gripPreset;
		}
		Map<String, byte[]> copy = new LinkedHashMap<>();
		if (files != null) {
			for (Map.Entry<String, byte[]> e : files.entrySet()) {
				if (e.getKey() == null || e.getValue() == null) {
					continue;
				}
				copy.put(e.getKey().trim(), e.getValue());
			}
		}
		this.files = Collections.unmodifiableMap(copy);
	}

	public String slug() {
		return slug;
	}

	public String displayName() {
		return displayName;
	}

	public PackKind kind() {
		return kind;
	}

	/** Null unless kind is LARGE_HANDHELD (then non-null). */
	public GripPreset gripPreset() {
		return gripPreset;
	}

	public Map<String, byte[]> files() {
		return files;
	}

	public byte[] requireFile(String stem) {
		byte[] data = files.get(stem);
		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("Missing PNG for stem: " + stem);
		}
		return data;
	}
}
