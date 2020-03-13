package descartes.info.l3ag2.eyetrek.classes;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.pojo.Leaf;

/**
 * Created by Dorian Quaboul on 19/04/2018.
 *
 * Cette classe permet de fournir les Views correspondant à la liste des items à afficher.
 */
public class AdapterRecyclerViewHistory extends RecyclerView.Adapter<ViewHolderRecyclerViewHistory> {

    private Context context;

    //Liste des items à afficher
    private List<File> listItem;

    public AdapterRecyclerViewHistory(Context context, List<File> listItem) {
        this.context = context;
        this.listItem = listItem;
    }

    /**
     * Permet de créer les viewHolder pour chaque cellule.
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ViewHolderRecyclerViewHistory onCreateViewHolder(ViewGroup parent, int viewType) {
        //Permet de déterminer la vue correspondant au squelette d'une cellule
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history,null);

        return(new ViewHolderRecyclerViewHistory(item));
    }

    /**
     * Permet de remplir chaque cellule à une certaine position
     * @param holder
     * @param position position de la cellule
     */
    @Override
    public void onBindViewHolder(ViewHolderRecyclerViewHistory holder, int position) {
        String extension = FileUtils.getExtension(listItem.get(position).getName());
        ImageView imageCell = holder.getImageItem();
        ImageButton typeCell = holder.getTypeItem();

        switch (extension) {
            case ".jpg" :
                Glide.with(context).load(listItem.get(position)).into(imageCell);
                typeCell.setImageResource(R.drawable.ic_photo_camera_gray_24dp);
                break;
            case ".mp4" :
                Glide.with(context).load(listItem.get(position)).into(imageCell);
                typeCell.setImageResource(R.drawable.ic_videocam_gray_24dp);
                break;
            case ".wav" :
                Glide.with(context).load(R.drawable.bird_button).into(imageCell);
                typeCell.setImageResource(R.drawable.ic_music_note_gray_24dp);
                break;
        }

        holder.setItemClickListener((v,pos)->{
            String ext = FileUtils.getExtension(listItem.get(pos).getName());
            //On affiche l'image de la cellule dans une boîte de dialogue
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater adbInflater = LayoutInflater.from(context);

            switch (ext) {
                case ".jpg" :
                    View layoutImg = adbInflater.inflate(R.layout.dialog_history_image, null);
                    ImageView imgDialog = layoutImg.findViewById(R.id.dialog_image_history);
                    Glide.with(context).load(listItem.get(pos)).into(imgDialog);
                    TextView titreDialog = layoutImg.findViewById(R.id.dialog_titre_history);
                    titreDialog.setText(listItem.get(pos).getName());

                    //on place l'image dans la boîte de dialogue
                    builder.setView(layoutImg)
                            .setCancelable(true)
                            .create().show();
                    break;
                case ".mp4" :
                    View layoutVid = adbInflater.inflate(R.layout.dialog_history_video, null);
                    VideoView vidDialog = layoutVid.findViewById(R.id.dialog_video_history);
                    vidDialog.setVideoURI(Uri.fromFile(listItem.get(pos)));
                    vidDialog.setMediaController(new MediaController(context));
                    vidDialog.start();

                    TextView titreVidDialog = layoutVid.findViewById(R.id.dialog_titre_history);
                    titreVidDialog.setText(listItem.get(pos).getName());

                    //on place l'image dans la boîte de dialogue
                    builder.setView(layoutVid)
                            .setCancelable(true)
                            .create().show();
                    break;
                case ".wav" :
                    View layoutSound = adbInflater.inflate(R.layout.dialog_history_image, null);
                    ImageView soundDialog = layoutSound.findViewById(R.id.dialog_image_history);
                    Glide.with(context).load(R.drawable.bird_button).into(soundDialog);
                    TextView titreSoundDialog = layoutSound.findViewById(R.id.dialog_titre_history);
                    titreSoundDialog.setText(listItem.get(pos).getName());

                    //on place l'image dans la boîte de dialogue
                    builder.setView(layoutSound)
                            .setCancelable(true)
                            .create().show();

                    MediaPlayer mp = MediaPlayer.create(context,Uri.fromFile(listItem.get(pos)));
                    mp.start();
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mediaPlayer.release();
                        }
                    });
                    break;
            }

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

    public List getListItem() {
        return listItem;
    }

    public void setListItem(List<File> listItem) {
        this.listItem = listItem;
    }

}
