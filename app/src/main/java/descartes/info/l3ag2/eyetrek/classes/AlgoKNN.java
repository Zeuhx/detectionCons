package descartes.info.l3ag2.eyetrek.classes;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import descartes.info.l3ag2.eyetrek.R;

/**
 * Created by Dorian Quaboul
 */
public class AlgoKNN {

	private static int K_VOISINS = 10;
	private static Context context;

	public static double max_aspectRatio;
	public static double min_aspectRatio;
	public static double max_whiteAreaRatio;
	public static double min_whiteAreaRatio;
	public static double max_perimeterToArea;
	public static double min_perimeterToArea;
	public static double max_perimeterToHull;
	public static double min_perimeterToHull;
	public static double max_hullAreaRatio;
	public static double min_hullAreaRatio;
	public static double max_dispersion;
	public static double min_dispersion;
	public static double max_roundness;
	public static double min_roundness;

	public static void setContext(Context c) {
		context = c;
	}
	//private static int K_VOISINS = (int)Math.sqrt(153)-2;
	
	
	/**
	 * Extraction des lignes contenues dans le csv
	 * 
	 * @return liste des lignes du csv
	 * @throws IOException
	 */
	public static List<String> getDataLine() throws IOException {
		InputStream is = null;// = context.getResources().openRawResource(R.raw.feature_data_vect1);

		BufferedReader bufferReader = new BufferedReader(new InputStreamReader(is));
		
		List<String> dataLines = new ArrayList<>();
		
		String line;
		//Tant qu'il y a des lignes, on les ajoute a la liste
		while((line = bufferReader.readLine()) != null) {
			dataLines.add(line);
		}
		
		return dataLines;
	}
	
