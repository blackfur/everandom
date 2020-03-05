package sample.note;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class PropertyFragment extends PreferenceFragmentCompat
        //implements Preference.OnPreferenceChangeListener
        {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.properties, rootKey);
    }

    /*
    SharedPreferences opt;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        opt = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        getPreferenceManager().findPreference("limit").setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.i("option.modify", newValue.toString());
        Log.i("option.check", opt.getString("limit", "-1"));
        Log.i("option.check", opt.getString("host", "-1"));
        return true;
    }
     */
}
