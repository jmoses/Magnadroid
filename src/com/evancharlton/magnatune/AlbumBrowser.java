package com.evancharlton.magnatune;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.evancharlton.magnatune.objects.Album;
import com.evancharlton.magnatune.objects.Song;

public class AlbumBrowser extends Activity {
	private static final String TAG = "Magnatune_AlbumBrowser";

	private static final int DIALOG_BUFFERING = 10;

	private LoadAlbumTask mLoadTask;
	private LoadArtTask mArtTask;

	private ImageView mArtwork;
	private TextView mAlbumTitle;
	private TextView mArtistTitle;
	private ListView mSongList;
	private SimpleAdapter mSongAdapter;
	private List<HashMap<String, String>> mSongData = new ArrayList<HashMap<String, String>>();
	private String mArtist;
	private String mAlbum;

	private MediaController mController;
	private SongControl mSongControl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.album_details);

		String[] from = new String[] {
				Song.TITLE,
				Song.DURATION_TEXT
		};
		int[] to = new int[] {
				android.R.id.text1,
				android.R.id.text2
		};

		mArtwork = (ImageView) findViewById(R.id.artwork);
		mSongList = (ListView) findViewById(android.R.id.list);
		mArtistTitle = (TextView) findViewById(R.id.artist);
		mAlbumTitle = (TextView) findViewById(R.id.album);

		mSongAdapter = new SimpleAdapter(this, mSongData, android.R.layout.simple_list_item_2, from, to);
		mSongList.setAdapter(mSongAdapter);
		mSongList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> list, View row, int position, long id) {
				HashMap<String, String> info = mSongData.get(position);
				String url = MagnatuneAPI.getMP3Url(info.get(Song.MP3));
				if (mSongControl != null && url.equals(mSongControl.getUrl())) {
					mController.show(0);
				} else {
					if (mController != null) {
						// tear down the old
						mSongControl.release();
					}
					mController = new MediaController(AlbumBrowser.this);
					mController.setAnchorView(list);
					mController.setMinimumWidth(list.getWidth());
					mController.setMinimumHeight(list.getHeight());
					mSongControl = new SongControl(url, info.get(Song.TITLE));
					mController.setMediaPlayer(mSongControl);
					showDialog(DIALOG_BUFFERING);
				}
			}
		});

		// start loading
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			String albumId = extras.getString(Album.ID);
			mAlbum = extras.getString(Album.TITLE);
			mArtist = extras.getString(Album.ARTIST);

			mAlbumTitle.setText(mAlbum);
			mArtistTitle.setText(mArtist);
			setTitle(mAlbum);

			if (albumId != null) {
				// load the artwork
				mArtTask = new LoadArtTask();
				mArtTask.activity = this;
				mArtTask.execute(mArtist, mAlbum);

				// load the tracks
				mLoadTask = new LoadAlbumTask();
				mLoadTask.activity = this;
				mLoadTask.execute(albumId);
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int which) {
		switch (which) {
			case DIALOG_BUFFERING:
				ProgressDialog buffering = new ProgressDialog(this);
				buffering.setMessage(getString(R.string.buffering));
				buffering.setIndeterminate(true);
				buffering.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						removeDialog(DIALOG_BUFFERING);
						mSongControl.release();
					}
				});
				return buffering;
		}
		return super.onCreateDialog(which);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mSongControl != null) {
			mSongControl.release();
		}
	}

	private static class LoadAlbumTask extends AsyncTask<String, HashMap<String, String>, Boolean> {
		public AlbumBrowser activity;
		private DecimalFormat mSecondsFormat;

		@Override
		protected void onPreExecute() {
			activity.setProgressBarIndeterminateVisibility(true);
			mSecondsFormat = new DecimalFormat("00");
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Boolean doInBackground(String... params) {
			String url = MagnatuneAPI.getFilterUrl(1, "albums", params[0]);
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
					long minutes = (long) Math.floor(duration / 60);
					long seconds = duration % 60;
					songInfo.put(Song.DURATION, String.valueOf(duration));
					songInfo.put(Song.DURATION_TEXT, String.format("Duration: %d:%s", minutes, mSecondsFormat.format(seconds)));
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
			return false;
		}

		@Override
		protected void onProgressUpdate(HashMap<String, String>... updates) {
			activity.mSongData.add(updates[0]);
			activity.mSongAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			activity.setProgressBarIndeterminateVisibility(false);
		}
	}

	private static class LoadArtTask extends AsyncTask<String, Integer, Drawable> {
		public AlbumBrowser activity;

		@Override
		protected Drawable doInBackground(String... params) {
			String url = MagnatuneAPI.getCoverArtUrl(params[0], params[1], 100);
			String cached = MagnatuneAPI.getCacheFileName(url);
			File img = new File(cached);
			if (!img.exists()) {
				try {
					Log.d(TAG, "Loading cover art: " + url);
					URL request = new URL(url);
					MagnatuneAPI.cacheCoverArt(url, (InputStream) request.getContent());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Log.d(TAG, "Loading cached image: " + cached);
			return Drawable.createFromPath(cached);
		}

		@Override
		protected void onPostExecute(Drawable result) {
			if (result != null) {
				Log.d(TAG, "Loaded cover art!");
				activity.mArtwork.setImageDrawable(result);
			} else {
				Log.d(TAG, "Could not load into Drawable");
			}
		}
	}

	private class SongControl implements MediaController.MediaPlayerControl, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
		private MediaPlayer mPlayer;
		private int mBufferPercent = 0;
		private String mUrl;
		private String mTitle;

		public SongControl(String url, String title) {
			mUrl = url;
			mTitle = title;
			mPlayer = new MediaPlayer();
			try {
				Log.d(TAG, "Buffering preview MP3: " + url);
				mPlayer.setDataSource(url);
				mPlayer.prepareAsync();
				mPlayer.setOnBufferingUpdateListener(this);
				mPlayer.setOnPreparedListener(this);
				mPlayer.setOnCompletionListener(this);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public String getUrl() {
			return mUrl;
		}

		@Override
		public int getBufferPercentage() {
			return mBufferPercent;
		}

		@Override
		public int getCurrentPosition() {
			return mPlayer.getCurrentPosition();
		}

		@Override
		public int getDuration() {
			return mPlayer.getDuration();
		}

		@Override
		public boolean isPlaying() {
			return mPlayer.isPlaying();
		}

		@Override
		public void pause() {
			mPlayer.pause();
		}

		@Override
		public void seekTo(int pos) {
			mPlayer.seekTo(pos);
		}

		@Override
		public void start() {
			mPlayer.start();
		}

		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			mBufferPercent = percent;
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			removeDialog(DIALOG_BUFFERING);
			mController.show(0);
			mController.invalidate();
			mPlayer.start();
			setTitle(getString(R.string.currently_playing) + " " + mTitle);
		}

		public void release() {
			mPlayer.release();
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			setTitle(mAlbum);
			mController.hide();
		}
	}
}
