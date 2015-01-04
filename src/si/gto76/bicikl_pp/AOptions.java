package si.gto76.bicikl_pp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

public class AOptions extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aoptions);

		initializeViews();
	}

	private void initializeViews() {
		NumberPicker speedPicker = (NumberPicker) this.findViewById(R.id.numberPickerSpeed);
		speedPicker.setMaxValue(40);
		speedPicker.setMinValue(5);
		speedPicker.setValue(Conf.cyclingSpeed);
		//speedPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		speedPicker.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				Conf.cyclingSpeed = newVal;
			}
		});

		NumberPicker availabilityPicker = (NumberPicker) this.findViewById(R.id.numberPickerAvailability);
		availabilityPicker.setMaxValue(12);
		availabilityPicker.setMinValue(1);
		availabilityPicker.setValue(Conf.acceptableAvailability);
		//speedPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		availabilityPicker.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				Conf.acceptableAvailability = newVal;
			}
		});
	}

	public void close(View v) {
		finish();
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
