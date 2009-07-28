package com.evancharlton.magnatune;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.evancharlton.magnatune.objects.Artist;

public class ArtistList extends LazyActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mFrom = new String[] {
				Artist.NAME,
				Artist.BIO
		};

		mTo = new int[] {
				android.R.id.text1,
				android.R.id.text2
		};

		super.onCreate(savedInstanceState, R.layout.list, android.R.layout.simple_list_item_2);
	}

	@Override
	public LoadTask newLoadTask() {
		return new LoadArtistsTask();
	}

	@Override
	protected void setTaskActivity() {
		mLoadTask.activity = this;
	}

	private static class LoadArtistsTask extends LoadTask {
		@SuppressWarnings("unchecked")
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				URL request = new URL(MagnatuneAPI.getFilterUrl(activity.mPage, "artists", null));
				String jsonRaw = MagnatuneAPI.getContent((InputStream) request.getContent());
				JSONArray artists = new JSONArray(jsonRaw);
				JSONObject artistObject;
				for (int i = 0; i < artists.length(); i++) {
					artistObject = artists.getJSONObject(i);
					HashMap<String, String> artistInfo = new HashMap<String, String>();
					artistInfo.put(Artist.ID, artistObject.getString("pk"));

					artistObject = artistObject.getJSONObject("fields");
					artistInfo.put(Artist.BIO, artistObject.getString("bio"));
					artistInfo.put(Artist.CITY, artistObject.getString("city"));
					artistInfo.put(Artist.STATE, artistObject.getString("state"));
					artistInfo.put(Artist.COUNTRY, artistObject.getString("country"));
					artistInfo.put(Artist.NAME, artistObject.getString("title"));
					publishProgress(artistInfo);
				}
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			return false;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> list, View row, int position, long id) {
		startActivityForPosition(ArtistBrowser.class, position);
	}
}
