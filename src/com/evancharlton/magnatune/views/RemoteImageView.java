package com.evancharlton.magnatune.views;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.evancharlton.magnatune.HTTPQueue;
import com.evancharlton.magnatune.HTTPThread;
import com.evancharlton.magnatune.R;

public class RemoteImageView extends ImageView {
	private static final String TAG = "RemoteImageView";

	private String mLocal;
	private String mRemote;

	public RemoteImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RemoteImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setLocalURI(String local) {
		mLocal = local;
	}

	public void setRemoteURI(String uri) {
		if (uri.startsWith("http")) {
			mRemote = uri;
		}
	}

	public void loadImage() {
		if (mRemote != null) {
			if (mLocal == null) {
				mLocal = Environment.getExternalStorageDirectory() + "/.remote-image-view-cache/" + mRemote.hashCode() + ".jpg";
			}
			// check for the local file here instead of in the thread because
			// otherwise previously-cached files wouldn't be loaded until after
			// the remote ones have been downloaded.
			File local = new File(mLocal);
			if (local.exists()) {
				setFromLocal();
			} else {
				Log.d(TAG, "Loading remote image");
				// we already have the local reference, so just make the parent
				// directories here instead of in the thread.
				local.getParentFile().mkdirs();
				HTTPQueue queue = HTTPQueue.getInstance();
				queue.enqueue(new HTTPThread(mRemote, mLocal, mHandler), HTTPQueue.PRIORITY_HIGH);
				setImageResource(R.drawable.icon);
			}
		}
	}

	private void setFromLocal() {
		Drawable d = Drawable.createFromPath(mLocal);
		if (d != null) {
			setImageDrawable(d);
		} else {
			// FIXME: Sometimes it can't load a file which it just downloaded
			// but if it scrolls off and comes back, it will load just fine (so
			// there isn't anything wrong with the drawable).
			Log.d(TAG, "Drawable is null! Using placeholder.");
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setFromLocal();
		}
	};
}
