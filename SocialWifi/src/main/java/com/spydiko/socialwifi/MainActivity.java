package com.spydiko.socialwifi;

import android.app.Activity;
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
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class MainActivity extends Activity implements View.OnClickListener, PullToRefreshAttacher.OnRefreshListener {

	private final static String TAG = MainActivity.class.getSimpleName();
	private SocialWifi socialWifi;
	//	private Button buttonScan;
	private SimpleAdapter simpleAdapter;
	private ArrayList<HashMap<String, String>> arrayList;
	private HashMap<String, String> clickedWifi;
	private ListView lv;
	private String ITEM_KEY = "key", BSSID_KEY = "bssid", EXISTS_KEY = "exist", EXTRAS_KEY = "extra", IMAGE_KEY = "image", SIGNAL_KEY = "signal";
	private BroadcastReceiver broadcastReceiver;
	private FileOutputStream outputStream;
	private Context context;
	private TextView usernameTextView, swipe2Refresh;
	private CheckBox showPasswordCB;
	private PullToRefreshAttacher mPullToRefreshAttacher;

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
//		try {
//			File file = new File(getFilesDir(), "server.xml");
//			boolean deleted = file.delete();
//			Log.d(TAG, "server deletion is " + deleted);
//			outputStream.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
    }

	@Override
	protected void onPause() {
		super.onPause();
		try {
			outputStream = openFileOutput("local.xml", Context.MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(outputStream);
			socialWifi.createXMLString(osw, socialWifi.getWifies());
			Log.d(TAG, "create XML file");
		} catch (Exception e) {
			e.printStackTrace();
		}
		unregisterReceiver(broadcastReceiver);
		SocialWifi.editor.commit();
		Log.d(TAG, "onPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		Log.d(TAG, "onResume");
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {

			if (resultCode == RESULT_OK) {
				socialWifi.setSharedPreferenceBoolean("notFirstTime", true);
			}
			if (resultCode == RESULT_CANCELED) {
				finish();
			}

			usernameTextView.setText(socialWifi.getSharedPreferenceString("username") + " ");
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		socialWifi = (SocialWifi) getApplication();
		usernameTextView = (TextView) findViewById(R.id.usernameTextView);
		swipe2Refresh = (TextView) findViewById(R.id.swipe2refresh);
		if (!socialWifi.getSharedPreferenceBoolean("notFirstTime")) {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivityForResult(intent, 1);
		}
		//		buttonScan = (Button) findViewById(R.id.buttonScan);
		//		buttonScan.setOnClickListener(this);
		context = this;

		//		if (!socialWifi.getSharedPreferenceString("username").equals("")) {
		//			Toast.makeText(this, "Welcome " + socialWifi.getSharedPreferenceString("username"), Toast.LENGTH_LONG).show();
		//		}

		usernameTextView.setText(socialWifi.getSharedPreferenceString("username") + " ");
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
		simpleAdapter = new SimpleAdapter(this, arrayList, R.layout.row, new String[]{ITEM_KEY, IMAGE_KEY}, new int[]{R.id.list_value, R.id.signal}) {
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
				} else {
					list_value.setTextColor(Color.parseColor("#FFFFFF"));
					list_value.setBackgroundResource(android.R.color.transparent);
					list_value.setOnClickListener(null);
					right.setImageResource(android.R.drawable.ic_menu_upload);
				}
				return row;
			}

		};
		//        simpleAdapter = new InteractiveSimpleAdapter(this, arrayList, R.layout.row, new String[]{ITEM_KEY}, new int[]{R.id.list_value},socialWifi,this);

/*        File file = new File(getFilesDir(), "server.xml");
        if (file.exists()) {
            socialWifi.setWifies(socialWifi.readFromXML("server.xml"));
//            Log.d (TAG,"exists");
        } else {
//            Log.d(TAG,"doesn't exist");
        }*/

		//        wifies=socialWifi.ReadFromXML("pass.xml");

		broadcastReceiver = new

				BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						swipe2Refresh.setVisibility(View.GONE);
						update();
						if (arrayList.size() == 0) {
							Log.d(TAG, "No Wifi networks found...");
							Toast.makeText(context, "No Wifi networks found...", Toast.LENGTH_LONG).show();
							swipe2Refresh.setVisibility(View.VISIBLE);
						} else {
							lv.setAdapter(simpleAdapter);
							simpleAdapter.notifyDataSetChanged();
							mPullToRefreshAttacher.setRefreshComplete();
						}
					}
				};

		// Create a PullToRefreshAttacher instance
		mPullToRefreshAttacher = PullToRefreshAttacher.get(this);

		// Retrieve the PullToRefreshLayout from the content view
		PullToRefreshLayout ptrLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);

		// Give the PullToRefreshAttacher to the PullToRefreshLayout, along with a refresh listener.
		ptrLayout.setPullToRefreshAttacher(mPullToRefreshAttacher, this);

	}

	public void update() {
		List<ScanResult> results = socialWifi.getWifi().getScanResults();
		int size = results.size();

		boolean containsFlag = false;
		for (WifiPass wifi : socialWifi.getWifies()) {
			Log.d(TAG, "Update: " + wifi.getSsid() + " " + wifi.getPassword() + " " + wifi.getBssid());

		}
		//        Log.d(TAG, "the size is " + size);
		arrayList.clear();
		try {
			size = size - 1;
			while (size >= 0) {
				containsFlag = false;
				HashMap<String, String> item = new HashMap<String, String>();
				item.put(ITEM_KEY, results.get(size).SSID);
//				Log.d(TAG, "level (db) of " + results.get(size).SSID + " :" + results.get(size).level);
                item.put(SIGNAL_KEY, Integer.toString(results.get(size).level));
				if (results.get(size).level > -60)
					item.put(IMAGE_KEY, Integer.toString(R.drawable.wifi_full));
				else if (results.get(size).level > -80)
					item.put(IMAGE_KEY, Integer.toString(R.drawable.wifi_good));
				else if (results.get(size).level > -90)
					item.put(IMAGE_KEY, Integer.toString(R.drawable.wifi_weak));
				else
					item.put(IMAGE_KEY, Integer.toString(R.drawable.wifi_no_signal));
				item.put(EXTRAS_KEY, results.get(size).capabilities + "\n" + results.get(size).level);
				item.put(BSSID_KEY, results.get(size).BSSID);
				item.put(EXISTS_KEY, "n");
				for (int i = 0; i < arrayList.size(); i++) {
					if (arrayList.get(i).containsValue(item.get(ITEM_KEY).trim())) {
//						Log.d(TAG, "Same SSID: " + item.get(ITEM_KEY));
                        containsFlag = true;
					}
				}
				size--;
				if (containsFlag) continue;
				arrayList.add(item);
				for (WifiPass wifi : socialWifi.getWifies()) {
					if (item.get(BSSID_KEY).contains(wifi.getBssid())) {
						item.put(EXISTS_KEY, "y");
						//todo Check ssid
					}
				}
			}
			Collections.sort(arrayList, new SortBySignalStrength());
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
				socialWifi.getLocation();
				UploadToServer uploadToServer = new UploadToServer(this, socialWifi);
				uploadToServer.execute();
				break;
			case R.id.preferences:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.map_pref:
				startActivity(new Intent(this, Map.class));
				break;
			case R.id.logout:
				socialWifi.logout();
				Intent intent = new Intent(this, LoginActivity.class);
				startActivityForResult(intent, 1);
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
			//			case (R.id.buttonScan):
			//				socialWifi.getWifi().startScan();
			//                Log.d(TAG, "wifi start scan");
			//				break;
			case (R.id.right):
				clickedWifi = (HashMap<String, String>) simpleAdapter.getItem((Integer) v.getTag());
				if (clickedWifi.get(EXISTS_KEY).equals("y")) {
					Toast.makeText(this, "Error button clicked...\n" + clickedWifi.get(ITEM_KEY), Toast.LENGTH_SHORT).show();
					reportNewPassword();
				} else {
					Toast.makeText(this, "Upload button clicked...\n" + clickedWifi.get(ITEM_KEY), Toast.LENGTH_SHORT).show();
					uploadPassword();
				}
				break;
			case (R.id.list_value):
				clickedWifi = (HashMap<String, String>) simpleAdapter.getItem((Integer) v.getTag());
				connectWithPassword();
				break;
			default:
				break;
		}
	}

	/**
	 * Using the credentials provided by the server, attempt to connect to the selected network.
	 */
	public void connectWithPassword() {
		int typeOfEncryption = 0;
		String extraInfo = clickedWifi.get(EXTRAS_KEY);
		String ssid = clickedWifi.get(ITEM_KEY);
		String bssid = clickedWifi.get(BSSID_KEY);
		String password = null;
		for (WifiPass wifiPass : socialWifi.getWifies()) {
			if (wifiPass.getSsid().equals(ssid)) {
				if (wifiPass.getBssid().equals(bssid)) {
					password = wifiPass.getPassword();
				}
			}
		}
		if (extraInfo.contains("WEP")) typeOfEncryption = 1;
		else if (extraInfo.contains("WPA2")) typeOfEncryption = 3;
		else if (extraInfo.contains("WAP") || extraInfo.contains("WPA")) typeOfEncryption = 2;
		socialWifi.removeNetwork(ssid);
		socialWifi.connect(ssid, password, typeOfEncryption);
		//                Toast.makeText(this, "Selected...\n" + clickedWifi.get(ITEM_KEY), Toast.LENGTH_SHORT).show();
	}

	/**
	 * Open a dialog to input the password and upload the password to the server.
	 */
	public void uploadPassword() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View view = inflater.inflate(R.layout.upload_dialog, null);
		showPasswordCB = (CheckBox) view.findViewById(R.id.show_password);
		showPasswordCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				EditText password = (EditText) view.findViewById(R.id.dialog_password);
				if (b) {
					password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				} else {
					password.setInputType(129);
				}
			}
		});
		builder.setView(view)
				.setPositiveButton(R.string.uploadPassword, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						EditText password = (EditText) view.findViewById(R.id.dialog_password);
						socialWifi.getLocation();
						UploadToServer uploadToServer = new UploadToServer(clickedWifi.get(ITEM_KEY), clickedWifi.get(BSSID_KEY), password.getText().toString(), context, socialWifi, clickedWifi.get(EXTRAS_KEY));
						uploadToServer.execute();
						dialog.dismiss();
					}
				})
				.setNegativeButton(R.string.cancelUploadPassword, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		TextView ssid = (TextView) view.findViewById(R.id.dialog_ssid);
		ssid.setText(clickedWifi.get(ITEM_KEY));
		Dialog uploadDialog = builder.create();
		uploadDialog.show();
	}

	/**
	 * Send the new, corrected password to the server, considering current password is not working.
	 */
	public void reportNewPassword() {
		//todo update the server with new credentials for this network
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View view = inflater.inflate(R.layout.upload_dialog, null);
		showPasswordCB = (CheckBox) view.findViewById(R.id.show_password);
		showPasswordCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				EditText password = (EditText) view.findViewById(R.id.dialog_password);
				if (b) {
					password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				} else {
					password.setInputType(129);
				}
			}
		});
		builder.setView(view)
				.setPositiveButton(R.string.uploadPassword, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						EditText password = (EditText) view.findViewById(R.id.dialog_password);
						socialWifi.getLocation();
						UploadToServer uploadToServer = new UploadToServer(clickedWifi.get(ITEM_KEY), clickedWifi.get(BSSID_KEY), password.getText().toString(), context, socialWifi, clickedWifi.get(EXTRAS_KEY), true);
						uploadToServer.execute();
						dialog.dismiss();
					}
				})
				.setNegativeButton(R.string.cancelUploadPassword, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		TextView ssid = (TextView) view.findViewById(R.id.dialog_ssid);
		ssid.setText(clickedWifi.get(ITEM_KEY));
		Dialog uploadDialog = builder.create();
		uploadDialog.show();

	}

	@Override
	public void onRefreshStarted(View view) {
		if (socialWifi.getWifi().isWifiEnabled() == false) {
			Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
			socialWifi.getWifi().setWifiEnabled(true);
		}
		socialWifi.getWifi().startScan();
	}
}
