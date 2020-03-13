package descartes.info.l3ag2.eyetrek.fragment.animalanalysis;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.classes.DatabaseHandler;
import descartes.info.l3ag2.eyetrek.fragment.FragmentAnimal;
import descartes.info.l3ag2.eyetrek.pojo.Animal;


public class FragmentPads extends Fragment {


    public FragmentPads() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        DatabaseHandler databaseHandler = new DatabaseHandler(getContext());
        View view = inflater.inflate(R.layout.fragment_pads, container, false);
        RadioGroup groupeCoussinet = view.findViewById(R.id.groupeNbCoussinet);
        RadioButton coussinet4 = view.findViewById(R.id.radio_4Coussinets);
        RadioButton coussinet5 = view.findViewById(R.id.radio_5Coussinets);
        RadioGroup groupeGriffe = view.findViewById(R.id.groupeGriffe);
        RadioButton avecGriffe = view.findViewById(R.id.radio_Oui);
        RadioButton sansGriffe = view.findViewById(R.id.radio_Non);
        Button valider = view.findViewById(R.id.valider);
        Button reset = view.findViewById(R.id.reset);

        ImageButton cancel = view.findViewById(R.id.cancel);

        cancel.setOnClickListener((v)->{
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentAnimal());
            fragmentTransaction.commit();
        });


        //Click pour reset tous les boutons
        reset.setOnClickListener((v) -> {
            groupeCoussinet.clearCheck();
            groupeGriffe.clearCheck();
            for (int i = 0; i < groupeCoussinet.getChildCount(); i++) {
                groupeCoussinet.getChildAt(i).setEnabled(true);
            }
            for (int i = 0; i < groupeGriffe.getChildCount(); i++) {
                groupeGriffe.getChildAt(i).setEnabled(true);
            }
        });
        valider.setOnClickListener((v) -> {
            if (coussinet4.isChecked() || coussinet5.isChecked()) {
                if (avecGriffe.isChecked() || sansGriffe.isChecked()) {
                    List<Animal> animalList = new ArrayList<Animal>();
                    int nbCoussinetID = groupeCoussinet.getCheckedRadioButtonId();
                    RadioButton radioCoussinet = view.findViewById(nbCoussinetID);
                    String nbCoussinet = radioCoussinet.getText().toString();
                    int nbGriffeID = groupeGriffe.getCheckedRadioButtonId();
                    RadioButton radioGriffe = view.findViewById(nbGriffeID);
                    String nbGriffe = radioGriffe.getText().toString();
                    if (nbCoussinet.equals("4 Coussinets")) {
                        if (nbGriffe.equals("Avec Griffes")) {
                            animalList = databaseHandler.getAnimalsFromRequest("SELECT * FROM animal WHERE nbCoussinet=4 AND griffe=1");
                        } else {
                            animalList = databaseHandler.getAnimalsFromRequest("SELECT * FROM animal WHERE nbCoussinet=4 AND griffe=0");
                        }
                    } else if (nbCoussinet.equals("5 Coussinets")) {
                        if (nbGriffe.equals("Avec Griffes")) {
                            animalList = databaseHandler.getAnimalsFromRequest("SELECT * FROM animal WHERE nbCoussinet=5 AND griffe=1");
                        } else {
                            animalList = databaseHandler.getAnimalsFromRequest("SELECT * FROM animal WHERE nbCoussinet=5 AND griffe=0");
                        }
                    }
                    String res = "";
                    for (Animal animal : animalList) {
                        Log.e("NOM", animal.getNom());
                        res = res + animal.getNom() + "\n";
                    }
                    createAlertBox("Résultats", res);
                } else {
                    createAlertBox("Erreur", "Veuillez remplir tous les champs");
                }
            } else {
                createAlertBox("Erreur", "Veuillez remplir tous les champs");
            }
        });
        coussinet4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //On désactive tous les boutons
                    for (int i = 0; i < groupeGriffe.getChildCount(); i++) {
                        groupeGriffe.getChildAt(i).setEnabled(true);
                    }
                    for (int i = 0; i < groupeGriffe.getChildCount(); i++) {
                        groupeGriffe.getChildAt(i).setEnabled(true);
                    }
                }
            }
        });
        coussinet5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //On désactive tous les boutons
                    for (int i = 0; i < groupeGriffe.getChildCount(); i++) {
                        groupeGriffe.getChildAt(i).setEnabled(true);
                    }
                    for (int i = 0; i < groupeGriffe.getChildCount(); i++) {
                        groupeGriffe.getChildAt(i).setEnabled(true);
                    }
                }
            }
        });
        return view;
    }

    /**
     * Fonction permettant de créer une AlertBox
     *
     * @param title
     * @param msg
     */

    private void createAlertBox(String title, String msg) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).show();
    }

}
