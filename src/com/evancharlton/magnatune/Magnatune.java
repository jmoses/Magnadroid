package com.evancharlton.magnatune;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class Magnatune extends Activity {
	private static final int MENU_SETTINGS = Menu.FIRST;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		((Button) findViewById(R.id.album)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Magnatune.this, AlbumList.class));
			}
		});

		((Button) findViewById(R.id.genre)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Magnatune.this, GenreList.class));
			}
		});

		((Button) findViewById(R.id.artist)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Magnatune.this, ArtistList.class));
			}
		});

		((Button) findViewById(R.id.search)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Magnatune.this, SearchActivity.class));
			}
		});

		((Button) findViewById(R.id.downloads)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Magnatune.this, DownloadList.class));
			}
		});

		((Button) findViewById(R.id.newest)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Magnatune.this, LatestAlbums.class));
			}
		});
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