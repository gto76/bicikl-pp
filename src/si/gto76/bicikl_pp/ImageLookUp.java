package si.gto76.bicikl_pp;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

abstract class ImageLookUp extends AsyncTask<String, Void, Bitmap> {
	
	private static String KEY = "&key=AIzaSyCTBXziQ9NtE633QxhmSqEhRTgfgGldrrk";

	final String address;
	final Context context;

	public ImageLookUp(Context context) {
		this.context = context;
		this.address = "https://maps.googleapis.com/maps/api/streetview";
	}

	abstract void onSuccessfulFetch(Bitmap image) throws JSONException;

	@Override
	protected void onPostExecute(Bitmap image) {
		if (image == null) {
			Toast.makeText(context, "Error occured while downloading image data.", Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			onSuccessfulFetch(image);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private Point getScreenSize() {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size;
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		try {
			Point size = getScreenSize();
			String query = "?size="+size.x+"x"+size.y+"&location=" + params[0] + "," + params[1]
					+ "&fov=90&pitch=10"+KEY;

			String combinedAddress = address + query;
			System.out.println("#####"+combinedAddress);
			final HttpGet request = new HttpGet(combinedAddress);
			request.addHeader("Accept", "image/jpeg");
			final HttpClient client = new DefaultHttpClient();
			final HttpResponse response = client.execute(request);
			System.out.println("#####"+response.getStatusLine());
			final HttpEntity entity = response.getEntity();
			BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
			
			InputStream instream = bufHttpEntity.getContent();
			Bitmap image = BitmapFactory.decodeStream(instream);
			
			InputStream in = new java.net.URL(combinedAddress).openStream();
			Bitmap mIcon11 = BitmapFactory.decodeStream(in);
			
			
			return mIcon11;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
