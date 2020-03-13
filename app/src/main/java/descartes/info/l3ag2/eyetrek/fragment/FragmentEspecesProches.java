package descartes.info.l3ag2.eyetrek.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.classes.Adapter;
import descartes.info.l3ag2.eyetrek.classes.Departement;
import descartes.info.l3ag2.eyetrek.classes.UtilitaireResultat;
import descartes.info.l3ag2.eyetrek.classes.ViewHolder;

/**
 * Created by Antony on 29/03/2019.
 *
 * Cette classe permet d'afficher la liste des différents départements.
 */

public class FragmentEspecesProches extends Fragment {

    //séparateur du fichier csv contenant les départements de France
    public final static char SEPARATOR=',';

    private RecyclerView recyclerView;

    /*
    //ArrayList qui prendra chaque ligne du fichier csv des départements
    private ArrayList<String> sList = new ArrayList<>(UtilitaireResultat.readFile(activity.getResources().openRawResource(R.raw.departement)));
    */

    //ArrayList qui contiendra la liste des départements de France
    private List<Departement> departementsList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.fragment_especes_proches);
        // Chargement du noeud parent du fichier layout fragment_analyse.xml
        View view = inflater.inflate(R.layout.fragment_especes_proches, container, false);

        //remplir la ville
        ajouterDepartements();

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        //définit l'agencement des cellules, ici de façon verticale, comme une ListView
        //recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //pour adapter en grille comme une RecyclerView, avec 2 cellules par ligne
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));

        //puis créer un Adapter, lui fournir notre liste de départements.
        //cet adapter servira à remplir notre recyclerview
        recyclerView.setAdapter(new Adapter(departementsList, getActivity()));

        /*

        recyclerView.setOnClickListener(new RecyclerView.ViewHolder(getContext(), departementsList) {
            public void onBindViewHolder(ViewHolder viewHolder, int position) {
                Departement departement = departementsList.get(position);
                viewHolder.bind(departement, getActivity());
                viewHolder.itemView.setOnClickListener({
                        Log.e("onCreateView()", "Click sur un bouton dans FrangmentEespecesProches");
                        Log.e("onCreateView()", departementsList.toString());
                });
            }
        });
        */


        return view;
    }



    private void ajouterDepartements() {
        /*
        departementsList.add(new Departement("01 - Ain","https://fr.wikipedia.org/wiki/Ain_(département)#/media/File:Ain_departement_locator_map.svg"));
        departementsList.add(new Departement("02 - Aisne","https://upload.wikimedia.org/wikipedia/commons/5/58/Aisne-Position.svg"));
        departementsList.add(new Departement("03 - Allier","https://upload.wikimedia.org/wikipedia/commons/1/19/Allier-Position.svg"));
        departementsList.add(new Departement("04 - Alpes-de-Haute-Provence","https://upload.wikimedia.org/wikipedia/commons/6/6b/Alpes-de-Haute-Provence-Position.svg"));
        */


        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String[]> data = new ArrayList<>();
        String sep = new Character(SEPARATOR).toString();

        lines = UtilitaireResultat.readFile(getActivity().getResources().openRawResource(R.raw.departement));

        for(String line : lines){
            Log.e("ajouterDepartements()", line);
            try{
                StringTokenizer st = new StringTokenizer(line, ",");
                ArrayList<String> liste = new ArrayList<>();

                while(st.hasMoreTokens()){
                    liste.add(st.nextToken());
                }

                departementsList.add( new Departement( liste.get(1) +" - "+ liste.get(2) ,"@drawable/"+ liste.get(4)));
            } catch(Throwable t){
                t.printStackTrace();
            }
            /*
            String[] oneData = line.split(sep);
            data.add(oneData);
            */
        }

        /*
        for(int i=0 ; i<data.size() ; i++){
            String[] tabStr = data.get(i);
            departementsList.add( new Departement( tabStr[2]+" - "+tabStr[3] ,"@drawable/"+tabStr[5] ));
        }
        */


    }

}
