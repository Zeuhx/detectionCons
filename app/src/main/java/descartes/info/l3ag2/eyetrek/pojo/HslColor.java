package descartes.info.l3ag2.eyetrek.pojo;

import java.util.List;

/**
 * Created by Ayaz ABDUL CADER on 19/03/2018.
 */

//Classe définissant la couleur dominante d'une feuille
public class HslColor {
    private int id;
    private List<Integer> hsls;

    public HslColor(int id, List<Integer> hsls){
        this.id = id;
        this.hsls = hsls;
    }
    public HslColor(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getHsls() {
        return hsls;
    }

    public void setHsls(List<Integer> hsls) {
        this.hsls = hsls;
    }

    public void addColor(int color){
        this.hsls.add(color);
    }
}
