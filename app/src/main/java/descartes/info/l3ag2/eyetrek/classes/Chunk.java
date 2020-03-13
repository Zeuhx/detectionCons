package descartes.info.l3ag2.eyetrek.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Dorian on 24/03/2018.
 */

public class Chunk {

    //durée correspondant à 0.5 s convertie en samples
    private static final int DUREE_CHUNK = 22050;

    private List<Float> valuesEnvelope;
    private List<Float> valuesRaw;

    private float maxEnergyLevel;
    private float moyEnvelopeEnergy;
    private float difference;
    private float percentageChange;
    private float noiseThreshold;
    private String type;
    private int num;

    private List<Syllabe> candidateSyllabes;

    public Chunk(List<Float> valuesEnvelope, List<Float> valuesRaw) {
        this.valuesEnvelope = valuesEnvelope;
        this.valuesRaw = valuesRaw;
        this.candidateSyllabes = new ArrayList<>();
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getNum() {
        return this.num;
    }


    public void setMoyEnvelopeEnergy() {
        float totalEnergy = 0f;

        for(Float f : valuesEnvelope) {
            totalEnergy += f;
        }

        this.moyEnvelopeEnergy = totalEnergy / (float)valuesEnvelope.size();
    }

    /**
     * Le maximum des valeurs de l'enveloppe sonore du segment
     */
    public void setMaxEnergyLevel() {
        this.maxEnergyLevel = Collections.max(valuesEnvelope);
    }

    /**
     *
     */
    public void detectSyllabes() {
        boolean findSyllabe = false;

        int offSet = 0;
        int onSet = 0;

        for(int i = 0; i < valuesEnvelope.size(); i++) {
            if(findSyllabe) {
                if(valuesEnvelope.get(i) < noiseThreshold) {
                    offSet = i;
                    findSyllabe = false;
                    candidateSyllabes.add(new Syllabe(onSet,offSet,(DUREE_CHUNK*num)+onSet,(DUREE_CHUNK*num)+offSet));
                }
            }else {
                if(valuesEnvelope.get(i) > noiseThreshold) {
                    onSet = i;
                    findSyllabe = true;
                }
            }
        }
    }

    /**
     *
     */
    public void setDifference() {
        this.difference = this.maxEnergyLevel - this.moyEnvelopeEnergy;
    }

    /**
     *
     */
    public void setPercentageChange() {
        this.percentageChange = (difference / this.maxEnergyLevel) * 100;
    }

    /**
     * 
     */
    public void setTypeChunk() {
        if(percentageChange > 50f) {
            this.type = "Noise";
        }
        else {
            this.type = "Silence";
        }
    }

    /**
     * Détermine le seuil de bruit
     */
    public void setNoiseThreshold() {
        this.noiseThreshold = this.maxEnergyLevel - (this.maxEnergyLevel * ((100f - this.percentageChange)/100f));
    }

    /**
     *
     * @return le type du chunk ("Silence" ou "Noise")
     */
    public String getType() {
        return this.type;
    }

    /**
     *
     * @return la liste des potentielles syllabes
     */
    public List<Syllabe> getCandidateSyllabes() {
        return candidateSyllabes;
    }

    public int getNbCandidateSyllabes() {
        return candidateSyllabes.size();
    }

}
