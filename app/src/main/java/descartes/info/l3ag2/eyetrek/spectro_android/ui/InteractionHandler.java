package descartes.info.l3ag2.eyetrek.spectro_android.ui;

import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;

import descartes.info.l3ag2.eyetrek.spectro_android.UiConfig;

/**
 * Class responsible for interpreting the user's gestures and drawing malleable objects
 * to the screen accordingly.
 * @author Ben
 *
 */
public class InteractionHandler {

	private SpectrogramSurfaceView ssv;
	private Runnable onLongPress;
	private Handler handler;
	
	private float lastTouchX;
	private float lastTouchY;
	private int activePointerId = -1;
	private float centreX;
	private float centreY;
	private int action;
	private int pointerIndex;
	private float x;
	private float y;
	private float dx;
	private float dy;

	private float selectRectL = 0;
	private float selectRectR = 0;
	private float selectRectT = 0;
	private float selectRectB = 0;

	private int selectedCorner;

	protected InteractionHandler (SpectrogramSurfaceView specSurfaceView) {
		ssv = specSurfaceView;
		handler = new Handler();
		// define the Runnable to execute when the spectrogram is long-pressed:
		onLongPress = new Runnable() {
			public void run() {
				ssv.selecting = true;
				// calculate positions for corners based on where the user has touched the screen:
				
				// user's touch was at (centreX, centreY)
				selectRectL = (centreX - UiConfig.SELECT_RECT_WIDTH/2 < 0) ? 0 : centreX - UiConfig.SELECT_RECT_WIDTH/2;
				selectRectR = (centreX + UiConfig.SELECT_RECT_WIDTH/2 > ssv.getWidth()) ? ssv.getWidth() : centreX + UiConfig.SELECT_RECT_WIDTH/2;
				selectRectT = (centreY - UiConfig.SELECT_RECT_HEIGHT/2 < 0) ? 0 : centreY - UiConfig.SELECT_RECT_HEIGHT/2;
				selectRectB = (centreY + UiConfig.SELECT_RECT_HEIGHT/2 > ssv.getHeight()) ? ssv.getHeight() : centreY + UiConfig.SELECT_RECT_HEIGHT/2;
				// update selection rectangle with new dimensions:
				ssv.updateSelectRect(selectRectT, selectRectB, selectRectL, selectRectR);
				// show the confirm/cancel buttons:
				ssv.enableCaptureButtonContainer();
			}
		};
	}

	/**
	 * Determine the type of touch event that has occurred and process it accordingly.
	 * @param ev
	 */
	public void handleTouchEvent(MotionEvent ev) {
		
		if (activePointerId != -1 && ev.getPointerId(ev.getActionIndex()) != activePointerId) // ignore other fingers for now
			return;
		
		action = MotionEventCompat.getActionMasked(ev); 
		switch (action) {
		case MotionEvent.ACTION_DOWN:  //finger pressed on screen
			handleDown(ev);
			break;

		case MotionEvent.ACTION_MOVE:  //occurs when there is a spatial difference between ACTION_UP and ACTION_DOWN
			handleMove(ev);
			break;

		default: //catch MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP
			handlePointerUp(ev); // user has lifted their finger
		}
	}

	/**
	 * Handle a "finger down" event on the spectrogram by pausing it from scrolling or, if selecting,
	 * deciding which selection rectangle corner is to be moved.
	 * @param ev
	 */
	private void handleDown(MotionEvent ev) {		
		if (!ssv.selecting) {
			// if scrolling, pause
			ssv.pauseScrolling();
			//run the long-press runnable if not cancelled by move event (0.5 second timeout)
			handler.postDelayed(onLongPress, 500); 
		}
		
		// find the location of the user's touch:
		pointerIndex = MotionEventCompat.getActionIndex(ev);
		
		if (pointerIndex == -1)
			return;
		
		x = MotionEventCompat.getX(ev, pointerIndex); 
		centreX = MotionEventCompat.getX(ev, pointerIndex);
		centreY = MotionEventCompat.getY(ev, pointerIndex);
		
		// Remember where we started (for dragging)
		lastTouchX = x;
		lastTouchY = MotionEventCompat.getY(ev, pointerIndex);
		
		// Save the ID of this pointer [finger], in case of drag 
		activePointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
		if (ssv.selecting) {
			//decide which corner is being dragged based on proximity
			selectedCorner = 0;
			if (Math.abs(centreX-selectRectL) <= UiConfig.SELECT_RECT_CORNER_RADIUS && Math.abs(centreY-selectRectT) <= UiConfig.SELECT_RECT_CORNER_RADIUS) {
				//user touched top-left corner
				selectedCorner = 1;
			}
			if (Math.abs(centreX-selectRectR) <= UiConfig.SELECT_RECT_CORNER_RADIUS && Math.abs(centreY-selectRectT) <= UiConfig.SELECT_RECT_CORNER_RADIUS) {
				//user touched top-right corner
				selectedCorner = 2;
			}
			if (Math.abs(centreX-selectRectL) <= UiConfig.SELECT_RECT_CORNER_RADIUS && Math.abs(centreY-selectRectB) <= UiConfig.SELECT_RECT_CORNER_RADIUS) {
				//user touched bottom-left corner
				selectedCorner = 3;
			}
			if (Math.abs(centreX-selectRectR) <= UiConfig.SELECT_RECT_CORNER_RADIUS && Math.abs(centreY-selectRectB) <= UiConfig.SELECT_RECT_CORNER_RADIUS) {
				//user touched bottom-right corner
				selectedCorner = 4;
			}
		}
	}

