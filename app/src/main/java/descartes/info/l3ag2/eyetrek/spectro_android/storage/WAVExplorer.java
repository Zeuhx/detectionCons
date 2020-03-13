package descartes.info.l3ag2.eyetrek.spectro_android.storage;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *  A class for extracting information from a .wav file based on
 *  information in its header. It also provides a means for extracting
 *  the samples as an array.
 * 
 */

public class WAVExplorer {
	private RandomAccessFile wavFile; //file to open
	private long fileLength; //length of whole file in bytes
	private int dataLength; //number of bytes in data section
	private short numChannels; //number of channels in the file
	private int sampleRate; //sampling frequency Fs used to encode the file
	private int bitsPerSample; //number of bits used to hold each sample
	private int numSamples; //number of samples in the file
	private double[] firstChannelArray; //array of samples from first (left) channel
	private double[] secondChannelArray; //array of samples from second (right) channel, if it exists
	private boolean isMono; // true if there is only one channel, i.e. signal is mono, not stereo
	private int duration; //duration of WAV file in seconds




	public WAVExplorer(String filepath) {
		String loc = null;
		try {
			loc = Environment.getExternalStorageDirectory().getAbsolutePath();
			File f = new File(filepath);
			wavFile = new RandomAccessFile(f, "r");
						
			fileLength = wavFile.length();
			
			wavFile.seek(22);
			numChannels = wavFile.readShort();
			numChannels = Short.reverseBytes(numChannels); //must reverse bits since little-endian
			
			sampleRate = wavFile.readInt(); //at offset 24 so no seeking necessary
			sampleRate = Integer.reverseBytes(sampleRate); //little-endian
			wavFile.seek(34);
			bitsPerSample = wavFile.readByte();
			if (bitsPerSample > 34) {
				Log.e("WAVExplorer","Sample size of "+bitsPerSample+" bits not supported. Please use a file with a sample size of 32 bits or lower.");
				throw new IOException();
			}
			
			wavFile.seek(40);
			dataLength = wavFile.readInt();
			dataLength = Integer.reverseBytes(dataLength); //little-endian
			
			numSamples = (8*dataLength)/(numChannels*bitsPerSample);
			firstChannelArray = new double[numSamples];

			if (numChannels == 1) { //mono, only one channel of samples
				isMono = true;

				for (int i = 0; i < numSamples; i++) {
					//wavFile already at offset 44, no need to seek
					if (bitsPerSample == 8) firstChannelArray[i] = wavFile.readByte();
					if (bitsPerSample == 16) firstChannelArray[i] = Short.reverseBytes(wavFile.readShort());
					if (bitsPerSample == 32) firstChannelArray[i] = Integer.reverseBytes(wavFile.readInt());
				}
				
			}
			else {
				if (numChannels == 2) { //stereo, two channels of samples
					isMono = false;
					secondChannelArray = new double [numSamples];
					for (int i = 0; i < numSamples; i++) {
						//wavFile already at offset 44, no need to seek
						if (bitsPerSample == 8) {
							firstChannelArray[i] = wavFile.readByte();
							secondChannelArray[i] = wavFile.readByte();
						}
						if (bitsPerSample == 16) {
							firstChannelArray[i] = Short.reverseBytes(wavFile.readShort());
							secondChannelArray[i] = Short.reverseBytes(wavFile.readShort());
						}
						if (bitsPerSample == 32) {
							firstChannelArray[i] = Integer.reverseBytes(wavFile.readInt());
							secondChannelArray[i] = Integer.reverseBytes(wavFile.readInt());
						}
					}
				}
				else throw new IOException();
			}
			duration = numSamples * sampleRate;
			wavFile.close();
		} catch (FileNotFoundException e) {
			Log.e("WAVExplorer","Couldn't find file "+loc+"/"+filepath);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public double[] getFirstChannelData() { //get a copy of the first channel data
		int length = firstChannelArray.length;
		double toReturn[] = new double[length];
		for (int i = 0; i < length; i++) {
			toReturn[i] = firstChannelArray[i];
		}
		return toReturn;
	}
	
	public double[] getSecondChannelData() { //get a copy of the second channel data
		if (!isMono) {
			int length = secondChannelArray.length;
			double toReturn[] = new double[length];
			for (int i = 0; i < length; i++) {
				toReturn[i] = secondChannelArray[i];
			}
			return toReturn;
		}
		else {
			Log.e("WAVExplorer","File is not stereo; no second channel available.");
			return null;
		}
	}
	
	public long getFileLength() {
		return fileLength;
	}	
	
	public int getDataLength() {
		return dataLength;
	}
	
	public int getSampleRate() {
		return sampleRate;
	}
	
	public int getNumSamples() {
		return numSamples;
	}
	
	public int getNumChannels() {
		return numChannels;
	}
	
	public int getBitsPerSample() {
		return bitsPerSample;
	}
	
	public boolean isMono() {
		return isMono;
	}
	
	public int getDuration() {
		return duration;
	}
}
