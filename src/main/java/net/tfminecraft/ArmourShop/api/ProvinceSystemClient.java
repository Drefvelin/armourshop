package net.tfminecraft.ArmourShop.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import net.tfminecraft.ArmourShop.Cache;

/**
 * Minimal HTTP client for ProvinceSystem skins plugin routes.
 */
public class ProvinceSystemClient {

	private static final int TIMEOUT_MS = 8000;

	/** Shared result for link/start and skins code issue. */
	public static final class CodeResult {
		public final boolean ok;
		public final String code;
		public final String expiresAt;
		public final String error;

		private CodeResult(boolean ok, String code, String expiresAt, String error) {
			this.ok = ok;
			this.code = code;
			this.expiresAt = expiresAt;
			this.error = error;
		}

		public static CodeResult success(String code, String expiresAt) {
			return new CodeResult(true, code, expiresAt, null);
		}

		public static CodeResult fail(String error) {
			return new CodeResult(false, null, null, error);
		}
	}

	public static CodeResult startDiscordLink(String playerUuid, String minecraftName) {
		String body = "{"
			+ "\"player_uuid\":\"" + escapeJson(playerUuid) + "\","
			+ "\"minecraft_name\":\"" + escapeJson(minecraftName == null ? "" : minecraftName) + "\""
			+ "}";
		return postForCode(
			"/skins/discord/link/start",
			body,
			"API returned OK but no link code."
		);
	}

	public static CodeResult issueSkinsCode(String playerUuid) {
		String uuid = playerUuid == null ? "" : playerUuid.trim();
		if (uuid.isEmpty()) {
			return CodeResult.fail("player_uuid is required");
		}
		String body = "{\"player_uuid\":\"" + escapeJson(uuid) + "\"}";
		return postForCode(
			"/skins/codes",
			body,
			"API returned OK but no skins code."
		);
	}

	/** Result for unlink (and similar ok/error POSTs). */
	public static final class SimpleResult {
		public final boolean ok;
		public final String error;

		private SimpleResult(boolean ok, String error) {
			this.ok = ok;
			this.error = error;
		}

		public static SimpleResult success() {
			return new SimpleResult(true, null);
		}

		public static SimpleResult fail(String error) {
			return new SimpleResult(false, error);
		}
	}

	public static SimpleResult unlinkDiscord(String playerUuid) {
		String uuid = playerUuid == null ? "" : playerUuid.trim();
		if (uuid.isEmpty()) {
			return SimpleResult.fail("player_uuid is required");
		}
		String body = "{\"player_uuid\":\"" + escapeJson(uuid) + "\"}";
		return postSimple("/skins/discord/link/unlink", body);
	}

	private static SimpleResult postSimple(String path, String jsonBody) {
		String base = Cache.skinsApiBaseUrl;
		String key = Cache.skinsPluginKey;
		if (base == null || base.isEmpty() || key == null || key.isEmpty()) {
			return SimpleResult.fail(
				"Skins API is not configured (skins-api.base-url / plugin-key in config.yml)."
			);
		}

		HttpURLConnection connection = null;
		try {
			@SuppressWarnings("deprecation")
			URL url = new URL(base + path);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(TIMEOUT_MS);
			connection.setReadTimeout(TIMEOUT_MS);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("X-Plugin-Key", key);
			connection.setRequestProperty("Accept", "application/json");

			byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
			connection.setFixedLengthStreamingMode(bytes.length);
			try (OutputStream out = connection.getOutputStream()) {
				out.write(bytes);
			}

			int status = connection.getResponseCode();
			String response = readBody(
				status >= 200 && status < 300
					? connection.getInputStream()
					: connection.getErrorStream()
			);

			if (status == 200) {
				return SimpleResult.success();
			}

			String detail = jsonString(response, "detail");
			if (detail == null || detail.isEmpty()) {
				detail = response == null || response.isEmpty()
					? ("HTTP " + status)
					: response;
			}
			if (status == 401) {
				return SimpleResult.fail("Unauthorized (check skins-api.plugin-key). " + detail);
			}
			return SimpleResult.fail(detail);
		} catch (Exception e) {
			return SimpleResult.fail("Could not reach skins API: " + e.getMessage());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private static CodeResult postForCode(String path, String jsonBody, String missingCodeMsg) {
		String base = Cache.skinsApiBaseUrl;
		String key = Cache.skinsPluginKey;
		if (base == null || base.isEmpty() || key == null || key.isEmpty()) {
			return CodeResult.fail(
				"Skins API is not configured (skins-api.base-url / plugin-key in config.yml)."
			);
		}

		HttpURLConnection connection = null;
		try {
			@SuppressWarnings("deprecation")
			URL url = new URL(base + path);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(TIMEOUT_MS);
			connection.setReadTimeout(TIMEOUT_MS);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("X-Plugin-Key", key);
			connection.setRequestProperty("Accept", "application/json");

			byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
			connection.setFixedLengthStreamingMode(bytes.length);
			try (OutputStream out = connection.getOutputStream()) {
				out.write(bytes);
			}

			int status = connection.getResponseCode();
			String response = readBody(
				status >= 200 && status < 300
					? connection.getInputStream()
					: connection.getErrorStream()
			);

			if (status == 200) {
				String code = jsonString(response, "code");
				String expires = jsonString(response, "expires_at");
				if (code == null || code.isEmpty()) {
					return CodeResult.fail(missingCodeMsg);
				}
				return CodeResult.success(code, expires);
			}

			String detail = jsonString(response, "detail");
			if (detail == null || detail.isEmpty()) {
				detail = response == null || response.isEmpty()
					? ("HTTP " + status)
					: response;
			}
			if (status == 401) {
				return CodeResult.fail("Unauthorized (check skins-api.plugin-key). " + detail);
			}
			return CodeResult.fail(detail);
		} catch (Exception e) {
			return CodeResult.fail("Could not reach skins API: " + e.getMessage());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private static String readBody(InputStream stream) throws Exception {
		if (stream == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		try (BufferedReader in = new BufferedReader(
			new InputStreamReader(stream, StandardCharsets.UTF_8)
		)) {
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
		}
		return sb.toString();
	}

	/** Extract a JSON string field value (simple, no nested objects). */
	static String jsonString(String json, String key) {
		if (json == null || key == null) {
			return null;
		}
		String needle = "\"" + key + "\"";
		int keyIdx = json.indexOf(needle);
		if (keyIdx < 0) {
			return null;
		}
		int colon = json.indexOf(':', keyIdx + needle.length());
		if (colon < 0) {
			return null;
		}
		int i = colon + 1;
		while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
			i++;
		}
		if (i >= json.length()) {
			return null;
		}
		char c = json.charAt(i);
		if (c == '"') {
			StringBuilder out = new StringBuilder();
			i++;
			while (i < json.length()) {
				char ch = json.charAt(i++);
				if (ch == '\\' && i < json.length()) {
					out.append(json.charAt(i++));
					continue;
				}
				if (ch == '"') {
					break;
				}
				out.append(ch);
			}
			return out.toString();
		}
		int start = i;
		while (i < json.length()) {
			char ch = json.charAt(i);
			if (ch == ',' || ch == '}' || ch == ']') {
				break;
			}
			i++;
		}
		return json.substring(start, i).trim();
	}

	static String escapeJson(String raw) {
		if (raw == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(raw.length() + 8);
		for (int i = 0; i < raw.length(); i++) {
			char ch = raw.charAt(i);
			switch (ch) {
				case '\\':
				case '"':
					sb.append('\\').append(ch);
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				default:
					sb.append(ch);
			}
		}
		return sb.toString();
	}
}
