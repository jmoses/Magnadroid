package com.evancharlton.magnatune;

import java.util.HashMap;

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
		@Override
		protected Boolean doInBackground(String... params) {
			return loadUrl(MagnatuneAPI.getFilterUrl("artists", null));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> list, View row, int position, long id) {
		startActivityForPosition(ArtistBrowser.class, position);
	}

	@Override
	protected HashMap<String, String> loadJSON(JSONObject artistObject) throws JSONException {
		HashMap<String, String> artistInfo = new HashMap<String, String>();
		artistInfo.put(Artist.ID, artistObject.getString("pk"));

		artistObject = artistObject.getJSONObject("fields");
		artistInfo.put(Artist.BIO, artistObject.getString("bio"));
		artistInfo.put(Artist.CITY, artistObject.getString("city"));
		artistInfo.put(Artist.STATE, artistObject.getString("state"));
		artistInfo.put(Artist.COUNTRY, artistObject.getString("country"));
		artistInfo.put(Artist.NAME, artistObject.getString("title"));
		return artistInfo;
	}
}
