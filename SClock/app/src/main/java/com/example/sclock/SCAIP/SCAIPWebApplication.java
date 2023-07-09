package com.example.sclock.SCAIP;

import android.app.Application;

public class SCAIPWebApplication extends Application{

    private static SCAIPWebApplication application;
    private SCAIPListener SCAIPListener;
    private SCAIPConstruct SCAIPConstruct;

    public void onCreate() {
        super.onCreate();
        application = this;
    }
    public synchronized static SCAIPWebApplication getInstance() { return application; }

    public void setSCAIPListener (SCAIPListener SCAIPListener) { this.SCAIPListener = SCAIPListener; }
    public SCAIPListener getSCAIPListener() {return SCAIPListener;}

    public void setSCAIPConstruct (SCAIPConstruct SCAIPConstruct) { this.SCAIPConstruct = SCAIPConstruct; }
    public SCAIPConstruct getSCAIPConstruct() {return SCAIPConstruct;}
}
