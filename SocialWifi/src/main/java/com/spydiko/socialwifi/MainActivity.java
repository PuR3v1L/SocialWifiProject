package com.spydiko.socialwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private SocialWifi socialWifi;
    private Button buttonScan;
    private XmlSerializer xs;
    private SimpleAdapter simpleAdapter;
    private ArrayList<HashMap<String,String>> arrayList;
    private ArrayList<WifiPass> wifies;
    private final static String TAG = MainActivity.class.getSimpleName();
    private ListView lv;
    private String ITEM_KEY="key", BSSID_KEY="bssid", EXISTS_KEY="exist", EXTRAS_KEY="extra";
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onStop() {
        unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socialWifi = (SocialWifi) getApplication();
        buttonScan = (Button) findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(this);
        wifies = new ArrayList<WifiPass>();
        arrayList = new ArrayList<HashMap<String, String>>();
        lv = (ListView) findViewById(R.id.list_scan);
        simpleAdapter = new SimpleAdapter(this,arrayList,R.layout.row,new String[] {ITEM_KEY}, new int[] {R.id.list_value}){
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                NoParentPressImageView right = (NoParentPressImageView) row.findViewById(R.id.right);
                right.setTag(position);
                right.setOnClickListener(MainActivity.this);
                NoParentPressTextView list_value = (NoParentPressTextView) row.findViewById(R.id.list_value);
                list_value.setTag(position);
                if (arrayList.get(position).get(EXISTS_KEY).equals("y")){
                    list_value.setTextColor(Color.parseColor("#00FF00"));
                    list_value.setBackgroundResource(R.drawable.mybutton);
                    list_value.setOnClickListener(MainActivity.this);
                    right.setImageResource(android.R.drawable.ic_dialog_alert);
                }
                return row;
            }
        };
        // ----------- Read all the saved wifi --------------
        File file = new File(getFilesDir(),"pass.xml");
        if(file.exists()){
            wifies=socialWifi.ReadFromXML("pass.xml");
//            Log.d (TAG,"exists");
        }
        else{
//            Log.d(TAG,"doesn't exist");
        }

//        wifies=socialWifi.ReadFromXML("pass.xml");
        // --------------------------------------------------
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                update();
                lv.setAdapter(simpleAdapter);
                simpleAdapter.notifyDataSetChanged();
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void update() {
        List<ScanResult> results = socialWifi.getWifi().getScanResults();
        int size = results.size();
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
                for (WifiPass wifi : wifies){
                    if (item.get(BSSID_KEY).contains(wifi.getBssid())){
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
        HashMap<String,String> temp;
        switch (v.getId()){
            case (R.id.buttonScan):
                socialWifi.getWifi().startScan();
//                Log.d(TAG, "wifi start scan");
                break;
            case (R.id.right):
                temp =(HashMap<String,String>) simpleAdapter.getItem((Integer) v.getTag());
                if (temp.get(EXISTS_KEY).equals("y")){
                    Toast.makeText(this, "Error button clicked...\n"+temp.get(ITEM_KEY), Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(this, "Upload button clicked...\n"+temp.get(ITEM_KEY), Toast.LENGTH_SHORT).show();
                break;
            case (R.id.list_value):
                temp =(HashMap<String,String>) simpleAdapter.getItem((Integer) v.getTag());
                Toast.makeText(this, "Selected...\n"+temp.get(ITEM_KEY), Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
