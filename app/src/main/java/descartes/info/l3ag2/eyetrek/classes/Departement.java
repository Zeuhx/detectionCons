package descartes.info.l3ag2.eyetrek.classes;

/**
 * Created by Antony on 29/03/2019.
 *
 * Cette classe est l'objet Departement.
 */

public class Departement {
    private String text;
    private String imageUrl;

    public Departement(String text, String imageUrl) {
        this.text = text;
        this.imageUrl = imageUrl;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
