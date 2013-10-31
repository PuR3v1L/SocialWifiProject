package com.spydiko.socialwifi;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by spiros on 10/3/13.
 */
public class PrefsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String TAG = PrefsActivity.class.getSimpleName();
	ListPreference areaRadiusList;
	SocialWifi socialWifi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		socialWifi = (SocialWifi) this.getApplication();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getActionBar().setDisplayHomeAsUpEnabled(true);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		setSummaries();
	}

	private void setSummaries() {
		setAreaRadiusListSum();
	}

	private void setAreaRadiusListSum() {
		areaRadiusList = (ListPreference) findPreference("area_radius_list");
		areaRadiusList.setSummary(getResources().getString(R.string.area_radius_sum) + " " + socialWifi.getAreaRadius() + " km");
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
		Log.d(TAG, "onSharedPreferenceChanged: " + s);
		if (s.equals("areaRadius")) setAreaRadiusListSum();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}