package sample.note;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import static sample.note.Global.appContext;

public class PropertyFragment extends PreferenceFragmentCompat
        //implements SharedPreferences.OnSharedPreferenceChangeListener
        implements Preference.OnPreferenceChangeListener
{

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.properties, rootKey);

        findPreference("host").setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key =preference.getKey();
        if(key.equalsIgnoreCase("host")){

            EditTextPreference prefix = findPreference("prefix");
            prefix.setText(newValue.toString());

//            SharedPreferences opt=
//                    PreferenceManager.getDefaultSharedPreferences(appContext);
//            SharedPreferences.Editor editor=opt.edit();
//            editor.putString("prefix",newValue.toString());
//            editor.commit();

            return true;
        }
        return false;
    }
}
