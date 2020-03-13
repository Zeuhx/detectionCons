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

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHands extends Fragment {


    public FragmentHands() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        DatabaseHandler databaseHandler = new DatabaseHandler(getContext());
        View view = inflater.inflate(R.layout.fragment_hands, container, false);
        //Nombre de doigts
        RadioGroup groupeDoigt = view.findViewById(R.id.groupeNbSabot);
        RadioButton doigt5 = view.findViewById(R.id.radio_5);
        RadioButton doigt5a = view.findViewById(R.id.radio_5a);
        RadioButton doigt4 = view.findViewById(R.id.radio_4);
        //Palme ou non
        RadioGroup groupePalme = view.findViewById(R.id.groupePalme);
        RadioButton palme = view.findViewById(R.id.radio_Oui);
        RadioButton nonpalme = view.findViewById(R.id.radio_Non);
        //Meme taille ou non
        RadioGroup groupeTaille = view.findViewById(R.id.groupeForme);
        RadioButton egale = view.findViewById(R.id.radio_egale);
        RadioButton nonegale = view.findViewById(R.id.radio_nonegale);
        //Bouton
        Button reset = view.findViewById(R.id.reset);
        Button valider = view.findViewById(R.id.valider);
        ImageButton cancel = view.findViewById(R.id.cancel);

        cancel.setOnClickListener((v)->{
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentAnimal());
            fragmentTransaction.commit();
        });
        for (int i = 0; i < groupePalme.getChildCount(); i++) {
            groupePalme.getChildAt(i).setEnabled(false);
        }
        for (int i = 0; i < groupePalme.getChildCount(); i++) {
            groupeTaille.getChildAt(i).setEnabled(false);
        }
        /**
         * Click sur le bouton valider
         */
        valider.setOnClickListener((v) -> {
            if (doigt5.isChecked() || doigt5a.isChecked() || doigt4.isChecked()) {
                if (palme.isChecked() || nonpalme.isChecked() || egale.isChecked() || nonegale.isChecked() || doigt4.isChecked()) {
                    List<Animal> animalList = new ArrayList<>();
                    int nbDoigtID = groupeDoigt.getCheckedRadioButtonId();
                    RadioButton radioDoigt = view.findViewById(nbDoigtID);
                    String nbDoigt = radioDoigt.getText().toString();
                    switch (nbDoigt) {
                        case "4 doigts":
                            //Celui la c'est bon
                            animalList = databaseHandler.getAnimalsFromRequest("SELECT * FROM animal WHERE nbDoigt=4");
                            break;
                        case "5 doigts":
                            int nbPalmeID = groupePalme.getCheckedRadioButtonId();
                            RadioButton radioPalme = view.findViewById(nbPalmeID);
                            String nbPalme = radioPalme.getText().toString();
                            Log.e("Palme", nbPalme);
                            if (nbPalme.equals("Palmes")) {
                                animalList = databaseHandler.getAnimalsFromRequest("SELECT * FROM animal WHERE nbDoigt=5 AND palme=1 AND doigtA=0");
                            } else {
                                animalList = databaseHandler.getAnimalsFromRequest("SELECT * FROM animal WHERE nbDoigt=5 AND palme=0 AND doigtA=0");
                            }
                            break;
                        default:
                            int nbTailleID = groupeTaille.getCheckedRadioButtonId();
                            RadioButton radioTaille = view.findViewById(nbTailleID);
                            String nbTaille = radioTaille.getText().toString();
                            if (nbTaille.equals("Même taille")) {
                                animalList = databaseHandler.getAnimalsFromRequest("SELECT * FROM animal WHERE nbDoigt=5 AND doigtA=1 AND memeTaille=1 AND palme=0");
                            } else {
                                animalList = databaseHandler.getAnimalsFromRequest("SELECT * FROM animal WHERE nbDoigt=5 AND doigtA=1 AND memeTaille=0 AND palme=0");
                            }
                            break;
                    }
                    String res = "";
                    for (Animal animal : animalList) {
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
        //Click pour reset tous les boutons
        reset.setOnClickListener((v) -> {
            groupeDoigt.clearCheck();
            groupePalme.clearCheck();
            groupeTaille.clearCheck();
            for (int i = 0; i < groupeDoigt.getChildCount(); i++) {
                groupeDoigt.getChildAt(i).setEnabled(true);
            }
            for (int i = 0; i < groupePalme.getChildCount(); i++) {
                groupePalme.getChildAt(i).setEnabled(false);
            }
            for (int i = 0; i < groupeTaille.getChildCount(); i++) {
                groupeTaille.getChildAt(i).setEnabled(false);
            }
        });
        //Si l'utilisateur clique sur 4 doigts
        doigt4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < groupePalme.getChildCount(); i++) {
                        groupePalme.getChildAt(i).setEnabled(false);
                    }
                    for (int i = 0; i < groupeTaille.getChildCount(); i++) {
                        groupeTaille.getChildAt(i).setEnabled(false);
                    }
                }
            }
        });
        //Si l'utilisateur clique sur 5 doigts
        doigt5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //On désactive tous les boutons
                    for (int i = 0; i < groupeTaille.getChildCount(); i++) {
                        groupeTaille.getChildAt(i).setEnabled(false);
                    }
                    for (int i = 0; i < groupePalme.getChildCount(); i++) {
                        groupePalme.getChildAt(i).setEnabled(true);
                    }
                }
            }
        });
        //Si l'utilisateur clique sur 5 doigts arrière
        doigt5a.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //On désactive tous les boutons
                    for (int i = 0; i < groupePalme.getChildCount(); i++) {
                        groupePalme.getChildAt(i).setEnabled(false);
                    }
                    for (int i = 0; i < groupeTaille.getChildCount(); i++) {
                        groupeTaille.getChildAt(i).setEnabled(true);
                    }
                }
            }
        });
        //Si l'utilisateur clique sur 5 doigts arrière
        palme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //On désactive tous les boutons
                    for (int i = 0; i < groupeDoigt.getChildCount(); i++) {
                        groupeDoigt.getChildAt(i).setEnabled(false);
                    }
                    for (int i = 0; i < groupeTaille.getChildCount(); i++) {
                        groupeTaille.getChildAt(i).setEnabled(false);
                    }
                }
            }
        });
        //Si l'utilisateur clique sur 5 doigts arrière
        nonpalme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //On désactive tous les boutons
                    for (int i = 0; i < groupeDoigt.getChildCount(); i++) {
                        groupeDoigt.getChildAt(i).setEnabled(false);
                    }
                    for (int i = 0; i < groupeTaille.getChildCount(); i++) {
                        groupeTaille.getChildAt(i).setEnabled(false);
                    }
                }
            }
        });
        //Si l'utilisateur clique sur 5 doigts arrière
        egale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //On désactive tous les boutons
                    for (int i = 0; i < groupeDoigt.getChildCount(); i++) {
                        groupeDoigt.getChildAt(i).setEnabled(false);
                    }
                    for (int i = 0; i < groupePalme.getChildCount(); i++) {
                        groupePalme.getChildAt(i).setEnabled(false);
                    }
                }
            }
        });
        //Si l'utilisateur clique sur 5 doigts arrière
        nonegale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //On désactive tous les boutons
                    for (int i = 0; i < groupeDoigt.getChildCount(); i++) {
                        groupeDoigt.getChildAt(i).setEnabled(false);
                    }
                    for (int i = 0; i < groupePalme.getChildCount(); i++) {
                        groupePalme.getChildAt(i).setEnabled(false);
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
