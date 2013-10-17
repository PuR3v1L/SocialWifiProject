package com.spydiko.socialwifi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by spiros on 10/15/13.
 */
public class Map extends Activity {

	private static final String TAG = Map.class.getSimpleName();
	// Google Map
	private GoogleMap googleMap;
	private SocialWifi socialWifi;
	private ArrayList<WifiPass> wifies;
	private int strokeColor = 0xff00c70f; //red outline
	private int shadeColor = 0x4400c70f; //opaque red fill

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

		try {
			wifies = socialWifi.readFromXMLPython("mine.xml");
			for (WifiPass wifi : wifies) {
				// create marker
				MarkerOptions marker = new MarkerOptions().position(new LatLng(wifi.getGeo().get(0), wifi.getGeo().get(1))).title(wifi.getSsid());
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
				// adding marker
				googleMap.addMarker(marker);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "mine.xml doesn't exist");
		}

		try {
			wifies = socialWifi.readFromXML("local.xml");
			for (WifiPass wifi : wifies) {
				// create marker
				MarkerOptions marker = new MarkerOptions().position(new LatLng(wifi.getGeo().get(0), wifi.getGeo().get(1))).title(wifi.getSsid());
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

				googleMap.addCircle(new CircleOptions()
						.center(new LatLng(wifi.getGeo().get(0), wifi.getGeo().get(1)))
						.radius(25)
						.strokeColor(strokeColor)
						.fillColor(shadeColor));


				// adding marker
				googleMap.addMarker(marker);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "local.xml doesn't exist");

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
