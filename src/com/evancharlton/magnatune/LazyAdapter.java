package com.evancharlton.magnatune;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.evancharlton.magnatune.views.RemoteImageView;

public class LazyAdapter extends SimpleAdapter implements Filterable {
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

	@Override
	public Filter getFilter() {
		return new ListFilter(mActivity);
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

	private static class ListFilter extends Filter {
		protected LazyActivity mActivity;

		public ListFilter(LazyActivity activity) {
			mActivity = activity;
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			mActivity.mPage = 1;
			mActivity.mFilter = constraint.toString();
			mActivity.mAdapter.setStopLoading(false);

			ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
			Uri uri = mActivity.mLoadTask.getLoadedURL();
			Uri.Builder builder = uri.buildUpon();
			builder.appendQueryParameter("page", "1");
			builder.appendQueryParameter("filter", constraint.toString());
			uri = builder.build();

			try {
				URL request = new URL(uri.toString());
				String jsonRaw = MagnatuneAPI.getContent((InputStream) request.getContent());
				JSONArray collection = new JSONArray(jsonRaw);
				for (int i = 0; i < collection.length(); i++) {
					try {
						HashMap<String, String> parsed = mActivity.loadJSON(collection.getJSONObject(i));
						if (parsed != null) {
							results.add(parsed);
						}
					} catch (JSONException e) {
					}
				}
			} catch (Exception e) {
			}

			FilterResults filterResults = new FilterResults();
			filterResults.values = results;
			filterResults.count = results.size();
			return filterResults;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			if (results.count > 0) {
				mActivity.mAdapterData.clear();
				mActivity.mAdapterData.addAll((ArrayList<HashMap<String, String>>) results.values);
				mActivity.mAdapter.notifyDataSetChanged();
			} else {
				mActivity.mList.setFilterText("");
			}
		}
	}
}