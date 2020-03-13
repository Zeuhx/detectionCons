package descartes.info.l3ag2.eyetrek.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;
import java.util.List;

import descartes.info.l3ag2.eyetrek.R;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by Ayaz ABDUL CADER
 * Ce Fragment correspond au Fragment Profil
 */
public class FragmentProfil extends Fragment {

    //SharedPreferences = Cache
    private static final String PREFS = "PREFERENCES";
    private static final String DIDACTICIEL = "PREFERENCE";
    SharedPreferences sharedPreferences;

    public FragmentProfil() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);
        ViewPager viewPager = view.findViewById(R.id.pager);
        TabLayout tabLayout = view.findViewById(R.id.filter);
        TextView username = view.findViewById(R.id.username);
        TextView year_exp = view.findViewById(R.id.year_exp);
        //Button logout = view.findViewById(R.id.logout);
        ImageButton settings = view.findViewById(R.id.settings);
        ImageButton logout = view.findViewById(R.id.logout);
        sharedPreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String prefs_lastname = sharedPreferences.getString("last_name", null);
        String prefs_firstname = sharedPreferences.getString("first_name", null);
        String prefs_username = prefs_firstname + " " + prefs_lastname;
        String prefs_yearexp = sharedPreferences.getString("year_exp", null);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        username.setText(prefs_username);
        year_exp.setText("Année(s) d'Expérience(s): " + prefs_yearexp);

        /**
         * Click sur le bouton de déconnexion
         */
        logout.setOnClickListener((v) -> {
            getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().commit();
            getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new FragmentLog()).commit();
        });

        /**
         * Click sur le bouton de paramètres
         */
        settings.setOnClickListener((v) -> {
            getFragmentManager().beginTransaction().replace(R.id.contenu_fragment, new FragmentOptions()).commit();
        });

        //On récupère le booléen permettant de savoir si l'utilisateur à déjà vu le didacticiel
        Boolean show = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).getBoolean("profil", false);

        if (!show) {
            new TapTargetSequence(getActivity())
                    .targets(
                            TapTarget.forView(view.findViewById(R.id.logout), "Déconnexion", "Ce bouton vous permet de vous déconnecter !").dimColor(android.R.color.black)
                                    .outerCircleColor(R.color.colorTheme)
                                    .targetCircleColor(R.color.transparent_gray)
                                    .transparentTarget(true)
                                    .textColor(android.R.color.black),
                            TapTarget.forView(view.findViewById(R.id.settings), "Modification", "Ce bouton permet de modifier votre profil !")
                                    .dimColor(android.R.color.black)
                                    .outerCircleColor(R.color.colorTheme)
                                    .targetCircleColor(R.color.transparent_gray)
                                    .transparentTarget(true))
                    .listener(new TapTargetSequence.Listener() {
                        //Si l'utilisateur parcours tout le didacticiel ou l'annule au milieu on ajoute un booléen
                        @Override
                        public void onSequenceFinish() {
                            SharedPreferences.Editor editor = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).edit();
                            editor.putBoolean("profil", true).commit();
                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                            SharedPreferences.Editor editor = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).edit();
                            editor.putBoolean("profil", true).commit();
                        }
                    }).start();


        }


        return view;
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {

        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new FragmentHistory(), "Historique");
        adapter.addFragment(new FragmentProposed(), "Proposition");
        viewPager.setAdapter(adapter);

    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


}
