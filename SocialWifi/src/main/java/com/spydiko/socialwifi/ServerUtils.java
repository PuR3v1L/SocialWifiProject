package com.spydiko.socialwifi;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * Created by jim on 28/10/2013.
 */
public class ServerUtils {

	public static final int CORRECT_REPORT = 0, CORRECT_UPDATE = 1, CORRECT_ADD = 2, CORRECT_USERINFO = 3;
	public static final int WRONG_USER = 4;
	public static final int WRONG_REPORT = 5, WRONG_UPDATE = 6, WRONG_ADD = 7, WRONG_USERINFO = 8;
	private static final String TAG = "ServerUtils";
	private DataInputStream dis;
	private DataOutputStream dos;
	private String ssid;
	private String bssid;
	private String password;
	private Socket sk;
	private String extraInfo;
	private String username;
	private String hostIPstr;
	private String numOfUploads;
	private int serverPort;
	private byte[] buffer;
	private byte[] pyBuffer;

	public ServerUtils() {
		hostIPstr = "83.212.121.161";
		serverPort = 44444;
	}

	public byte[] getPyBuffer() {
		return pyBuffer;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public boolean sendCredentials(String action, Object mEmail, Object macAddress, Object mPassword) {
		String response = null;
		try {
			sk = new Socket();
			SocketAddress remoteaddr = new InetSocketAddress(hostIPstr, serverPort);
			sk.setSoTimeout(5000);
			sk.connect(remoteaddr, 5000);
			Log.d(TAG, "Socket opened");
			dos = new DataOutputStream(sk.getOutputStream());
			dis = new DataInputStream(sk.getInputStream());
			Log.d(TAG, "Trying to sent message");
			dos.writeBytes(action + "\r\n");
			dos.writeBytes(mEmail + "\r\n");
			dos.writeBytes(macAddress + "\r\n");
			dos.writeBytes(mPassword + "\r\n");
			response = dis.readLine();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (response == null) {
			return false;
		} else if (response.equals("Done")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean setUsername(String username) {

		if (username.equals("")) return false;
		this.username = username;
		return true;
	}

	public void setWiFiInfo(String ssid, String bssid, String password, String extraInfo) {
		this.ssid = ssid;
		this.bssid = bssid;
		this.password = password;
		this.extraInfo = extraInfo;
	}

	public int tryToReport() {
		try {
			dos.writeBytes("changePass" + "\r\n");
			Log.d(TAG, ssid);
			dos.writeBytes(ssid + "\r\n");
			Log.d(TAG, bssid);
			dos.writeBytes(bssid + "\r\n");
			Log.d(TAG, password);
			dos.writeBytes(password + "\r\n");
			Log.d(TAG, username);
			dos.writeBytes(username + "\r\n");
			Log.d(TAG, "Password report sent");
			String response = dis.readLine();
			if (response.equals("Done")) return CORRECT_REPORT;
			else if (response.contains("notUser")) return WRONG_USER;
			else return WRONG_REPORT;
		} catch (Exception e) {
			e.printStackTrace();
			return WRONG_REPORT;
		}

	}

	public boolean tryToLocalize(SocialWifi socialWifi) {

		long current = System.currentTimeMillis();
		             /* Get current location through wifi*/
		while (!socialWifi.isGotLocation() && System.currentTimeMillis() - current < 10000) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (!socialWifi.isGotLocation()) {
			return false;
		}
		return true;

	}

	public boolean tryToConnect(WifiManager wifiManager, ConnectivityManager connectivityManager) {
		int typeOfEncryption = 0;
		Log.d(TAG, "extraInfo: " + extraInfo);
		//                if (extraInfo.contains("WEP")) typeOfEncryption = 1;
		//                else if (extraInfo.contains("WPA2")) typeOfEncryption = 3;
		//                else if (extraInfo.contains("WAP") || extraInfo.contains("WPA")) typeOfEncryption = 2;
		WifiUtils.removeNetwork(ssid, wifiManager);
		WifiUtils.connect(WifiUtils.createConfigFile(ssid, password, extraInfo), wifiManager, connectivityManager);
		            /* Check if password is correct and if the phone can connect to the network*/
		long current = System.currentTimeMillis();
		NetworkInfo networkInfo;
		boolean ok = false;
		Log.d(TAG, "entered");
		while (System.currentTimeMillis() - current < 10000) {
			networkInfo = connectivityManager.getActiveNetworkInfo();
			try {
				if (networkInfo != null && networkInfo.isConnected() && wifiManager.getConnectionInfo().getBSSID().equals(bssid)) {
					ok = true;
					Log.d(TAG, "correct pass");
					wifiManager.saveConfiguration();
					break;
				}
			} catch (Exception e) {

			}
		}
		if (!ok) {
			WifiUtils.removeNetwork(ssid, wifiManager);
			wifiManager.setWifiEnabled(false);
			wifiManager.setWifiEnabled(true);
			return false;
		}
		return true;
	}

	public boolean tryToOpenSocket() {
		try {
			Log.d(TAG, "Trying to open socket");
			SocketAddress remoteaddr = new InetSocketAddress(hostIPstr, serverPort);
			sk = new Socket();
			sk.setSoTimeout(10000);
			sk.connect(remoteaddr);
			Log.d(TAG, "Socket opened");
			dos = new DataOutputStream(sk.getOutputStream());
			dis = new DataInputStream(sk.getInputStream());
			Log.d(TAG, "Trying to sent message");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public int tryToUpdate(float areaRadius, double[] location) {
		try {
			dos.writeBytes("update" + "\r\n");
			dos.writeBytes(String.valueOf(areaRadius) + "\r\n");
			dos.writeBytes(Double.toString(location[0]) + "\r\n");
			dos.writeBytes(Double.toString(location[1]) + "\r\n");
			dos.writeBytes(username + "\r\n");
			Log.d(TAG, "Sent: " + String.valueOf(areaRadius) + " " + Double.toString(location[0]) + " " + Double.toString(location[1]) + " " + username);
			String inputMsg = dis.readLine();
			Log.d(TAG, "inputMsg: " + inputMsg);
			try {
				int size = Integer.parseInt(inputMsg);
				buffer = new byte[size];
				Log.d(TAG, "Buffer socket: " + sk.getReceiveBufferSize());
				sk.setReceiveBufferSize(size);
				dis.readFully(buffer, 0, buffer.length);
				dos.writeBytes("lemourios\r\n");
				inputMsg = dis.readLine();
				Log.d(TAG, "inputMsg: " + inputMsg);
				size = Integer.parseInt(inputMsg);
				Log.d(TAG, "Size: " + size);
				pyBuffer = new byte[size];
				sk.setReceiveBufferSize(size);
				dis.readFully(pyBuffer, 0, pyBuffer.length);
				Log.d(TAG, "Buffer size: " + buffer.length);
			} catch (NumberFormatException e) {
				Log.d(TAG, "Not User");
				e.printStackTrace();
				return WRONG_USER;
			} catch (IOException e) {
				e.printStackTrace();
				return WRONG_UPDATE;
			}

			Log.d(TAG, "Messages Received");
			return CORRECT_UPDATE;
		} catch (Exception e) {
			e.printStackTrace();
			return WRONG_UPDATE;
		}
	}

	public int tryToAdd(double[] location) {
		try {
			dos.writeBytes("addPass" + "\r\n");
			Log.d(TAG, Double.toString(location[0]));
			Log.d(TAG, Double.toString(location[1]));
			dos.writeBytes(Double.toString(location[0]) + "\r\n");
			dos.writeBytes(Double.toString(location[1]) + "\r\n");
			Log.d(TAG, ssid);
			dos.writeBytes(ssid + "\r\n");
			Log.d(TAG, bssid);
			dos.writeBytes(bssid + "\r\n");
			Log.d(TAG, password);
			dos.writeBytes(password + "\r\n");
			Log.d(TAG, username);
			dos.writeBytes(username + "\r\n");
			Log.d(TAG, "Password add sent");
			String response = dis.readLine();
			Log.d(TAG, "Response " + response);
			if (response.equals("Done")) return CORRECT_ADD;
			else if (response.contains("notUser")) {
				return WRONG_USER;
			} else return WRONG_ADD;
		} catch (Exception e) {
			e.printStackTrace();
			return WRONG_ADD;
		}
	}

	public int tryToGetUserInfo(SocialWifi socialWifi) {
		try {

			dos.writeBytes("userInfo" + "\r\n");
			dos.writeBytes(username + "\r\n");
			numOfUploads = dis.readLine();
			if (numOfUploads.contains("notUser")) return WRONG_USER;
			socialWifi.setNumOfUploads(numOfUploads);
			Log.d(TAG, "numOfUploads: " + numOfUploads);
			return CORRECT_USERINFO;
		} catch (IOException e) {
			e.printStackTrace();
			return WRONG_USERINFO;
		} catch (Exception e) {
			e.printStackTrace();
			return WRONG_USERINFO;
		}
	}

	public boolean tryToCloseSocket() {
		try {
			sk.close();
			dos.close();
			dis.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
			// Toast.makeText(context, "Error updating...\nTry again later...", Toast.LENGTH_SHORT).show();
		}
	}


}
