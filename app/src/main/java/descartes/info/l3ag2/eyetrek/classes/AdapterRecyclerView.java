package descartes.info.l3ag2.eyetrek.classes;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import descartes.info.l3ag2.eyetrek.pojo.Animal;
import descartes.info.l3ag2.eyetrek.pojo.Bird;
import descartes.info.l3ag2.eyetrek.pojo.Leaf;
import descartes.info.l3ag2.eyetrek.R;

/**
 * Created by Dorian on 27/02/2018.
 *
 * Cette classe permet de fournir les Views correspondant à la liste des items à afficher.
 */
public class AdapterRecyclerView extends RecyclerView.Adapter<ViewHolderRecyclerView> implements Filterable {

    private Context context;

    //Liste des items à afficher
    private List listItem;
    //Liste des resultats de la recherche
    private List listResult;
    private FilterSearch filterSearch;

    public AdapterRecyclerView(Context context, List listItem) {
        this.context = context;
        this.listItem = listItem;
        this.listResult = listItem;
    }

    /**
     * Permet de créer les viewHolder pour chaque cellule.
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ViewHolderRecyclerView onCreateViewHolder(ViewGroup parent, int viewType) {
        //Permet de déterminer la vue correspondant au squelette d'une cellule
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_encyclopedie,null);

        return(new ViewHolderRecyclerView(item));
    }

    /**
     * Permet de remplir chaque cellule à une certaine position
     * @param holder
     * @param position position de la cellule
     */
    @Override
    public void onBindViewHolder(ViewHolderRecyclerView holder, int position) {
        if(listItem.get(0) instanceof Leaf) {
            int idImageCell = context.getResources().getIdentifier(((Leaf)(listItem.get(position))).getPicture(),"drawable",context.getPackageName());
            String textCell = ((Leaf)(listItem.get(position))).getName();
            holder.setImageItem(idImageCell);
            ImageView imageCell = holder.getImageItem();
            Glide.with(context).load(idImageCell).into(imageCell);
            holder.setNomItem(textCell);
        } else if(listItem.get(0) instanceof Animal) {
            int idImageCell = context.getResources().getIdentifier(((Animal)(listItem.get(position))).getImage(),"drawable",context.getPackageName());
            String textCell = ((Animal)(listItem.get(position))).getNom();
            holder.setImageItem(idImageCell);
            ImageView imageCell = holder.getImageItem();
            Glide.with(context).load(idImageCell).into(imageCell);
            holder.setNomItem(textCell);
        } else if(listItem.get(0) instanceof Bird) {
            int idImageCell = context.getResources().getIdentifier(((Bird)(listItem.get(position))).getPicture(),"drawable",context.getPackageName());
            String textCell = ((Bird)(listItem.get(position))).getName();
            holder.setImageItem(idImageCell);
            ImageView imageCell = holder.getImageItem();
            Glide.with(context).load(idImageCell).into(imageCell);
            holder.setNomItem(textCell);
        }

        //Il faudra checker dans la bdd locale, si l'utilisateur a déjà analysé l'élément.

        holder.setItemClickListener((v,pos)->{
            //On affiche l'image de la cellule dans une boîte de dialogue
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            LayoutInflater adbInflater = LayoutInflater.from(context);
            View layout = adbInflater.inflate(R.layout.dialog_encyclopedie, null);

            ImageView imgDialog = layout.findViewById(R.id.dialog_image_encyclopedie);
            ImageButton flagDialog = layout.findViewById(R.id.dialog_flag_encyclopedie);

            TextView titreDialog = layout.findViewById(R.id.dialog_titre_encyclopedie);

            TextView description = layout.findViewById(R.id.description_espece);

            CardView bouton_wikipedia = layout.findViewById(R.id.bouton_wikipedia);


            if(listItem.get(0) instanceof Leaf) {
                int idImageCell = context.getResources().getIdentifier(((Leaf)(listItem.get(pos))).getPicture(),"drawable",context.getPackageName());
                titreDialog.setText(((Leaf)(listItem.get(pos))).getName());
                description.setText(UtilitaireResultat.get_description_feuille(((Leaf)(listItem.get(pos))).getName().replaceAll(" ", "_").toLowerCase(), context));
                Glide.with(context).load(idImageCell).into(imgDialog);
                bouton_wikipedia.setOnClickListener((event) -> {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://fr.wikipedia.org/wiki/" + ((Leaf)(listItem.get(pos))).getName().replaceAll(" ", "_").toLowerCase())));
                });
            } else if(listItem.get(0) instanceof Animal) {
                int idImageCell = context.getResources().getIdentifier(((Animal)(listItem.get(pos))).getImage(),"drawable",context.getPackageName());
                titreDialog.setText(((Animal)(listItem.get(pos))).getNom());
                Glide.with(context).load(idImageCell).into(imgDialog);
                bouton_wikipedia.setOnClickListener((event) -> {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://fr.wikipedia.org/wiki/" + ((Animal)(listItem.get(pos))).getNom().replaceAll(" ", "_").toLowerCase())));
                });
            } else if(listItem.get(0) instanceof Bird) {
                int idImageCell = context.getResources().getIdentifier(((Bird)(listItem.get(pos))).getPicture(),"drawable",context.getPackageName());
                titreDialog.setText(((Bird)(listItem.get(pos))).getName());
                Glide.with(context).load(idImageCell).into(imgDialog);
                Glide.with(context).load(R.drawable.ic_flag_france).into(flagDialog);
                bouton_wikipedia.setOnClickListener((event) -> {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://fr.wikipedia.org/wiki/" + ((Bird)(listItem.get(pos))).getName().replaceAll(" ", "_").toLowerCase())));
                });
            }



            //on place l'image dans la boîte de dialogue
            builder.setView(layout)
                    .setCancelable(true)
                    .create().show();
        });
    }

    /**
     * Retourne le nombre total d'élement
     * @return
     */
    @Override
    public int getItemCount() {
        return listItem.size();
    }

    @Override
    public Filter getFilter() {
        if(filterSearch == null) {
            filterSearch = new FilterSearch(listResult,this);
        }
        return filterSearch;
    }

    public List getListItem() {
        return listItem;
    }

    public void setListItem(List listItem) {
        this.listItem = listItem;
    }

    public void resetFilter() {
        this.filterSearch = null;
    }

    public List getListResult() {
        return listResult;
    }

    public void setListResult(List listResult) {
        this.listResult = listResult;
    }
}
