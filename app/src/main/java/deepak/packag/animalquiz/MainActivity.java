package deepak.packag.animalquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {


    //Keys of preference list inside the xml file.
    public static final String GUESSES = "settings_numberOfGuesses";
    public static final String ANIMAL_TYPE = "settings_animalsType";
    public static final String QUIZ_BACKGROUND_COLOR = "settings_quiz_background_color";
    public static final String QUIZ_FONT = "settings_quiz_font";


    private boolean isSettingsChanged = false;

    //Typeface is used for fonts only
    static Typeface chunkfive;
    static Typeface fontlerybrown;
    static  Typeface wonderbarDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Lets initialize the fonts in oncreate method
        chunkfive = Typeface.createFromAsset(getAssets(),"fonts/Chunkfive.otf");
        fontlerybrown = Typeface.createFromAsset(getAssets(), "fonts/FontleroyBrown.ttf");
        wonderbarDemo = Typeface.createFromAsset(getAssets(), "fonts/Wonderbar Demo.otf");

        //This means when the user run the app for the first time it will show the default setting mentioned in quiz_preference file by us.
        //false means that it will not read the default setting when the app run second time because may be user has made some setting changes.
        PreferenceManager.setDefaultValues(MainActivity.this,R.xml.quiz_preferences,false);

        //Now this means when there is a change in settings by the user, we are getting the changes from shared
        // preferences by clicking by the user (registerOnSharedPreferenceChangeListener) will help to notify us.
        //Check method settingsChangeListener
        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).
                registerOnSharedPreferenceChangeListener(settingsChangeListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        Intent preferenceIntent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(preferenceIntent);
        return super.onOptionsItemSelected(item);
    }


    //This will be called when user make some setting changes according to their preference and user get the same settings next time
    private SharedPreferences.OnSharedPreferenceChangeListener settingsChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        }
    };
}