package descartes.info.l3ag2.eyetrek.classes;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import descartes.info.l3ag2.eyetrek.R;

/**
 * Created by Antony on 29/03/2019.
 *
 * Cette classe permet d'utiliser les views de ViewHolder dans notre Adapter.
 */

public class Adapter extends RecyclerView.Adapter<ViewHolder> {

    List<Departement> list;
    Activity activity;

    //ajouter un constructeur prenant en entrée une liste
    public Adapter(List<Departement> list, Activity activity) {
        this.list = list;
        this.activity = activity;
    }

    //cette fonction permet de créer les viewHolder
    //et par la même indiquer la vue à inflater (à partir des layout xml)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.departement,viewGroup,false);
        return new ViewHolder(view, activity.getApplicationContext(), list);
    }

    //c'est ici que nous allons remplir notre cellule avec le texte/image de chaque Département
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        try{
            Departement departement = list.get(position);
            viewHolder.bind(departement, activity);
        } catch(Throwable t){
            t.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
