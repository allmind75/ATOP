package com.atop.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

import com.atop.chord.ChordFragment;
import com.example.ssm_test.R;

public class SurfaceViewActivity extends Activity {
	SurfaceViewMain mSurFaceView;
	ChordFragment mChord;
	LayoutInflater controlInflater = null;
	Bitmap image;
	private FragmentTransaction mFragmentTransaction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		mSurFaceView = new SurfaceViewMain(SurfaceViewActivity.this); // 서피스 뷰를
																		// 메인뷰로
		setContentView(mSurFaceView);

		// //나중에는 얘네 다 필요없음요

		controlInflater = LayoutInflater.from(getBaseContext()); // 서피스
																	// 뷰위에새로운뷰를띄운당.

		View viewControl = controlInflater.inflate(R.layout.float_surfaceview,
				null);

		LayoutParams layoutParamsControl = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		this.addContentView(viewControl, layoutParamsControl);

		// //나중에는 얘네 다 필요없음요

		mFragmentTransaction = getFragmentManager().beginTransaction();

		mChord = new ChordFragment(mSurFaceView);

		mFragmentTransaction.replace(R.id.fragment_container, mChord);
		mFragmentTransaction.addToBackStack(null);
		mFragmentTransaction.commit();

	}

	@Override
	protected void onDestroy() {

		mSurFaceView.stopSurfaceView();
		mChord.stopChord(1);
		mChord.CloseChord(false);

		super.onDestroy();
		finish();
	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();
		exit_dialog();

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit_dialog();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		mChord.stopChord(1);
		mSurFaceView.stopSurfaceView();

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onStop();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		// mSurFaceView.startSurfaceView();
		mChord.startChord(1);

	}

	public void exit_dialog() {
		AlertDialog.Builder longDialog = new AlertDialog.Builder(this);
		longDialog.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mSurFaceView.stopSurfaceView();
						mChord.CloseChord(false);
						finish();
					}
				}).setNegativeButton("No",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

					}
				});
		AlertDialog alert = longDialog.create();
		alert.setTitle("종료하시겠습니까?");
		alert.show();

	}
}
