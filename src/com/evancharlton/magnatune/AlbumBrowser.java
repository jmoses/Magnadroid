package com.evancharlton.magnatune;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.evancharlton.magnatune.objects.Album;
import com.evancharlton.magnatune.objects.Artist;
import com.evancharlton.magnatune.objects.Song;
import com.evancharlton.magnatune.views.RemoteImageView;
import com.evancharlton.magnatune.views.SongController;

public class AlbumBrowser extends LazyActivity {
	protected RemoteImageView mArtwork;
	protected TextView mAlbumTitle;
	protected TextView mArtistTitle;
	protected Button mPurchaseButton;

	protected String mAlbumId;
	protected String mArtistId;
	protected String mArtist;
	protected String mAlbum;
	protected String mGenre;

	protected SongController mController;
	protected DecimalFormat mSecondsFormat = new DecimalFormat("00");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		onCreate(savedInstanceState, R.layout.album_details);
	}

	protected void onCreate(Bundle savedInstanceState, int layoutRes) {
		mFrom = new String[] {
				Song.TITLE,
				Song.DURATION_TEXT
		};
		mTo = new int[] {
				android.R.id.text1,
				android.R.id.text2
		};

		// start loading
		Intent intent = getIntent();
		if (mAlbum == null) {
			mAlbum = intent.getStringExtra(Album.TITLE);
		}
		if (mArtist == null) {
			mArtist = intent.getStringExtra(Album.ARTIST);
		}
		if (mAlbumId == null) {
			mAlbumId = intent.getStringExtra(Album.ID);
		}
		if (mArtistId == null) {
			mArtistId = intent.getStringExtra(Artist.ID);
		}
		if (mGenre == null) {
			mGenre = intent.getStringExtra(Album.GENRE);
		}

		super.onCreate(savedInstanceState, layoutRes, android.R.layout.simple_list_item_2);

		mArtwork = (RemoteImageView) findViewById(R.id.artwork);
		mArtistTitle = (TextView) findViewById(R.id.artist);
		mAlbumTitle = (TextView) findViewById(R.id.album);
		mPurchaseButton = (Button) findViewById(R.id.buy);

		mPurchaseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(MagnatuneAPI.getPurchaseUrl(getIntent().getStringExtra(Album.SKU))));
				startActivity(intent);
			}
		});

		mAlbumTitle.setText(mAlbum);
		mArtistTitle.setText(mArtist);

		String url = MagnatuneAPI.getCoverArtUrl(mArtist, mAlbum, 100);
		mArtwork.setLocalURI(MagnatuneAPI.getCacheFileName(url));
		mArtwork.setRemoteURI(url);
		mArtwork.loadImage();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mArtist != null) {
			menu.add(Menu.NONE, MENU_ARTIST, Menu.FIRST + 1, format(R.string.menu_more_by_artist, mArtist));
		}
		if (mGenre != null) {
			menu.add(Menu.NONE, MENU_GENRE, Menu.FIRST + 2, format(R.string.menu_more_by_genre, mGenre));
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ARTIST:
				startActivity(new Intent(this, ArtistBrowser.class).putExtra(Artist.ID, mArtistId));
				return true;
			case MENU_GENRE:
				startActivity(new Intent(this, AlbumList.class).putExtra(MagnatuneAPI.EXTRA_FILTER, mGenre).putExtra(MagnatuneAPI.EXTRA_GROUP, "genres"));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mController = (SongController) findViewById(R.id.controller);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mController != null) {
			mController.destroy();
			mController = null;
			System.gc();
		}
	}

	protected String getDuration(Long duration) {
		long minutes = (long) Math.floor(duration / 60);
		long seconds = duration % 60;
		return String.format("Duration: %s:%s", minutes, mSecondsFormat.format(seconds));
	}

	private static class LoadAlbumTask extends LoadTask {
		@Override
		protected Boolean doInBackground(String... params) {
			AlbumBrowser activity = (AlbumBrowser) super.activity;
			if (activity.mAlbumId != null) {
				return loadUrl(MagnatuneAPI.getFilterUrl("albums", activity.mAlbumId));
			}
			return false;
		}
	}

	@Override
	protected LoadTask newLoadTask() {
		return new LoadAlbumTask();
	}

	@Override
	public void onItemClick(AdapterView<?> list, View row, int position, long id) {
		HashMap<String, String> info = mAdapterData.get(position);
		String url = MagnatuneAPI.getMP3Url(info.get(Song.MP3));
		try {
			mController.autoPlay(url);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			showDialog(DIALOG_ERROR_LOADING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected HashMap<String, String> loadJSON(JSONObject json) throws JSONException {
		json = json.getJSONObject("fields");
		HashMap<String, String> songInfo = new HashMap<String, String>();
		songInfo.put(Song.TITLE, json.getString("title"));
		// calculate the duration
		Long duration = json.getLong("duration");
		songInfo.put(Song.DURATION, String.valueOf(duration));
		songInfo.put(Song.DURATION_TEXT, getDuration(duration));
		songInfo.put(Song.MP3, json.getString("mp3"));
		return songInfo;
	}
}
