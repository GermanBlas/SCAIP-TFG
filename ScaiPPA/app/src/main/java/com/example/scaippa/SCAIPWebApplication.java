package com.example.scaippa;

import android.app.Application;

import com.example.scaippa.SCAIP.SCAIPListener;

public class SCAIPWebApplication  extends Application{

    private static SCAIPWebApplication application;
    private com.example.scaippa.SCAIP.SCAIPListener SCAIPListener;

    public void onCreate() {
        super.onCreate();
        application = this;
    }
    public synchronized static SCAIPWebApplication getInstance() { return application; }

    public void setSCAIPListener (SCAIPListener SCAIPListener) { this.SCAIPListener = SCAIPListener; }
    public SCAIPListener getSCAIPListener() {return SCAIPListener;}
}
