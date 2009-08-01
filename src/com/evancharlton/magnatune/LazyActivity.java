package com.evancharlton.magnatune;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.Window;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public abstract class LazyActivity extends Activity implements OnItemClickListener {
	protected static final int DIALOG_ERROR_LOADING = 10;

	protected static final int MENU_ARTIST = Menu.FIRST;
	protected static final int MENU_GENRE = Menu.FIRST + 1;

	protected ListView mList;
	protected LazyAdapter mAdapter;
	protected List<HashMap<String, String>> mAdapterData = new ArrayList<HashMap<String, String>>();
	protected String[] mFrom;
	protected int[] mTo;
	private boolean mMultipage = true;

	protected LoadTask mLoadTask;
	protected LoadQueue mScheduler = new LoadQueue();

	protected int mPage = 1;

	protected void onCreate(Bundle savedInstanceState, int layoutResId) {
		onCreate(savedInstanceState, layoutResId, android.R.layout.simple_list_item_1);
	}

	protected void onCreate(Bundle savedInstanceState, int layoutResId, int rowLayoutResId) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(layoutResId);

		mList = (ListView) findViewById(android.R.id.list);
		mAdapter = new LazyAdapter(this, mAdapterData, rowLayoutResId, mFrom, mTo);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);
		mList.setFastScrollEnabled(true);

		restoreState();

		if (mLoadTask == null) {
			mLoadTask = newLoadTask();
		}
		setTaskActivity();

		if (mLoadTask != null && mLoadTask.getStatus() == AsyncTask.Status.PENDING) {
			mLoadTask.execute();
		}
	}

	protected String format(int resId, Object... args) {
		return String.format(getString(resId, args));
	}

	@SuppressWarnings("unchecked")
	protected void restoreState() {
		// restore state
		Object[] saved = (Object[]) getLastNonConfigurationInstance();
		if (saved != null) {
			mAdapterData.addAll((Collection<? extends HashMap<String, String>>) saved[0]);
			mAdapter.notifyDataSetChanged();
			mLoadTask = (LoadTask) saved[1];
		}
	}

	@Override
	protected Dialog onCreateDialog(int which) {
		switch (which) {
			case DIALOG_ERROR_LOADING:
				return new AlertDialog.Builder(this).setTitle(R.string.error_loading_title).setMessage(R.string.error_loading_message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(DIALOG_ERROR_LOADING);
					}
				}).create();
		}
		return super.onCreateDialog(which);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return new Object[] {
				mAdapterData,
				mLoadTask
		};
	}

	final protected void setMultipage(boolean isMultipage) {
		mMultipage = isMultipage;
	}

	final protected boolean isMultipage() {
		return mMultipage;
	}

	final public void loadNextPage() {
		if (mMultipage) {
			mPage++;
			mLoadTask = newLoadTask();
			setTaskActivity();
			mLoadTask.execute();
		}
	}

	final public boolean isTaskFinished() {
		return mLoadTask.getStatus() == AsyncTask.Status.FINISHED;
	}

	abstract protected LoadTask newLoadTask();

	protected void setTaskActivity() {
		mLoadTask.activity = this;
	}

	protected void startActivityForPosition(Class<?> targetCls, int position) {
		HashMap<String, String> info = mAdapterData.get(position);
		Intent i = new Intent(this, targetCls);
		for (String key : info.keySet()) {
			i.putExtra(key, info.get(key));
		}
		startActivity(i);
	}

	protected class LoadQueue {
		public static final int PRIORITY_LOW = 0;
		public static final int PRIORITY_HIGH = 1;

		private ArrayList<LoadImageTask> mQueue = new ArrayList<LoadImageTask>();

		public void enqueue(LoadImageTask task) {
			enqueue(task, PRIORITY_LOW);
		}

		public void enqueue(LoadImageTask task, int priority) {
			if (mQueue.size() == 0 || priority == PRIORITY_LOW) {
				mQueue.add(task);
			} else {
				mQueue.add(1, task);
			}
			runFirst();
		}

		public void finished() {
			mQueue.remove(0);
			runFirst();
		}

		private void runFirst() {
			if (mQueue.size() > 0) {
				LoadImageTask task = mQueue.get(0);
				if (task.getStatus() == AsyncTask.Status.PENDING) {
					task.execute();
				} else if (task.getStatus() == AsyncTask.Status.FINISHED) {
					mQueue.remove(0);
					runFirst();
				}
			}
		}
	}

	private static class LoadImageTask extends AsyncTask<String, Integer, Boolean> {
		public LazyActivity activity;
		private String mUrl;

		public LoadImageTask(String url) {
			mUrl = url;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					URL request = new URL(mUrl);
					InputStream is = (InputStream) request.getContent();
					FileOutputStream fos = new FileOutputStream(MagnatuneAPI.getCacheFileName(mUrl));
					try {
						byte[] buffer = new byte[4096];
						int l;
						while ((l = is.read(buffer)) != -1) {
							fos.write(buffer, 0, l);
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						fos.flush();
						is.close();
						fos.close();
					}
				}
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			activity.mScheduler.finished();
		}
	}

	protected abstract static class LoadTask extends AsyncTask<String, HashMap<String, String>, Boolean> {
		public LazyActivity activity;
		protected boolean mCancelled;

		@Override
		protected void onPreExecute() {
			mCancelled = false;
			activity.setProgressBarIndeterminateVisibility(true);
			activity.mAdapter.setStopLoading(true);
			System.gc();
		}

		@Override
		protected void onProgressUpdate(HashMap<String, String>... updates) {
			activity.mAdapterData.add(updates[0]);
			activity.mAdapter.notifyDataSetChanged();
			activity.mAdapter.setStopLoading(false);
		}

		@Override
		protected void onCancelled() {
			mCancelled = true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			activity.setProgressBarIndeterminateVisibility(false);
			System.gc();
		}
	}
}