	/**
	 * Extraction des vecteurs de caracteristiques dans chaque ligne du
	 * csv.
	 * Creation d'une liste d'objets "Features" correspondant aux vecteurs de
	 * caract�ristiques.
	 * 
	 * @return liste des vecteurs de carateristiques.
	 * @throws IOException
	 */
	public static List<FeaturesVect> getListFeatures() throws IOException {
		//lignes du csv
		List<String> dataLines = getDataLine();
		//vecteurs caract�ristiques
		List<FeaturesVect> listFeatures = new ArrayList<>();
		int countLine = 0;
		
		//Pour chaque ligne du csv, on extrait une caract�ristique selon le d�limiteur ";" 
		for(String line : dataLines) {
			StringTokenizer stringTokenizer = new StringTokenizer(line,";");
			FeaturesVect features = new FeaturesVect();

			//On igore la premiere ligne qui contient tous les max des caractéristiques
			if(countLine == 0) {
				stringTokenizer.nextToken();

				max_aspectRatio = Double.parseDouble(stringTokenizer.nextToken());
				max_whiteAreaRatio = Double.parseDouble(stringTokenizer.nextToken());
				max_perimeterToArea = Double.parseDouble(stringTokenizer.nextToken());
				max_perimeterToHull = Double.parseDouble(stringTokenizer.nextToken());
				max_hullAreaRatio = Double.parseDouble(stringTokenizer.nextToken());
				max_dispersion = Double.parseDouble(stringTokenizer.nextToken());
				max_roundness = Double.parseDouble(stringTokenizer.nextToken());

				countLine++;
			}
			//On ignore la deuxieme ligne contenant les min
			else if(countLine == 1) {
				stringTokenizer.nextToken();

				min_aspectRatio = Double.parseDouble(stringTokenizer.nextToken());
				min_whiteAreaRatio = Double.parseDouble(stringTokenizer.nextToken());
				min_perimeterToArea = Double.parseDouble(stringTokenizer.nextToken());
				min_perimeterToHull = Double.parseDouble(stringTokenizer.nextToken());
				min_hullAreaRatio = Double.parseDouble(stringTokenizer.nextToken());
				min_dispersion = Double.parseDouble(stringTokenizer.nextToken());
				min_roundness = Double.parseDouble(stringTokenizer.nextToken());

				countLine++;
			}
			else{
				//On recupere la 1ere caract�ristique : le label
				int label = Integer.parseInt(stringTokenizer.nextToken());
				//On affecte cette valeur � ce vecteur
				features.setLabel(label);

				//On r�cup�re la 2e caract�ristique : le aspect ratio
				double aspectRatio = Double.parseDouble(stringTokenizer.nextToken());
				//On affecte cette valeur � ce vecteur
				features.setAspectRatio(aspectRatio);

				//On r�cup�re la 3e caract�ristique : le white area ratio
				double whiteAreaRatio = Double.parseDouble(stringTokenizer.nextToken());
				//On affecte cette valeur � ce vecteur
				features.setWhiteAreaRatio(whiteAreaRatio);

				//On recupere la 4e caract�ristique : le perimeter to area
				double perimeterToArea = Double.parseDouble(stringTokenizer.nextToken());
				//On affecte cette valeur a ce vecteur
				features.setPerimeterToArea(perimeterToArea);

				//On recupere la 5e caracteristique : le perimiter to hull
				double perimeterToHull = Double.parseDouble(stringTokenizer.nextToken());
				//On affecte cette valeur a ce vecteur
				features.setPerimeterToHull(perimeterToHull);

				//On recupere la 6e caracteristique : le hull area ratio
				double hullAreaRatio = Double.parseDouble(stringTokenizer.nextToken());
				//On affecte cette valeur � ce vecteur
				features.setHullAreaRatio(hullAreaRatio);
				
				//On recupere la 7e caracteristique : le shape factor
				double shapeFactor = Double.parseDouble(stringTokenizer.nextToken());
				//On affecte cette valeur � ce vecteur
				features.setShapeFactor(shapeFactor);

				//On r�cup�re la 8e caract�ristique : la roudness
				double roundness = Double.parseDouble(stringTokenizer.nextToken());
				//On affecte cette valeur � ce vecteur
				features.setRoundness(roundness);

				//On r�cup�re la 9e caract�ristique : la dispersion
				double dispersion = Double.parseDouble(stringTokenizer.nextToken());
				//On affecte cette valeur � ce vecteur
				features.setDispersion(dispersion);

				//On r�cup�re la 10e caract�ristique : centroid radial distance
				List<Double> centroidRadialDistance = new ArrayList<>();
				StringBuffer sbCentroidRadialDistance = new StringBuffer(stringTokenizer.nextToken());
				//On supprime les 2 crochets "[" et "]" qui entourent les valeurs
				sbCentroidRadialDistance.deleteCharAt(0);
				sbCentroidRadialDistance.deleteCharAt(sbCentroidRadialDistance.length()-1);
				StringTokenizer stCentroidRadialDistance = new StringTokenizer(sbCentroidRadialDistance.toString(), ",");
				while(stCentroidRadialDistance.hasMoreTokens()) {
					centroidRadialDistance.add(Double.parseDouble(stCentroidRadialDistance.nextToken()));
				}
				//On affecte ces valeurs � ce vecteur
				features.setCentroidRadialDistance(centroidRadialDistance);

				//on ajoute finalement la caract�ristique compl�te � la liste
				listFeatures.add(features);
			}
		}
		return listFeatures;
	}
	
	
	/**
	 * Calcul de la distance euclidienne entre 2 vecteurs de caract�ristiques de m�me dimension.
	 * 
	 * @param testFeatures vecteur de la partie test
	 * @param trainFeatures vecteur de la partie train
	 * @return distance euclidienne
	 */
	private static double distanceEuclidienneVectFeatures(FeaturesVect testFeatures, FeaturesVect trainFeatures) {
		double distance = 0.0;
		
		//On fait la somme des carr�s des diff�rences entre chaque coordonn�e des vecteurs
		distance += distancePowData(testFeatures.getAspectRatio(), trainFeatures.getAspectRatio());
		distance += distancePowData(testFeatures.getWhiteAreaRatio(), trainFeatures.getWhiteAreaRatio());
		distance += distancePowData(testFeatures.getPerimeterToArea(), trainFeatures.getPerimeterToArea());
		distance += distancePowData(testFeatures.getPerimeterToHull(), trainFeatures.getPerimeterToHull());
		distance += distancePowData(testFeatures.getHullAreaRatio(), trainFeatures.getHullAreaRatio());
		distance += distancePowData(testFeatures.getShapeFactor(), trainFeatures.getShapeFactor());
		distance += distancePowData(testFeatures.getRoundness(), trainFeatures.getRoundness());
		distance += distancePowData(testFeatures.getDispersion(), trainFeatures.getDispersion());
		
		//On fait la m�me op�ration entre chaque valeur de la caract�ristique Centroid Radial Distance
		for(int i = 0; i<testFeatures.getCentroidRadialDistance().size(); i++) {
			distance += distancePowData(testFeatures.getCentroidRadialDistance().get(i), trainFeatures.getCentroidRadialDistance().get(i));
		}
		
		return Math.sqrt(distance);
	}
	
