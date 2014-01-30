/*******************************************************************************
 * Copyright (c) 2013 "Harsh Panchal" <panchal.harsh18@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package com.hasta.romcontrol;


import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String CRT_ANIM = "crt";
    private static final String FBDELAY = "/sys/module/fbearlysuspend/parameters/fbdelay";
    private static final String FBDELAY_MS = "/sys/module/fbearlysuspend/parameters/fbdelay_ms";
    private static final String INITD = "/system/etc/init.d";
    private static final String LOGGER = "/data/logger";
    private static final String FSYNCODE = "/sys/kernel/fsync/mode";
    private static final String FSYNC = "fsyn";
    
    
    private static ContentResolver cr;

   
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
         
        switch (item.getItemId())
        {
        case R.id.log:
            Intent intent = new Intent(getBaseContext(), Change1.class);
            startActivity(intent);
           
            return true;
        default:
            return super.onOptionsItemSelected(item);}
	   
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager
                                .beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();
        Log.i("harsh_debug", "===========Rom Control Launched===========");
    }
    
    public static class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    	
        
		@Override
        public void onCreate(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);
                    getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
                    addPreferencesFromResource(R.xml.main);
                    SharedPreferences sharedPref = getPreferenceScreen().getSharedPreferences();
                    cr = getActivity().getContentResolver();
                    onSharedPreferenceChanged(sharedPref,"crt_toggle");
                    onSharedPreferenceChanged(sharedPref,"log_toggle");
                    onSharedPreferenceChanged(sharedPref,"fsync_toggle");
        }

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sp,
				String key) {
			if(key.equals("crt_toggle")) handleCRT();
			if(key.equals("log_toggle")) handleLogger();
			if(key.equals("fsync_toggle")) handleFsync();
			
		}
		
		public void handleCRT() {
			final CheckBoxPreference crt_toggle = (CheckBoxPreference) findPreference("crt_toggle");
	        final File f = new File(FBDELAY);
	        int crt = getInt(CRT_ANIM, 0);
	        crt_toggle.setChecked(crt != 0);
	        crt_toggle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
	                public boolean onPreferenceClick(Preference preference) {
	                    if (crt_toggle.isChecked()) {
	                        putInt(CRT_ANIM, 1);
	                        Log.d("harsh_debug","crt=>1");
	                        new SU().execute("echo 1 > "+FBDELAY,"echo 350 > "+FBDELAY_MS);
	                        Utils.mountSystemRW();
	                        Utils.copyAssets("03_crt",INITD,777,getActivity().getApplicationContext());
	                        ShowToast("Animation enabled!");
	                    } else {
	                    	putInt(CRT_ANIM, 0);
	                        Log.d("harsh_debug","crt=>0");
	                        new SU().execute("echo 0 > "+FBDELAY,"echo 0 > "+FBDELAY_MS);
	                        Utils.mountSystemRW();
	                        Utils.copyAssets("99_crtoff",INITD,777,getActivity().getApplicationContext());
	                        ShowToast("Animation disabled!");
	                    }
	                    return false;
	                }
	        });
	        if(!f.exists()) {
	            crt_toggle.setSummary("Unsupported kernel");
	            crt_toggle.setEnabled(false);
				Log.d("harsh_debug","CRT Animation not supported due to unsupported Kernel");
				putInt(CRT_ANIM, 0);
	        }
		}
		public void handleFsync() {
			final CheckBoxPreference fsync_toggle = (CheckBoxPreference) findPreference("fsync_toggle");
	        final File l = new File(FSYNCODE);
	        final int fs = getInt(FSYNC, 0);
	        fsync_toggle.setChecked(fs != 0);
	        fsync_toggle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
	                public boolean onPreferenceClick(Preference preference) {
	                    if (fsync_toggle.isChecked()) {
	                        putInt(FSYNC, 1);
	                        Log.d("harsh_debug","FSYNC=>1");
	                        new SU().execute("echo 1 > "+FSYNCODE);
	                        Utils.mountSystemRW();
	                        Utils.copyAssets("05_fsync",INITD,777,getActivity().getApplicationContext());
	                        ShowToast("Fsync disabled");
	                    } else {
	                    	putInt(FSYNC, 0);
	                        Log.d("harsh_debug","fsync=>0");
	                        new SU().execute("echo 0 > "+FSYNCODE);
	                        Utils.mountSystemRW();
	                        Utils.copyAssets("02_fsync",INITD,777,getActivity().getApplicationContext());
	                        ShowToast("Fsync enabled");
	                    }
	                    return false;
	                }
	        });
	        if(!l.exists()) {
	            fsync_toggle.setSummary("Unsupported kernel");
	            fsync_toggle.setEnabled(false);
				Log.d("harsh_debug","llll");
				putInt(FSYNC, 0);
	        }
		}
		
		
		public void handleLogger() {
	        final CheckBoxPreference logger = (CheckBoxPreference) findPreference("log_toggle");
	        int cocore = Utils.SU_retVal("echo $(uname -r) | grep -i -q cocore");
	        final File log_enable = new File(LOGGER);
	        logger.setChecked(log_enable.exists());
	        if (cocore == 0)
	            logger.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
	                public boolean onPreferenceClick(Preference preference) {
	                    if (logger.isChecked()) {
	                        new SU().execute("touch "+LOGGER,"chmod 777 "+LOGGER);
	                        Log.d("harsh_debug","logger enabled");
	                        ShowToast("Logger enabled");
	                    } else {
	                        new SU().execute("rm "+LOGGER);
	                        Log.d("harsh_debug","logger disabled");
	                        ShowToast("Logger disabled");
	                    }
	                    return false;
	                }
	            });
	        else {
	            logger.setEnabled(false);
	            logger.setSummary("Unsupported kernel");
	            Log.e("harsh_debug","Logger:not supported");
	        }
	    }
		
		
		public void showDialog(String title, String msg) {
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setTitle(title);
	        builder.setMessage(msg);
	        builder.setCancelable(false);
	        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {}
	        });
	        AlertDialog dialog = builder.create();
	        dialog.show();
	    }
		
		 public void ShowToast(String msg) {
		        Toast.makeText(getActivity().getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
		    }
	    public int getInt(String key, int defValue) {
	    	return Settings.System.getInt(cr, key, defValue);
	    }
	    
	    public void putInt(String key, int val) {
	    	Settings.System.putInt(cr, key, val);
	    }
	}
}
    
  