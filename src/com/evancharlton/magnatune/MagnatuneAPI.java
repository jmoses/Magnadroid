package com.evancharlton.magnatune;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;

import android.graphics.drawable.Drawable;
import android.os.Environment;

public class MagnatuneAPI {
	public static final String API_KEY = "cd7e283004d6bd87df6ec7d975e39e116e4d1d8a";
	public static final String API_BASE = "http://mobileapi.magnatune.com/" + API_KEY;
	public static final String EXTRA_GROUP = "group";
	public static final String EXTRA_FILTER = "filter";
	public static final String EXTRA_TITLE = "title";

	private static final String CACHE_DIRECTORY = "/.magnatune-cache/";

	private MagnatuneAPI() {
	}

	public static String getFilterUrl(int page, String group, String filter) {
		String url;
		if (filter == null) {
			url = String.format(API_BASE + "/%s/", URLEncoder.encode(group));
		} else {
			url = String.format(API_BASE + "/%s/%s/", URLEncoder.encode(group), URLEncoder.encode(filter));
		}
		url = url.replaceAll("\\+", "%20");
		return url + "?page=" + String.valueOf(page);
	}

	public static String getCoverArtUrl(String artist, String album) {
		return getCoverArtUrl(artist, album, 50);
	}

	public static String getCoverArtUrl(String artist, String album, int size) {
		return String.format("http://he3.magnatune.com/music/%s/%s/cover_%d.jpg", artist, album, size).replaceAll("\\s", "%20");
	}

	public static String getContent(HttpResponse response) throws IllegalStateException, IOException {
		return getContent(response.getEntity().getContent());
	}

	public static String getContent(InputStream is) throws IOException {
		StringBuilder builder = new StringBuilder();
		BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = buffer.readLine()) != null) {
			builder.append(line).append("\n");
		}
		buffer.close();
		return builder.toString().trim();
	}

	public static String getMP3Url(String mp3) {
		return String.format("http://he3.magnatune.com/all/%s", mp3);
	}

	private static String md5(String msg) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.reset();
		md.update(msg.getBytes());
		byte[] digest = md.digest();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < digest.length; i++) {
			String hex = Integer.toHexString(0xFF & digest[i]);
			if (hex.length() == 1) {
				sb.append("0");
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public static String getCacheFileName(String url) {
		try {
			return Environment.getExternalStorageDirectory() + CACHE_DIRECTORY + md5(url) + ".jpg";
		} catch (NoSuchAlgorithmException e) {
			return Environment.getExternalStorageDirectory() + CACHE_DIRECTORY + url.replaceAll("[^a-zA-Z0-9]+", "") + ".jpg";
		}
	}

	public static Drawable getCachedCoverArt(String url) {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String pathName = getCacheFileName(url);
			File cached = new File(pathName);
			if (cached.exists()) {
				return Drawable.createFromPath(pathName);
			}
		}
		return null;
	}

	public static void cacheCoverArt(String url, InputStream is) {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			try {
				String pathName = getCacheFileName(url);
				File output = new File(pathName);
				output.getParentFile().mkdirs();
				FileOutputStream fos = new FileOutputStream(output);
				byte[] buffer = new byte[4096];
				int l;
				while ((l = is.read(buffer)) != -1) {
					fos.write(buffer, 0, l);
				}
				fos.flush();
				is.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
