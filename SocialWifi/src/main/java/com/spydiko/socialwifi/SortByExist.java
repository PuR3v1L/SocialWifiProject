package com.spydiko.socialwifi;

import java.util.HashMap;

/**
 * Created by jim on 1/10/2013.
 */
public class SortByExist implements java.util.Comparator {
    private String EXISTS_KEY="exist";
    @Override
    public int compare(Object lhs, Object rhs) {
        HashMap<String,String> left = (HashMap<String,String>) lhs;
        HashMap<String,String> right = (HashMap<String,String>) rhs;
        if (left.get(EXISTS_KEY).equals("y") && right.get(EXISTS_KEY).equals("n")) return -1;
        else if (left.get(EXISTS_KEY).equals("n") && right.get(EXISTS_KEY).equals("y")) return 1;
        else return 0;
    }
}
