package com.spydiko.socialwifi;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by spiros on 11/11/13.
 */
public class LocationUtils implements LocationListener {

	private static final String TAG = LocationUtils.class.getSimpleName();
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final int MINIMUM_TIME_INTERVAL = 5 * 60 * 1000;
	private static final int MINIMUM_CHANGE_IN_DISTANCE = 1000;
	private static Location currentLocation = null;
	private static LocationManager locationManager;
	private boolean networkProvider, gpsProvider;


	public LocationUtils(SocialWifi socialWifi) {
		Log.d(TAG, "LocationUtils");
		networkProvider = true;
		gpsProvider = true;
		locationManager = (LocationManager) socialWifi.getSystemService(Context.LOCATION_SERVICE);

		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MINIMUM_TIME_INTERVAL, MINIMUM_CHANGE_IN_DISTANCE, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME_INTERVAL, MINIMUM_CHANGE_IN_DISTANCE, this);


	}

	public static Location getCurrentLocation() {
		if (currentLocation == null) {
			currentLocation = getBestCurrentLocation();
			if (System.currentTimeMillis() - currentLocation.getTime() > TWO_MINUTES * 3) {
				currentLocation = null;
			}
		}

		return currentLocation;
	}

	public static void setCurrentLocation(Location currentLocation) {
		LocationUtils.currentLocation = currentLocation;
	}

	private static Location getBestCurrentLocation() {
		for (String s : locationManager.getAllProviders()) {
			if (locationManager.getLastKnownLocation(s) != null) {
				if (isBetterLocation(locationManager.getLastKnownLocation(s), currentLocation))
					return locationManager.getLastKnownLocation(s);
			}
		}
		return null;
	}

	/**
	 * Determines whether one Location reading is better than the current Location fix
	 *
	 * @param location            The new Location that you want to evaluate
	 * @param currentBestLocation The current Location fix, to which you want to compare the new one
	 */
	protected static boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether two providers are the same
	 */
	private static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged");
		if (isBetterLocation(location, currentLocation)) {
			Log.d(TAG, "locationUpdated");
			currentLocation = location;
		}

		Log.d(TAG, "current location: lat: " + currentLocation.getLatitude() + " long: " + currentLocation.getLongitude());
	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {
		Log.d(TAG, "onStatusChanged: " + s);
	}

	@Override
	public void onProviderEnabled(String s) {
		Log.d(TAG, "onProviderEnabled: " + s);

	}

	@Override
	public void onProviderDisabled(String s) {
		Log.d(TAG, "onProviderDisabled: " + s);
		if (s.equals("network")) networkProvider = false;
		else if (s.equals("gps")) gpsProvider = false;

		//TODO If both off inform user to enable from settings.
	}

}
