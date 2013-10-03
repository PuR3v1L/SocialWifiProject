package com.spydiko.socialwifi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements View.OnClickListener {

    private SocialWifi socialWifi;
    private Button buttonScan;
    private XmlSerializer xs;
    private SimpleAdapter simpleAdapter;
    private ArrayList<HashMap<String, String>> arrayList;
    private HashMap<String, String> clickedWifi;
    private final static String TAG = MainActivity.class.getSimpleName();
    private Dialog loadingDialog;
    private ListView lv;
    private String ITEM_KEY = "key", BSSID_KEY = "bssid", EXISTS_KEY = "exist", EXTRAS_KEY = "extra";
    private BroadcastReceiver broadcastReceiver;
    private FileOutputStream outputStream;
    private Context context;

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        try {
            outputStream = openFileOutput("local.xml", Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(outputStream);
            socialWifi.createXMLString(osw, socialWifi.getWifies());
            File file = new File(getFilesDir(), "server.xml");
            boolean deleted = file.delete();
            Log.d(TAG, "server deletion is " + deleted);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socialWifi = (SocialWifi) getApplication();
        buttonScan = (Button) findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(this);
        context = this;
       /* AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_dialog,null));
        loadingDialog = builder.create();*/
//        loadingDialog.setCanceledOnTouchOutside(false);
        // ----------- Read all the saved wifi --------------
        File file = new File(getFilesDir(), "local.xml");
        if (file.exists()) {
            socialWifi.setWifies(socialWifi.readFromXML("local.xml"));
            Log.d(TAG, "exists");
        } else {
            Log.d(TAG, "doesn't exist");
        }
        // --------------------------------------------------
        arrayList = new ArrayList<HashMap<String, String>>();
        lv = (ListView) findViewById(R.id.list_scan);
        simpleAdapter = new SimpleAdapter(this, arrayList, R.layout.row, new String[]{ITEM_KEY}, new int[]{R.id.list_value}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                NoParentPressImageView right = (NoParentPressImageView) row.findViewById(R.id.right);
                right.setTag(position);
                right.setOnClickListener(MainActivity.this);
                NoParentPressTextView list_value = (NoParentPressTextView) row.findViewById(R.id.list_value);
                list_value.setTag(position);
                if (arrayList.get(position).get(EXISTS_KEY).equals("y")) {
                    list_value.setTextColor(Color.parseColor("#00FF00"));
                    list_value.setBackgroundResource(R.drawable.mybutton);
                    list_value.setOnClickListener(MainActivity.this);
                    right.setImageResource(android.R.drawable.ic_dialog_alert);
                }
                return row;
            }
        };

/*        File file = new File(getFilesDir(), "server.xml");
        if (file.exists()) {
            socialWifi.setWifies(socialWifi.readFromXML("server.xml"));
//            Log.d (TAG,"exists");
        } else {
//            Log.d(TAG,"doesn't exist");
        }*/

//        wifies=socialWifi.ReadFromXML("pass.xml");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                update();
                lv.setAdapter(simpleAdapter);
                simpleAdapter.notifyDataSetChanged();
            }
        };

    }

    public void update() {
        List<ScanResult> results = socialWifi.getWifi().getScanResults();
        int size = results.size();
        for (WifiPass wifi : socialWifi.getWifies()) {
            Log.d(TAG, "Update: " + wifi.getSsid() + " " + wifi.getPassword() + " " + wifi.getBssid());
        }
//        Log.d(TAG, "the size is " + size);
        arrayList.clear();
        try {
            size = size - 1;
            while (size >= 0) {
                HashMap<String, String> item = new HashMap<String, String>();
                item.put(ITEM_KEY, results.get(size).SSID);
                item.put(EXTRAS_KEY, results.get(size).capabilities + "\n" + results.get(size).level);
                item.put(BSSID_KEY, results.get(size).BSSID);
                item.put(EXISTS_KEY, "n");
                arrayList.add(item);
                size--;
                for (WifiPass wifi : socialWifi.getWifies()) {
                    if (item.get(BSSID_KEY).contains(wifi.getBssid())) {
                        item.put(EXISTS_KEY, "y");
                    }
                }
            }
            Collections.sort(arrayList, new SortByExist());
            simpleAdapter.notifyDataSetChanged();
//            lv.setAdapter(simpleAdapter);
        } catch (Exception e) {
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemToggleService: // Refresh from server
                UploadToServer uploadToServer = new UploadToServer(this, socialWifi);
                uploadToServer.execute();
                break;
            case R.id.list_value:
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (socialWifi.getWifi().isWifiEnabled() == false) {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            socialWifi.getWifi().setWifiEnabled(true);
        }

        switch (v.getId()) {
            case (R.id.buttonScan):
                socialWifi.getWifi().startScan();
//                Log.d(TAG, "wifi start scan");
                break;
            case (R.id.right):
                clickedWifi = (HashMap<String, String>) simpleAdapter.getItem((Integer) v.getTag());
                if (clickedWifi.get(EXISTS_KEY).equals("y")) {
                    Toast.makeText(this, "Error button clicked...\n" + clickedWifi.get(ITEM_KEY), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Upload button clicked...\n" + clickedWifi.get(ITEM_KEY), Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();
                    final View view= inflater.inflate(R.layout.upload_dialog, null);
                    builder.setView(view)
                            .setPositiveButton(R.string.uploadPassword, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    EditText password = (EditText) view.findViewById(R.id.dialog_password);
                                    UploadToServer uploadToServer = new UploadToServer(clickedWifi.get(ITEM_KEY),clickedWifi.get(BSSID_KEY),password.getText().toString(),context,socialWifi,clickedWifi.get(EXTRAS_KEY));
                                    uploadToServer.execute();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.cancelUploadPassword, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    TextView ssid =(TextView) view.findViewById(R.id.dialog_ssid);
                    ssid.setText(clickedWifi.get(ITEM_KEY));
                    Dialog uploadDialog = builder.create();
                    uploadDialog.show();
                }

                break;
            case (R.id.list_value):
                clickedWifi = (HashMap<String, String>) simpleAdapter.getItem((Integer) v.getTag());
                Toast.makeText(this, "Selected...\n" + clickedWifi.get(ITEM_KEY), Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
