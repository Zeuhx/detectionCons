package descartes.info.l3ag2.eyetrek.classes;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
public class AlgoKNNBirds {

	private static int K_VOISINS = 1;
	private static Context context;

	public static void setContext(Context c) {
		context = c;
	}
	
	/**
	 * Extraction des lignes contenues dans le csv
	 * 
	 * @return liste des lignes du csv
	 * @throws IOException
	 */
	private static List<String> getDataLine() throws IOException {

		InputStream is = null;// = context.getResources().openRawResource(R.raw.features_data_sound);

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
	 * caracteristiques.
	 * 
	 * @return liste des vecteurs de carateristiques.
	 * @throws IOException
	 */
	public static List<FeaturesVectBirds> getListFeatures() throws IOException {
		//lignes du csv
		List<String> dataLines = getDataLine();
		//vecteurs caracteristiques
		List<FeaturesVectBirds> listFeatures = new ArrayList<>();
		
		//Pour chaque ligne du csv, on extrait une caracteristique selon le delimiteur ";"
		for(String line : dataLines) {
			StringTokenizer stringTokenizer = new StringTokenizer(line,";");
				
			FeaturesVectBirds features = new FeaturesVectBirds();
			
			//On recupere la 1ere valeur : le label
			int label = Integer.parseInt(stringTokenizer.nextToken());
			//On affecte cette valeur a ce vecteur
			features.setLabel(label);

			//On initialise la liste des coefficients MFCCS
			List<Float> MFCCS = new ArrayList<>();

			//On recupere les 12 coefficients MFCC :
			for(int i = 0; i<12; i++) {
				MFCCS.add(Float.parseFloat(stringTokenizer.nextToken()));
			}

			features.setMFCCS(MFCCS);

			//on ajoute finalement tout à la liste
			listFeatures.add(features);
		}

		return listFeatures;
	}
	
	
	/**
	 * Calcul de la distance de Manhattan entre 2 vecteurs de caracteristiques de meme dimension.
	 * 
	 * @param testFeatures vecteur de la partie test
	 * @param trainFeatures vecteur de la partie train
	 * @return distance euclidienne
	 */
	private static double distanceManhattanVectFeatures(FeaturesVectBirds testFeatures, FeaturesVectBirds trainFeatures) {
		double distance = 0.0;
		
		//On calcule la distance de manhattan entre chaque valeur de la caracteristique MFCC
		for(int i = 0; i<testFeatures.getMFCCS().size(); i++) {
			distance += distanceAbsData(testFeatures.getMFCCS().get(i), trainFeatures.getMFCCS().get(i));
		}
		
		return distance;
	}
	
	/**
	 * La valeur absolue de la difference
	 * @param data1 1ere valeur
	 * @param data2 2eme valeur
	 * @return la valeur absolue de la difference entre 2 valeurs
	 */
	private static double distanceAbsData(double data1, double data2) {
		return Math.abs(data1 - data2);
	}

	/**
	 * 
	 * Renvoie les K Plus Proches Voisins (c'est a dire ceux qui ont la distance minimale avec le vecteur de test).
	 * On calcul tout d'abord les distances entre le vecteur de test et tous les vecteurs de la base de train.
	 * 
	 * @param trainingSet vecteurs de la base de train
	 * @param testFeatures vecteur test à tester
	 * @return k plus proches voisins
	 */
	public static List<Result> getPlusProchesVoisins(List<FeaturesVectBirds> trainingSet, FeaturesVectBirds testFeatures) {
		//Liste des k plus proches voisins
		List<Result> voisins = new ArrayList<>();
		//Liste contenant les resultats (label, distance) avec le vecteur de test
		List<Result> result = new ArrayList<>();
		
		//On stocke le label et la distance entre le vecteur de test et les vecteurs d'entrainement.
		for(int i = 0; i<trainingSet.size(); i++) {
			double dist = distanceManhattanVectFeatures(testFeatures, trainingSet.get(i));
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
	 * @return le label de la prediction
	 */
	public static List<Voisins> getPrediction(List<Result> voisins) {
		List<Voisins> countVoisin = new ArrayList<>();
		
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
			countVoisin.add(new Voisins(entry.getKey(),entry.getValue()));
		}

		//On veut la classe qui apparait le plus de fois.
		//On trie dans l'ordre decroissant les distances pour avoir les classes dominantes (nb d'occurrence)
		Collections.sort(countVoisin, (Voisins r1, Voisins r2) -> {
							
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

		return countVoisin;
	}
}
