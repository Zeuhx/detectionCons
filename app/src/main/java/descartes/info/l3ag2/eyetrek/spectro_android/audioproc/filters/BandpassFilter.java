package descartes.info.l3ag2.eyetrek.spectro_android.audioproc.filters;

/**
 * Interface that represents bandpass filters which operate on data presented
 * as an array of {@code short}s.
 * @author Ben
 *
 */
public interface BandpassFilter {
	
	/**
	 * Apply the bandpass filter to the provided samples in-place.
	 * @param samples
	 */
	public void applyFilter(short[] samples);

}
