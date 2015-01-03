package si.gto76.bicikl_pp.asynctasks;

import java.io.InputStream;

import org.json.JSONException;
import si.gto76.bicikl_pp.Conf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Used for getting images from Google Street View Image API.
 * Its execute method needs two parameters: lat and lng of the location.
 * You have to define onSuccessfulFetch method, that gets the Bitmap image when it arrives.
 */
public abstract class ImageLookUp extends AsyncTask<String, Void, Bitmap> {
	
	final String address;
	final Context context;

	public ImageLookUp(Context context) {
		this.context = context;
		this.address = "https://maps.googleapis.com/maps/api/streetview";
	}

	public abstract void onSuccessfulFetch(Bitmap image) throws JSONException;

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
					+ "&fov=90"+Conf.KEY;
			String combinedAddress = address + query;
			
			InputStream in = new java.net.URL(combinedAddress).openStream();
			Bitmap image = BitmapFactory.decodeStream(in);
			return image;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
