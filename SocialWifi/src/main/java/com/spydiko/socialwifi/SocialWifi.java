package com.spydiko.socialwifi;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

import com.bugsense.trace.BugSenseHandler;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jim on 5/9/2013.
 */
public class SocialWifi extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {

	private final static String TAG = SocialWifi.class.getSimpleName();
	private boolean gotLocation;
	private LocationManager lm;
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;
	private WifiManager wifi;
	private ConnectivityManager connectivityManager;
	private FileOutputStream outputStream;
	private OutputStreamWriter osw;
	private ArrayList<WifiPass> wifies;
	private double[] location_now;
	private float areaRadius;

	public void onCreate() {
		super.onCreate();
		BugSenseHandler.initAndStartSession(this, "6acdeebc");
		wifies = new ArrayList<WifiPass>();
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		editor = prefs.edit();
		loadPreferences();
	}

	public double[] getLocationCoord() {
		return location_now;
	}

	public WifiManager getWifi() {
		return wifi;
	}

	public ConnectivityManager getConnectivityManager() {
		return connectivityManager;
	}

	public static void createXMLString(OutputStreamWriter out, ArrayList<WifiPass> wifies) throws IllegalArgumentException, IllegalStateException, IOException {
		XmlSerializer xmlSerializer = Xml.newSerializer();
		xmlSerializer.setOutput(out);

		//Start Document
		xmlSerializer.startDocument("UTF-8", true);
		xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		//Open Tag <file>
		xmlSerializer.startTag("", "file");
		Double latitude;
		Double longitude;
		for (WifiPass wifiPass : wifies) {
			xmlSerializer.startTag("", "void");
			xmlSerializer.attribute("", "method", "put");
			latitude = wifiPass.getGeo().get(0);
			longitude = wifiPass.getGeo().get(1);
			xmlSerializer.startTag("", "double");
			xmlSerializer.text(latitude.toString());
			xmlSerializer.endTag("", "double");
			xmlSerializer.startTag("", "double");
			xmlSerializer.text(longitude.toString());
			xmlSerializer.endTag("", "double");
			xmlSerializer.startTag("", "string");
			xmlSerializer.text(wifiPass.getSsid());
			xmlSerializer.endTag("", "string");
			xmlSerializer.startTag("", "string");
			xmlSerializer.text(wifiPass.getBssid());
			xmlSerializer.endTag("", "string");
			xmlSerializer.startTag("", "string");
			xmlSerializer.text(wifiPass.getPassword());
			xmlSerializer.endTag("", "string");
			xmlSerializer.endTag("", "void");
		}

		//end tag <file>
		xmlSerializer.endTag("", "file");
		xmlSerializer.endDocument();
	}

	private void loadPreferences() {
		Log.d(TAG, "loadPreferences");
		areaRadius = prefs.getFloat("areaRadius", 3);

	}

