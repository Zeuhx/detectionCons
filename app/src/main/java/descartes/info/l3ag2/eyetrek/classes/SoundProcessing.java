package descartes.info.l3ag2.eyetrek.classes;


import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.EnvelopeFollower;
import be.tarsos.dsp.ZeroCrossingRateProcessor;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.mfcc.MFCC;

/**
 * Cette classe correspond aux différents traitements effectués pour la détection de syllabe
 * et l'extraction de caractéristiques d'un chant d'oiseaux
 *
 * Created by Dorian Quaboul on 21/03/2018.
 *
 * source (thèse / research paper) : https://www.um.edu.mt/library/oar/handle/123456789/8575
 */

public class SoundProcessing {

	/**
	 * Permet de déterminer l'architecture CPU de l'appareil Android actuellement utilisé et
	 * d'extraire depuis le dossier "assets" le fichier binaire correspondant à l'architecture
	 * de l'appareil Android.
	 * </p>
	 *
	 * @param context Context de l'application
	 */
	public static void init(Context context) {
		new AndroidFFMPEGLocator(context);
	}

	/**
	 * Permet de charger le contenu de l'enregistrement brut
	 * @param cheminFichier chemin du fichier correspondant à l'enregistrement
	 * @return liste contenant les valeurs de l'enregistrement brut
	 *
	 * Description de la méthode fromPipe de la classe AudioDispatcherFactory :
	 *
	 * public static AudioDispatcher fromPipe(final String source,final int targetSampleRate, final int audioBufferSize,final int bufferOverlap){
	 *      PipedAudioStream f = new PipedAudioStream(source);
	 *      TarsosDSPAudioInputStream audioStream = f.getMonoStream(targetSampleRate,0);
	 *      return new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);
	 * }
	 */
	public static List<Float> loadPCMData(String cheminFichier) {
		List<Float> recordDataBrut = new ArrayList<>();

		//on initialise l'audiodispatcher qui va parcourir l'enregistrement tous les 1024 valeurs
		AudioDispatcher dispatcherRecord = AudioDispatcherFactory.fromPipe(cheminFichier,44100, 1024, 0);
		dispatcherRecord.addAudioProcessor(new AudioProcessor() {

            @Override
            public boolean process(AudioEvent audioEvent) {
       			//Pour chaque buffer de taille 1024, on ajoute les valeurs du buffer dans la liste
                float[] buffer = audioEvent.getFloatBuffer();
                for(float value : buffer) {
                    recordDataBrut.add(value);
                }
                return true;
            }

            @Override
            public void processingFinished() {
			}
        });

		Thread loadDataThread = new Thread(dispatcherRecord);
		//on lance le thread de remplissage de la liste des valeurs
        loadDataThread.start();
        try {
        	//On attend que le chargement des données se termine
			loadDataThread.join();
		}catch(InterruptedException e) {
        	e.printStackTrace();
		}

		//Log.e("taille record",recordDataBrut.size()+"");
		return recordDataBrut;
	}


