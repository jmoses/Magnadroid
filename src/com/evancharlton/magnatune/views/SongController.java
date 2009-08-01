package com.evancharlton.magnatune.views;

import java.io.FileDescriptor;
import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.evancharlton.magnatune.R;

public class SongController extends RelativeLayout {
	private static final String TAG = "SongController";

	private ToggleImageButton mPlayPauseBtn;
	private SeekBar mSeekBar;

	private MediaPlayer mMediaPlayer = new MediaPlayer();
	private MediaHandler mMediaHandler = new MediaHandler();
	private SeekThread mSeekThread = new SeekThread();

	private PowerManager mPowerManager;
	private WakeLock mWakeLock;

	public SongController(Context context) {
		this(context, null);
	}

	public SongController(Context context, AttributeSet attrs) {
		super(context, attrs);

		// initialize the UI
		LayoutInflater.from(context).inflate(R.layout.song_controller, this);

		mSeekBar = (SeekBar) findViewById(R.id.seekbar);
		mPlayPauseBtn = (ToggleImageButton) findViewById(R.id.playpause);

		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					mMediaPlayer.seekTo(progress);
					mMediaPlayer.start();
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		mPlayPauseBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					pause();
				} else {
					play();
				}
			}
		});
		mPlayPauseBtn.setEnabled(false);
		setVisibility(View.GONE);

		mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void finalize() throws Throwable {
		freeWakeLock();
		mMediaPlayer.release();
		super.finalize();
	}

	public void autoPlay(String url) throws IllegalArgumentException, IllegalStateException, IOException {
		setVisibility(View.VISIBLE);

		mSeekThread.cancel();
		mSeekThread = new SeekThread();

		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnBufferingUpdateListener(mMediaHandler);
		mMediaPlayer.setOnCompletionListener(mMediaHandler);
		mMediaPlayer.setOnPreparedListener(mMediaHandler);
		mMediaPlayer.setOnSeekCompleteListener(mMediaHandler);
		mMediaPlayer.setDataSource(url);
		mMediaPlayer.prepareAsync();

		mSeekBar.setMax(100);
		mSeekBar.setProgress(0);
		mSeekBar.setSecondaryProgress(0);

		setVisibility(View.VISIBLE);
	}

	public void destroy() {
		freeWakeLock();
		try {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
		} catch (IllegalStateException e) {
			// noop
		}
		mMediaPlayer.release();
		mSeekThread.cancel();
	}

	public void release() {
		freeWakeLock();
		try {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.setDataSource((FileDescriptor) null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void freeWakeLock() {
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

	protected void play() {
		mMediaPlayer.start();
		mPlayPauseBtn.setEnabled(true);
	}

	protected void pause() {
		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}
	}

	private class MediaHandler implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener {
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			percent = percent * (mSeekBar.getMax() / 100);
			mSeekBar.setSecondaryProgress(percent);
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			// we don't want to lose the wakelock right now because then the
			// user has to unlock their device again. The wakelock has
			// ON_AFTER_RELEASE to fix that usability nuisance.
			setVisibility(View.GONE);
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			play();
			mSeekBar.setMax(mp.getDuration());
			freeWakeLock();
			mWakeLock.acquire(mp.getDuration());
			mSeekThread.start();
		}

		@Override
		public void onSeekComplete(MediaPlayer mp) {
		}
	}

	private class SeekThread extends Thread {
		private boolean mCancelled = false;

		public void run() {
			while (!mCancelled) {
				try {
					mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					break;
				}
			}
		}

		public void cancel() {
			mCancelled = true;
		}
	};
}
