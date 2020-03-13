package descartes.info.l3ag2.eyetrek.classes;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import descartes.info.l3ag2.eyetrek.R;

/**
 * Created by Antony on 29/03/2019.
 *
 * Cette classe permet de fournir les Views correspondant à la liste des items à afficher.
 */

public class ViewHolder extends RecyclerView.ViewHolder {
    private TextView textViewView;
    private ImageView imageView;

    //itemView est la vue correspondante à 1 cellule
    public ViewHolder(View itemView, Context context, List<Departement> list) {
        super(itemView);
        textViewView = (TextView) itemView.findViewById(R.id.text);
        imageView = (ImageView) itemView.findViewById(R.id.image);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position=getAdapterPosition();
                Toast.makeText(context, list.get(position).getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //puis ajouter une fonction pour remplir la cellule en fonction d'un Departement
    public void bind(Departement departement, Activity activity){
        try{
            textViewView.setText(departement.getText());
            String uri = departement.getImageUrl();
            int imageResource = activity.getResources().getIdentifier(uri, null, activity.getPackageName());
            Drawable res = resizeImage(imageResource, activity);
            imageView.setImageDrawable(res);
        } catch (Throwable t){
            t.printStackTrace();
        }

    }

    public Drawable resizeImage(int imageResource, Activity activity) {// R.drawable.large_image
        // Get device dimensions
        Display display = activity.getWindowManager().getDefaultDisplay();
        double deviceWidth = display.getWidth();

        BitmapDrawable bd = (BitmapDrawable) activity.getResources().getDrawable(
                imageResource);
        double imageHeight = bd.getBitmap().getHeight();
        double imageWidth = bd.getBitmap().getWidth();

        double ratio = deviceWidth / imageWidth;
        int newImageHeight = (int) (imageHeight * ratio);

        Bitmap bMap = BitmapFactory.decodeResource(activity.getResources(), imageResource);
        Drawable drawable = new BitmapDrawable(activity.getResources(),
                getResizedBitmap(bMap, newImageHeight, (int) deviceWidth));

        return drawable;
    }

    /************************ Resize Bitmap *********************************/
    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }
}
