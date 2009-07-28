package com.evancharlton.magnatune;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.evancharlton.magnatune.objects.Download;

public class DownloadList extends LazyActivity {
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
			if (email != null) {
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
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				}
				return false;
			}
			return false;
		}
	}
}
