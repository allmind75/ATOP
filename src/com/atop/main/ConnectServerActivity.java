package com.atop.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.atop.chord.ChordFragment;
import com.atop.keyboard.KeyboardMouseActivity;
import com.atop.network.AES256Cipher;
import com.atop.network.NetworkLogin;
import com.atop.network.NetworkTCP;
import com.atop.wol.WolActivity;
import com.example.ssm_test.R;

public class ConnectServerActivity extends Activity implements OnClickListener {
	private final String TAG = "Class::ConnectServerActivity";

	private static final String key = "qkrgustkdwlswnduswkdghksgh123456";
	private static NetworkTCP tcp;
	private static NetworkLogin tcp_login;
	EditText edit_ip;
	EditText edit_password;
	private MyApplication myApp; // 얘는 ip저장하려고 있는친구
	ChordFragment mChord;
	Button btn_Login_Start;

	private SharedPreferences serveripPref;
	private boolean tcp_login_start = false;
	private Menu mainMenu;
	private int checkFlag = 0;
	private Button btnscreenNum, btnscreenNum2, btnscreenNum3, btnscreenNum4,
			btnscreenNum5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect_server);

		myApp = (MyApplication) getApplication();
		edit_ip = (EditText) findViewById(R.id.edittext_ip); // 서버 아이피 가져오기
		edit_ip.setSelection(edit_ip.length());
		edit_password = (EditText) findViewById(R.id.edittext_password);
		edit_password.setSelection(edit_password.length());
		mChord = new ChordFragment(null);
		myApp.setChord(mChord);

		serveripPref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
		String saveIp = serveripPref.getString("IP", null);

		if (saveIp != null) {
			edit_ip.setText(saveIp);

		}

		btn_Login_Start = (Button) findViewById(R.id.btn_server_login);
		btn_Login_Start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checkFlag == 0) {
					Toast.makeText(ConnectServerActivity.this,
							"화면 모드를 선택해 주세요", Toast.LENGTH_SHORT).show();

				} else {

					SharedPreferences.Editor edit = serveripPref.edit();
					edit.putString("IP", getEditView_Ip());
					edit.commit();

					String encode = edit_password.getText().toString();
					Log.e(TAG, "password : " + encode);
					try {
						encode = AES256Cipher.AES_Encode(encode, key);
						Log.e(TAG, "encode : " + encode);
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (!tcp_login_start) {
						tcp_login = new NetworkLogin(getEditView_Ip(),
								mhandler, encode);
						tcp_login.start();
						tcp_login_start = true;
					} else {
						tcp_login.sendMessage(encode);
					}
				}
			}

		});

		btnscreenNum = (Button) findViewById(R.id.btn_layout1);
		btnscreenNum.setOnClickListener(this);
		btnscreenNum.setTag(1);
		btnscreenNum2 = (Button) findViewById(R.id.btn_layout2);
		btnscreenNum2.setOnClickListener(this);
		btnscreenNum2.setTag(2);
		btnscreenNum3 = (Button) findViewById(R.id.btn_layout3);
		btnscreenNum3.setOnClickListener(this);
		btnscreenNum3.setTag(3);
		btnscreenNum4 = (Button) findViewById(R.id.btn_layout4);
		btnscreenNum4.setOnClickListener(this);
		btnscreenNum4.setTag(4);
		btnscreenNum5 = (Button) findViewById(R.id.btn_layout5);
		btnscreenNum5.setOnClickListener(this);
		btnscreenNum5.setTag(5);
	}

	private void setDeviceNum(int num) {
		myApp.setDeviceNum(num);
		checkFlag = num;
	}

	private void setFreeDevice(int num) {
		switch (num) {
		case 1: {
			btnscreenNum.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.layout_1, 0, 0);
			break;
		}
		case 2: {
			btnscreenNum2.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.layout_2, 0, 0);
			break;
		}
		case 3: {
			btnscreenNum3.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.layout_3, 0, 0);
			break;
		}
		case 4: {
			btnscreenNum4.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.layout_4, 0, 0);
			break;
		}
		case 5: {
			btnscreenNum5.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.layout_5, 0, 0);
			break;
		}
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {

		int selectNum = Integer.parseInt(v.getTag().toString());

		if (selectNum != checkFlag && checkFlag != 0) {
			setFreeDevice(checkFlag);
		}

		switch (selectNum) {

		case 1: {

			btnscreenNum.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.check_layout_1, 0, 0);
			setDeviceNum(1);

			break;
		}
		case 2: {
			btnscreenNum2.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.check_layout_2, 0, 0);
			setDeviceNum(2);
			break;
		}
		case 3: {
			btnscreenNum3.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.check_layout_3, 0, 0);
			setDeviceNum(3);
			break;
		}
		case 4: {
			btnscreenNum4.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.check_layout_4, 0, 0);
			setDeviceNum(4);
			break;
		}
		case 5: {
			btnscreenNum5.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.check_layout_5, 0, 0);
			setDeviceNum(5);
			break;
		}
		default:
			break;
		}

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
			Intent intent_1 = new Intent(ConnectServerActivity.this,
					SettingActivity.class);
			startActivity(intent_1);
			return true;
		case R.id.action_item_wifiSetting:
			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			return true;
		case R.id.action_item_wol:
			Intent intent_2 = new Intent(ConnectServerActivity.this,
					WolActivity.class);
			startActivity(intent_2);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// ImageButton

	Handler mhandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) // 이상 없음.
			{

			case 1: { // 로그인 정보가 맞음

				tcp_login_start = false;
				setNetwork(); // 바로시작
				break;
			}
			case 2: { // 로그인 정보가 틀림
				Toast.makeText(ConnectServerActivity.this,
						"비밀번호가 틀립니다.\n 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
				break;
			}
			case 3: {
				Toast.makeText(ConnectServerActivity.this,
						"서버와 연결이 실패했습니다.\n 다시 시도해 주세요", Toast.LENGTH_SHORT)
						.show();
				tcp_login_start = false;
				break;

			}
			}
		}

	};

	private void setNetwork() {

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		tcp = new NetworkTCP(myApp.getIP());

		tcp.start();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MyApplication myapp = (MyApplication) getApplication();
		myapp.setTCP(tcp);

		Intent intent = new Intent(ConnectServerActivity.this,
				KeyboardMouseActivity.class); // 키보드로 넘어감
		startActivity(intent);

	}

	private String getEditView_Ip() {

		myApp.setIP(edit_ip.getText().toString());

		return myApp.getIP();

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		mChord = new ChordFragment(null);
		myApp.setChord(mChord);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
