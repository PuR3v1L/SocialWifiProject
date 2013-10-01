package com.spydiko.socialwifi;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jim on 5/9/2013.
 */
public class SocialWifi extends Application {

    private final static String TAG = SocialWifi.class.getSimpleName();
    private boolean gotLocation;
    private LocationManager lm;
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
    private WifiManager wifi;
    private ConnectivityManager connectivityManager;

    public void onCreate() {
        super.onCreate();
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }


    public WifiManager getWifi() {
        return wifi;
    }

    public ConnectivityManager getConnectivityManager() {
        return connectivityManager;
    }

    public static void CreateXMLString(OutputStreamWriter out, ArrayList<WifiPass> wifies) throws IllegalArgumentException, IllegalStateException, IOException {
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

    public ArrayList<WifiPass> ReadFromXML(String xmlFile) {

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
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        gotLocation = false;
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

    public boolean isGotLocation() {
        return gotLocation;
    }

    public void setGotLocation(boolean gotLocation) {
        this.gotLocation = gotLocation;
    }

    private class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            String message = String.format(
                    "New Location Longitude: %1$s Latitude: %2$s Time: %3$s",
                    location.getLongitude(), location.getLatitude(), location.getTime()-System.currentTimeMillis()
            );
            Log.d(TAG,message);
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



}
