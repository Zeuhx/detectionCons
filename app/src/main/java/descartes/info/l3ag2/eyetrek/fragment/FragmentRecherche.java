package descartes.info.l3ag2.eyetrek.fragment;

import android.content.SharedPreferences;

import android.os.Bundle;

import android.support.design.widget.TabLayout;

import android.support.v4.app.Fragment;

import android.support.v7.widget.DefaultItemAnimator;

import android.support.v7.widget.GridLayoutManager;

import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.SearchView;

import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;

import android.view.ViewGroup;

import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;

import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;

import java.util.List;

import descartes.info.l3ag2.eyetrek.classes.AdapterRecyclerView;

import descartes.info.l3ag2.eyetrek.classes.DatabaseHandler;

import descartes.info.l3ag2.eyetrek.pojo.Animal;
import descartes.info.l3ag2.eyetrek.pojo.Bird;
import descartes.info.l3ag2.eyetrek.pojo.Leaf;

import descartes.info.l3ag2.eyetrek.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Cette classe représente la page de recherche/encyclopédie.
 * <p>
 * La liste des résultats de recherche est affinée/filtrée à chaque saisie de caractères.
 * <p>
 * <p>
 * <p>
 * Created by Dorian Quaboul
 * <p>
 * Updated by Ayaz ABDUL CADER
 */

public class FragmentRecherche extends Fragment {

    private static final String DIDACTICIEL = "PREFERENCE";

    private TabLayout searchFilter;

    private SearchView barreRecherche;

    private RecyclerView recycler;

    private AdapterRecyclerView adapter;

    private List<Leaf> listFeuilles;

    private List<Animal> listAnimaux;

    private List<Bird> listOiseaux;


