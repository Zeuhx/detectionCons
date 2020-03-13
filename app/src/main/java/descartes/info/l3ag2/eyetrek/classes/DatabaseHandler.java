package descartes.info.l3ag2.eyetrek.classes;

import android.content.ContentValues;

import android.content.Context;

import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;

import java.util.List;

import java.util.Scanner;

import descartes.info.l3ag2.eyetrek.pojo.Animal;
import descartes.info.l3ag2.eyetrek.pojo.Bird;
import descartes.info.l3ag2.eyetrek.pojo.HslColor;
import descartes.info.l3ag2.eyetrek.pojo.Leaf;
/**
 * Created by Ayaz ABDUL CADER on 27/02/2018.
 * Updated by Dorian QUABOUL on 06/05/2018
 */

/**
 * Source: https://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "db_eyetrek";

    private static final String TABLE_LEAFS = "leafs";
    private static final String LEAF_ID = "id";
    private static final String LEAF_NAME = "name";
    private static final String LEAF_PICTURE = "picture";

    private static final String TABLE_BIRD = "bird";
    private static final String BIRD_ID = "id";
    private static final String BIRD_NAME = "name";
    private static final String BIRD_PICTURE = "picture";

    private static final String TABLE_DIDACTICIEL = "didacticiel";
    private static final String DIDACTICIEL_ID = "id";
    private static final String DIDACTICIEL_MENU = "menu";
    private static final String DIDACTICIEL_ANALYSE = "analyse";
    private static final String DIDACTICIEL_SETTINGS = "settings";
    private static final String DIDACTICIEL_PROFIL = "profil";
    private static final String DIDACTICIEL_SEARCH = "search";

    private static final String TABLE_COLORS = "colors";
    private static final String COLORS_ID = "id";
    private static final String COLORS_COLOR = "color";

    private static final String TABLE_ANIMAL = "animal";
    private static final String ANIMAL_ID = "id";
    private static final String ANIMAL_NOM = "nom";
    private static final String ANIMAL_IMAGE = "image";
    private static final String ANIMAL_NBDOIGT = "nbDoigt";
    private static final String ANIMAL_DOIGTA = "doigtA";
    private static final String ANIMAL_PALME = "palme";
    private static final String ANIMAL_MEMETAILLE = "memeTaille";
    private static final String ANIMAL_NBCOUSSINET = "nbCoussinet";
    private static final String ANIMAL_GRIFFES = "griffe";
    private static final String ANIMAL_NBSABOT = "nbSabot";
    private static final String ANIMAL_CONCAVE = "concave";
    private static final String ANIMAL_CONVEXE = "convexe";
    private static final String ANIMAL_CIRCULAIRE = "circulaire";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Création de la table didacticiel
        String CREATE_DIDACTICIEL_TABLE = "create table if not exists " + TABLE_DIDACTICIEL + " ("
                + DIDACTICIEL_ID + " INTEGER PRIMARY KEY," + DIDACTICIEL_MENU + " BOOLEAN," + DIDACTICIEL_ANALYSE + " BOOLEAN,"
                + DIDACTICIEL_SETTINGS + " BOOLEAN, " + DIDACTICIEL_PROFIL + " BOOLEAN, " + DIDACTICIEL_SEARCH + " BOOLEAN " + " )";
        //Création de la table feuille
        String CREATE_LEAF_TABLE = "create table if not exists " + TABLE_LEAFS + " ("
                + LEAF_ID + " INTEGER PRIMARY KEY," + LEAF_NAME + " VARCHAR(60)," + LEAF_PICTURE + " VARCHAR(80) " + " )";
        //Création de la table oiseau
        String CREATE_BIRD_TABLE = "create table if not exists " + TABLE_BIRD + " ("
                + BIRD_ID + " INTEGER PRIMARY KEY," + BIRD_NAME + " VARCHAR(60)," + BIRD_PICTURE + " VARCHAR(80) " + " )";
        //Création de la table couleurs
        String CREATE_COLORS_TABLE = "create table if not exists " + TABLE_COLORS + " ("
                + COLORS_ID + " INTEGER PRIMARY KEY," + COLORS_COLOR + " VARCHAR(100)  )";
        //Création de la table animal
        String CREATE_ANIMAL_TABLE = "create table if not exists " + TABLE_ANIMAL + " (" + ANIMAL_ID + " INTEGER PRIMARY KEY," + ANIMAL_NOM + " VARCHAR(60), "
                + ANIMAL_IMAGE + " VARCHAR(60), " + ANIMAL_NBDOIGT + " INTEGER(1), " + ANIMAL_DOIGTA + " INTEGER(1), " + ANIMAL_PALME + " INTEGER(1), " + ANIMAL_MEMETAILLE + " INTEGER(1), " +
                ANIMAL_NBCOUSSINET + " INTEGER(1), " + ANIMAL_GRIFFES + " INTEGER(1), " + ANIMAL_NBSABOT + " INTEGER(1), " + ANIMAL_CONCAVE + " INTEGER(1), " +
                ANIMAL_CONVEXE + " INTEGER(1), " + ANIMAL_CIRCULAIRE + " INTEGER(1) ) ";

        sqLiteDatabase.execSQL(CREATE_ANIMAL_TABLE);
        sqLiteDatabase.execSQL(CREATE_DIDACTICIEL_TABLE);
        sqLiteDatabase.execSQL(CREATE_LEAF_TABLE);
        sqLiteDatabase.execSQL(CREATE_BIRD_TABLE);
        sqLiteDatabase.execSQL(CREATE_COLORS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DIDACTICIEL);
        onCreate(sqLiteDatabase);
    }
    /**

     * Create

     * Read

     * Update

     * Delete

     */


    /**
     * Ajout d'une couleur
     *
     * @param hslColor
     */
    public void addColor(HslColor hslColor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //values.put(COLORS_ID, hslColor.getId());
        String colorsString = hslColor.getHsls().toString();
        values.put(COLORS_COLOR, colorsString);
        db.insert(TABLE_COLORS, null, values);
        db.close();
    }


    /**
     * Fonction permettant de récupérer toutes les couleurs des feuilles
     *
     * @return
     */
    public List<Integer> getAllColors() {
        List<Integer> colorsList = new ArrayList<Integer>();
        String selectQuery = "SELECT  * FROM " + TABLE_COLORS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HslColor hslColor = new HslColor();
                int id = Integer.parseInt(cursor.getString(0));
                String listString = cursor.getString(1);
                String[] items = listString.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                //Ajout des couleurs de toutes les feuilles
                for (int i = 0; i < items.length; i++) {
                    colorsList.add(Integer.parseInt(items[i]));
                }
            } while (cursor.moveToNext());

        }
        return colorsList;
    }

    /**
     * Met à jour les préférences lié au didacticiel
     *
     * @param value
     * @param entry
     * @return
     */

    public int updateDidacticiel(String value, Boolean entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        int id = 0;
        switch (value) {
            case DIDACTICIEL_MENU:
                id = 1;
                values.put(DIDACTICIEL_MENU, entry);
            case DIDACTICIEL_ANALYSE:
                id = 2;
                values.put(DIDACTICIEL_ANALYSE, entry);
            case DIDACTICIEL_SETTINGS:
                id = 3;
                values.put(DIDACTICIEL_SETTINGS, entry);
            case DIDACTICIEL_PROFIL:
                id = 4;
                values.put(DIDACTICIEL_PROFIL, entry);
            case DIDACTICIEL_SEARCH:
                id = 5;
                values.put(DIDACTICIEL_SEARCH, entry);

        }
        return db.update(TABLE_DIDACTICIEL, values, DIDACTICIEL_ID + " = ?",
                new String[]{String.valueOf(id)});

    }

    /**
     * Méthode servant à retourner le booleen correspondant à la préférence
     *
     * @param value
     * @return
     */

    public boolean getDidacticiel(String value) {
        SQLiteDatabase db = this.getReadableDatabase();
        int id = 0;
        switch (value) {
            case DIDACTICIEL_MENU:
                id = 1;
            case DIDACTICIEL_ANALYSE:
                id = 2;
            case DIDACTICIEL_SETTINGS:
                id = 3;
            case DIDACTICIEL_PROFIL:
                id = 4;
            case DIDACTICIEL_SEARCH:
                id = 5;

        }
        Cursor cursor = db.query(TABLE_DIDACTICIEL, new String[]{value}, DIDACTICIEL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        cursor.moveToFirst();
        return cursor.getString(0).equals("1");
    }

    /**
     * Méthode permettant l'ajout d'une empreinte animal
     *
     * @param animal
     */
    public void addAnimal(Animal animal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ANIMAL_NOM, animal.getNom());
        values.put(ANIMAL_IMAGE, animal.getImage());
        values.put(ANIMAL_NBDOIGT, animal.getNbDoigt());
        values.put(ANIMAL_DOIGTA, animal.getDoigtA());
        values.put(ANIMAL_PALME, animal.getPalme());
        values.put(ANIMAL_MEMETAILLE, animal.getMemeTaille());
        values.put(ANIMAL_NBCOUSSINET, animal.getNbCoussinet());
        values.put(ANIMAL_GRIFFES, animal.getGriffe());
        values.put(ANIMAL_NBSABOT, animal.getNbSabot());
        values.put(ANIMAL_CONCAVE, animal.getConcave());
        values.put(ANIMAL_CONVEXE, animal.getConvexe());
        values.put(ANIMAL_CIRCULAIRE, animal.getCirculaire());
        db.insert(TABLE_ANIMAL, null, values);
        db.close();
    }

    /**
     * Retourne une liste d'animaux correspondant à la requête
     *
     * @param query
     * @return
     */
    public List<Animal> getAnimalsFromRequest(String query) {
        List<Animal> animalList = new ArrayList<Animal>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Animal animal = new Animal();
                animal.setId(Integer.parseInt(cursor.getString(0)));
                animal.setNom(cursor.getString(1));
                animal.setImage(cursor.getString(2));
                animal.setNbDoigt(Integer.parseInt(cursor.getString(3)));
                animal.setDoigtA(Integer.parseInt(cursor.getString(4)));
                animal.setPalme(Integer.parseInt(cursor.getString(5)));
                animal.setMemeTaille(Integer.parseInt(cursor.getString(6)));
                animal.setNbCoussinet(Integer.parseInt(cursor.getString(7)));
                animal.setGriffe(Integer.parseInt(cursor.getString(8)));
                animal.setNbSabot(Integer.parseInt(cursor.getString(9)));
                animal.setConcave(Integer.parseInt(cursor.getString(10)));
                animal.setConvexe(Integer.parseInt(cursor.getString(11)));
                animal.setCirculaire(Integer.parseInt(cursor.getString(12)));
                animalList.add(animal);

            } while (cursor.moveToNext());

        }
        cursor.close();
        return animalList;

    }


    /**
     * Ajout d'empreinte animal depuis un CSV
     *
     * @param inputStream
     * @param context
     */
    public void addAnimalFromCsv(InputStream inputStream, Context context) {
        DatabaseHandler db = new DatabaseHandler(context);
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String str[] = line.split(";");
            db.addAnimal(new Animal(Integer.parseInt(str[0]), str[1], str[2], Integer.parseInt(str[3]), Integer.parseInt(str[4]), Integer.parseInt(str[5])
                    , Integer.parseInt(str[6]), Integer.parseInt(str[7]), Integer.parseInt(str[8]), Integer.parseInt(str[9]), Integer.parseInt(str[10]),
                    Integer.parseInt(str[11]), Integer.parseInt(str[12])));
        }
        scanner.close();

    }

    /**
     * Retourne tous les animaux de la base de données
     *
     * @return
     */
    public List<Animal> getAllAnimals() {
        List<Animal> animalList = new ArrayList<Animal>();
        String selectQuery = "SELECT  * FROM " + TABLE_ANIMAL;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Animal animal = new Animal();
                animal.setId(Integer.parseInt(cursor.getString(0)));
                animal.setNom(cursor.getString(1));
                animal.setImage(cursor.getString(2));
                animal.setNbDoigt(Integer.parseInt(cursor.getString(3)));
                animal.setDoigtA(Integer.parseInt(cursor.getString(4)));
                animal.setPalme(Integer.parseInt(cursor.getString(5)));
                animal.setMemeTaille(Integer.parseInt(cursor.getString(6)));
                animal.setNbCoussinet(Integer.parseInt(cursor.getString(7)));
                animal.setGriffe(Integer.parseInt(cursor.getString(8)));
                animal.setNbSabot(Integer.parseInt(cursor.getString(9)));
                animal.setConcave(Integer.parseInt(cursor.getString(10)));
                animal.setConvexe(Integer.parseInt(cursor.getString(11)));
                animal.setCirculaire(Integer.parseInt(cursor.getString(12)));
                animalList.add(animal);

            } while (cursor.moveToNext());

        }
        return animalList;

    }


    /**
     * Méthode permettant l'ajout d'un oiseau
     *
     * @param bird
     */
    public void addBird(Bird bird) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BIRD_ID, bird.getId());
        values.put(BIRD_NAME, bird.getName());
        values.put(BIRD_PICTURE, bird.getPicture());
        db.insert(TABLE_BIRD, null, values);
        db.close();
    }

    /**
     * Retourne un oiseau de la base de données
     *
     * @param id
     * @return
     */
    public Bird getBird(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BIRD, new String[]{BIRD_ID,
                        BIRD_NAME, BIRD_PICTURE}, BIRD_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Bird bird = new Bird(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2));
        return bird;

    }

    /**
     * Ajout d'oiseaux depuis un CSV
     *
     * @param inputStream
     * @param context
     */
    public void addBirdFromCsv(InputStream inputStream, Context context) {
        DatabaseHandler db = new DatabaseHandler(context);
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String str[] = line.split(";");
            db.addBird(new Bird(Integer.parseInt(str[0]), str[1], str[2]));
        }
        scanner.close();
    }

    /**
     * Retourne tous les oiseaux de la base de données
     *
     * @return
     */
    public List<Bird> getAllBirds() {
        List<Bird> birdList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_BIRD;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Bird bird = new Bird();
                bird.setId(Integer.parseInt(cursor.getString(0)));
                bird.setName(cursor.getString(1));
                bird.setPicture(cursor.getString(2));
                birdList.add(bird);
            } while (cursor.moveToNext());

        }
        return birdList;
    }

    /**
     * Retourne une liste d'oiseaux dont le nom contien le paramètre
     *
     * @param name
     * @return
     */
    public List<Bird> getBirdsFromName(String name) {
        List<Bird> birdList = new ArrayList<Bird>();
        String selectQuery = "SELECT  id, name, picture FROM " + TABLE_BIRD + " WHERE name LIKE " + "'" + name + "%'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Bird bird = new Bird();
                bird.setId(Integer.parseInt(cursor.getString(0)));
                bird.setName(cursor.getString(1));
                bird.setPicture(cursor.getString(2));
                birdList.add(bird);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return birdList;
    }

    /**
     * Méthode permettant l'ajout d'une feuille
     *
     * @param leaf
     */
    public void addLeaf(Leaf leaf) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LEAF_NAME, leaf.getName());
        values.put(LEAF_PICTURE, leaf.getPicture());
        db.insert(TABLE_LEAFS, null, values);
        db.close();
    }

    /**
     * Retourne une feuille de la base de données
     *
     * @param id
     * @return
     */
    public Leaf getLeaf(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LEAFS, new String[]{LEAF_ID,
                        LEAF_NAME, LEAF_PICTURE}, LEAF_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Leaf leaf = new Leaf(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2));
        return leaf;

    }

    /**
     * Retourne toutes les feuilles de la base de données
     *
     * @return
     */
    public List<Leaf> getAllLeafs() {
        List<Leaf> leafList = new ArrayList<Leaf>();
        String selectQuery = "SELECT  * FROM " + TABLE_LEAFS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Leaf leaf = new Leaf();
                leaf.setId(Integer.parseInt(cursor.getString(0)));
                leaf.setName(cursor.getString(1));
                leaf.setPicture(cursor.getString(2));
                leafList.add(leaf);
            } while (cursor.moveToNext());
        }
        return leafList;
    }

    /**
     * Retourne une liste de feuille dont le nom contien le paramètre
     *
     * @param name
     * @return
     */
    public List<Leaf> getLeafsFromName(String name) {
        List<Leaf> leafList = new ArrayList<Leaf>();
        String selectQuery = "SELECT  id, name, picture FROM " + TABLE_LEAFS + " WHERE name LIKE " + "'" + name + "%'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Leaf leaf = new Leaf();
                leaf.setId(Integer.parseInt(cursor.getString(0)));
                leaf.setName(cursor.getString(1));
                leaf.setPicture(cursor.getString(2));
                leafList.add(leaf);

            } while (cursor.moveToNext());

        }
        cursor.close();
        return leafList;
    }

    /**
     * Met à jour une feuille dans la base de données
     */
    public int updateLeaf(Leaf leaf) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LEAF_NAME, leaf.getName());
        values.put(LEAF_PICTURE, leaf.getPicture());
        return db.update(TABLE_LEAFS, values, LEAF_ID + " = ?",
                new String[]{String.valueOf(leaf.getId())});
    }

    /**
     * Supprime une feuille dans la base de donnée
     */
    public void deleteLeaf(Leaf leaf) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LEAFS, LEAF_ID + " = ?",
                new String[]{String.valueOf(leaf.getId())});
        db.close();

    }

    /**
     * Compte le nombre de feuille dans la base de données
     *
     * @return
     */
    public int getLeafsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LEAFS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        return cursor.getCount();

    }

    /**
     * Ajout des feuilles depuis un fichier csv
     *
     * @param inputStream
     * @param context
     */
    public void addLeafFromCsv(InputStream inputStream, Context context) {
        DatabaseHandler db = new DatabaseHandler(context);
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String str[] = line.split(";");
            db.addLeaf(new Leaf(Integer.parseInt(str[0]), str[1], str[2]));
        }
        scanner.close();

    }


    /**
     * Fonction permettant de voir si une table est vide
     *
     * @param table
     * @return vrai si la table est vide
     */

    public boolean emptyTable(String table) {
        String query = "SELECT count(*) FROM " + table;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        if (count > 0) {
            cursor.close();
            return false;

        } else {
            cursor.close();
            return true;
        }

    }

}
