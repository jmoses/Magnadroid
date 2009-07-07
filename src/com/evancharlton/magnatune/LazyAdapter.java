package com.evancharlton.magnatune;

import java.util.List;
import java.util.Map;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

public class LazyAdapter extends SimpleAdapter {
	private static final String TAG = "Magnatune_LazyAdapter";

	private boolean mDone = false;
	private LazyActivity mActivity;

	public LazyAdapter(LazyActivity context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
		mActivity = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// see if we need to load the next page
		if (!mDone && getCount() - position < 15) {
			if (mActivity.isTaskFinished()) {
				Log.d(TAG, "Loading the next page");
				mActivity.loadNextPage();
			}
		}

		return super.getView(position, convertView, parent);
	}

	public void setStopLoading(boolean done) {
		mDone = done;
	}
}