    private TabLayout.OnTabSelectedListener tabListener = new TabLayout.OnTabSelectedListener() {

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            switch (tab.getPosition()) {
                //Onglet Feuilles
                case 0:
                    //On définit le placeholder qui sera écrit dans la barre de recherche
                    barreRecherche.setQueryHint("Recherche de feuilles");
                    //On définit la liste des items qui seront affichés et qui pourront être recherchés
                    adapter.setListItem(listFeuilles);
                    //On initialise la liste des résultats à la liste initiale (contenant tous les items)
                    adapter.setListResult(listFeuilles);
                    break;
                //Onglet Empreintes
                case 1:
                    barreRecherche.setQueryHint("Recherche d'empreintes");
                    adapter.setListItem(listAnimaux);
                    adapter.setListResult(listAnimaux);
                    break;
                //Onglet Oiseaux
                case 2:
                    barreRecherche.setQueryHint("Recherche d'oiseaux");
                    adapter.setListItem(listOiseaux);
                    adapter.setListResult(listOiseaux);
                    break;
            }

            /*

                A chaque fois que l'on change d'onglet de recherche :

                - On vide la liste de résultats de recherche (en mettant à null le FilterSearch)

                - On met à jour la liste d'items affichés (on notifie le changement de données à l'adapter).

                - On enlève le focus de la barre de recherche.

                - On vide le contenu de la barre de recherche.

             */
            adapter.resetFilter();
            adapter.notifyDataSetChanged();
            barreRecherche.clearFocus();
            barreRecherche.setQuery("", true);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }

    };

    public FragmentRecherche() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        DatabaseHandler databaseHandler = new DatabaseHandler(getContext());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recherche, container, false);
        //On initialise les 3 listes
        initList(databaseHandler);
        barreRecherche = view.findViewById(R.id.searchbarre);
        searchFilter = view.findViewById(R.id.searchfilter);
        setFilterTab(searchFilter);
        //Il s'agit d'une vue flexible qui s'adapte à un grand nombre de données
        recycler = view.findViewById(R.id.recycler_feuille);

        /*

            On instancie les propriétés de la RecyclerView :

            - Le type de remplissage des données (en grille avec 3 cellules par ligne)

            - Le type d'animation de remplissage par default

         */
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recycler.setItemAnimator(new DefaultItemAnimator());

        /*

            On instancie l'adapter, on lui fournit la liste de feuilles (par défaut l'onglet

            "Feuilles" est actif).

            Cet adapter permet de remplir le RecyclerView.

         */
        adapter = new AdapterRecyclerView(getContext(), databaseHandler.getAllLeafs());
        recycler.setAdapter(adapter);
        //On définit un listener pour la saisie de la recherche
        barreRecherche.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String search) {
                return false;

            }

            /**

             * On filtre la search à chaque saisie de caractères (à chaque fois le text change)

             * @param search

             * @return

             */

            @Override
            public boolean onQueryTextChange(String search) {
                //Recherche suivant chaque tab
                adapter.getFilter().filter(search, (count) -> {
                    //Tab Feuilles
                    if (searchFilter.getSelectedTabPosition() == 0) {
                        List<Leaf> leafs = databaseHandler.getLeafsFromName(search);
                        if (leafs == null) {
                            Toast.makeText(getContext(), "Aucun résultat pour cette search", Toast.LENGTH_SHORT).show();
                        } else {
                            //On remplace le contenu uniquement par les feuilles contenant le mot recherché
                            adapter = new AdapterRecyclerView(getContext(), leafs);
                            recycler.setAdapter(adapter);
                        }
                    }
                    //Tab Animaaux
                    else if (searchFilter.getSelectedTabPosition() == 1) {
                        List<Animal> animals = new ArrayList<Animal>();
                        for (Animal animal : databaseHandler.getAllAnimals()) {
                            if (animal.getNom().toLowerCase().startsWith(search)) {
                                animals.add(animal);
                            }
                        }
                        if (animals.isEmpty()) {
                            Toast.makeText(getContext(), "Aucun résultat pour cette search", Toast.LENGTH_SHORT).show();
                        } else {
                            adapter = new AdapterRecyclerView(getContext(), animals);
                            recycler.setAdapter(adapter);
                        }
                    }
                    //Tab Oiseaux
                    else {
                        List<Bird> birds = databaseHandler.getBirdsFromName(search);
                        if (birds == null) {
                            Toast.makeText(getContext(), "Aucun résultat pour cette search", Toast.LENGTH_SHORT).show();
                        } else {
                            //On remplace le contenu uniquement par les oiseaux contenant le mot recherché
                            adapter = new AdapterRecyclerView(getContext(), birds);
                            recycler.setAdapter(adapter);
                        }
                    }

                });
                return false;

            }

        });
        barreRecherche.setOnCloseListener(() -> {
            return false;
        });
        //On récupère le booléen permettant de savoir si l'utilisateur à déjà vu le didacticiel
        Boolean show = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).getBoolean("search", false);
        if (!show) {
            new TapTargetSequence(getActivity())
                    .targets(
                            TapTarget.forView(view.findViewById(android.support.v7.appcompat.R.id.search_mag_icon), "Barre de recherche", "Cette barre permet d'effectuer des recherches !")
                                    .dimColor(android.R.color.black)
                                    .outerCircleColor(R.color.colorTheme)
                                    .targetCircleColor(R.color.transparent_gray)
                                    .textColor(android.R.color.black)
                                    .transparentTarget(true)
                                    .targetRadius(80))
                    .listener(new TapTargetSequence.Listener() {
                        //Si l'utilisateur parcours tout le didacticiel ou l'annule au milieu on ajoute un booléen

                        @Override
                        public void onSequenceFinish() {
                            SharedPreferences.Editor editor = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).edit();
                            editor.putBoolean("search", true).commit();

                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                            SharedPreferences.Editor editor = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).edit();
                            editor.putBoolean("search", true).commit();

                        }

                    }).start();

        }
        return view;

    }

    /**
     * On initialise la barre de filtre de recherche en mettant 3 onglets.
     *
     * @param tabLayout
     */

    private void setFilterTab(TabLayout tabLayout) {
        TabLayout.Tab feuilles = tabLayout.newTab();
        feuilles.setText("Feuilles");
        TabLayout.Tab empreintes = tabLayout.newTab();
        empreintes.setText("Empreintes");
        TabLayout.Tab oiseaux = tabLayout.newTab();
        oiseaux.setText("Oiseaux");

        tabLayout.addTab(feuilles);
        tabLayout.addTab(empreintes);
        tabLayout.addTab(oiseaux);
        tabLayout.addOnTabSelectedListener(tabListener);
    }

    /**
     * On instancie les 3 listes correspondant aux 3 recherches possibles.
     */
    private void initList(DatabaseHandler databaseHandler) {
        listAnimaux = databaseHandler.getAllAnimals();
        listOiseaux = databaseHandler.getAllBirds();
        listFeuilles = databaseHandler.getAllLeafs();
    }
}
