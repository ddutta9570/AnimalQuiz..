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
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {


    //Keys of preference list inside the xml file.
    public static final String GUESSES = "settings_numberOfGuesses";
    public static final String ANIMAL_TYPE = "settings_animalsType";
    public static final String QUIZ_BACKGROUND_COLOR = "settings_quiz_background_color";
    public static final String QUIZ_FONT = "settings_quiz_font";

    //MainActivityFragment class
    MainActivityFragment myAnimalQuizFragment;


    private boolean isSettingsChanged = false;

    //Typeface is used for fonts only
    static Typeface chunkfive;
    static Typeface fontlerybrown;
    static Typeface wonderbarDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Lets initialize the fonts in onCreate method
        chunkfive = Typeface.createFromAsset(getAssets(),"fonts/Chunkfive.otf");
        fontlerybrown = Typeface.createFromAsset(getAssets(), "fonts/FontleroyBrown.ttf");
        wonderbarDemo = Typeface.createFromAsset(getAssets(), "fonts/Wonderbar Demo.otf");

        //This means when the user run the app for the first time it will show the default setting mentioned in quiz_preference file by us.
        //false means that it will not read the default setting when the app run second time because may be user has made some setting changes.
        PreferenceManager.setDefaultValues(MainActivity.this,R.xml.quiz_preferences,false);

        //Now this means when there is a change in settings by the user, we are getting the changes from shared
        // preferences by clicking by the user (registerOnSharedPreferenceChangeListener) will help to notify us.
        //Check method settingsChangeListener
        //Basically a settings change listener
        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).
                registerOnSharedPreferenceChangeListener(settingsChangeListener);

        //Loading the framents class and xml in the main activity
        myAnimalQuizFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.animalQuizFragment);

        //Here we are getting the settings changes which are saved in sharedPreferences
        myAnimalQuizFragment.modifyAnimalGuessRows(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
        myAnimalQuizFragment.modifyTypeOfAnimalInQuiz(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
        myAnimalQuizFragment.modifyQuizFont(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
        myAnimalQuizFragment.modifyBackgroundColor(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
        myAnimalQuizFragment.resetAnimalQuiz();
        isSettingsChanged = false;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent preferencesIntent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(preferencesIntent);
        return true;
    }


    //When user click on the preference setting option this will save that settings and  user get the same settings next time
    private SharedPreferences.OnSharedPreferenceChangeListener settingsChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            isSettingsChanged = true;

            //If the user is interacted with GUESSES settings portion of guessing number of options
            if (key.equals(GUESSES)){

                //modifyAnimalGuessRows is a method and sharedPreference will save the settings according to the user setting
                myAnimalQuizFragment.modifyAnimalGuessRows(sharedPreferences);
                myAnimalQuizFragment.resetAnimalQuiz();


            }else if (key.equals(ANIMAL_TYPE)){

                //This animalType contains the animal type
                //To void duplicate value we use set
                Set<String> animalType = sharedPreferences.getStringSet(ANIMAL_TYPE,null);

                if (animalType!=null && animalType.size()>0){

                    //If wild or tame is checked it will show that specific category animals
                    myAnimalQuizFragment.modifyTypeOfAnimalInQuiz(sharedPreferences);
                    myAnimalQuizFragment.resetAnimalQuiz();

                }else {//If the user doesn't provide any animal type, below sharedPreferences will show the default wild animalType

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    //default_animal_type is saved
                    animalType.add(getString(R.string.default_animal_type));
                    editor.putStringSet(ANIMAL_TYPE,animalType);
                    editor.apply();

                    Toast.makeText(MainActivity.this,R.string.toast_message,Toast.LENGTH_SHORT).show();

                }

            }else if (key.equals(QUIZ_FONT)){

                myAnimalQuizFragment.modifyQuizFont(sharedPreferences);
                myAnimalQuizFragment.resetAnimalQuiz();

            } else if (key.equals(QUIZ_BACKGROUND_COLOR)){

                myAnimalQuizFragment.modifyBackgroundColor(sharedPreferences);
                myAnimalQuizFragment.resetAnimalQuiz();

            }

            Toast.makeText(MainActivity.this,R.string.change_message,Toast.LENGTH_SHORT).show();


        }
    };
}