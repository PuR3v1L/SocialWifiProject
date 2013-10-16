package com.spydiko.socialwifi;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by spiros on 10/15/13.
 */
public class Map extends Activity {
	// Google Map
	private GoogleMap googleMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_layout);

		try {
			// Loading map
			initilizeMap();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * function to load map. If map is not created it will create it for you
	 */
	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}

		// create marker
		MarkerOptions marker = new MarkerOptions().position(new LatLng(40.6253908, 22.9622492)).title("Geia sou Thalia");

		// adding marker
		googleMap.addMarker(marker);

		googleMap.setMyLocationEnabled(true); // false to disable

		googleMap.getUiSettings().setMyLocationButtonEnabled(true);

	}

	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
	}
}
