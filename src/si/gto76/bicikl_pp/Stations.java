package si.gto76.bicikl_pp;

import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Stations extends Activity {
	
	public static final String ADDRESS = "https://prevoz.org/api/bicikelj/list/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stations);
		
		getJson(null);
	}

	public void getJson(View v) {
		final StationsLookUp arrivals = new StationsLookUpStations(getApplicationContext());
		arrivals.execute("");
	}
	
	///////////// BUTTONS

	@SuppressLint("NewApi") private void createButton(String id, String name, LinearLayout layout) {
		LayoutParams lparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		Button button = new Button(this);
		button.setLayoutParams(lparams);
		button.setText(name);

		Intent intent = new Intent(this, Station.class);
		UserButtonListener buttonListener = new UserButtonListener(id, intent);
		
		button.setOnClickListener(buttonListener);
		layout.addView(button);
	}
	

	class UserButtonListener implements View.OnClickListener {
		private String id;
		private Intent intent;
		public UserButtonListener(String id, Intent intent) { 
			this.id = id;
			this.intent = intent;
		}
		@Override
		public void onClick(View v) {
			Bundle bundle = new Bundle(); 
			bundle.putString("id", id);
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}
	
	
	
	///////////// GET JSON FROM URL
	
	private class StationsLookUpStations extends StationsLookUp {

		public StationsLookUpStations(Context ctx) {
			super(ctx);
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			if (result == null) {
				Toast.makeText(context, "Prislo je do napake.", Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				JSONObject markers = result.getJSONObject("markers");
				LinearLayout layout = (LinearLayout) findViewById(R.id.stationsLayout);
				
				Iterator<String> iter = markers.keys();
			    while (iter.hasNext()) {
			        String key = iter.next();
			        try {
			        	JSONObject station = markers.getJSONObject(key);
			            String stationName = station.getString("name");
			            JSONObject stationAvailability = station.getJSONObject("station");
			            String available = stationAvailability.getString("available");
			            String free = stationAvailability.getString("free");
			            
			            String buttonText = stationName+" "+available+"/"+free; 
			            createButton(key, buttonText, layout);
			        } catch (JSONException e) {
			        }
			    }
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	////////////////// MENU

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stations, menu);
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
			case R.id.paths:
				intent = new Intent(this, Paths.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
