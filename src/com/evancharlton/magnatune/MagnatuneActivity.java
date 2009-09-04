package com.evancharlton.magnatune;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class MagnatuneActivity extends Activity {
	private static final int MENU_SETTINGS = 100;
	private static final int MENU_ABOUT = 101;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.FIRST, MENU_SETTINGS, Menu.FIRST, R.string.menu_settings).setIcon(R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, MENU_ABOUT, Menu.NONE, R.string.menu_about).setIcon(R.drawable.ic_menu_info_details);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_SETTINGS:
				startActivity(new Intent(this, Settings.class));
				return true;
			case MENU_ABOUT:
				new AboutDialog(this).show();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
