package com.evancharlton.magnatune;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.evancharlton.magnatune.objects.Album;
import com.evancharlton.magnatune.objects.Artist;

public class ArtistBrowser extends LazyActivity {
	private String mArtistId = null;
	private TextView mNameText;
	private TextView mBioText;
	private TextView mLocationText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mFrom = new String[] {
				Album.TITLE,
				Album.GENRE
		};
		mTo = new int[] {
				android.R.id.text1,
				android.R.id.text2
		};

		Intent i = getIntent();
		mArtistId = i.getStringExtra(Artist.ID);

		super.onCreate(savedInstanceState, R.layout.artist_details, R.layout.album_row);

		mNameText = (TextView) findViewById(R.id.artist);
		mBioText = (TextView) findViewById(R.id.bio);
		mLocationText = (TextView) findViewById(R.id.location);

		String name = i.getStringExtra(Artist.NAME);
		mNameText.setText(name);
		setTitle(name);
		mBioText.setText(i.getStringExtra(Artist.BIO));
		String location = "";
		String city = i.getStringExtra(Artist.CITY);
		String country = i.getStringExtra(Artist.COUNTRY);
		String state = i.getStringExtra(Artist.STATE);

		location = country;
		if (state != null && state.length() > 0) {
			location = state + ", " + location;
		}
		if (city != null && city.length() > 0) {
			location = city + ", " + location;
		}

		mLocationText.setText(location);
	}

	@Override
	protected LoadTask newLoadTask() {
		return new LoadAlbumsTask();
	}

	@Override
	protected void setTaskActivity() {
		mLoadTask.activity = this;
	}

	@Override
	public void onItemClick(AdapterView<?> list, View row, int position, long id) {
		startActivityForPosition(AlbumBrowser.class, position);
	}

	protected static class LoadAlbumsTask extends LoadTask {
		@SuppressWarnings("unchecked")
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				URL request = new URL(MagnatuneAPI.getFilterUrl(mPage, "artists", ((ArtistBrowser) activity).mArtistId));
				String jsonRaw = MagnatuneAPI.getContent((InputStream) request.getContent());
				JSONArray albums = new JSONArray(jsonRaw);
				JSONObject albumObject;
				for (int i = 0; i < albums.length(); i++) {
					HashMap<String, String> albumInfo = new HashMap<String, String>();
					albumObject = albums.getJSONObject(i);
					albumInfo.put(Album.ID, albumObject.getString("pk"));

					albumObject = albumObject.getJSONObject("fields");
					String album = albumObject.getString("title");
					albumInfo.put(Album.TITLE, album);
					String artist = albumObject.getString("artist_text");
					albumInfo.put(Album.ARTIST, artist);
					albumInfo.put(Album.SKU, albumObject.getString("sku"));
					albumInfo.put(Album.GENRE, albumObject.getString("genre_text"));
					albumInfo.put(Album.ARTWORK, MagnatuneAPI.getCoverArtUrl(artist, album, 50));
					publishProgress(albumInfo);
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}
}
