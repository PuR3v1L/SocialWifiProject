package com.spydiko.socialwifi;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by spiros on 10/15/13.
 */
public class Map extends Activity {
	// Google Map
	private GoogleMap googleMap;
	private SocialWifi socialWifi;
	private ArrayList<WifiPass> wifies;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_layout);
		socialWifi = (SocialWifi) getApplication();
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
		wifies = socialWifi.readFromXMLPython("mine.xml");

		for (WifiPass wifi : wifies) {
			// create marker
			MarkerOptions marker = new MarkerOptions().position(new LatLng(wifi.getGeo().get(0), wifi.getGeo().get(1))).title(wifi.getSsid());
			marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
			// adding marker
			googleMap.addMarker(marker);
		}
		googleMap.setMyLocationEnabled(true); // false to disable

		googleMap.getUiSettings().setMyLocationButtonEnabled(true);

	}

	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
	}
}
