package com.evancharlton.magnatune;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.evancharlton.magnatune.objects.Album;
import com.evancharlton.magnatune.objects.Artist;

public class AlbumList extends LazyActivity {
	protected String mGroup = "albums";
	protected String mFilter = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mFrom = new String[] {
				Album.TITLE,
				Album.ARTIST,
				Album.ARTWORK
		};

		mTo = new int[] {
				android.R.id.text1,
				android.R.id.text2,
				android.R.id.icon
		};

		Intent i = getIntent();
		String group = i.getStringExtra(MagnatuneAPI.EXTRA_GROUP);
		if (group != null) {
			mGroup = group;
		}

		String filter = i.getStringExtra(MagnatuneAPI.EXTRA_FILTER);
		if (filter != null) {
			mFilter = filter;
		}

		String title = i.getStringExtra(MagnatuneAPI.EXTRA_TITLE);
		if (title != null) {
			setTitle(title);
		}

		super.onCreate(savedInstanceState, R.layout.list, R.layout.album_row);
	}

	@Override
	public void onItemClick(AdapterView<?> list, View row, int position, long id) {
		startActivityForPosition(AlbumBrowser.class, position);
	}

	@Override
	protected LoadTask newLoadTask() {
		return new LoadAlbumsTask();
	}

	@Override
	protected void setTaskActivity() {
		mLoadTask.activity = this;
	}

	protected String getUrl() {
		return MagnatuneAPI.getFilterUrl(mGroup, mFilter);
	}

	protected static class LoadAlbumsTask extends LoadTask {
		@Override
		protected Boolean doInBackground(String... params) {
			AlbumList activity = (AlbumList) super.activity;
			mUrl = activity.getUrl();
			return loadUrl(mUrl);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!result) {
				activity.showDialog(DIALOG_ERROR_LOADING);
			}
		}
	}

	@Override
	protected HashMap<String, String> loadJSON(JSONObject albumObject) throws JSONException {
		HashMap<String, String> albumInfo = new HashMap<String, String>();
		albumInfo.put(Album.ID, albumObject.getString("pk"));

		albumObject = albumObject.getJSONObject("fields");
		String album = albumObject.getString("title");
		albumInfo.put(Album.TITLE, album);
		albumInfo.put(Artist.ID, albumObject.getString("artist"));
		String artist = albumObject.getString("artist_text");
		albumInfo.put(Album.ARTIST, artist);
		albumInfo.put(Album.SKU, albumObject.getString("sku"));
		albumInfo.put(Album.GENRE, albumObject.getString("genre_text"));
		albumInfo.put(Album.ARTWORK, MagnatuneAPI.getCoverArtUrl(artist, album, 50));
		return albumInfo;
	}
}
