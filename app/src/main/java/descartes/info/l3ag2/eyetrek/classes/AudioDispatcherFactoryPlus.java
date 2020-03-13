/*
*      _______                       _____   _____ _____
*     |__   __|                     |  __ \ / ____|  __ \
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|
*
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
*
*/


package descartes.info.l3ag2.eyetrek.classes;

/**
 * Created by Dorian on 03/04/2018.
 */

import java.io.ByteArrayInputStream;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.UniversalAudioInputStream;

/**
 * The Factory creates {@link AudioDispatcher} objects from various sources: the
 * configured default microphone, PCM wav files or PCM samples piped from a
 * sub-process. It depends on the javax.sound.* packages and does not work on Android.
 *
 * @author Joren Six
 * @see AudioDispatcher
 */
public class AudioDispatcherFactoryPlus {

    /**
     * Create a stream from an array of bytes and use that to create a new
     * AudioDispatcher.
     *
     * @param byteArray
     *            An array of bytes, containing audio information.
     * @param audioFormat
     *            The format of the audio represented using the bytes.
     * @param audioBufferSize
     *            The size of the buffer defines how much samples are processed
     *            in one step. Common values are 1024,2048.
     * @param bufferOverlap
     *            How much consecutive buffers overlap (in samples). Half of the
     *            AudioBufferSize is common.
     * @return A new AudioDispatcher.
     */
    public static AudioDispatcher fromByteArray(final byte[] byteArray, final TarsosDSPAudioFormat audioFormat,
                                                final int audioBufferSize, final int bufferOverlap) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
        TarsosDSPAudioInputStream audioStream = new UniversalAudioInputStream(bais, audioFormat);
        return new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);
    }



    /**
     * Create a stream from an array of floats and use that to create a new
     * AudioDispatcher.
     *
     * @param floatArray
     *            An array of floats, containing audio information.
     * @param sampleRate
     * 			  The sample rate of the audio information contained in the buffer.
     * @param audioBufferSize
     *            The size of the buffer defines how much samples are processed
     *            in one step. Common values are 1024,2048.
     * @param bufferOverlap
     *            How much consecutive buffers overlap (in samples). Half of the
     *            AudioBufferSize is common.
     * @return A new AudioDispatcher.
     *             If the audio format is not supported.
     */
    public static AudioDispatcher fromFloatArray(final float[] floatArray, final int sampleRate, final int audioBufferSize, final int bufferOverlap){
        final TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(sampleRate, 16, 1, true, false);
        final TarsosDSPAudioFloatConverter converter = TarsosDSPAudioFloatConverter.getConverter(format);
        final byte[] byteArray = new byte[floatArray.length * format.getFrameSize()];
        converter.toByteArray(floatArray, byteArray);
        return AudioDispatcherFactoryPlus.fromByteArray(byteArray, format, audioBufferSize, bufferOverlap);
    }
}
