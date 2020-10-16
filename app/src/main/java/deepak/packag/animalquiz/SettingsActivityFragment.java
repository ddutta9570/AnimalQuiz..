package deepak.packag.animalquiz;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class SettingsActivityFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Here we are putting the quiz_preference.xml file in this class which is extended from PreferenceFragment
        //Because the xml is preference type, we can check the xml from inside
        addPreferencesFromResource(R.xml.quiz_preferences);

    }

}