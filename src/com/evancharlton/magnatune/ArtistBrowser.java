package com.evancharlton.magnatune;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.evancharlton.magnatune.objects.Album;
import com.evancharlton.magnatune.objects.Artist;
import com.evancharlton.magnatune.objects.Model;

public class ArtistBrowser extends LazyActivity {
	private String mArtistId = null;
	private TextView mNameText;
	private TextView mBioText;
	private TextView mLocationText;

	private String mName;
	private String mBio;
	private String mCity;
	private String mCountry;
	private String mState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mFrom = new String[] {
				Album.TITLE,
				Album.GENRE,
				Album.ARTWORK
		};
		mTo = new int[] {
				android.R.id.text1,
				android.R.id.text2,
				android.R.id.icon
		};

		Intent i = getIntent();
		mArtistId = i.getStringExtra(Artist.ID);

		super.onCreate(savedInstanceState, R.layout.artist_details, R.layout.album_row);

		mNameText = (TextView) findViewById(R.id.artist);
		mBioText = (TextView) findViewById(R.id.bio);
		mLocationText = (TextView) findViewById(R.id.location);

		mName = i.getStringExtra(Artist.NAME);
		mBio = i.getStringExtra(Artist.BIO);
		mCity = i.getStringExtra(Artist.CITY);
		mCountry = i.getStringExtra(Artist.COUNTRY);
		mState = i.getStringExtra(Artist.STATE);

		if (mName == null) {
			mName = "Loading ...";
			mBio = "Loading artist, please wait ...";
		}

		setDetails();
	}

	private void setDetails() {
		mNameText.setText(mName);
		setTitle(mName);
		mBioText.setText(mBio);
		String location = "";

		location = mCountry;
		if (mState != null && mState.length() > 0) {
			location = mState + ", " + location;
		}
		if (mCity != null && mCity.length() > 0) {
			location = mCity + ", " + location;
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
		@Override
		protected Boolean doInBackground(String... params) {
			return loadUrl(MagnatuneAPI.getFilterUrl("artists", ((ArtistBrowser) activity).mArtistId));
		}

		@Override
		protected void onProgressUpdate(HashMap<String, String>... updates) {
			String model = updates[0].get(Model.TYPE);
			if (model == null || Album.MODEL.equals(model)) {
				super.onProgressUpdate(updates);
			} else {
				ArtistBrowser activity = (ArtistBrowser) super.activity;
				activity.mName = updates[0].get(Artist.NAME);
				activity.mBio = updates[0].get(Artist.BIO);
				activity.mCity = updates[0].get(Artist.CITY);
				activity.mCountry = updates[0].get(Artist.COUNTRY);
				activity.mState = updates[0].get(Artist.STATE);
				activity.setDetails();
			}
		}
	}

	@Override
	protected HashMap<String, String> loadJSON(JSONObject albumObject) throws JSONException {
		HashMap<String, String> albumInfo = new HashMap<String, String>();
		String type = albumObject.getString("model");
		albumInfo.put(Model.TYPE, type);
		if (Artist.MODEL.equals(type)) {
			albumObject = albumObject.getJSONObject("fields");
			albumInfo.put(Artist.NAME, albumObject.getString("title"));
			albumInfo.put(Artist.BIO, albumObject.getString("bio"));
			albumInfo.put(Artist.CITY, albumObject.getString("city"));
			albumInfo.put(Artist.COUNTRY, albumObject.getString("country"));
			albumInfo.put(Artist.STATE, albumObject.getString("state"));
		} else {
			albumInfo.put(Album.ID, albumObject.getString("pk"));
			albumObject = albumObject.getJSONObject("fields");
			String album = albumObject.getString("title");
			albumInfo.put(Album.TITLE, album);
			String artist = albumObject.getString("artist_text");
			albumInfo.put(Album.ARTIST, artist);
			albumInfo.put(Album.SKU, albumObject.getString("sku"));
			albumInfo.put(Album.GENRE, albumObject.getString("genre_text"));
			albumInfo.put(Album.ARTWORK, MagnatuneAPI.getCoverArtUrl(artist, album, 50));
		}
		return albumInfo;
	}
}
