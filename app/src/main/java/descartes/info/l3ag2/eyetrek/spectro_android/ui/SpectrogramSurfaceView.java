package descartes.info.l3ag2.eyetrek.spectro_android.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

//import com.google.android.gms.location.LocationClient;

import java.util.Date;

import descartes.info.l3ag2.eyetrek.fragment.FragmentAudio;
import descartes.info.l3ag2.eyetrek.spectro_android.DynamicAudioConfig;
import descartes.info.l3ag2.eyetrek.spectro_android.LocationClient;
import descartes.info.l3ag2.eyetrek.spectro_android.storage.AudioBitmapConverter;

import static java.lang.Thread.sleep;


/**
 * SurfaceView that holds the spectrogram.
 * @author Ben
 *
 */
public class SpectrogramSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	private FragmentAudio spectroFragment;
	private DynamicAudioConfig dac;
	private SpectrogramDrawer sd;
	private InteractionHandler interactionHandler;
	private Context context;
	protected boolean selecting = false; //true if user has entered the selection state
	private String filename;
	private LocationClient lc;
	private AlertDialog loadingAlert; //used to force user to wait for capture

	private int INPUT_SIZE = 299;

	public SpectrogramSurfaceView(Context context) {
		super(context);
		init(context);
	}

	public SpectrogramSurfaceView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
		init(context);
	}

	public SpectrogramSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
		getHolder().addCallback(this);
		interactionHandler = new InteractionHandler(this);
		dac = new DynamicAudioConfig(context);

		// Create the "capture in progress" alert dialog to be used later
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Capture in progress...");
		// Add a spinner to the dialog
		final ProgressBar pb = new ProgressBar(context);
		builder.setView(pb);
		loadingAlert = builder.create();
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		if(spectroFragment == null) Log.e("surfaceCreated()", "this.spectroFragment est null !");


		// initialise the spectrogram drawer
		sd = new SpectrogramDrawer(dac, this.getWidth(), this.getHeight(), this.getHolder());
		// disable the resume button (as the user has not paused the spectrogram yet):
		spectroFragment.disableResumeButton();
		// set the initial values for the axis text views:
		spectroFragment.setLeftTimeText(-sd.getScreenFillTime());
		spectroFragment.setRightTimeText(sd.getTimeFromStopAtPixel(getWidth()));
		spectroFragment.setTopFreqText(sd.getMaxFrequency() / 1000);



	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// nothing to do
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// stop the spectrogram drawer and nullify it
		if (sd != null) {
			sd.stop();
		}
		sd = null;
	}

	public void stop() {
		if (sd != null) {
			sd.stop();
		}
		if (selecting) {
			cancelSelection();
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// pass the touch event to the interaction handler:
		interactionHandler.handleTouchEvent(ev);
		// update the axis text labels based on how much the user has scrolled:
		float leftTextTime = sd.getTimeAtPixel(0) > -sd.getScreenFillTime() ? -sd.getScreenFillTime() : sd.getTimeAtPixel(0);
		spectroFragment.setLeftTimeText(leftTextTime); //TODO update these live
		spectroFragment.setRightTimeText(sd.getTimeAtPixel(getWidth()));
		
		return true;
	}

	public void pauseScrolling() {
		if (sd != null) {
			sd.pauseScrolling();
			// show the resume button when scrolling is paused
			spectroFragment.enableResumeButton();
		} else {
			Log.e("pauseScrolling()", "SpectrogramDrawer est null dans SpectrogramSurfaceView !");
		}
	}

	public void resumeScrolling() {
		if (selecting)
			cancelSelection();
		sd = new SpectrogramDrawer(dac, this.getWidth(), this.getHeight(), this.getHolder());
		// disable the resume button once scrolling is resumed
		spectroFragment.disableResumeButton();
		spectroFragment.texte_tuto.setVisibility(View.GONE);
	}


	/**
	 * Presents to the user an alert dialog in which they csn enter a filename for the capture they have 
	 * just made.
	 */
	public void confirmSelection() {
		/*
		//create and display an AlertDialog requesting a filename for the new capture
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("What did you hear?");
		final EditText inputText = new EditText(context);
		inputText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		builder.setView(inputText);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// execute the capture operations:
				filename = inputText.getText().toString().trim();
				new CaptureTask(context).execute();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
		*/

		//filename = Environment.getExternalStorageDirectory() + "/EyeTrek/Sounds/RecordBird_" + (new Date().getTime()) + ".wav";
		filename = "RecordBird_" + (new Date().getTime());

		float[] dimens = interactionHandler.getSelectRectDimensions();
		Bitmap bitmapToStore = sd.getBitmapToStore(dimens[0],dimens[1],dimens[2],dimens[3]);
	}

	public Bitmap getBitmap(){
		//filename = "RecordBird_" + (new Date().getTime());
		/*
		filename = Environment.getExternalStorageDirectory() + "/EyeTrek/Sounds/RecordBird_" + (new Date().getTime()) + ".wav";





		float[] dimens = interactionHandler.getSelectRectDimensions();
		Bitmap bitmapToStore = sd.getBitmapToStore(dimens[0],dimens[1],dimens[2],dimens[3]);
		short[] audioToStore = sd.getAudioToStore(dimens[0],dimens[1],dimens[2],dimens[3]);
		AudioBitmapConverter abc;

		abc = new AudioBitmapConverter(filename, dac, bitmapToStore, audioToStore, null);
		abc.writeThisCbaToFile(filename, DynamicAudioConfig.STORE_DIR_NAME);
		abc.storeJPEGandWAV();


		// Options d'encodage du bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		// On met l'encodage à ARGB_8888 car c'est le seul que TensorFlow supporte
		bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
		// On mlet les dimentions qu'ils faut
		bmOptions.outWidth = INPUT_SIZE;
		bmOptions.outHeight = INPUT_SIZE;

		return BitmapFactory.decodeFile(filename, bmOptions);
		*/





		float[] dimens = interactionHandler.getSelectRectDimensions();
		return sd.getBitmapToStore(dimens[0],dimens[1],dimens[2],dimens[3]);
	}

	/**
	 * If a selection is cancelled, hide the selection rectangle and its associated buttons.
	 */
	public void cancelSelection() {
		sd.hideSelectRect();
		spectroFragment.disableCaptureButtonContainer();
		selecting = false;
	}

	/**
	 * Demands that the spectrogram drawer slide the spectrogram by the specified offset.
	 * @param offset
	 */
	protected void slideTo(int offset) {
		sd.quickSlide(offset);
	}

	/**
	 * Draw the selection rectangle using the specified bounds and move its capture button
	 * container accordingly.
	 * @param selectRectL
	 * @param selectRectT
	 * @param selectRectR
	 * @param selectRectB
	 */
	protected void updateSelectRect(float selectRectL, float selectRectT, float selectRectR, float selectRectB) {
		sd.drawSelectRect(selectRectL,selectRectT,selectRectR,selectRectB);
		//spectroFragment.moveCaptureButtonContainer(selectRectL, selectRectT, selectRectR, selectRectB);
	}

	protected void enableCaptureButtonContainer() {
		spectroFragment.enableCaptureButtonContainer();
	}

	public void setLocationClient(LocationClient lc) {
		this.lc = lc;
	}

	public void setSpectroFragment(FragmentAudio spectroFragment) {
		this.spectroFragment = spectroFragment;
		if(this.spectroFragment == null) Log.e("setSpectroFragment()", "this.spectroFragment est null !");
	}



	/**
	 * AsyncTask that stores the user's bitmap and audio capture to disk.
	 * @author Ben
	 *
	 */
	private class CaptureTask extends AsyncTask<Void, Void, Void> {
		private Context context;
		
		public CaptureTask(Context context) {
			this.context = context;
		}

		/**
		 * Show the "capture in progress" dialog
		 */
		@Override
		protected void onPreExecute() {
			loadingAlert.show();
		}
		
		/**
		 * Requests a bitmap and a chunk of audio based on the user's selection and saves it to disk.
		 */
		@Override
		protected Void doInBackground(Void... arg0) {
			float[] dimens = interactionHandler.getSelectRectDimensions();
			Bitmap bitmapToStore = sd.getBitmapToStore(dimens[0],dimens[1],dimens[2],dimens[3]);
			short[] audioToStore = sd.getAudioToStore(dimens[0],dimens[1],dimens[2],dimens[3]);
			AudioBitmapConverter abc;
			/*
			if (lc != null)
				abc = new AudioBitmapConverter(filename, dac, bitmapToStore,audioToStore,lc.getLastLocation());
			else
				abc = new AudioBitmapConverter(filename, dac, bitmapToStore, audioToStore, null);
			*/
			abc = new AudioBitmapConverter(filename, dac, bitmapToStore, audioToStore, null);
			abc.writeThisCbaToFile(filename, DynamicAudioConfig.STORE_DIR_NAME);
			abc.storeJPEGandWAV();
			return null;
		}
		
		/**
		 * Dismiss the "capture in progress" dialog, show a "capture completed" toast and update the library's files list.
		 */
		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(context, "Capture completed!", Toast.LENGTH_SHORT).show();
			loadingAlert.dismiss();
			//((SpectroActivity)spectroFragment.getActivity()).updateLibraryFiles();
		}
	}


}
