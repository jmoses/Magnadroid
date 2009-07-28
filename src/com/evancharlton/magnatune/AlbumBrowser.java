package com.evancharlton.magnatune;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.evancharlton.magnatune.objects.Album;
import com.evancharlton.magnatune.objects.Song;
import com.evancharlton.magnatune.views.RemoteImageView;
import com.evancharlton.magnatune.views.SongController;

public class AlbumBrowser extends LazyActivity {
	protected RemoteImageView mArtwork;
	protected TextView mAlbumTitle;
	protected TextView mArtistTitle;
	protected Button mPurchaseButton;

	protected String mAlbumId;
	protected String mArtist;
	protected String mAlbum;

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
		@SuppressWarnings("unchecked")
		@Override
		protected Boolean doInBackground(String... params) {
			AlbumBrowser activity = (AlbumBrowser) super.activity;
			if (activity.mAlbumId != null) {
				String url = MagnatuneAPI.getFilterUrl(1, "albums", activity.mAlbumId);
				try {
					URL request = new URL(url);
					String jsonRaw = MagnatuneAPI.getContent((InputStream) request.getContent());
					JSONArray songs = new JSONArray(jsonRaw);
					JSONObject songObject;
					for (int i = 0; i < songs.length(); i++) {
						songObject = songs.getJSONObject(i).getJSONObject("fields");
						HashMap<String, String> songInfo = new HashMap<String, String>();
						songInfo.put(Song.TITLE, songObject.getString("title"));
						// calculate the duration
						Long duration = songObject.getLong("duration");
						songInfo.put(Song.DURATION, String.valueOf(duration));
						songInfo.put(Song.DURATION_TEXT, activity.getDuration(duration));
						songInfo.put(Song.MP3, songObject.getString("mp3"));
						publishProgress(songInfo);
					}
					return true;
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return false;
		}

		@Override
		protected void onProgressUpdate(HashMap<String, String>... updates) {
			activity.mAdapterData.add(updates[0]);
			activity.mAdapter.notifyDataSetChanged();
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
