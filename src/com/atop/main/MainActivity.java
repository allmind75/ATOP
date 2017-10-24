package com.atop.main;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.atop.callstate.CallStateReceiver;
import com.atop.wol.WolActivity;
import com.example.ssm_test.R;

public class MainActivity extends Activity {
	private final String TAG = "Class::MainActivity";
	private static String dirPath; // 폴더가 생성될 장소
	private String enviornment;
	private SharedPreferences mainPref;
	private MyApplication myApp;
	private boolean isCall = true;
	private String settingState;
	private CallStateReceiver callStateReceiver;
	private Menu mainMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		enviornment = Environment.getExternalStorageState();

		if (enviornment.equals(Environment.MEDIA_MOUNTED)) {

			dirPath = "/sdcard/ATOP";
			File file = new File(dirPath);
			if (!file.exists()) { // 원하는 경로에 폴더가 있는지 확인
				Log.i("폴더", "생성");
				file.mkdirs();
			}
		} else
			Toast.makeText(MainActivity.this, "SD Card 인식 실패",
					Toast.LENGTH_SHORT).show();
		myApp = (MyApplication) getApplication();

		mainPref = getSharedPreferences("mainpref", 0);

		settingState = mainPref.getString("setting", null);

		if (settingState != null) {
			isCall = mainPref.getBoolean("call", true);
			myApp.setCall(isCall);
		} else {
			SharedPreferences.Editor editor = mainPref.edit();
			editor.putBoolean("call", isCall);
			editor.commit();
			myApp.setCall(isCall);// 맨처음은 트루로 시작함
		}
		callStateReceiver = new CallStateReceiver();

		ImageButton btn_makeroom = (ImageButton) findViewById(R.id.imageButton_makeroom);
		btn_makeroom.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						ConnectServerActivity.class);
				startActivity(intent);

			}
		});
		ImageButton btn_enterroom = (ImageButton) findViewById(R.id.imageButton_enterroom);
		btn_enterroom.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						SurfaceViewActivity.class);
				startActivity(intent);

			}
		});

		ImageButton btn_transfile = (ImageButton) findViewById(R.id.imageButton_transandroid); // 파일전송
		btn_transfile.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						SendFilesActivity.class);
				startActivity(intent);
				finish();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_bar, menu);
		// getMenuInflater().inflate(R.menu.action_bar, menu);
		mainMenu = menu;

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_item_option:
			Intent intent_1 = new Intent(MainActivity.this,
					SettingActivity.class);
			startActivity(intent_1);
			return true;
		case R.id.action_item_wifiSetting:
			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			return true;
		case R.id.action_item_wol:
			Intent intent_2 = new Intent(MainActivity.this, WolActivity.class);
			startActivity(intent_2);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// //if (event.getAction() == KeyEvent.ACTION_DOWN) {
	// switch (keyCode) {
	// case KeyEvent.KEYCODE_MENU:
	// Log.e(TAG, "onKeyDown");
	// mainMenu.performIdentifierAction(R.id.action_item_setting, 0);
	// openOptionsMenu();
	// return true;
	// default:
	// return super.onKeyDown(keyCode, event);
	// }
	// //}
	//
	// //return false;
	// }

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.e(TAG, "Main Activity 종료 되었습니다.");
	}

	@Override
	public void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		settingState = mainPref.getString("setting", null);

		if (settingState != null) { // 나중에 여기서 전화랑 알람 할지 안할지 설정해줘야해 세팅에 안들어가도
									// 켯을때 잇어야지
			isCall = mainPref.getBoolean("call", true);
			myApp.setCall(isCall);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
