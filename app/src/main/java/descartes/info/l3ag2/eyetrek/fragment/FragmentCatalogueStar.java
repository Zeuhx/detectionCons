package descartes.info.l3ag2.eyetrek.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.interfaces.IOnBackPressed;

public class FragmentCatalogueStar extends Fragment {

    public static final String TAG = "FragmentCatalogueStar";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalogue_star, container, false);

        Toolbar toolbar = getActivity().findViewById(R.id.toolBarAstro);
        toolbar.setTitle("Catalogue Des Etoiles");
        toolbar.setNavigationIcon(ContextCompat.getDrawable(getContext(),R.drawable.ic_arrow_back_black_32dp));
        toolbar.setNavigationOnClickListener((viewl) -> {
            getActivity().onBackPressed();
        });
        return view;
    }
}