	public ArrayList<WifiPass> readFromXML(String xmlFile) {

		ArrayList<WifiPass> userData = new ArrayList<WifiPass>();
		FileInputStream fis;
		InputStreamReader isr;
		char[] inputBuffer;
		String data = null;
		try {
			fis = this.openFileInput(xmlFile);
			isr = new InputStreamReader(fis);
			inputBuffer = new char[fis.available()];
			isr.read(inputBuffer);
			data = new String(inputBuffer);
			isr.close();
			fis.close();
		} catch (
				FileNotFoundException e3
				)

		{
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (
				IOException e
				)

		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		XmlPullParserFactory factory = null;
		try

		{
			factory = XmlPullParserFactory.newInstance();
		} catch (
				XmlPullParserException e2
				)

		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		factory.setNamespaceAware(true);
		XmlPullParser xpp = null;
		try

		{
			xpp = factory.newPullParser();
		} catch (
				XmlPullParserException e2
				)

		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		try

		{
			xpp.setInput(new StringReader(data));
		} catch (
				XmlPullParserException e1
				)

		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		int eventType = 0;
		try

		{
			eventType = xpp.getEventType();
		} catch (
				XmlPullParserException e1
				)

		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		WifiPass wifiPass;
		String name = null;
		int check = 0;
		String password = null;
		String ssid = null;
		String bssid = null;
		String latitude = null;
		String longitude = null;
		while (eventType != XmlPullParser.END_DOCUMENT)

		{
			if (eventType == XmlPullParser.START_DOCUMENT) {
				//                System.out.println("Start document");
			} else if (eventType == XmlPullParser.START_TAG) {
				//                System.out.println("Start tag " + xpp.getName());
				name = xpp.getName().concat("start");
				if (xpp.getAttributeCount() > 0) {
					//                    Log.d(TAG, xpp.getAttributeName(0));
					if (xpp.getAttributeName(0).equals("method")) {
						check = 1;
					}
				}
			} else if (eventType == XmlPullParser.TEXT) {
				//                Log.d(TAG, "Text: " + xpp.getText());
				if (check == 1 && name.equals("doublestart")) {
					latitude = xpp.getText();
					//                    Log.d(TAG, "Lati: " + latitude);
					check = 2;
				} else if (check == 2 && name.equals("doublestart")) {
					longitude = xpp.getText();
					//                    Log.d(TAG, "Long: " + longitude);
					check = 3;
				} else if (check == 3 && name.equals("stringstart")) {
					ssid = xpp.getText();
					//                    Log.d(TAG, "ssid: " + ssid);
					check = 4;
				} else if (check == 4 && name.equals("stringstart")) {
					bssid = xpp.getText();
					//                    Log.d(TAG, "bssid: " + bssid);
					check = 5;
				} else if (check == 5 && name.equals("stringstart")) {
					password = xpp.getText();
					//                    Log.d(TAG, "pass: " + password);
					check = -1;
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				//                System.out.println("End tag " + xpp.getName());
				name = xpp.getName().concat("end");
				if (xpp.getName().equals("void") && check == -1) {
					ArrayList<Double> temp = new ArrayList<Double>();
					Double lat = Double.parseDouble(latitude);
					Double lon = Double.parseDouble(longitude);
					temp.add(lat);
					temp.add(lon);
					wifiPass = new WifiPass(ssid, bssid, password, temp);
					userData.add(wifiPass);
					check = 0;
				}
			}
			try {
				eventType = xpp.next();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return userData;
	}

	public void getLocation() {
		lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		gotLocation = false;
		Log.d(TAG, "getLocation");
	  /*
     * Loop over the array backwards, and if you get an accurate location,
     * then break out the loop
     */
		lm.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER,
				MINIMUM_TIME_BETWEEN_UPDATES,
				MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
				new MyLocationListener()
		);

	}

	public float getAreaRadius() {
		return areaRadius;
	}

	public void setAreaRadius(float areaRadius) {
		Log.d(TAG, "areaRadius changed to: " + areaRadius);
		this.areaRadius = areaRadius;
		editor.putFloat("areaRadius", areaRadius);
		editor.commit();
	}

	public boolean isGotLocation() {
		return gotLocation;
	}

	public void setGotLocation(boolean gotLocation) {
		this.gotLocation = gotLocation;
	}

	public ArrayList<WifiPass> getWifies() {
		return wifies;
	}

	public void setWifies(ArrayList<WifiPass> wifies) {
		this.wifies = wifies;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
		Log.d(TAG, s + " changed!");
		if (s.equals("area_radius_list")) setAreaRadius(Float.valueOf(sharedPreferences.getString("area_radius_list", "3")));

	}

	public void setSharedPreferenceBoolean(String pref, Boolean value) {
		editor.putBoolean(pref, value);
		editor.commit();
	}

	public boolean getSharedPreferenceBoolean(String pref) {
		return prefs.getBoolean(pref, false);
	}

	public void setSharedPreferenceString(String pref, String value) {
		editor.putString(pref, value);
		editor.commit();
	}

	public String getSharedPreferenceString(String pref) {
		return prefs.getString(pref, "");
	}

	public void logout() {
		setSharedPreferenceBoolean("notFirstTime", false);
	}

	private class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
			location_now = new double[2];
			String message = String.format(
					"New Location Longitude: %1$s Latitude: %2$s Time: %3$s",
					location.getLongitude(), location.getLatitude(), location.getTime() - System.currentTimeMillis()
			);
			location_now[0] = location.getLatitude();
			location_now[1] = location.getLongitude();
			Log.d(TAG, message);
			gotLocation = true;
			lm.removeUpdates(this);
		}

		public void onStatusChanged(String s, int i, Bundle b) {

		}

		public void onProviderDisabled(String s) {

		}

		public void onProviderEnabled(String s) {

		}

	}

	public int addNewWifi(String ssid, String bssid, String password, List<Double> geo, ArrayList<WifiPass> arrayList) {
		WifiPass wifiPass = new WifiPass(ssid, bssid, password, geo);
		int check = checkIfWifiExists(wifiPass, arrayList);
		int result = 0;
		switch (check) {
			case 0:
				result = 0;
				break;
			case 1:
				result = 1;
				break;
			case -1:
				updatePassword();
				result = -1;
				break;
			default:
				break;
		}
		return result;
	}

	private void updatePassword() {

	}

	private void checkConnection() {

	}

	public int checkIfWifiExists(WifiPass wifiPass, ArrayList<WifiPass> arrayList) {
		for (WifiPass temp : arrayList) {
			if (wifiPass.getBssid().equals(temp.getBssid())) {
				if (wifiPass.getPassword().equals(temp.getPassword())) {
					return 0;
				} else {
					return -1;
				}
			}
		}
		return 1;
	}

	public void storeXML(byte[] buffer) {
		try {
			outputStream = openFileOutput("server.xml", Context.MODE_PRIVATE);
			outputStream.write(buffer);
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeNetwork(String ssid) {
		List<WifiConfiguration> list = wifi.getConfiguredNetworks();
		for (WifiConfiguration i : list) {
			Log.d(TAG, i.SSID + " " + i.BSSID);
			if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
				wifi.removeNetwork(i.networkId);
			}
		}
	}


	public void connect(String networkSSID, String networkPass, int typeOfEncryption) {
		WifiConfiguration conf = new WifiConfiguration();
		conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
		// Case of WPA
		switch (typeOfEncryption) {
			case 1:
				Log.d(TAG, "WEP");
				conf.wepKeys[0] = "\"" + networkPass + "\"";
				conf.wepTxKeyIndex = 0;
				conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
				break;
			case 2:
				Log.d(TAG, "WPA/WPA");
				conf.preSharedKey = "\"" + networkPass + "\"";
				break;
			case 3:
				Log.d(TAG, "WPA2");
				conf.preSharedKey = quoteNonHex(networkPass, 64);
				break;
			default:
				break;
		}
		conf.status = WifiConfiguration.Status.ENABLED;
		conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		int inet = wifi.addNetwork(conf);
		Log.d(TAG, "Added " + inet);
		if (inet > 0) {
			List<WifiConfiguration> list = wifi.getConfiguredNetworks();
			for (WifiConfiguration i : list) {
				if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
					Log.d(TAG, "Done!");
					wifi.disconnect();
					boolean b = wifi.enableNetwork(inet, true);
					Log.d(TAG, "Result " + b);
					boolean c = wifi.reconnect();
					Log.d(TAG, "Result " + c);
					break;
				}
			}
		}

	}

	private static String quoteNonHex(String value, int... allowedLengths) {
		return isHexOfLength(value, allowedLengths) ? value : convertToQuotedString(value);
	}

	/**
	 * Encloses the incoming string inside double quotes, if it isn't already quoted.
	 *
	 * @param string the input string
	 * @return a quoted string, of the form "input".  If the input string is null, it returns null
	 * as well.
	 */
	private static String convertToQuotedString(String string) {
		if (string == null || string.length() == 0) {
			return null;
		}
		// If already quoted, return as-is
		if (string.charAt(0) == '"' && string.charAt(string.length() - 1) == '"') {
			return string;
		}
		return '\"' + string + '\"';
	}

	private static final Pattern HEX_DIGITS = Pattern.compile("[0-9A-Fa-f]+");

	/**
	 * @param value          input to check
	 * @param allowedLengths allowed lengths, if any
	 * @return true if value is a non-null, non-empty string of hex digits, and if allowed lengths are given, has
	 * an allowed length
	 */
	private static boolean isHexOfLength(CharSequence value, int... allowedLengths) {
		if (value == null || !HEX_DIGITS.matcher(value).matches()) {
			return false;
		}
		if (allowedLengths.length == 0) {
			return true;
		}
		for (int length : allowedLengths) {
			if (value.length() == length) {
				return true;
			}
		}
		return false;
	}
}
