package com.spydiko.socialwifi;

import java.util.List;

/**
 * Created by jim on 1/10/2013.
 */
public class WifiPass {
    private String ssid;
    private String bssid;
    private String password;
    private List<Double> geo;

    public WifiPass (String ssid, String bssid, String password, List<Double> geo){
        this.ssid = ssid;
        this.bssid = bssid;
        this.password = password;
        this.geo = geo;
    }

    public WifiPass (String ssid, String password){
        this.ssid = ssid;
        this.password = password;
        this.geo = null;
        this.bssid = null;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Double> getGeo() {
        return geo;
    }

    public void setGeo(List<Double> geo) {
        this.geo = geo;
    }
}
