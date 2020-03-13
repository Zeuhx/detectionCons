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
 * Created by Dorian Quaboul on 19/04/2018.
 */

public class ViewHolderRecyclerViewHistory extends RecyclerView.ViewHolder {
    //Image contenue dans l'item
    private ImageView imageItem;
    //Icone du type de contenu dans l'item (image ou son ou video)
    private ImageButton typeItem;

    private ItemClickListener itemClickListener;

    //itemView est la vue correspondante à une cellule
    public ViewHolderRecyclerViewHistory(View itemView) {
        super(itemView);

        this.imageItem = itemView.findViewById(R.id.item_image);
        this.typeItem = itemView.findViewById(R.id.item_type);

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

    public ImageButton getTypeItem() { return typeItem; }

}
