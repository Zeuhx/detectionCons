package descartes.info.l3ag2.eyetrek.classes;

import java.util.List;

/**
 * Created by Dorian Quaboul
 */

public class FeaturesVectBirds {
	
	private int label;
	private List<Float> MFCCS;

	public FeaturesVectBirds() { }

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public List<Float> getMFCCS() {
		return MFCCS;
	}

	public void setMFCCS(List<Float> MFCCS) {
		this.MFCCS = MFCCS;
	}

}
