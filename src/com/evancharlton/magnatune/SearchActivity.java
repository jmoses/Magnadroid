package com.evancharlton.magnatune;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.evancharlton.magnatune.objects.Album;
import com.evancharlton.magnatune.objects.Artist;
import com.evancharlton.magnatune.objects.SearchResult;

public class SearchActivity extends LazyActivity {
	private static final String TAG = "Magnatune_SearchActivity";

	private static final int MIN_LENGTH = 3;

	private EditText mQuery;
	private Button mSearch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mFrom = new String[] {
				SearchResult.TITLE,
				SearchResult.SUBTEXT,
				SearchResult.ICON_URL
		};
		mTo = new int[] {
				android.R.id.text1,
				android.R.id.text2,
				android.R.id.icon
		};
		super.onCreate(savedInstanceState, R.layout.search, R.layout.search_row);

		mAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				if (view instanceof ImageView) {
					if (textRepresentation.length() > 0) {
						view.setVisibility(View.VISIBLE);
					} else {
						view.setVisibility(View.GONE);
					}
					return true;
				}
				return false;
			}
		});

		mQuery = (EditText) findViewById(R.id.query);
		mSearch = (Button) findViewById(R.id.search);

		mSearch.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startSearch();
			}
		});

		mQuery.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mSearch.setEnabled(s != null && s.length() > MIN_LENGTH);
			}
		});

		mQuery.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER && mQuery.getText().length() > MIN_LENGTH) {
					startSearch();
					return true;
				}
				return false;
			}
		});
	}

	@Override
	protected LoadTask newLoadTask() {
		return new SearchTask();
	}

	@Override
	public void onItemClick(AdapterView<?> list, View row, int position, long id) {
		HashMap<String, String> info = mAdapterData.get(position);
		String model = info.get(SearchResult.MODEL);
		if (SearchResult.MODEL_ALBUM.equals(model)) {
			startActivityForPosition(AlbumBrowser.class, position);
		} else if (SearchResult.MODEL_ARTIST.equals(model)) {
			startActivityForPosition(ArtistBrowser.class, position);
		} else if (SearchResult.MODEL_SONG.equals(model)) {
			// TODO
		}
	}

	private void startSearch() {
		if (mLoadTask.getStatus() == AsyncTask.Status.RUNNING) {
			mLoadTask.cancel(true);
		}
		mLoadTask = newLoadTask();
		setTaskActivity();
		if (mLoadTask.getStatus() == AsyncTask.Status.PENDING) {
			mAdapterData.clear();
			mAdapter.notifyDataSetChanged();
			mLoadTask.execute(mQuery.getText().toString().trim());
		}
	}

	private static class SearchTask extends LoadTask {
		@SuppressWarnings("unchecked")
		@Override
		protected Boolean doInBackground(String... params) {
			if (params.length == 1 && params[0].length() > MIN_LENGTH) {
				String url = MagnatuneAPI.getFilterUrl(1, "search", params[0]);
				Log.d(TAG, "Searching: " + url);
				try {
					URL request = new URL(url);
					String jsonRaw = MagnatuneAPI.getContent((InputStream) request.getContent());
					JSONArray results = new JSONArray(jsonRaw);
					JSONObject resultObject;
					for (int i = 0; i < results.length(); i++) {
						if (mCancelled == true) {
							return true;
						}
						HashMap<String, String> resultInfo = new HashMap<String, String>();
						resultObject = results.getJSONObject(i);
						String id = resultObject.getString("pk");
						resultInfo.put(SearchResult.ID, id);
						String model = resultObject.getString("model");
						resultInfo.put(SearchResult.MODEL, model);

						resultObject = resultObject.getJSONObject("fields");
						String title = resultObject.getString("title");
						resultInfo.put(SearchResult.TITLE, title);
						if (SearchResult.MODEL_ALBUM.equals(model)) {
							resultInfo.put(Album.TITLE, title);
							resultInfo.put(Album.ARTIST, resultObject.getString("artist_text"));
							resultInfo.put(Album.ID, id);
							resultInfo.put(SearchResult.ICON_URL, "FIXME");
							resultInfo.put(SearchResult.SUBTEXT, String.format("%s (%s)", resultObject.getString("artist_text"), resultObject.getString("genre_text")));
						} else if (SearchResult.MODEL_ARTIST.equals(model)) {
							resultInfo.put(Artist.NAME, resultObject.getString("title"));
							resultInfo.put(Artist.BIO, resultObject.getString("bio"));
							resultInfo.put(Artist.ID, id);
							resultInfo.put(SearchResult.SUBTEXT, resultObject.getString("bio"));
						} else if (SearchResult.MODEL_SONG.equals(model)) {
							resultInfo.put(SearchResult.SUBTEXT, resultObject.getString("artist_text") + " - " + resultObject.getString("album_text"));
						}
						publishProgress(resultInfo);
					}
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return false;
			}
			return true;
		}
	}
}