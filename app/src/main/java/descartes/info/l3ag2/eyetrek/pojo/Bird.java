package descartes.info.l3ag2.eyetrek.pojo;

/**
 * Created by Dorian QUABOUL on 06/05/2018
 */

public class Bird {

    private int id;
    private String name;
    private String picture;

    public Bird(){

    }
    public Bird(int id, String name, String picture) {
        this.id = id;
        this.name = name;
        this.picture = picture;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}