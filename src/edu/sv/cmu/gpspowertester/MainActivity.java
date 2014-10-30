package edu.sv.cmu.gpspowertester;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	
	Double Lon = null, Lat = null;
	
	LocationListener gpslistener;
	LocationManager locman;
	
	Boolean is_gps_scanning = false;
	
	long last_gps_update_at = 0;
	long last_battery_update_at = 0;
	long app_started_at = 0;
	
	float battery_level = (float) -1.0;
	float battery_level_at_start = (float) -1.0;
	
	long total_gps_updates = 0;
	
	
	public TextView gps_is_on_textview, last_gps_update_textview, lat_textview, lon_textview,
					battery_level_textview, battery_level_updated_textview,
					battery_level_at_start_textview, started_at_textview,
					total_gps_updates_textview, gps_updates_per_second_textview,
					discharge_rate_textview;

	IntentFilter battery_intent_filter;
	Intent battery_status;
	
	Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.w("gpspowertester", "Main activity started");
		app_started_at = System.currentTimeMillis();
		
		
		findUIElements();
		startSGPScanning();
		
		
		context = getBaseContext();
		
		battery_intent_filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		battery_status = context.registerReceiver(null, battery_intent_filter);
		
		int level = battery_status.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = battery_status.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		battery_level_at_start = level / (float)scale;

		BatteryLevelGetter to_run = new BatteryLevelGetter();
		int interval = 500; //ms
		to_run.interval = interval;
		Handler handler = new Handler();
		handler.postDelayed(to_run, interval);
		Log.w("gpspowertester", "Handler posted onStarted");
		
		updateUI();

		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	public void startSGPScanning() {
		gpslistener = new MyGPSListener();
		locman = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpslistener);
		is_gps_scanning = true;
		updateUI();
	}
	
	
	public class MyGPSListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			Lat = Double.valueOf(location.getLatitude());
			Lon = Double.valueOf(location.getLongitude());
			last_gps_update_at = System.currentTimeMillis();
			total_gps_updates += 1;
			updateUI();
		}
		
		@Override
			public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}
		
		@Override
			public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}
		
		@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}
	}
	
	public void findUIElements() {
		gps_is_on_textview = (TextView)findViewById(R.id.gps_is_on_textview);
		last_gps_update_textview = (TextView)findViewById(R.id.last_gps_update_textview);
		lat_textview = (TextView)findViewById(R.id.lat_textview);
		lon_textview = (TextView)findViewById(R.id.lon_textview);
		battery_level_textview = (TextView)findViewById(R.id.battery_level_textview);
		battery_level_updated_textview = (TextView)findViewById(R.id.battery_level_updated_textview);
		battery_level_at_start_textview = (TextView)findViewById(R.id.battery_level_at_start_textview);
		started_at_textview = (TextView)findViewById(R.id.started_at_textview);
		total_gps_updates_textview = (TextView)findViewById(R.id.total_gps_updates_textview);
		gps_updates_per_second_textview = (TextView)findViewById(R.id.gps_updates_per_second_textview);
		discharge_rate_textview = (TextView)findViewById(R.id.discharge_rate_textview);

		
	}
	
	public void updateUI(){
		if(is_gps_scanning) {
			gps_is_on_textview.setText("GPS is on");
		} else {
			gps_is_on_textview.setText("GPS is off");
		}
		
		if(last_gps_update_at > 0) {
			last_gps_update_textview.setText("Last GPS update at " + Long.valueOf(last_gps_update_at).toString() + " (" + Long.valueOf((System.currentTimeMillis() - last_gps_update_at)/1000).toString() + " seconds ago)");
		}
		
		if (Lon != null && Lat != null) {
			lat_textview.setText("Latitude: " + Double.toString(Lat));
			lon_textview.setText("Longitude: " + Double.toString(Lon));
		}
		
		if(battery_level >= 0) {
			battery_level_textview.setText("Battery level: " + Float.toString(battery_level));
		}
		
		if(last_battery_update_at > 0) {
			battery_level_updated_textview.setText("Last battery level update at " + Long.valueOf(last_battery_update_at).toString() + " (" + Long.valueOf((System.currentTimeMillis() - last_battery_update_at)/1000).toString() + " seconds ago)");
		}
		
		battery_level_at_start_textview.setText("Battery level at start: " + Float.toString(battery_level_at_start));
		started_at_textview.setText("App started at: " + Long.toString(app_started_at) + " (running for " + Long.toString((System.currentTimeMillis() - app_started_at)/1000) + " seconds)");
		total_gps_updates_textview.setText("Total GPS updates: " + Long.toString(total_gps_updates));
		
		long app_run_time = (System.currentTimeMillis() - app_started_at)/1000;
		if(app_run_time > 0) {
			gps_updates_per_second_textview.setText("GPS updates per second: " + Double.toString(total_gps_updates / (double)app_run_time));
			float dcharge = battery_level_at_start - battery_level;
			if (dcharge > 0.0) {
				discharge_rate_textview.setText("Discharge rate: " + Float.toString((float)100.0 * dcharge / app_run_time) + "% per second");
			}
		}
		
	}
	
	public class BatteryLevelGetter implements Runnable {

		public Integer interval;
		
		@Override
		public void run() {
			battery_status = context.registerReceiver(null, battery_intent_filter);
			int level = battery_status.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = battery_status.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			battery_level = level / (float)scale;
			last_battery_update_at = System.currentTimeMillis();
			updateUI();
			start_new_instance();
		}
		
		public void start_new_instance() {
			BatteryLevelGetter to_run = new BatteryLevelGetter();
			to_run.interval = interval;
			Handler handler = new Handler();
			handler.postDelayed(to_run, interval);
		}
		
	}
	
}
