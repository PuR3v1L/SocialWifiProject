package com.spydiko.socialwifi;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by spiros on 10/15/13.
 */
public class Map extends Activity implements android.location.LocationListener {

	private static final String TAG = Map.class.getSimpleName();
	// Google Map
	private GoogleMap googleMap;
	private SocialWifi socialWifi;
	private ArrayList<WifiPass> wifies;
	private int strokeColor = 0x0000c70f; //green outline
	private int shadeColor = 0x4400c70f; //opaque green fill
	private LocationManager locationManager;

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
				MarkerOptions marker = new MarkerOptions().position(new LatLng(wifi.getGeo().get(0), wifi.getGeo().get(1))).title(wifi.getSsid() + "\n" + wifi.getBssid());
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

		googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

		googleMap.setMyLocationEnabled(true); // false to disable

		googleMap.getUiSettings().setMyLocationButtonEnabled(true);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER

	}

	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
	}

	@Override
	public void onLocationChanged(Location location) {

		CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15).build();

		googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

		locationManager.removeUpdates(this);

	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {

	}

	@Override
	public void onProviderEnabled(String s) {

	}

	@Override
	public void onProviderDisabled(String s) {

	}
}
