package descartes.info.l3ag2.eyetrek.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cette classe représente les syllabes avec les indices
 * de début et de fin dans un segment et l'enregistrement complet.
 *
 * Created by Dorian on 25/03/2018.
 */

public class Syllabe {

    //marqueur de début de syllabe dans un chunk
    private int onSetChunk;
    //marqueur de début de syllabe dans l'enregistrement complet
    private int onSetRecord;
    //marqueur de fin de syllabe dans un chunk
    private int offSetChunk;
    //marqueur de fin de syllabe dans l'enregistrement complet
    private int offSetRecord;
    //données brutes de la syllabe
    private List<Float> dataRawSyllabe;
    //données de l'enveloppe sonore de la syllabe
    private List<Float> dataEnvelopeSyllabe;
    //List MFCCS des windows de la syllabe
    private List<float[]> listMFCCs;


    public Syllabe(int onSetChunk, int offSetChunk, int onSetRecord, int offSetRecord) {
        this.onSetChunk = onSetChunk;
        this.onSetRecord = onSetRecord;
        this.offSetChunk = offSetChunk;
        this.offSetRecord = offSetRecord;
        this.dataRawSyllabe = new ArrayList<>();
        this.dataEnvelopeSyllabe = new ArrayList<>();
        this.listMFCCs = new ArrayList<>();
    }

    /**
     * @return l'indice de début de la syllabe (enregistrement complet)
     */
    public int getOnSetRecord() {
        return onSetRecord;
    }

    /**
     * Permet de définir l'indice de début de la syllabe (enregistrement complet)
     * @param onSetRecord l'indice de début
     */
    public void setOnSetRecord(int onSetRecord) {
        this.onSetRecord = onSetRecord;
    }

    /**
     *
     * @return l'indice de fin de la syllabe (enregistrement complet)
     */
    public int getOffSetRecord() {
        return offSetRecord;
    }

    /**
     * Permet de définir l'indice de fin de la syllabe (enregistrement complet)
     * @param offSetRecord l'indice de fin
     */
    public void setOffSetRecord(int offSetRecord) {
        this.offSetRecord = offSetRecord;
    }

    public int getOnSetChunk() {
        return onSetChunk;
    }

    public void setOnSetChunk(int onSetChunk) {
        this.onSetChunk = onSetChunk;
    }

    public int getOffSetChunk() {
        return offSetChunk;
    }

    public void setOffSetChunk(int offSetChunk) {
        this.offSetChunk = offSetChunk;
    }

    public void setDataRawSyllabe(List<Float> data) {
        for(int i = onSetRecord; i<= offSetRecord; i++) {
            dataRawSyllabe.add(data.get(i));
        }
    }

    public void setDataEnvelopeSyllabe(List<Float> data) {
        for(int i = onSetRecord; i<= offSetRecord; i++) {
            dataEnvelopeSyllabe.add(data.get(i));
        }
    }

    /**
     * Permet de tranformer la liste des valeurs de l'enveloppe sonore en un tableau de float
     * @return le tableau des valeurs de l'enveloppe sonore
     */
    public float[] getFloatBufferEnvelope() {
        float[] buffer = new float[dataEnvelopeSyllabe.size()];

        for(int i = 0; i<dataEnvelopeSyllabe.size(); i++)  {
            buffer[i] = dataEnvelopeSyllabe.get(i);
        }

        return buffer;
    }

    /**
     * Permet de transformer la liste des valeurs de l'enregistrement brut
     * en un tableau de float
     * @return le tableau des valeurs de l'enregistrement brut
     */
    public float[] getFloatBufferRaw() {
        float[] buffer = new float[dataRawSyllabe.size()];

        for(int i = 0; i<dataRawSyllabe.size(); i++)  {
            buffer[i] = dataRawSyllabe.get(i);
        }

        return buffer;
    }

    /**
     * Permet de définir la liste des coefficients MFCC contenus dans les différentes fenêtres
     * de la syllabe
     * @param listMFCCs liste des coefficients MFCC
     */
    public void setListMFCCs(List<float[]> listMFCCs) {
        int size = listMFCCs.size();

        if(size > 1) {

            float[] sommeCoeff = {0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f};

            for(int i = 0; i < size; i++) {
                for(int j = 0; j < listMFCCs.get(i).length; j++) {
                    sommeCoeff[j] += listMFCCs.get(i)[j];
                }
            }

            for(int j = 0; j < sommeCoeff.length; j++) {
                sommeCoeff[j] /= size;
            }

            this.listMFCCs = Arrays.asList(sommeCoeff);
        }
        else {
            this.listMFCCs = listMFCCs;
        }
    }

    /**
     *
     * @return la liste des coefficients MFCC contenus dans les fenêtres de la syllabe
     */
    public List<float[]> getListMFCCs() {
        return this.listMFCCs;
    }

    public List<Float> getListFloatMFCCs() {
        List<Float> list = new ArrayList<>();

        if(listMFCCs.size()>0) {
            float[] coeff = listMFCCs.get(0);
            for(int i = 0; i<coeff.length;i++) {
                list.add(coeff[i]);
            }
        }

        return list;

    }

    public List<Float> normalizeMFCC() {
        List<Float> normalizedMFCC = new ArrayList<>();

        if(listMFCCs.size()>0) {
            float[] coeff = listMFCCs.get(0);
            for(int i = 0; i<coeff.length;i++) {
                normalizedMFCC.add((float)Math.atan(coeff[i]));
            }
        }

        return normalizedMFCC;

    }


    @Override
    public String toString() {
        return "Syllabe{" +
                "onSetChunk=" + onSetChunk +
                ", onSetRecord=" + onSetRecord +
                ", offSetChunk=" + offSetChunk +
                ", offSetRecord=" + offSetRecord +
                ", dataRawSyllabe=" + dataRawSyllabe +
                ", dataEnvelopeSyllabe=" + dataEnvelopeSyllabe +
                ", listMFCCs=" + listMFCCs +
                '}';
    }
}