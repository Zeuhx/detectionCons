package descartes.info.l3ag2.eyetrek.classes;

public class Result {
	
	private int label;
	private double distance;
	
	
	public Result(int label, double distance) {
		this.label = label;
		this.distance = distance;
	}


	public int getLabel() {
		return label;
	}


	public void setLabel(int label) {
		this.label = label;
	}


	public double getDistance() {
		return distance;
	}


	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	public String toString() {
		return "Label : "+label+", Distance : "+distance;
	}
	
}
