package neinlabs.silenceplease;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rey.material.widget.Slider;
import com.rey.material.widget.Spinner;

import me.alexrs.prefs.lib.Prefs;


public class Settings extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final TextView range_tv = (TextView)findViewById(R.id.range_tv);
        Spinner Duration_spinner = (Spinner)findViewById(R.id.Duration_spinner);
        Spinner Action_spinner = (Spinner)findViewById(R.id.Action_spinner);
        String[] items = {"1 minute","5 minutes","10 minutes","30 minutes","1 hour"};
        String[] items2 = {"vibration","silent"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_spn, items);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, R.layout.row_spn, items2);
        adapter.setDropDownViewResource(R.layout.row_spn_dropdown);
        adapter2.setDropDownViewResource(R.layout.row_spn_dropdown);
        Duration_spinner.setAdapter(adapter);
        Duration_spinner.setOnItemClickListener(new Spinner.OnItemClickListener() {
            @Override
            public boolean onItemClick(Spinner spinner, View view, int i, long l) {
                Prefs.with(Settings.this).save("position", i);
                return true;
            }
        });
        Duration_spinner.setSelection(Prefs.with(this).getInt("position", 0));
        Action_spinner.setAdapter(adapter2);
        Action_spinner.setOnItemClickListener(new Spinner.OnItemClickListener() {
            @Override
            public boolean onItemClick(Spinner spinner, View view, int i, long l) {
                Prefs.with(Settings.this).save("action_pos", i);
                return true;
            }
        });
        Action_spinner.setSelection(Prefs.with(this).getInt("action_pos", 0));
        Slider slider = (Slider)findViewById(R.id.range_slider);
        slider.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider slider, float v, float v1, int i, int i1) {
                Prefs.with(Settings.this).save("slider_pos", v1);
                Prefs.with(Settings.this).save("slider_val", i1);
                range_tv.setText(String.valueOf(slider.getValue())+" meter(s)");
            }
        });
        slider.setPosition(Prefs.with(this).getFloat("slider_pos", new Float(0.0)),false);

        range_tv.setText(String.valueOf(slider.getValue())+" meter(s)");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
