package com.evancharlton.magnatune;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

import com.evancharlton.magnatune.objects.Genre;

public class GenreList extends ListActivity {

	private List<HashMap<String, String>> mAdapterData;
	private SimpleAdapter mAdapter;

	private LoadGenresTask mLoadTask;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		mAdapterData = new ArrayList<HashMap<String, String>>();

		String[] from = new String[] {
			Genre.TITLE
		};

		int[] to = new int[] {
			android.R.id.text1
		};

		mAdapter = new SimpleAdapter(this, mAdapterData, android.R.layout.simple_list_item_1, from, to);
		getListView().setAdapter(mAdapter);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> list, View row, int position, long id) {
				HashMap<String, String> info = mAdapterData.get(position);
				if (info != null) {
					Intent i = new Intent(GenreList.this, AlbumList.class);
					i.putExtra(MagnatuneAPI.EXTRA_GROUP, "genres");
					i.putExtra(MagnatuneAPI.EXTRA_FILTER, info.get(Genre.ID));
					i.putExtra(MagnatuneAPI.EXTRA_TITLE, String.format("Magnatune - %s albums", info.get(Genre.TITLE)));
					startActivity(i);
				}
			}
		});

		// restore state
		Object[] saved = (Object[]) getLastNonConfigurationInstance();
		if (saved != null) {
			mAdapterData.addAll((ArrayList<HashMap<String, String>>) saved[0]);
			mAdapter.notifyDataSetChanged();

			mLoadTask = (LoadGenresTask) saved[1];
		}
		if (mLoadTask == null) {
			mLoadTask = new LoadGenresTask();
		}
		mLoadTask.activity = this;
		if (mLoadTask.getStatus() == AsyncTask.Status.PENDING) {
			mLoadTask.execute();
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return new Object[] {
				mAdapterData,
				mLoadTask
		};
	}

	private static class LoadGenresTask extends AsyncTask<String, HashMap<String, String>, Boolean> {
		public GenreList activity;

		@Override
		protected void onPreExecute() {
			activity.setProgressBarIndeterminateVisibility(true);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				URL request = new URL(MagnatuneAPI.getFilterUrl(1, "genres", null));
				String jsonRaw = MagnatuneAPI.getContent((InputStream) request.getContent());
				JSONArray genres = new JSONArray(jsonRaw);
				JSONObject genreObject;
				for (int i = 0; i < genres.length(); i++) {
					genreObject = genres.getJSONObject(i);
					HashMap<String, String> genreInfo = new HashMap<String, String>();
					genreInfo.put(Genre.ID, genreObject.getString("pk"));

					genreObject = genreObject.getJSONObject("fields");
					genreInfo.put(Genre.TITLE, genreObject.getString("title"));
					publishProgress(genreInfo);
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
			activity.mAdapterData.add(updates[0]);
			activity.mAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			activity.setProgressBarIndeterminateVisibility(false);
		}
	}
}
