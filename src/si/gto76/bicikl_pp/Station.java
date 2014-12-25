package si.gto76.bicikl_pp;

import org.json.JSONException;
import org.json.JSONObject;

import si.gto76.bicikl_pp.Stations.UserButtonListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Station extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String id = extras.getString("id");
//		if (id == null) {
//			finish();
//		} else {
//			TextView title = (TextView) findViewById(R.id.title);
//			title.setText(id);
//		}
		
		getJson(id);
	}
	
	public void getJson(String id) {
		final StationsLookUp arrivals = new StationsLookUpStation(getApplicationContext(), id);
		arrivals.execute("");
	}
	
	///////////// GET JSON FROM URL
	
	private class StationsLookUpStation extends StationsLookUp {
		
		private String id;

		public StationsLookUpStation(Context ctx, String id) {
			super(ctx);
			this.id = id;
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			if (result == null) {
				Toast.makeText(context, "Prislo je do napake.", Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				JSONObject markers = result.getJSONObject("markers");
				JSONObject station = markers.getJSONObject(id);
				String stationName = station.getString("name");
				
				TextView title = (TextView) findViewById(R.id.title);
				title.setText(stationName);
				
				JSONObject stationAvailability = station.getJSONObject("station");
	            String available = stationAvailability.getString("available");
	            String free = stationAvailability.getString("free");
								
				LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
				createTextView("Available: "+available, layout);
				createTextView("Free: "+free, layout);
				
				//createButton(stationName, layout);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	////////////////// MENU
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.station, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.action_settings:
				return true;
			case R.id.map:
				intent = new Intent(this, Map.class);
				startActivity(intent);
				return true;
			case R.id.stations:
				intent = new Intent(this, Stations.class);
				startActivity(intent);
				return true;
			case R.id.paths:
				intent = new Intent(this, Paths.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	///////////// BUTTONS

	@SuppressLint("NewApi") private void createButton(String name, LinearLayout layout) {
		LayoutParams lparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		Button button = new Button(this);
		button.setLayoutParams(lparams);
		button.setText(name);
		layout.addView(button);
	}
	
	@SuppressLint("NewApi") private void createTextView(String name, LinearLayout layout) {
		LayoutParams lparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		TextView textView = new TextView(this);
		textView.setLayoutParams(lparams);
		textView.setText(name);
		textView.setGravity(Gravity.CENTER);
		textView.setTextColor(Color.WHITE);
		textView.setTextSize(29);
		layout.addView(textView);
	}
	

}
