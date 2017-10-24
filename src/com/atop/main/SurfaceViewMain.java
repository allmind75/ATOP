package com.atop.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.example.ssm_test.R;

public class SurfaceViewMain extends SurfaceView implements Callback {
	private final String TAG = "Class::SurfaceViewMain";
	private SurfaceHolder mSurfaceHolder;
	private DrawThread thread;
	float screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
	float screenHeight =  getContext().getResources().getDisplayMetrics().heightPixels;
	float smallScreenHeight = (float) ((getContext().getResources().getDisplayMetrics().xdpi) * 4.1732283);
	Matrix matrix = new Matrix();

	public SurfaceViewMain(Context context) {
		super(context);
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		// thread = new DrawThread(mSurfaceHolder, this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		thread = new DrawThread(mSurfaceHolder, this);

		thread.setRunning(true);

		thread.start();

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d("*********", "surfaceChanged()");

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		thread.SetLoop(false);

		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				Log.e(TAG, "surfaceDestroyed" + e);
				e.getStackTrace();
			}
		}

	}

	public void stopSurfaceView() { // 종료시 그림 그리기 그만
		thread.isFinish(true);
		thread.setRunning(false);
	}

	public void setThreadimage(byte[] bitmap, int length, float height) {

		thread.setImage(bitmap, length, (int) screenWidth, (int) height);

	}

	public void setThreadimageBitmap(Bitmap bitmap, float height) {
		thread.setImageBitmap(bitmap, (int) screenWidth, (int) screenHeight);
	}

	public void startSurfaceView() {
		thread.isFinish(false);

	}
}

class DrawThread extends Thread {

	private final String TAG = "Class::SurfaceViewMain - Thread";

	SurfaceHolder mHolder;
	private SurfaceViewMain msurfaceview;
	private Boolean running;
	private Bitmap image;
	private Boolean finish;
	private byte[] imageByte;
	private int imageLength;

	// fps
	private long tick = 0;
	private int fps = 0;

	public DrawThread(SurfaceHolder Holder, SurfaceViewMain surfaceview) {

		msurfaceview = surfaceview;
		this.mHolder = Holder;
		running = true;
		finish = false;
	}

	public void SetLoop(boolean b) {

		running = b;
	}

	public void isFinish(boolean b) {

		finish = b;

	}

	public void setRunning(Boolean run) {
		this.running = run;
	}

	@Override
	public void run() {

		android.os.Process.setThreadPriority(NORM_PRIORITY);
		super.run();
		Canvas canvas;

		while (running) {
			canvas = null;
			try {
				canvas = mHolder.lockCanvas();
				synchronized (mHolder) {

					if (image != null && !finish) { // tcp 연결할때 씀

						canvas.drawBitmap(image, 0, 0, null);

						if (System.currentTimeMillis() - tick > 1000) {
							Log.d(TAG, "FPS : " + fps);
							fps = 0;
							tick = System.currentTimeMillis();
						} else
							fps++;

					} else
						continue;

				}
			} finally {
				if (canvas != null) {

					mHolder.unlockCanvasAndPost(canvas);

				}
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setImage(byte[] bitmap, int length, int Width, int Height) {

		imageByte = bitmap;
		imageLength = length;
		Bitmap Image;
		
		try {

			Image = BitmapFactory.decodeByteArray(imageByte, 0, imageLength);
			if (Image != null) {

				image = Bitmap.createScaledBitmap(Image, Width, Height, true);
				Image.recycle();
				Image = null;
			}

		} catch (OutOfMemoryError ex) {
			Log.e(TAG, "OUT of Memory !!!!!");
		}

	}

	public void setImageBitmap(Bitmap bitmap, int width, int height) {
		if (image != null) {
			image = Bitmap.createScaledBitmap(bitmap, width, height, true);
		}
	}

}
