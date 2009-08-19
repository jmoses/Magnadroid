package com.evancharlton.magnatune;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import android.graphics.drawable.Drawable;
import android.os.Environment;

public class MagnatuneAPI {
	public static final String API_KEY = "cd7e283004d6bd87df6ec7d975e39e116e4d1d8a";
	public static final String API_BASE = "http://mobileapi.magnatune.com/" + API_KEY;
	public static final String EXTRA_GROUP = "group";
	public static final String EXTRA_FILTER = "filter";
	public static final String EXTRA_TITLE = "title";

	public static final String MUSIC_DIRECTORY = Environment.getExternalStorageDirectory() + "/Magnatune/";
	public static final String CACHE_DIRECTORY = Environment.getExternalStorageDirectory() + "/.magnatune-cache/";

	private static StringBuilder builder = new StringBuilder();

	private MagnatuneAPI() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			new File(CACHE_DIRECTORY).mkdirs();
		}
	}

	public static String getFilterUrl(String group, String filter) {
		String url;
		if (filter == null) {
			url = String.format(API_BASE + "/%s/", URLEncoder.encode(group));
		} else {
			url = String.format(API_BASE + "/%s/%s/", URLEncoder.encode(group), URLEncoder.encode(filter));
		}
		url = url.replaceAll("\\+", "%20");
		return url;
	}

	public static String getCoverArtUrl(String artist, String album) {
		return getCoverArtUrl(artist, album, 50);
	}

	public static String getCoverArtUrl(String artist, String album, int size) {
		return String.format("http://he3.magnatune.com/music/%s/%s/cover_%d.jpg", artist, album, size).replaceAll("\\s", "%20");
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
		return String.format("http://he3.magnatune.com/all/%s", mp3.replace(".mp3", "-lofi.mp3"));
	}

	public static String getCacheFileName(String url) {
		builder.setLength(0);
		builder.append(CACHE_DIRECTORY);
		builder.append(url.hashCode()).append(".jpg");
		return builder.toString();
	}

	public static String getCachedCoverArt(String artist, String album, int size) {
		return getCacheFileName(getCoverArtUrl(artist, album, size));
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

	public static String getPurchaseUrl(String sku) {
		return String.format("https://magnatune.com/buy/buy_dl_pp?sku=%s", sku);
	}

	public static String getDownloadUrl(String email) {
		return String.format("http://magnatune.com/buy/redownload_xml?email=%s", email);
	}
}
