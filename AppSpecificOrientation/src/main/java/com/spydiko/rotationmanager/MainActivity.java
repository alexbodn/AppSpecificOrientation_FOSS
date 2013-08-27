package com.spydiko.rotationmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener{

    private final static String TAG = MainActivity.class.getSimpleName();
    LinearLayout progBar;
    LinearLayout buttonsLayout;
    LinearLayout globalOrientation;
    //    boolean mExternalStorageAvailable;
    //    boolean mExternalStorageWriteable;
    private boolean test;
    private PackageManager packageManager;
    private InteractiveArrayAdapter adapter;
    private List<String> names;
    private ArrayList<Model> activities;
    private AppSpecificOrientation myapp;
    private Vibrator vibe;
    private ListView lv;
    private Button buttonClearAll;
    private ImageView orientationButton;
    private ContentObserver rotationObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (android.provider.Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
                orientationButton.setImageDrawable(getResources().getDrawable(R.drawable.tb_on));
                AppSpecificOrientation.setCheck_button(true);
            } else {
                orientationButton.setImageDrawable(getResources().getDrawable(R.drawable.tb_off));
                AppSpecificOrientation.setCheck_button(false);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "destroyed");
    }

    @Override
    protected void onStop() {
        super.onStop();


        Log.d(TAG, "stopped");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myapp = (AppSpecificOrientation) getApplication();
        buttonClearAll = (Button) findViewById(R.id.button2);
        vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        orientationButton = (ImageView) findViewById(R.id.orientationButton);
        globalOrientation = (LinearLayout) findViewById(R.id.globalOrientation);
        if (Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
            orientationButton.setImageDrawable(getResources().getDrawable(R.drawable.tb_on));
            AppSpecificOrientation.setCheck_button(true);
        } else {
            orientationButton.setImageDrawable(getResources().getDrawable(R.drawable.tb_off));
            AppSpecificOrientation.setCheck_button(false);
        }
        getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), true,
                rotationObserver);
        orientationButton.setOnClickListener(this);
        buttonClearAll.setOnClickListener(this);
        //Chech external storage
        /*mExternalStorageAvailable = false;
        mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        Log.d(TAG,""+mExternalStorageAvailable);
        Log.d(TAG,""+mExternalStorageWriteable);*/

        activities = new ArrayList<Model>();
        names = new ArrayList<String>();
        lv = (ListView) findViewById(R.id.appList);
        final ArrayList<Model> data = (ArrayList<Model>) getLastNonConfigurationInstance();
        if (data == null) {
            Log.d(TAG, "null");
            packageManager = getPackageManager();
            this.adapter = new InteractiveArrayAdapter(this, activities, (AppSpecificOrientation) getApplication());
            lv.setAdapter(adapter);
            UpdateData updateData = new UpdateData();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                updateData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
            else updateData.execute((Void[]) null);
        } else {
            Log.d(TAG, "ok");
            activities = data;
            buttonsLayout = (LinearLayout) findViewById(R.id.twoButtons);
            progBar = (LinearLayout) findViewById(R.id.channelsProgress);
            this.adapter = new InteractiveArrayAdapter(this, activities, (AppSpecificOrientation) getApplication());
            lv.setAdapter(adapter);
            progBar.setVisibility(View.GONE);
            lv.setVisibility(View.VISIBLE);
            buttonsLayout.setVisibility(View.VISIBLE);
            globalOrientation.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }


    }

    public Object onRetainNonConfigurationInstance() {
        Log.d(TAG, "onRetain");
        final ArrayList<Model> data = activities;
        return data;
    }

    protected void onResume() {
        super.onResume();
        //        MenuItem temp = (MenuItem) findViewById(R.id.itemToggleService);
        //        if (AppSpecificOrientation.isServiceRunning()) {
        //            temp.setTitle(R.string.titleServiceStart);
        //            temp.setIcon(android.R.drawable.ic_media_pause);
        //        } else {
        //            temp.setTitle(R.string.titleServiceStop);
        //            temp.setIcon(android.R.drawable.ic_media_play);
        //        }
        //registering our receiver
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregister our receiver
    }

    public void updateApps() {
        //		Log.d(TAG, "0");
        Intent localIntent = new Intent("android.intent.action.MAIN", null);
        localIntent.addCategory("android.intent.category.LAUNCHER");
        //		Log.d(TAG, "1");
        activities.clear();
        names.clear();
        packageManager = getPackageManager();
        //		Log.d(TAG, "2");
        List<ResolveInfo> rInfo = packageManager.queryIntentActivities(localIntent, 1);
        //		Log.d(TAG, "3");
        List<ApplicationInfo> packages = new ArrayList<ApplicationInfo>();
        //		Log.d(TAG, "4");

        for (ResolveInfo info : rInfo) {
            packages.add(info.activityInfo.applicationInfo);
        }
        Model temp;
        for (ApplicationInfo packageInfo : packages) {
            //			Log.d(TAG, "Installed package :" + packageInfo.packageName);
            if (names.contains(packageInfo.packageName)) {
                continue;
            }
            names.add(packageInfo.packageName);
            temp = new Model((String) packageManager.getApplicationLabel(packageInfo));
            temp.setPackageName(packageInfo.packageName);
            Drawable pic = packageInfo.loadIcon(packageManager);
            temp.setLabel(pic);
            //			Log.d(TAG, "Installed package :" + temp.getName());
            //temp.put(IS_CHECKED, true);
            if (myapp.loadPreferences(packageInfo.packageName, true)) temp.setSelectedPortrait(true);
            if (myapp.loadPreferences(packageInfo.packageName, false)) temp.setSelectedLandscape(true);
            activities.add(temp);
            //			Log.d(TAG, "Launch Activity :" + packageManager.getLaunchIntentForPackage(packageInfo.packageName));
        }
        Collections.sort(activities, new SortByString());
        //		Log.d(TAG, "END");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //		Log.d(TAG, "createOptions");
        if (AppSpecificOrientation.isServiceRunning()) {
            menu.findItem(R.id.itemToggleService).setTitle(R.string.titleServiceStart);
            menu.findItem(R.id.itemToggleService).setIcon(android.R.drawable.ic_media_pause);
        } else {
            menu.findItem(R.id.itemToggleService).setTitle(R.string.titleServiceStop);
            menu.findItem(R.id.itemToggleService).setIcon(android.R.drawable.ic_media_play);
        }
        if (AppSpecificOrientation.getBoot()) menu.findItem(R.id.setOnBoot).setChecked(true);
        else menu.findItem(R.id.setOnBoot).setChecked(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                //				Log.d(TAG, "action_settings");
                packageManager = getPackageManager();
                UpdateData updateData = new UpdateData();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    updateData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                else updateData.execute((Void[]) null);
                //				Log.d(TAG, "execute");
                break;
            case R.id.itemToggleService:
                //                Log.d(TAG, "entered");
                if (AppSpecificOrientation.isServiceRunning()) {
                    item.setTitle(R.string.titleServiceStop);
                    item.setIcon(android.R.drawable.ic_media_play);
                    stopService(new Intent(this, NewOrieService.class));
                    AppSpecificOrientation.setServiceRunning(false);
                    //                    Log.d(TAG, "if");
                } else {
                    item.setTitle(R.string.titleServiceStart);
                    item.setIcon(android.R.drawable.ic_media_pause);
                    startService(new Intent(this, NewOrieService.class));
                    //                    Log.d(TAG, "else");
                }
                break;

            case R.id.setOnBoot:
                if (AppSpecificOrientation.getBoot()) {
                    item.setChecked(false);
                    AppSpecificOrientation.setBoot(false);
                    //                    Log.d(TAG, "onBoot set to false");
                } else {
                    item.setChecked(true);
                    AppSpecificOrientation.setBoot(true);
                    //                    Log.d(TAG, "onBoot set to true");
                }

                break;
            case R.id.howTo:
                startActivityForResult((new Intent(this, HowToActivity.class)),1);
                break;
            case R.id.about:
                startActivityForResult((new Intent(this, AboutActivity.class)),1);
                break;
        }
        return true;
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        // Collect data from the intent and use it

    }

    @Override
    public void onClick(View view) {
        //        Button temp = (Button) view;
        ImageView tmp;
        vibe.vibrate(50);
        switch (view.getId()) {
            case (R.id.button2):
                for (Model mdl : activities) {
                    mdl.setSelectedPortrait(false);
                    mdl.setSelectedLandscape(false);
                }
                adapter.notifyDataSetChanged();
                break;
            case (R.id.orientationButton):
                tmp = (ImageView) findViewById(view.getId());
                if (AppSpecificOrientation.isCheck_button()) {
                    tmp.setImageDrawable(getResources().getDrawable(R.drawable.tb_on));
                    AppSpecificOrientation.setCheck_button(false);
                } else {
                    tmp.setImageDrawable(getResources().getDrawable(R.drawable.tb_off));
                    AppSpecificOrientation.setCheck_button(true);
                }
                Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, AppSpecificOrientation.isCheck_button() ? 1 : 0);
                break;
        }
    }
    public class UpdateData extends AsyncTask<Void, Void, Void> {
        LinearLayout progBar;
        LinearLayout buttonsLayout;

        @Override
        protected Void doInBackground(Void... voids) {
            //            Log.d(TAG, "doInBackground");
            updateApps();
            return null;
        }

        @Override
        protected void onPreExecute() {
            //            Log.d(TAG, "onPreExecute1");
            super.onPreExecute();
            //            Log.d(TAG, "onPreExecute2");
            buttonsLayout = (LinearLayout) findViewById(R.id.twoButtons);
            progBar = (LinearLayout) findViewById(R.id.channelsProgress);
            //            Log.d(TAG, "onPreExecute3");
            lv.setVisibility(View.GONE);
            globalOrientation.setVisibility(View.INVISIBLE);
            buttonsLayout.setVisibility(View.INVISIBLE);
            progBar.setVisibility(View.VISIBLE);
            //            Log.d(TAG, "onPreExecute3");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progBar.setVisibility(View.GONE);
            lv.setVisibility(View.VISIBLE);
            buttonsLayout.setVisibility(View.VISIBLE);
            globalOrientation.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

}