	/**
	 * Le carr� de la diff�rences de la distance euclidienne
	 * @param data1 1ere valeur
	 * @param data2 2eme valeur
	 * @return le carr� de la diff�rence entre 2 valeurs
	 */
	private static double distancePowData(double data1, double data2) {
		return Math.pow(data1 - data2, 2);
	}

	/**
	 * 
	 * Renvoie les K Plus Proches Voisins (c'est � dire ceux qui ont la distance minimale avec le vecteur de test).
	 * On calcul tout d'abord les distances entre le vecteur de test et tous les vecteurs de la base de train.
	 * 
	 * @param trainingSet vecteurs de la base de train
	 * @param testFeatures vecteur test � tester
	 * @return k plus proches voisins
	 */
	public static List<Result> getPlusProchesVoisins(List<FeaturesVect> trainingSet, FeaturesVect testFeatures) {
		//Liste des k plus proches voisins
		List<Result> voisins = new ArrayList<>();
		//Liste contenant les resultats (label, distance) avec le vecteur de test
		List<Result> result = new ArrayList<>();
		
		//On stocke le label et la distance entre le vecteur de test et les vecteurs d'entrainement.
		for(int i = 0; i<trainingSet.size(); i++) {
			double dist = distanceEuclidienneVectFeatures(testFeatures, trainingSet.get(i));
			int label = trainingSet.get(i).getLabel();
			
			Result res = new Result(label,dist);
			result.add(res);
		}
		
		//On trie dans l'ordre croissant les distances pour avoir les distances minimales en premier.
		Collections.sort(result, (Result r1, Result r2) -> {
				
			if(r1.getDistance() < r2.getDistance()) {
				return -1;
			}
			else if (r1.getDistance() > r2.getDistance()) {
				return 1;
			}
			else {
				return 0;
			}
			
		});
		//On ajoute les K Plus Proches Voisins
		for(int i = 0; i<K_VOISINS; i++) {
			voisins.add(result.get(i));
		}
		
		return voisins;
	}
	
	
	/**
	 * Extraction de la classe dominante.
	 * On extrait la classe qui apparait le plus de fois dans la liste des
	 * voisins (celle qui le plus grand nombre d'occurrence).
	 * 
	 * @param voisins les k plus proches voisins
	 * @return le label de la pr�diction 
	 */
	public static List<Voisins> getPrediction(List<Result> voisins) {
		List<Voisins> countVoisins = new ArrayList<>();
		
		HashMap<Integer,Integer> hashMap = new HashMap<Integer,Integer>();
		
		//On compte le nombre d'occurence par classe
		for(int i = 0; i<voisins.size(); i++) {
			Integer label = voisins.get(i).getLabel();
			
			if(hashMap.containsKey(label)) {
				hashMap.put(label, hashMap.get(label)+1);
			}
			else {
				hashMap.put(label,1);
			}	
		}
		
		for(Map.Entry<Integer,Integer> entry : hashMap.entrySet()) {
			countVoisins.add(new Voisins(entry.getKey(),entry.getValue()));
		}
		
		
		//On veut la classe qui apparait le plus de fois.
		//On trie dans l'ordre d�croissant les distances pour avoir les classes dominantes (nb d'occurrence)
		Collections.sort(countVoisins, (Voisins r1, Voisins r2) -> {
							
			if(r1.getCount() > r2.getCount()) {
					return -1;
			}
			else if (r1.getCount() < r2.getCount()) {
				return 1;
			}
			else {
				return 0;
			}
		});
		
		return countVoisins;
	}

}
