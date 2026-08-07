package net.tfminecraft.ArmourShop.pack;

/**
 * Grip presets for large_handheld (locked display templates).
 */
public enum GripPreset {
	BOTTOM("bottom"),
	MIDDLE("middle"),
	TOP("top");

	private final String id;

	GripPreset(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public String modelFileName() {
		return "grip_" + id + ".json";
	}

	public String parentModelPath() {
		return PackPaths.NAMESPACE + ":item/grip_" + id;
	}

	public static GripPreset fromId(String raw) {
		if (raw == null) {
			throw new IllegalArgumentException("grip_preset is required");
		}
		String id = raw.trim().toLowerCase();
		for (GripPreset p : values()) {
			if (p.id.equals(id)) {
				return p;
			}
		}
		throw new IllegalArgumentException("Unknown grip_preset: " + raw);
	}
}
