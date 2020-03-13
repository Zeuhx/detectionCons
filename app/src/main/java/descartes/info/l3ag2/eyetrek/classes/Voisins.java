package descartes.info.l3ag2.eyetrek.classes;

import android.util.Log;

public class Voisins {

	private int label;
	private int count;
	
	public Voisins(int label, int count) {
		this.label = label;
		this.count = count;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getFrequency(){
		return (int)((count/10.0)*100);
	}

	public int getFrequencyBird(int nbElement) { Log.e("count&nb",count + ", " + nbElement); return (int)((count/(double)nbElement)*100);}
	
	public void incrementCount() {
		this.count++;
	}
	
	public String toString() {
		return "Label : " + label + ", Count : " + count;
	}
	
}
