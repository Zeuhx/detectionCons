package descartes.info.l3ag2.eyetrek.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import descartes.info.l3ag2.eyetrek.classes.CatalogueConsAdaptater;
import descartes.info.l3ag2.eyetrek.classes.ConstItem;
import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.interfaces.IOnBackPressed;

public class FragmentCatalogueCons extends Fragment {

    public static final String TAG = "FragmentCatalogueCons";
    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private CatalogueConsAdaptater mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalogue_constellation, container, false);

        toolbar = getActivity().findViewById(R.id.toolBarAstro);


        toolbar.setTitle("Catalogue Des Constellations");
        toolbar.setNavigationIcon(ContextCompat.getDrawable(getContext(),R.drawable.ic_arrow_back_black_32dp));
        toolbar.setNavigationOnClickListener((viewl) -> {
            getActivity().onBackPressed();
        });

        mRecyclerView = view.findViewById(R.id.recyclerView);
        buildRecyclerView();

        return view;
    }

    /**
     * La RecyclerView charge uniquement les vues visible à l'ecran
     * lors d'un scrool elle reutilisera les vues qui disparaissent
     * pour charger les elements suivants
     */
    public void buildRecyclerView() {

        ArrayList<ConstItem> mConstellation = new ArrayList<>();
        String[] ConsteList = getResources().getStringArray(R.array.constellations_titles);
        String[] ConsteInfo = getResources().getStringArray(R.array.constellations_info);
        String[] ConsteImages = getResources().getStringArray(R.array.constellations_images);

        for (int i = 0; i < ConsteList.length; i++) {
            Log.d(TAG, "buildRecyclerView: Constellation : Constellation Name : " + ConsteList[i]);
            mConstellation.add(new ConstItem(getResources().getIdentifier(ConsteImages[i],"drawable",getContext().getPackageName()), ConsteInfo[i], ConsteList[i],"Information",i));
        }

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new CatalogueConsAdaptater(mConstellation);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

    }

}