	/**
	 * Handle a "finger moved" event by either scrolling the spectrogram or moving a selection
	 * rectangle corner.
	 * @param ev
	 */
	private void handleMove(MotionEvent ev) {
		// Find the index of the active pointer and fetch its position
		pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
		if (pointerIndex == -1)
			return;
		// Calculate the distance moved
		if (!ssv.selecting) {
			// if not selecting, then scroll the spectrogram
			x = MotionEventCompat.getX(ev, pointerIndex); //Note: never care about y axis
			dx = x - lastTouchX;
			
			if (dx > 5 || dx < -5) { //only if moved more than 5 pixels
				handler.removeCallbacks(onLongPress); //cancel long-press runnable
				ssv.slideTo((int) dx);
				// Remember this touch position for the next move event
			}
			lastTouchX = x;
			
		} else { 
			//if selecting mode entered, allow user to move corners to adjust select-area rectangle size
			x = MotionEventCompat.getX(ev, pointerIndex);
			y = MotionEventCompat.getY(ev, pointerIndex);
			dx = x - lastTouchX;
			dy = y - lastTouchY;
			moveSelectRectCorner(selectedCorner, dx, dy);				
			lastTouchX = x;
			lastTouchY = y;
		}
	}

	/**
	 * Cancel the long-press runnable because the user has lifted their finger before the timeout.
	 */
	private void cancelLongpress() {
		handler.removeCallbacks(onLongPress);
		activePointerId = MotionEvent.INVALID_POINTER_ID;
	}

	/**
	 * Handle the user lifting their finger from the screen by cancelling the long-press runnable
	 * and resetting the active pointer ID.
	 * 
	 * @param ev
	 */
	private void handlePointerUp(MotionEvent ev) {
		cancelLongpress();
		activePointerId = -1;
	}

	/**
	 * Recalculate selection rectangle dimensions based on which corner the user moved and 
	 * how far they moved it.
	 * 
	 * @param cornerIndex - the index of the corner moved by the user
	 * @param dx - how far the user moved the corner horizontally, in pixels
	 * @param dy - how far the user moved the corner vertically, in pixels
	 */
	public void moveSelectRectCorner(int cornerIndex, float dx, float dy) {
		int width = ssv.getWidth();
		int height = ssv.getHeight();
		switch(cornerIndex) {
		// if 0 then not near any corner
		case 1:
			//top-left corner moved
			selectRectL += dx;
			selectRectT += dy;
			break;
		case 2:
			//top-right corner moved
			selectRectR += dx;
			selectRectT += dy;
			break;
		case 3:
			//bottom-left corner moved
			selectRectL += dx;
			selectRectB += dy;
			break;
		case 4:
			//bottom-right corner moved
			selectRectR += dx;
			selectRectB += dy;
			break;
		}
		// make sure that the selection rectangle coordinates have not exceeded their limits:
		selectRectL = (selectRectL < 0) ? 0 : selectRectL;
		selectRectR = (selectRectR < 0) ? 0 : selectRectR;
		selectRectL = (selectRectL > width) ? width : selectRectL;
		selectRectR = (selectRectR > width) ? width : selectRectR;
		selectRectT = (selectRectT < 0) ? 0 : selectRectT;
		selectRectB = (selectRectB < 0) ? 0 : selectRectB;
		selectRectT = (selectRectT > height) ? height : selectRectT;
		selectRectB = (selectRectB > height) ? height : selectRectB;
		// redraw the selection rectangle:
		ssv.updateSelectRect(selectRectL, selectRectT, selectRectR, selectRectB);
	}

	/**
	 * Returns the dimensions of the selection rectangle.
	 * @return A float-array consisting of [selectRectL, selectRectT, selectRectR, selectRectB]
	 */
	protected float[] getSelectRectDimensions() {
		float[] dimens = {selectRectL, selectRectT, selectRectR, selectRectB};
		return dimens;
	}

}
