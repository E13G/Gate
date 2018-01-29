package com.jpinto.portao.ui;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import com.jpinto.portao.R;
import com.jpinto.portao.network.HttpGetRequest;
import java.util.concurrent.ExecutionException;


public class HomeActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String apiKey;
    private int userCode;
    private int switchState;
    private Boolean userCodeValid;
    private Boolean switchStateValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupSharedPreferences();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button btn_open = findViewById(R.id.open_button);
        final GradientDrawable btnShape = (GradientDrawable)btn_open.getBackground();

        btn_open.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                btnShape.setColor(getResources().getColor(R.color.colorPrimaryDark,null));
                /*requestProgress.setVisibility(View.VISIBLE);*/

                final Handler mHandler = new Handler();
                final int lifeSignDelay = 250;

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnShape.setColor(getResources().getColor(R.color.colorAccent,null));
                    }
                }, lifeSignDelay);

                //Verify if valid codes are being used
                if (userCodeValid && switchStateValid){
                    sendRequest();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        loadApiFromSharedPreferences(sharedPreferences);
        loadUserCodeFromSharedPreferences(sharedPreferences);
        loadStateFromSharedPreferences(sharedPreferences);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void loadUserCodeFromSharedPreferences(SharedPreferences sharedPreferences) {
        Log.wtf("Int",getString(R.string.pref_switch_user_code_default));
        try {
            userCodeValid = true;
            userCode = Integer.parseInt(
                    sharedPreferences.getString(getString(R.string.pref_switch_user_code_key),
                            getString(R.string.pref_switch_user_code_default)));
        }catch(NumberFormatException numE){
            userCodeValid = false;
            notANumberToast();
        }
    }

    private void loadStateFromSharedPreferences(SharedPreferences sharedPreferences) {

        try {
            switchStateValid = true;
            switchState = Integer.parseInt(
                    sharedPreferences.getString(getString(R.string.pref_switch_state_key),
                            getString(R.string.pref_switch_state_default)));
        }catch(NumberFormatException numE){
            switchStateValid = false;
            notANumberToast();
        }
    }

    private void notANumberToast(){
        Toast toast = Toast.makeText(getApplicationContext(), "The Value input is not a number", Toast.LENGTH_SHORT);
        toast.show();
    }



    private void loadApiFromSharedPreferences(SharedPreferences sharedPreferences) {
        apiKey = sharedPreferences.getString(getString(R.string.pref_api_key),getString(R.string.pref_api_default));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_btnList) {
            Intent intent = new Intent(this, CatalogActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String sendRequest(){

        //Some url endpoint that you may have
        String myUrl = "https://api.thingspeak.com/update?api_key="+ apiKey +
                "&field1="+ userCode + "&field2="+ switchState;

        //String to place our result in
        String result;
        //Instantiate new instance of our class
        HttpGetRequest getRequest = new HttpGetRequest();
        //Perform the doInBackground method, passing in our url
        try {
            result = getRequest.execute(myUrl).get();

        } catch (InterruptedException e) {
            e.printStackTrace();
            result="Interrupt Error";

        } catch (ExecutionException e) {
            e.printStackTrace();
            result="Execution Error";
        }
        return result;

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals(getString(R.string.pref_switch_user_code_key))) loadUserCodeFromSharedPreferences(sharedPreferences);

        if(key.equals(getString(R.string.pref_api_key))) loadApiFromSharedPreferences(sharedPreferences);

        if(key.equals(getString(R.string.pref_switch_state_key))) loadStateFromSharedPreferences(sharedPreferences);
    }
}
