package com.evancharlton.magnatune;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Magnatune extends Activity {
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
	}
}