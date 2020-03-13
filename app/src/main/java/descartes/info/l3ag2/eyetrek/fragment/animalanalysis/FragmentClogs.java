package descartes.info.l3ag2.eyetrek.fragment.animalanalysis;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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


public class FragmentClogs extends Fragment {


    public FragmentClogs() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        DatabaseHandler databaseHandler = new DatabaseHandler(getContext());
        View view = inflater.inflate(R.layout.fragment_clogs, container, false);
        RadioGroup groupeNbSabot = view.findViewById(R.id.groupeNbSabot);
        RadioButton paire = view.findViewById(R.id.radio_Paire);
        RadioButton impaire = view.findViewById(R.id.radio_Impaire);
        RadioGroup groupePaire = view.findViewById(R.id.groupeDoigt);
        RadioButton paire4 = view.findViewById(R.id.radio_4);
        RadioButton paire2 = view.findViewById(R.id.radio_2);
        RadioGroup groupeForme = view.findViewById(R.id.groupeForme);
        RadioButton concave = view.findViewById(R.id.radio_concave);
        RadioButton convexe = view.findViewById(R.id.radio_convexe);
        RadioButton circulaire = view.findViewById(R.id.radio_circulaire);
        Button reset = view.findViewById(R.id.reset);
        Button valider = view.findViewById(R.id.valider);
        ImageButton cancel = view.findViewById(R.id.cancel);

        cancel.setOnClickListener((v)->{
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentAnimal());
            fragmentTransaction.commit();
        });




        for (int i = 0; i < groupePaire.getChildCount(); i++) {
            groupePaire.getChildAt(i).setEnabled(false);
        }
        for (int i = 0; i < groupeForme.getChildCount(); i++) {
            groupeForme.getChildAt(i).setEnabled(false);
        }
        /**
         * Click sur le bouton valider
         */
        valider.setOnClickListener((v) -> {
            if (paire.isChecked() || impaire.isChecked()) {
                if (paire4.isChecked() || paire2.isChecked() &&(concave.isChecked() || convexe.isChecked() || circulaire.isChecked()) || impaire.isChecked()) {
                    List<Animal> animals = new ArrayList<Animal>();
                    int nbSabotID = groupeNbSabot.getCheckedRadioButtonId();
                    RadioButton radioSabot = view.findViewById(nbSabotID);
                    String nbSabot = radioSabot.getText().toString();
                    if ((nbSabot.equals("Mono"))) {
                        animals = databaseHandler.getAnimalsFromRequest("SELECT * FROM animal WHERE nbSabot=1");
                    } else {
                        int nbPaireID = groupePaire.getCheckedRadioButtonId();
                        RadioButton radioPaire = view.findViewById(nbPaireID);
                        String nbPaire = radioPaire.getText().toString();
                        if (nbPaire.equals("4")) {
                            animals = databaseHandler.getAnimalsFromRequest("SELECT * FROM animal WHERE nbSabot=4");
                        } else {
                            int formeID = groupeForme.getCheckedRadioButtonId();
                            RadioButton radioForme = view.findViewById(formeID);
                            String forme = radioForme.getText().toString();
                            switch (forme) {
                                case "Circulaire":
                                    animals = databaseHandler.getAnimalsFromRequest("SELECT * FROM animal WHERE nbSabot=2 AND circulaire=1");
                                    break;
                                case "Convexe":
                                    animals = databaseHandler.getAnimalsFromRequest("SELECT * FROM animal WHERE nbSabot=2 AND convexe=1");
                                    break;
                                default:
                                    animals = databaseHandler.getAnimalsFromRequest("SELECT * FROM animal WHERE nbSabot=2 AND concave=1");
                                    break;
                            }
                        }
                    }
                    String res = "";
                    for (Animal animal : animals) {
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
            groupeNbSabot.clearCheck();
            groupeForme.clearCheck();
            groupePaire.clearCheck();
            for (int i = 0; i < groupePaire.getChildCount(); i++) {
                groupePaire.getChildAt(i).setEnabled(false);
            }
            for (int i = 0; i < groupeNbSabot.getChildCount(); i++) {
                groupeNbSabot.getChildAt(i).setEnabled(true);
            }
            for (int i = 0; i < groupeForme.getChildCount(); i++) {
                groupeForme.getChildAt(i).setEnabled(false);
            }
        });

        //Si l'utilisateur clique sur impaire
        impaire.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //On désactive tous les boutons
                    for (int i = 0; i < groupePaire.getChildCount(); i++) {
                        groupePaire.getChildAt(i).setEnabled(false);
                    }
                    for (int i = 0; i < groupeForme.getChildCount(); i++) {
                        groupeForme.getChildAt(i).setEnabled(false);
                    }
                }
            }
        });
        //Si l'utilisateur clique sur impaire
        paire.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //On désactive tous les boutons
                    for (int i = 0; i < groupePaire.getChildCount(); i++) {
                        groupePaire.getChildAt(i).setEnabled(true);
                    }
                    for (int i = 0; i < groupeForme.getChildCount(); i++) {
                        groupeForme.getChildAt(i).setEnabled(false);
                    }
                }
            }
        });
        //Si l'utilisateur clique sur nombre de doigts = 4
        paire4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < groupeForme.getChildCount(); i++) {
                        groupeForme.getChildAt(i).setEnabled(false);
                    }
                    for (int i = 0; i < groupeNbSabot.getChildCount(); i++) {
                        groupeNbSabot.getChildAt(i).setEnabled(false);
                    }
                }
            }
        });
        //Si l'utilisateur clique sur nombre de doigts = 2
        paire2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < groupeForme.getChildCount(); i++) {
                        groupeForme.getChildAt(i).setEnabled(true);
                    }
                    for (int i = 0; i < groupeNbSabot.getChildCount(); i++) {
                        groupeNbSabot.getChildAt(i).setEnabled(false);
                    }
                }
            }
        });
        concave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < groupePaire.getChildCount(); i++) {
                        groupePaire.getChildAt(i).setEnabled(false);
                    }
                    for (int i = 0; i < groupeNbSabot.getChildCount(); i++) {
                        groupeNbSabot.getChildAt(i).setEnabled(false);
                    }
                }
            }
        });
        convexe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < groupePaire.getChildCount(); i++) {
                        groupePaire.getChildAt(i).setEnabled(false);
                    }
                    for (int i = 0; i < groupeNbSabot.getChildCount(); i++) {
                        groupeNbSabot.getChildAt(i).setEnabled(false);
                    }
                }
            }
        });
        circulaire.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < groupePaire.getChildCount(); i++) {
                        groupePaire.getChildAt(i).setEnabled(false);
                    }
                    for (int i = 0; i < groupeNbSabot.getChildCount(); i++) {
                        groupeNbSabot.getChildAt(i).setEnabled(false);
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
