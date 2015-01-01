package si.gto76.bicikl_pp;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

abstract class LookUp extends AsyncTask<String, Void, JSONObject> {

	final String address;
	final Context context;

	public LookUp(Context context, String address) {
		this.context = context;
		this.address = address;
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		try {
			String combinedAddress = address;
			if (params != null && params.length != 0) {
				combinedAddress = address+params[0];
			}
			final HttpGet request = new HttpGet(combinedAddress);
			request.addHeader("Accept", "application/json");
			final HttpClient client = new DefaultHttpClient();
			final HttpResponse response = client.execute(request);
			final HttpEntity entity = response.getEntity();
			String responseJson = EntityUtils.toString(entity);
			//if (responseJson.length() < 300) {
			//	System.out.println(responseJson);
			//}
			return new JSONObject(responseJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void runPeriodically(int miliseconds) {
		final Handler handler = new Handler();
		Timer timer = new Timer();
		TimerTask asynchronousTask = new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						try {
							LookUp.this.execute();
						} catch (Exception e) {
						}
					}
				});
			}
		};
		timer.schedule(asynchronousTask, 3000, miliseconds);
	}
	
}