	/**
	 * Permet de charger le contenu de l'enveloppe sonore de l'enregistrement
	 * @param cheminFichier chemin du fichier correspondant à l'enregitrement
	 * @return la liste de valeurs de l'enveloppe sonore
	 */
	public static List<Float> loadPCMEnergyEnveloppe(String cheminFichier) {

		List<Float> recordDataEnveloppe = new ArrayList<>();

		EnvelopeFollower envelope = new EnvelopeFollower(44100);

		AudioDispatcher dispatcherRecord = AudioDispatcherFactory.fromPipe(cheminFichier,44100, 1024, 0);
        dispatcherRecord.addAudioProcessor(envelope);

        dispatcherRecord.addAudioProcessor(new AudioProcessor() {

            @Override
            public boolean process(AudioEvent audioEvent) {
                //pour chaque buffer de 1024 valeurs, on récupère toutes les valeurs de l'enveloppe
				//et on les ajoute
                float[] buffer = audioEvent.getFloatBuffer();
                for(float value : buffer) {
                    recordDataEnveloppe.add(value);
                }
                return true;
            }

            @Override
            public void processingFinished() {}
        });

		Thread envelopeThread = new Thread(dispatcherRecord);
		//on lance le thread de remplissage de la liste des valeurs
		envelopeThread.start();
		try {
			//On attend que le chargement des données se termine
			envelopeThread.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
		//Log.e("taille envelop",recordDataEnveloppe.size()+"");
		return recordDataEnveloppe;
    }

	/**
	 *
	 * @param envelopePCMData
	 * @return
	 */
	public static List<Syllabe> automaticSegmentation (List<Float> envelopePCMData, List<Float> rawPCMData) {
		List<Chunk> listChunk = splitInChunk(envelopePCMData,rawPCMData);
		List<Syllabe> listSyllabe = new ArrayList<>();

		for(Chunk c : listChunk) {
			List<Syllabe> list = c.getCandidateSyllabes();
			for(Syllabe s : list) {
				listSyllabe.add(s);
			}
		}

		joinSyllabe(listSyllabe);
		//Log.e("syl",listSyllabe.toString());
		deleteSyllabe(listSyllabe);
		//Log.e("syl",listSyllabe.toString());
		loadDataSyllabe(listSyllabe, rawPCMData, envelopePCMData);

		return listSyllabe;
    }

	/**
	 *
	 * sampleRate = 44100
	 * time = 0.5
	 * time = sample / sampleRate
	 *
	 * @param envelopePCMData
	 * @return
	 */
	private static List<Chunk> splitInChunk(List<Float> envelopePCMData, List<Float> rawPCMData) {
		double sampleRate = 44100.0;
		double seconds = 0.5;
		int count = 0;

		List<Float> valuesEnvelopeTemp = new ArrayList<>();
		List<Float> valuesRawTemp = new ArrayList<>();

		List<Chunk> listChunk = new ArrayList<>();

		int numChunk = 0;

		for (int i = 0; i < envelopePCMData.size(); i++) {
			if(count < seconds * sampleRate) {
				valuesEnvelopeTemp.add(envelopePCMData.get(i));
				valuesRawTemp.add(rawPCMData.get(i));
				count++;
			}
			else if(count == (seconds * sampleRate)) {
				Chunk chunk = new Chunk(valuesEnvelopeTemp, valuesRawTemp);
				chunk.setMaxEnergyLevel();
				chunk.setMoyEnvelopeEnergy();
				chunk.setDifference();
				chunk.setPercentageChange();
				chunk.setTypeChunk();

				if(chunk.getType().equals("Noise")) {
					chunk.setNum(numChunk);
					chunk.setNoiseThreshold();
					chunk.detectSyllabes();
					listChunk.add(chunk);
				}
				//On incremente le nb de chunk
				numChunk++;
				//On réinitialise le compteur à 0
				count = 0;
				//On réinitialise la liste de valeurs
				valuesEnvelopeTemp = new ArrayList<>();
				valuesRawTemp = new ArrayList<>();
			}
		}

		return listChunk;
	}

	/**
	 *
	 * @param candidateSyllabes
	 */
	private static void joinSyllabe(List<Syllabe> candidateSyllabes) {
		if(candidateSyllabes.size() > 1) {
			double sampleRate = 44100;
			double minTime = 0.15;

			double minCalcTime = sampleRate * minTime;

			List<Integer> indiceSyllabeToDelete = new ArrayList<>();

			for(int i = 0; i <candidateSyllabes.size(); i++) {
				int onSetSyllabe = candidateSyllabes.get(i).getOnSetRecord();
				int offSetSyllabe = candidateSyllabes.get(i).getOffSetRecord();
				//la derniere syllabe ne peut pas être jointe donc on prend l'avant derniere
				if(i != candidateSyllabes.size()-1) {
					int onSetNextSyllabe = candidateSyllabes.get(i + 1).getOnSetRecord();

					if (onSetNextSyllabe - offSetSyllabe < minCalcTime) {
						candidateSyllabes.get(i + 1).setOnSetRecord(onSetSyllabe);
						indiceSyllabeToDelete.add(i);
					}
				}
			}
			if(indiceSyllabeToDelete.size()> 0){
				deleteIndice(indiceSyllabeToDelete, candidateSyllabes);
			}
		}

	}

	/**
	 *
	 * @param indices
	 * @param candidateSyllabes
	 */
	private static void deleteIndice(List<Integer> indices, List<Syllabe> candidateSyllabes) {
		Iterator<Syllabe> iterator = candidateSyllabes.iterator();

		int i = 0;
		int j = 0;
		
		while (iterator.hasNext() && j<indices.size()) {
			iterator.next();

			if(i == indices.get(j)) {
				iterator.remove();
				j++;
			}
			i++;
		}
	}

	/**
	 *
	 * @param candidateSyllabes
	 */
	private static void deleteSyllabe(List<Syllabe> candidateSyllabes) {
		double sampleRate = 44100;
		double minTime = 0.15;

		double minCalcTime = sampleRate * minTime;

		//la liste des indices des syllabes à supprimer
		List<Integer> indiceSyllabeToDelete = new ArrayList<>();

		for(int i = 0; i <candidateSyllabes.size(); i++) {

			int onSetSyllabe = candidateSyllabes.get(i).getOnSetRecord();
			int offSetSyllabe = candidateSyllabes.get(i).getOffSetRecord();

			if( offSetSyllabe - onSetSyllabe < minCalcTime) {
				indiceSyllabeToDelete.add(i);
			}
		}

		//si le tableau contient au moins 1 val, on peut appliquer la suppression de syllabe
		if(indiceSyllabeToDelete.size()> 0){
			deleteIndice(indiceSyllabeToDelete, candidateSyllabes);
		}
	}

	/**
	 *
	 * @param listSyllabe
	 * @param rawPCMData
	 * @param envelopePCMData
	 */
	 private static void loadDataSyllabe(List<Syllabe> listSyllabe, List<Float> rawPCMData, List<Float> envelopePCMData) {
		for(Syllabe s : listSyllabe) {
			s.setDataRawSyllabe(rawPCMData);
			s.setDataEnvelopeSyllabe(envelopePCMData);
		}
	 }
	 
	 	/**
	 *
	 * @param listSyllabes
	 * @return
	 */
	public static void extractMFCC(List<Syllabe> listSyllabes) {

		for(Syllabe s : listSyllabes) {

			List<float[]> listMFCCS = new ArrayList<>();

			AudioDispatcher audioDispatcher = AudioDispatcherFactoryPlus.fromFloatArray(s.getFloatBufferRaw(),44100,256,128);

			MFCC mfcc = new MFCC(256, 44100f, 12, 20, 133.3334f, 22050f);
			audioDispatcher.addAudioProcessor(mfcc);

			audioDispatcher.addAudioProcessor(new AudioProcessor() {

				@Override
				public void processingFinished() {
				}

				@Override
				public boolean process(AudioEvent audioEvent) {
					listMFCCS.add(mfcc.getMFCC());
					return true;
				}
			});

			Thread extractMFCCThread = new Thread(audioDispatcher);
			extractMFCCThread.start();
			try {
				//On attend que le chargement des données se termine
				extractMFCCThread.join();
				s.setListMFCCs(listMFCCS);
			}catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
		//Frame based feature
	/*public static void spectralCentroid{}
	
	private static void spectralFlux{}
	
	private static void spectralFlatness{}
	
	private static void spectralRollOffPoint{}*/
	
	/*
		Le taux de passages à zéro par trame se produisant dans un signal.
		Un passage par zéro se produit lorsque des trames adjacentes ont 
		des signes différents, un négatif et un positif.
	*/
	/*private static void zeroCrossingRate() {
		
		new AndroidFFMPEGLocator(context);
		
        AudioDispatcher dispatcher = AudioDispatcherFactoryPlus.fromPipe(cheminFichier,44100, 256, 128);
		
		ZeroCrossingRateProcessor zeroCrossingRate = new ZeroCrossingRateProcessor();
		
		dispatcher.addAudioProcessor(zeroCrossingRate);
		
		dispatcher.addAudioProcessor(new AudioProcessor() {
			@Override
				public void processingFinished() {
				}

				@Override
				public boolean process(AudioEvent audioEvent) {
					mfccList.add(mfcc.getMFCC());
					return true;
				}
			
			
		}
		
		
	}
	
	//la moyenne du niveau d'énergie du signal calculée à partir de son enveloppe sonore
	private static float signalEnergy(List<Float> recordEnveloppe) {
		int nbLevelEnergy = recordEnveloppe.size();
		float somme = 0f;
		
		for(int i = 0; i< recordEnveloppe.size(); i++) {
			somme += recordBuffer.get(i);
		}
		
		return somme / (float)nbLevelEnergy;
	}
	
	
	//Syllabe based features
	private static void bandwidth() {
	}
	
	//durée d'une syllabe en secondes
	private static double syllabeDuration(Syllabe s) {
		return (s.getOffSetChunk() - s.getOnSetChunk()) / 44100.0;
	}
	
	private void frequencyRange() {}*/
}
