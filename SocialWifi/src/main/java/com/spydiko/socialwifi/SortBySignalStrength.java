package com.spydiko.socialwifi;

import java.util.HashMap;

/**
 * Created by spiros on 10/17/13.
 */
public class SortBySignalStrength implements java.util.Comparator {
	private static final String TAG = SortBySignalStrength.class.getSimpleName();
	private String SIGNAL_KEY = "signal";

	@Override
	public int compare(Object lhs, Object rhs) {
		HashMap<String, String> left = (HashMap<String, String>) lhs;
		HashMap<String, String> right = (HashMap<String, String>) rhs;
		//		Log.d(TAG, "left: " + Integer.valueOf(left.get(SIGNAL_KEY)) + " right: " + Integer.valueOf(right.get(SIGNAL_KEY)));
		if (Integer.valueOf(left.get(SIGNAL_KEY)) > Integer.valueOf(right.get(SIGNAL_KEY)))
			return -1;
		else if (Integer.valueOf(left.get(SIGNAL_KEY)) < Integer.valueOf(right.get(SIGNAL_KEY)))
			return 1;
		else return 0;
	}
}
