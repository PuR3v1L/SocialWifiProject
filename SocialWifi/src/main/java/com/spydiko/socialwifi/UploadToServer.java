package com.spydiko.socialwifi;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;
import android.webkit.WebBackForwardList;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

/**
 * Created by spiros on 10/2/13.
 */
public class UploadToServer extends AsyncTask<Void, Void, Void> {

    private static final String TAG = UploadToServer.class.getSimpleName();
    private Context context;
    private Socket sk;
    private DataOutputStream dos;
    private DataInputStream dis;
    private String message;
    private String hostIPstr = "155.207.133.206";
    private int serverPort = 44444;
    private Dialog loadingDialog;
    private int size;
    private boolean add;
    private String ssid, bssid, password;
    private byte[] buffer;
    private SocialWifi socialWifi;
    private double[] location;

    public UploadToServer(Context context, SocialWifi socialWifi) {
        super();
        this.socialWifi = socialWifi;
        this.context = context;
        add = false;
    }

    public UploadToServer(String ssid, String bssid, String password, Context context, SocialWifi socialWifi) {
        super();
        this.ssid = ssid;
        this.bssid = bssid;
        this.password = password;
        this.socialWifi = socialWifi;
        this.context = context;
        add = true;
    }

    @Override
    protected Void doInBackground(Void... params) {

        if (add) {
            while (!socialWifi.isGotLocation()) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            location = socialWifi.getLocationCoord();
        }


        try {
            Log.d(TAG, "Trying to open socket");
            sk = new Socket();
            SocketAddress remoteaddr = new InetSocketAddress(hostIPstr, serverPort);
            sk.connect(remoteaddr, 5000);
            sk.setSoTimeout(5000);
            Log.d(TAG, "Socket opened");
            dos = new DataOutputStream(sk.getOutputStream());
            dis = new DataInputStream(sk.getInputStream());
            Log.d(TAG, "Trying to sent message");
            if (add) {
                dos.writeBytes("addPass" + "\r\n");
                Log.d(TAG,Double.toString(location[0]));
                Log.d(TAG,Double.toString(location[1]));
                dos.writeBytes(Double.toString(location[0])+ "\r\n");
                dos.writeBytes(Double.toString(location[1])+ "\r\n");
                dos.writeBytes(ssid+ "\r\n");
                dos.writeBytes(bssid+ "\r\n");
                dos.writeBytes(password+ "\r\n");
                Log.d(TAG, "Password add sent");
            } else {
                dos.writeBytes("update" + "\r\n");
                dos.writeBytes("3.3" + "\r\n");
                dos.writeBytes("52.523742" + "\r\n");
                dos.writeBytes("13.412333" + "\r\n");
                size = Integer.parseInt(dis.readLine());
                Log.d(TAG, "Size: " + size);
                buffer = new byte[size];
                Log.d(TAG, "Buffer socket: " + sk.getReceiveBufferSize());
                sk.setReceiveBufferSize(size);
                dis.read(buffer);
                Log.d(TAG, "Messages sent");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        loadingDialog = new Dialog(context, R.style.CustomDialog);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        if (add) {

        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d(TAG, "onPostExecute");
        try {
            Log.d(TAG, "Closing Everything");
            dos.close();
            dis.close();
            sk.close();
            if (add) {
                ArrayList<WifiPass> tmp = socialWifi.getWifies();
                ArrayList<Double> loc = new ArrayList<Double>();
                loc.add(0, location[0]);
                loc.add(1, location[1]);
                tmp.add(new WifiPass(ssid, bssid, password, loc));
                socialWifi.setWifies(tmp);
                Toast.makeText(context, "Success!\nNew password stored!", Toast.LENGTH_SHORT).show();
            } else {
                socialWifi.storeXML(buffer);
                socialWifi.setWifies(socialWifi.readFromXML("server.xml"));
                Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
            }
            socialWifi.getWifi().startScan();
        } catch (Exception e) {
            Toast.makeText(context, "Error updating...\nTry again later...", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        loadingDialog.dismiss();
    }


    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}