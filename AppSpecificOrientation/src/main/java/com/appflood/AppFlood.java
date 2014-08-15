package com.appflood;

import org.json.JSONObject;

/**
 * Created by aleix on 6/08/14.
 */
public class AppFlood {
    public static boolean AD_ALL;

    public static class AFRequestDelegate{
        public AFRequestDelegate(Object... args){};

        public void onFinish(JSONObject arg0){};
    }

    public static void initialize(Object... args){};
    public static void preload(Object... args){};

}
