package descartes.info.l3ag2.eyetrek;


import junit.framework.TestCase;

import java.util.ArrayList;
import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.classes.UtilitaireResultat;

/**
 * Classe des testes unitaire de la classe UtilitaireResultat.java
 *
 * Created by Antony Torbey on 12/03/2018.
 */

public class UtilitaireResultatTest extends TestCase {


    public void testReadFile(){

        ArrayList<String> res = new ArrayList<String>(UtilitaireResultat.readFile("branches/scanFeuilles/app/src/main/res/raw/liste_leaf1.csv"));

        res = new ArrayList<String>(UtilitaireResultat.readFile("branches/scanFeuilles/app/src/main/res/raw/liste_leaf_details.csv"));

    }


    public void testGetInfo(){

        ArrayList<String> res = new ArrayList<String>();

        res = UtilitaireResultat.getInfo("acer_campestre");
        assertNotNull(res);

        res = UtilitaireResultat.getInfo("ailanthus_altissima");
        assertNotNull(res);

        res = UtilitaireResultat.getInfo("carya_ovata");
        assertNotNull(res);

        res = UtilitaireResultat.getInfo("cotoneaster_horizontalis");
        assertNotNull(res);

        res = UtilitaireResultat.getInfo("gleditsia_triacanthos");
        assertNotNull(res);

        res = UtilitaireResultat.getInfo("laburnum_anagyroides");
        assertNotNull(res);

        res = UtilitaireResultat.getInfo("mahonia_aquifolium");
        assertNotNull(res);

        res = UtilitaireResultat.getInfo("populus_canescens");
        assertNotNull(res);

        res = UtilitaireResultat.getInfo("rhamnus_cathartica");
        assertNotNull(res);

        res = UtilitaireResultat.getInfo("spiraea_salicifolia");
        assertNotNull(res);

    }


}