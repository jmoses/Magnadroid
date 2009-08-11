package com.evancharlton.magnatune;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Magnatune extends Activity {
	private static final int MENU_SETTINGS = Menu.FIRST;

	private static final String ROW_TEXT = "text";
	private static final String ROW_ICON = "icon";

	private ListView mList;
	private SimpleAdapter mAdapter;
	private ArrayList<HashMap<String, String>> mAdapterData = new ArrayList<HashMap<String, String>>();
	private ArrayList<Intent> mIntents = new ArrayList<Intent>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mList = (ListView) findViewById(android.R.id.list);
		mAdapter = new SimpleAdapter(this, mAdapterData, R.layout.home_row, new String[] {
				ROW_TEXT,
				ROW_ICON
		}, new int[] {
				android.R.id.text1,
				android.R.id.icon
		});
		mList.setAdapter(mAdapter);

		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> list, View row, int position, long id) {
				startActivity(mIntents.get(position));
			}
		});

		add(R.drawable.newest, R.string.newest_albums, new Intent(this, LatestAlbums.class));
		add(R.drawable.albums, R.string.albums, new Intent(this, AlbumList.class));
		add(R.drawable.icon, R.string.genres, new Intent(this, GenreList.class));
		add(R.drawable.artists, R.string.artists, new Intent(this, ArtistList.class));
		add(R.drawable.search, R.string.search, new Intent(this, SearchActivity.class));
		add(R.drawable.download, R.string.purchased_albums, new Intent(this, DownloadList.class));
	}

	private void add(int icon, int text, Intent intent) {
		mIntents.add(intent);
		HashMap<String, String> row = new HashMap<String, String>();
		row.put(ROW_TEXT, getString(text));
		row.put(ROW_ICON, String.valueOf(icon));
		mAdapterData.add(row);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, R.string.menu_settings).setIcon(R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_SETTINGS:
				startActivity(new Intent(this, Settings.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}