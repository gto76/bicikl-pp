package si.gto76.bicikl_pp;

import si.gto76.bicikl_pp.DbContract.DbOptions;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;

public class AOptions extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aoptions);

		createGui();
	}

	private void createGui() {
		NumberPicker speedPicker = (NumberPicker) this.findViewById(R.id.numberPickerSpeed);
		speedPicker.setMaxValue(Conf.MAX_SPEED);
		speedPicker.setMinValue(Conf.MIN_SPEED);
		speedPicker.setValue(Conf.cyclingSpeed);
		// speedPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		NumberPicker availabilityPicker = (NumberPicker) this.findViewById(R.id.numberPickerAvailability);
		availabilityPicker.setMaxValue(Conf.MAX_AVAILABILITY);
		availabilityPicker.setMinValue(Conf.MIN_AVAILABILITY);
		availabilityPicker.setValue(Conf.acceptableAvailability);
		// speedPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
	}

	public void okButtonPressed(View v) {
		Context context = getApplicationContext();
		Conf.cyclingSpeed = writeToDb(context, R.id.numberPickerSpeed, DbOptions.OPTION_ID_CYCLING_SPEED);
		Conf.acceptableAvailability = writeToDb(context, R.id.numberPickerAvailability,
				DbOptions.OPTION_ID_ACCEPTABLE_AVAILABILITY);
		finish();
	}

	private int writeToDb(Context context, int viewId, String optionId) {
		NumberPicker numberPicker = (NumberPicker) this.findViewById(viewId);
		int value = numberPicker.getValue();
		DbOptionsApi.writeOptionValueToDb(context, optionId, value);
		return value;
	}

	// ////////////////////////////
	// /////////// MENU ///////////
	// ////////////////////////////

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.station, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		int itemId = item.getItemId();
		if (itemId == R.id.map) {
			intent = new Intent(this, AMap.class);
			startActivity(intent);
			return true;
		} else if (itemId == R.id.stations) {
			intent = new Intent(this, AStations.class);
			startActivity(intent);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

}
