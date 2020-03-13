package descartes.info.l3ag2.eyetrek.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import descartes.info.l3ag2.eyetrek.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentPreferences extends Fragment {

    //SharedPreferences = Cache
    private static final String PREFS = "PREFERENCES";
    private static final String DIDACTICIEL = "PREFERENCE";
    SharedPreferences sharedPreferences;

    public FragmentPreferences() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preferences, container, false);
        Switch didacticielSwitch = view.findViewById(R.id.didacticiel);
        TextView didacticielText = view.findViewById(R.id.didacticiel_text);

        Boolean menu = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).getBoolean("menu", false);
        Boolean analyse = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).getBoolean("analyse", false);
        Boolean settings = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).getBoolean("settings", false);
        Boolean profil = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).getBoolean("profil", false);
        Boolean search = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).getBoolean("search", false);
        Boolean sawDidacticiel = getDidacticiel(menu,analyse,settings,profil,search);
        if (!sawDidacticiel){
            didacticielSwitch.setChecked(true);
            didacticielText.setText("OUI");
        }
        else {
            didacticielSwitch.setChecked(false);
            didacticielText.setText("NON");
        }

        didacticielSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked && (sawDidacticiel)){
                    //Didacticiel ON et l'utilisateur à déjà vu le didacticiel
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).edit();
                    editor.putBoolean("menu", false).apply();
                    editor.putBoolean("analyse", false).apply();
                    editor.putBoolean("settings", false).apply();
                    editor.putBoolean("profil", false).apply();
                    editor.putBoolean("search", false).apply();
                    didacticielText.setText("OUI");


                }
                else if ((!isChecked)&& (!sawDidacticiel)) {
                    //Didacticiel OFF et l'utilisateur n'a jamais vu le didacticiel
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).edit();
                    editor.putBoolean("menu", true).apply();
                    editor.putBoolean("analyse", true).apply();
                    editor.putBoolean("settings", true).apply();
                    editor.putBoolean("profil", true).apply();
                    editor.putBoolean("search", true).apply();
                    didacticielText.setText("NON");
                } else if (isChecked){
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).edit();
                    editor.putBoolean("menu", false).apply();
                    editor.putBoolean("analyse", false).apply();
                    editor.putBoolean("settings", false).apply();
                    editor.putBoolean("profil", false).apply();
                    editor.putBoolean("search", false).apply();
                    didacticielSwitch.setChecked(true);
                    didacticielText.setText("OUI");
                } else {
                    didacticielSwitch.setChecked(false);
                    didacticielText.setText("NON");
                    //Didacticiel OFF et l'utilisateur n'a jamais vu le didacticiel
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).edit();
                    editor.putBoolean("menu", true).apply();
                    editor.putBoolean("analyse", true).apply();
                    editor.putBoolean("settings", true).apply();
                    editor.putBoolean("profil", true).apply();
                    editor.putBoolean("search", true).apply();
                }
            }
        });


        return view;

    }

    public boolean getDidacticiel(Boolean menu, Boolean analyse, Boolean settings, Boolean profil, Boolean search){
        List<Boolean> didacticielValues = new ArrayList<>();
        didacticielValues.add(menu);
        didacticielValues.add(settings);
        didacticielValues.add(profil);
        didacticielValues.add(search);
        didacticielValues.add(analyse);

        for (Boolean didacticielValue : didacticielValues){
            if (!didacticielValue){
                return false;
            }
        }
        return true;
    }

}
