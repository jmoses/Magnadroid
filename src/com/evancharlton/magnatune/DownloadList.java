package com.evancharlton.magnatune;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.evancharlton.magnatune.objects.Download;

public class DownloadList extends LazyActivity {
	private static final int DIALOG_NO_EMAIL = 10;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mFrom = new String[] {
				Download.ARTIST,
				Download.ALBUM,
				Download.ARTWORK
		};
		mTo = new int[] {
				android.R.id.text1,
				android.R.id.text2,
				android.R.id.icon
		};
		setMultipage(false);
		super.onCreate(savedInstanceState, R.layout.list, R.layout.album_row);
		SharedPreferences prefs = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);
		String email = prefs.getString(getString(R.string.pref_email_address), null);
		if (email == null || email.length() == 0) {
			showDialog(DIALOG_NO_EMAIL);
		}
	}

	@Override
	protected Dialog onCreateDialog(int which) {
		switch (which) {
			case DIALOG_NO_EMAIL:
				return new AlertDialog.Builder(this).setPositiveButton(R.string.set_email_address, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(DIALOG_NO_EMAIL);
						startActivity(new Intent(DownloadList.this, Settings.class));
						finish();
					}
				}).setNegativeButton(R.string.continue_msg, null).setTitle(R.string.warning_no_email_set).setMessage(R.string.warning_no_email_set_message).create();
		}
		return super.onCreateDialog(which);
	}

	@Override
	protected LoadTask newLoadTask() {
		return new LoadDownloadsTask();
	}

	@Override
	public void onItemClick(AdapterView<?> list, View row, int position, long id) {
		startActivityForPosition(DownloadAlbum.class, position);
	}

	private static class LoadDownloadsTask extends LoadTask {
		private static final HashMap<String, String> NODES = new HashMap<String, String>();
		static {
			NODES.put("artist", Download.ARTIST);
			NODES.put("album", Download.ALBUM);
			NODES.put("username", Download.USERNAME);
			NODES.put("password", Download.PASSWORD);
			NODES.put("page", Download.PAGE);
			NODES.put("wav_zip", Download.Format.WAV);
			NODES.put("mp3_zip", Download.Format.MP3);
			NODES.put("ogg_zip", Download.Format.OGG);
			NODES.put("vbr_zip", Download.Format.VBR);
			NODES.put("aac_zip", Download.Format.AAC);
			NODES.put("flac_zip", Download.Format.FLAC);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Boolean doInBackground(String... params) {
			DownloadList activity = (DownloadList) super.activity;
			SharedPreferences prefs = activity.getSharedPreferences(activity.getString(R.string.preferences), Context.MODE_PRIVATE);
			String email = prefs.getString(activity.getString(R.string.pref_email_address), null);
			if (email != null && email.length() > 0) {
				try {
					URL url = new URL(MagnatuneAPI.getDownloadUrl(email));
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document document = builder.parse((InputStream) url.getContent());
					NodeList list = document.getElementsByTagName("download");
					Node node;
					Node child;
					Node grandchild;
					String nodeName;
					String value;
					for (int i = 0; i < list.getLength(); i++) {
						HashMap<String, String> row = new HashMap<String, String>();
						node = list.item(i);
						child = node.getFirstChild();
						while (child != null) {
							nodeName = child.getNodeName();
							value = NODES.get(nodeName);
							if (value != null) {
								row.put(value, child.getFirstChild().getNodeValue());
							} else if (nodeName.equals("formats")) {
								grandchild = child.getFirstChild();
								while (grandchild != null) {
									nodeName = grandchild.getNodeName();
									value = NODES.get(nodeName);
									if (value != null) {
										row.put(value, grandchild.getFirstChild().getNodeValue());
									}
									grandchild = grandchild.getNextSibling();
								}
							}
							child = child.getNextSibling();
						}
						row.put(Download.ARTWORK, MagnatuneAPI.getCoverArtUrl(row.get(Download.ARTIST), row.get(Download.ALBUM), 50));
						publishProgress(row);
					}
					return true;
				} catch (Exception e) {
					activity.setException(e);
				}
				return false;
			}
			return false;
		}
	}

	@Override
	protected HashMap<String, String> loadJSON(JSONObject json) throws JSONException {
		return null;
	}
}
