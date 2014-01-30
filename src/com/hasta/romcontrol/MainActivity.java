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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String CRT_ANIM = "crt";
    private static final String FBDELAY = "/sys/module/fbearlysuspend/parameters/fbdelay";
    private static final String FBDELAY_MS = "/sys/module/fbearlysuspend/parameters/fbdelay_ms";
    private static final String INITD = "/system/etc/init.d";
    private static final String FSYNCODE = "/sys/kernel/fsync/mode";
    private static final String FSYNC = "fsyn";
    private static final String TWEAKS = "tweaks";
    private static final String TWEAKS2 = "tweaks2";
    private static final String TWEAKS3 = "tweaks3";
    private static final String TWEAKS4 = "tweaks4";
    private static final String TWEAKS5 = "tweaks5";
    private static final String TWEAKS6 = "tweaks6";
    
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
            Intent intent = new Intent(getBaseContext(), Changelog.class);
            startActivity(intent);
           
            return true;
        case R.id.about:
        	Toast.makeText(getApplicationContext(), 
                    "By hastalafiesta, check out my github!!!", Toast.LENGTH_LONG).show();
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
                    onSharedPreferenceChanged(sharedPref,"fsync_toggle");
                    onSharedPreferenceChanged(sharedPref,"cputweaks_toggle");
                    onSharedPreferenceChanged(sharedPref,"battweaks_toggle");
                    onSharedPreferenceChanged(sharedPref,"perftweaks_toggle");
                    onSharedPreferenceChanged(sharedPref,"vac_toggle");
                    onSharedPreferenceChanged(sharedPref,"sdtweaks_toggle");
                    onSharedPreferenceChanged(sharedPref,"nettweaks_toggle");
        }

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sp,
				String key) {
			if(key.equals("crt_toggle")) handleCRT();
			if(key.equals("fsync_toggle")) handleFsync();
			if(key.equals("cputweaks_toggle")) handleCPU();
			if(key.equals("battweaks_toggle")) handleBATT();
			if(key.equals("perftweaks_toggle")) handlePERF();
			if(key.equals("sdtweaks_toggle")) handleSD();
			if(key.equals("vac_toggle")) handleVAC();
			if(key.equals("nettweaks_toggle")) handleNET();
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
	                        new SU().execute("echo 1 > "+FBDELAY,"echo 350 > "+FBDELAY_MS);
	                        Utils.mountSystemRW();
	                        Utils.copyAssets("crt_on",INITD,777,getActivity().getApplicationContext());
	                        ShowToast("Animation enabled!");
	                    } else {
	                    	putInt(CRT_ANIM, 0);	                        
	                        new SU().execute("echo 0 > "+FBDELAY,"echo 0 > "+FBDELAY_MS);
	                        Utils.mountSystemRW();
	                        Utils.copyAssets("crt_off",INITD,777,getActivity().getApplicationContext());
	                        ShowToast("Animation disabled!");
	                    }
	                    return false;
	                }
	        });
	        if(!f.exists()) {
	            crt_toggle.setSummary("Unsupported kernel");
	            crt_toggle.setEnabled(false);
				
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
	                        new SU().execute("echo 1 > "+FSYNCODE);
	                        Utils.mountSystemRW();
	                        Utils.copyAssets("fsync_off",INITD,777,getActivity().getApplicationContext());
	                        ShowToast("Fsync disabled");
	                    } else {
	                    	putInt(FSYNC, 0);
	                        new SU().execute("echo 0 > "+FSYNCODE);
	                        Utils.mountSystemRW();
	                        Utils.copyAssets("fsync_on",INITD,777,getActivity().getApplicationContext());
	                        ShowToast("Fsync enabled");
	                    }
	                    return false;
	                }
	        });
	        if(!l.exists()) {
	            fsync_toggle.setSummary("Unsupported kernel");
	            fsync_toggle.setEnabled(false);
				putInt(FSYNC, 0);
	        }
		}
		public void handleCPU(){
			final CheckBoxPreference cputweaks_toggle = (CheckBoxPreference) findPreference("cputweaks_toggle");
	        final File a = new File(INITD);
	        final int tw = getInt(TWEAKS, 0);
	        cputweaks_toggle.setChecked(tw != 0);
	        cputweaks_toggle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
	                public boolean onPreferenceClick(Preference preference) {
	                    if (cputweaks_toggle.isChecked()) {
	                        putInt(TWEAKS, 1);
	                        Utils.mountSystemRW();
	                        Utils.copyAssets("cpu_tweaks",INITD,777,getActivity().getApplicationContext());
	                        ShowToast("Tweaks enabled, now reboot!");
	                    } else {
	                    	putInt(TWEAKS, 0);
	                        Utils.mountSystemRW();
	                        new SU().execute("rm /system/etc/init.d/cpu_tweaks");
	                        ShowToast("Tweaks removed, now reboot!");
	                    }
	                    return false;
	                }
	        });
	        if(!a.exists()) {
	            cputweaks_toggle.setSummary("Unsupported kernel");
	            cputweaks_toggle.setEnabled(false);
				putInt(TWEAKS, 0);
	        }
	        }
	       
	        public void handleBATT(){
				final CheckBoxPreference battweaks_toggle = (CheckBoxPreference) findPreference("battweaks_toggle");
		        final File b = new File(INITD);
		        final int tw2 = getInt(TWEAKS2, 0);
		        battweaks_toggle.setChecked(tw2 != 0);
		        battweaks_toggle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
		                public boolean onPreferenceClick(Preference preference) {
		                    if (battweaks_toggle.isChecked()) {
		                        putInt(TWEAKS2, 1);
		                        Utils.mountSystemRW();
		                        Utils.copyAssets("bat",INITD,777,getActivity().getApplicationContext());
		                        ShowToast("Battery tweaks enabled, changes will be applied after reboot!");
		                    } else {
		                    	putInt(TWEAKS2, 0);
		                        Utils.mountSystemRW();
		                        new SU().execute("rm /system/etc/init.d/bat");
		                        ShowToast("Battery tweaks removed!");
		                    }
		                    return false;
		                }
		        });
		        if(!b.exists()) {
		            battweaks_toggle.setSummary("Unsupported kernel");
		            battweaks_toggle.setEnabled(false);
					putInt(TWEAKS2, 0);
		        }
		}
	        public void handlePERF(){
				final CheckBoxPreference perftweaks_toggle = (CheckBoxPreference) findPreference("perftweaks_toggle");
		        final File c = new File(INITD);
		        final int tw3 = getInt(TWEAKS3, 0);
		        perftweaks_toggle.setChecked(tw3 != 0);
		        perftweaks_toggle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
		                public boolean onPreferenceClick(Preference preference) {
		                    if (perftweaks_toggle.isChecked()) {
		                        putInt(TWEAKS3, 1);
		                        Utils.mountSystemRW();
		                        Utils.copyAssets("perf",INITD,777,getActivity().getApplicationContext());
		                        ShowToast("Performance tweaks enabled, changes will be applied after reboot!");
		                    } else {
		                    	putInt(TWEAKS3, 0);
		                        Utils.mountSystemRW();
		                        new SU().execute("rm /system/etc/init.d/perf");
		                        ShowToast("Performance tweaks removed!");
		                    }
		                    return false;
		                }
		        });
		        if(!c.exists()) {
		            perftweaks_toggle.setSummary("Unsupported kernel");
		            perftweaks_toggle.setEnabled(false);
					putInt(TWEAKS3, 0);
		        }
		}
	        
	        public void handleSD(){
				final CheckBoxPreference sdtweaks_toggle = (CheckBoxPreference) findPreference("sdtweaks_toggle");
		        final File d = new File(INITD);
		        final int tw4 = getInt(TWEAKS4, 0);
		        sdtweaks_toggle.setChecked(tw4 != 0);
		        sdtweaks_toggle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
		                public boolean onPreferenceClick(Preference preference) {
		                    if (sdtweaks_toggle.isChecked()) {
		                        putInt(TWEAKS4, 1);
		                        Utils.mountSystemRW();
		                        Utils.copyAssets("sd",INITD,777,getActivity().getApplicationContext());
		                        ShowToast("SD tweaks enabled, changes will be applied after reboot!");
		                    } else {
		                    	putInt(TWEAKS4, 0);
		                        Utils.mountSystemRW();
		                        new SU().execute("rm /system/etc/init.d/perf");
		                        ShowToast("SD tweaks removed!");
		                    }
		                    return false;
		                }
		        });
		        if(!d.exists()) {
		            sdtweaks_toggle.setSummary("Unsupported kernel");
		            sdtweaks_toggle.setEnabled(false);
					putInt(TWEAKS4, 0);
		        }
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
    
  