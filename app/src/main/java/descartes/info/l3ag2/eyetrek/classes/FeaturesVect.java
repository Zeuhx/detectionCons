package descartes.info.l3ag2.eyetrek.classes;

import java.util.List;

/**
 * Created by Dorian Quaboul
 */
public class FeaturesVect {
	
	private int label;
	private double aspectRatio;
	private double whiteAreaRatio;
	private double perimeterToArea;
	private double perimeterToHull;
	private double hullAreaRatio;
	private double shapeFactor;
	private double roundness;
	private double dispersion;
	
	private List<Double> centroidRadialDistance;


	public FeaturesVect() {
		
	}

	public int getLabel() {
		return label;
	}


	public void setLabel(int label) {
		this.label = label;
	}


	public double getAspectRatio() {
		return aspectRatio;
	}


	public void setAspectRatio(double aspectRatio) {
		this.aspectRatio = aspectRatio;
	}


	public double getWhiteAreaRatio() {
		return whiteAreaRatio;
	}


	public void setWhiteAreaRatio(double whiteAreaRatio) {
		this.whiteAreaRatio = whiteAreaRatio;
	}


	public double getPerimeterToArea() {
		return perimeterToArea;
	}


	public void setPerimeterToArea(double perimeterToArea) {
		this.perimeterToArea = perimeterToArea;
	}


	public double getPerimeterToHull() {
		return perimeterToHull;
	}


	public void setPerimeterToHull(double perimeterToHull) {
		this.perimeterToHull = perimeterToHull;
	}


	public double getHullAreaRatio() {
		return hullAreaRatio;
	}


	public void setHullAreaRatio(double hullAreaRatio) {
		this.hullAreaRatio = hullAreaRatio;
	}
	
	
	public double getShapeFactor() {
		return shapeFactor;
	}
	
	public void setShapeFactor(double shapeFactor) {
		this.shapeFactor = shapeFactor;
	}

	public double getRoundness() {
		return roundness;
	}


	public void setRoundness(double roundness) {
		this.roundness = roundness;
	}


	public double getDispersion() {
		return dispersion;
	}


	public void setDispersion(double dispersion) {
		this.dispersion = dispersion;
	}


	public List<Double> getCentroidRadialDistance() {
		return centroidRadialDistance;
	}


	public void setCentroidRadialDistance(List<Double> centroidRadialDistance) {
		this.centroidRadialDistance = centroidRadialDistance;
	}
}
