package com.evancharlton.magnatune;

import java.util.List;
import java.util.Map;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.evancharlton.magnatune.views.RemoteImageView;

public class LazyAdapter extends SimpleAdapter {
	public static final String IMAGE = "LazyAdapter_image";

	private boolean mDone = false;
	private boolean mFlinging = false;
	private LazyActivity mActivity;

	public LazyAdapter(LazyActivity context, List<? extends Map<String, String>> data, int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
		mActivity = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// see if we need to load the next page
		if (!mDone && (getCount() - position) <= 1) {
			if (mActivity.isTaskFinished()) {
				mActivity.loadNextPage();
			}
		}

		View ret = super.getView(position, convertView, parent);
		if (ret != null) {
			RemoteImageView riv = (RemoteImageView) ret.findViewById(android.R.id.icon);
			if (riv != null && !mFlinging) {
				riv.loadImage();
			}
		}
		return ret;
	}

	public void setStopLoading(boolean done) {
		mDone = done;
	}

	public void setFlinging(boolean flinging) {
		mFlinging = flinging;
	}

	@Override
	public void setViewImage(final ImageView image, final String value) {
		if (value != null && value.length() > 0 && image instanceof RemoteImageView) {
			RemoteImageView riv = (RemoteImageView) image;
			riv.setLocalURI(MagnatuneAPI.getCacheFileName(value));
			riv.setRemoteURI(value);
			super.setViewImage(image, R.drawable.icon);
		} else {
			image.setVisibility(View.GONE);
		}
	}
}