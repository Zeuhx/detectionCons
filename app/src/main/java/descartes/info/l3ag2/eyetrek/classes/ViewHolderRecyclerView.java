package descartes.info.l3ag2.eyetrek.classes;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import descartes.info.l3ag2.eyetrek.R;

/**
 * Permet de garder les références vers les vues de chaque cellule du RecyclerView.
 *
 * Created by Dorian on 27/02/2018.
 */

public class ViewHolderRecyclerView extends RecyclerView.ViewHolder {
    //Image contenue dans l'item
    private ImageView imageItem;
    //Nom contenu dans l'item
    private TextView nomItem;
    //Icone de capture contenu dans l'item
    //private ImageButton iconeItem;

    private ItemClickListener itemClickListener;

    //itemView est la vue correspondante à une cellule
    public ViewHolderRecyclerView(View itemView) {
        super(itemView);

        this.imageItem = itemView.findViewById(R.id.item_image);
        this.nomItem = itemView.findViewById(R.id.item_name);
        //this.iconeItem = itemView.findViewById(R.id.item_icone);

        itemView.setOnClickListener((v)-> {
            itemClickListener.onItemClick(v,getLayoutPosition());
        });
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public ImageView getImageItem() {
        return imageItem;
    }

    public void setImageItem(int picture) {
        this.imageItem.setImageResource(picture);
    }

    public TextView getNomItem() {
        return nomItem;
    }

    public void setNomItem(String nomItem) {
        this.nomItem.setText(nomItem);
    }
}
