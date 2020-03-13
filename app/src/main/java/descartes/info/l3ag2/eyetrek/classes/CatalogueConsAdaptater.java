package descartes.info.l3ag2.eyetrek.classes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.activity.AstroActivity;
import descartes.info.l3ag2.eyetrek.fragment.FragmentConsCatalogDetails;


public class CatalogueConsAdaptater extends RecyclerView.Adapter<CatalogueConsAdaptater.ExampleViewHolder> {

    private static final String TAG = "CatalogueConsAdaptater";

    private ArrayList<ConstItem> mExampleList;
    private OnItemClickListener mListener;


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

   /* public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }*/


    /**
     * Le ViewHolder est un objet utilise pour accelerer le rendu de la liste heritee de RecyclerView
    */

    public static class ExampleViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mTextView1;
        public TextView mTextView2;
        public TextView mTextView3;
        public String string;
        public int id;


        // A chaque nouvelle cellule construite nous allons recuperer avec findViewByID()
        // toute les nouvelles vues

        public ExampleViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.thumbnail);
            mTextView1 = itemView.findViewById(R.id.ConstellationTitle);
            mTextView2 = itemView.findViewById(R.id.ConstellationInfo);
            mTextView3 = itemView.findViewById(R.id.titleConsCat);


            /**
             * En cliquant sur un item du RecyclerView nous allons afficher un nouveau fragment compose d'un identifiant unique
             */

            //

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d(TAG, "onClick: idCons " + id);

                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    Fragment myFragment = FragmentConsCatalogDetails.newInstance(id);

                    String backStateName = myFragment.getClass().getName();


                    FragmentManager manager = activity.getSupportFragmentManager();
                    boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

                    if (!fragmentPopped){ //fragment not in back stack, create it
                        FragmentTransaction ft = manager.beginTransaction();
                        ft.replace(R.id.fragment_contenairAstro, myFragment);
                        ft.addToBackStack(backStateName);
                        ft.commit();
                    }

                }
            });

        }

    }

    public CatalogueConsAdaptater(ArrayList<ConstItem> exampleList) {
        mExampleList = exampleList;
    }


    /**
     *  onCreateViewHolder() est exécute lors de la creation d'un ViewHolder et on retourne evh
     */


    @Override
    public ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.const_catalogue_item, parent, false);
        ExampleViewHolder evh = new ExampleViewHolder(v, mListener);

        return evh;
    }

    /**
     * onBindViewHolder() est execute quand le ViewHolder est de nouveau lie à l'adapter
     * c'est-à-dire lors du recyclage d'une cellule
     * On recupere en parametre l'instance du ViewHolder et la position courante de la liste
     */


    @Override
    public void onBindViewHolder(ExampleViewHolder holder, int position) {
        ConstItem currentItem = mExampleList.get(position);

        Log.d(TAG, "onBindViewHolder : " + currentItem);
        holder.mImageView.setImageResource(currentItem.getImageUrl());
        holder.mTextView1.setText(currentItem.getTitle());
        holder.mTextView2.setText(currentItem.getInfo());
        holder.mTextView3.setText(currentItem.getSubTitle());
        holder.id = currentItem.getIdConstellation();


    }

    /**
     *  getItemCount() renvoie le nombre d'items de notre liste
     *  et .size() indique le nombre exacte d'element dans notre liste
     */

    @Override
    public int getItemCount() {
        return mExampleList.size();
    }
}