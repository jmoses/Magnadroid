package com.evancharlton.magnatune.objects;

import java.util.ArrayList;
import java.util.HashMap;

public class Download {
	public static final String USERNAME = "download_username";
	public static final String PASSWORD = "download_password";
	public static final String PAGE = "download_page";
	public static final String ARTIST = "download_artist";
	public static final String ALBUM = "download_album";
	public static final String ARTWORK = "download_artwork";

	public static final ArrayList<HashMap<String, String>> FORMATS = new ArrayList<HashMap<String, String>>();

	static {
		HashMap<String, String> wav = new HashMap<String, String>();
		HashMap<String, String> mp3 = new HashMap<String, String>();
		HashMap<String, String> ogg = new HashMap<String, String>();
		HashMap<String, String> vbr = new HashMap<String, String>();
		HashMap<String, String> aac = new HashMap<String, String>();
		HashMap<String, String> flac = new HashMap<String, String>();

		wav.put(Format.TYPE, Format.WAV);
		wav.put(Format.TEXT, Format.WAV_TEXT);

		mp3.put(Format.TYPE, Format.MP3);
		mp3.put(Format.TEXT, Format.MP3_TEXT);

		ogg.put(Format.TYPE, Format.OGG);
		ogg.put(Format.TEXT, Format.OGG_TEXT);

		vbr.put(Format.TYPE, Format.VBR);
		vbr.put(Format.TEXT, Format.VBR_TEXT);

		aac.put(Format.TYPE, Format.AAC);
		aac.put(Format.TEXT, Format.AAC_TEXT);

		flac.put(Format.TYPE, Format.FLAC);
		flac.put(Format.TEXT, Format.FLAC_TEXT);

		FORMATS.add(vbr);
		FORMATS.add(mp3);
		FORMATS.add(flac);
		FORMATS.add(ogg);
		FORMATS.add(aac);
		FORMATS.add(wav);
	}

	public class Format {
		public static final String WAV = "download_format_wav";
		public static final String MP3 = "download_format_mp3";
		public static final String OGG = "download_format_ogg";
		public static final String VBR = "download_format_vbr";
		public static final String AAC = "download_format_aac";
		public static final String FLAC = "download_format_flac";

		public static final String TEXT = "download_format_text";
		public static final String TYPE = "download_format_type";
		public static final String URL = "download_format_url";

		public static final String WAV_TEXT = "Waveform Audio";
		public static final String MP3_TEXT = "MP3";
		public static final String OGG_TEXT = "Ogg";
		public static final String VBR_TEXT = "Variable bitrate MP3";
		public static final String AAC_TEXT = "Advanced Audio Coding";
		public static final String FLAC_TEXT = "FLAC";
	}
}
