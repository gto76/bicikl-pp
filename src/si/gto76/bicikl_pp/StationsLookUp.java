package si.gto76.bicikl_pp;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;


abstract class StationsLookUp extends AsyncTask<String, Void, JSONObject> {

	public static final String ADDRESS = "https://prevoz.org/api/bicikelj/list/";
	final Context context;

	public StationsLookUp(Context ctx) {
		this.context = ctx;
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		try {
			final HttpGet request = new HttpGet(ADDRESS);
			request.addHeader("Accept", "application/json");
			final HttpClient hcl = new DefaultHttpClient();
			final HttpResponse response = hcl.execute(request);
			final HttpEntity entity = response.getEntity();
			return new JSONObject(EntityUtils.toString(entity));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
