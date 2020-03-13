package descartes.info.l3ag2.eyetrek.pojo;

/**
 * Created by Ayaz ABDUL CADER on 27/03/2018.
 */

public class Animal {
    private int id;
    private String nom;
    private String image;
    private int nbDoigt;
    private int doigtA;
    private int palme;
    private int memeTaille;
    private int nbCoussinet;
    private int griffe;
    private int nbSabot;
    private int concave;
    private int convexe;
    private int circulaire;


    public Animal() {
    }

    public Animal(int id, String nom, String image, int nbDoigt, int doigtA, int palme, int memeTaille, int nbCoussinet, int griffe, int nbSabot, int concave, int convexe, int circulaire) {
        this.id = id;
        this.nom = nom;
        this.image = image;
        this.nbDoigt = nbDoigt;
        this.doigtA = doigtA;
        this.palme = palme;
        this.memeTaille = memeTaille;
        this.nbCoussinet = nbCoussinet;
        this.griffe = griffe;
        this.nbSabot = nbSabot;
        this.concave = concave;
        this.convexe = convexe;
        this.circulaire = circulaire;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getNbDoigt() {
        return nbDoigt;
    }

    public void setNbDoigt(int nbDoigt) {
        this.nbDoigt = nbDoigt;
    }

    public int getDoigtA() {
        return doigtA;
    }

    public void setDoigtA(int doigtA) {
        this.doigtA = doigtA;
    }

    public int getPalme() {
        return palme;
    }

    public void setPalme(int palme) {
        this.palme = palme;
    }

    public int getMemeTaille() {
        return memeTaille;
    }

    public void setMemeTaille(int memeTaille) {
        this.memeTaille = memeTaille;
    }

    public int getNbCoussinet() {
        return nbCoussinet;
    }

    public void setNbCoussinet(int nbCoussinet) {
        this.nbCoussinet = nbCoussinet;
    }

    public int getGriffe() {
        return griffe;
    }

    public void setGriffe(int griffe) {
        this.griffe = griffe;
    }

    public int getNbSabot() {
        return nbSabot;
    }

    public void setNbSabot(int nbSabot) {
        this.nbSabot = nbSabot;
    }

    public int getConcave() {
        return concave;
    }

    public void setConcave(int concave) {
        this.concave = concave;
    }

    public int getConvexe() {
        return convexe;
    }

    public void setConvexe(int convexe) {
        this.convexe = convexe;
    }

    public int getCirculaire() {
        return circulaire;
    }

    public void setCirculaire(int circulaire) {
        this.circulaire = circulaire;
    }
}