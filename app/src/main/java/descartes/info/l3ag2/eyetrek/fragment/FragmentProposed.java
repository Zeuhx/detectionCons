package descartes.info.l3ag2.eyetrek.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import descartes.info.l3ag2.eyetrek.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentProposed extends Fragment {


    public FragmentProposed() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_proposed, container, false);
    }

}
