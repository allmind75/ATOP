package com.atop.keyboard;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.FloatMath;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.atop.callstate.CallStateReceiver;
import com.atop.chord.ChordFragment;
import com.atop.dialog.SettingDialog;
import com.atop.main.MyApplication;
import com.atop.network.NetworkTCP;
import com.atop.network.NetworkTCP_Image;
import com.atop.network.NetworkUDP_Image;
import com.example.ssm_test.R;

public class KeyboardMouseActivity extends Activity implements OnClickListener {

	private final String TAG = "Class::CombinKM";
	private boolean isonPause = false;

	private final String ProtocalFN = "특";
	private final String ProtocalSum = "조";
	private int udp_Mouse_Port = 9000;

	private Button btn_FN;
	private Button btn_copy, btn_paste, btn_shift, btn_ctrl, btn_drag;

	private ImageButton btn_Keyboard, btn_up, btn_down, btn_left, btn_right,
			btn_right_click, btn_left_click;

	private CustomKeyboard mCustomKeyboard;

	private View view_Touch; // 마우스 뷰

	private NetworkTCP tcp;
	private NetworkUDP_Image udp_image;
	private boolean isKtcp = false; // 키보드가 시작되었는지 확인
	private boolean isFN = false;
	private boolean isShift = false; // 쉬프트가 눌러졋는지
	private boolean isCtrl = false; // 컨트롤이 눌러졌는지
	private boolean isLong = false;

	private Vibrator vibe; // 진동오게하는애

	private DatagramPacket udppacket;
	private DatagramSocket udpsocket;
	private float lastX;
	private float lastY;
	private float firstX;
	private float firstY;
	private Date clickDown;
	private Date now;
	private Date oneClick;
	private Date twoClick;
	private final int[] tolerance = { 10, -10 };
	private MyApplication myApp;

	private float oldDist = 1f; // 두개로 집엇을때 첫번째
	private float newDist = 1f;

	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;

	private int mode = NONE;
	private int DeviceNum = 1;

	private int dClick = 0; // 0은 한번도 클릭안된거 1 은 한번클릭된거
	private ChordFragment mChord;
	private CallStateReceiver callStateReceiver;
	private String settingState;
	private SharedPreferences mainPref;
	private SettingDialog settingdialog;
	private FragmentTransaction mFragmentTransaction;
	private boolean is_Socket_connect = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.enableDefaults();
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_combine_km);

		try {
			this.udpsocket = new DatagramSocket(udp_Mouse_Port);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); // 진동 오는애

		myApp = (MyApplication) getApplication();
		tcp = myApp.getTCP(); // tcp 지정 해둠

		sendDeviceNum(); // pc 화면 돌아가는거

		mChord = myApp.getChord();
		DeviceNum = myApp.getDeviceNum(); // Layout Number
		mChord.setPhoneNumber(DeviceNum);

		mFragmentTransaction = getFragmentManager().beginTransaction();
		mFragmentTransaction.replace(R.id.ConnectServerFragment, mChord);
		mFragmentTransaction.addToBackStack(null);
		mFragmentTransaction.commit();

		udp_image = new NetworkUDP_Image(mChord, myApp.getIP());
		udp_image.start();

		myApp.setImageUDP(udp_image);

		is_Socket_connect = true; // 나중에 비상 종료 됫을때를 대비해서

		boolean isnotcall = myApp.getCall(); // iscall ==true전화끈는애사용안해야함
		callStateReceiver = new CallStateReceiver(); // 전화오면 끈는애
		if (!isnotcall) {
			callStateReceiver.CallState(isnotcall);
		}

		mCustomKeyboard = (CustomKeyboard) findViewById(R.id.custom_keyboardview);
		mCustomKeyboard.setTCP(tcp);

		setBasisButton();
		setViewtouch();

	}

	public void sendDeviceNum() {

		if (myApp.getDeviceNum() == 1 || myApp.getDeviceNum() == 2
				|| myApp.getDeviceNum() == 4) {
			tcp.sendMessage(ProtocalFN + "Rotate" + "\r\n");
		} else {
			tcp.sendMessage(ProtocalFN + "Original" + "\r\n");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "환경설정");
		menu.add(0, 2, 0, "FN 도움말");
		return true;

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1: {
			settingMenu();
			break;
		}
		case 2: {
			Dialog helpdialog = new Dialog(this);
			helpdialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			helpdialog.setContentView(getLayoutInflater().inflate(
					R.layout.dialog_help, null));
			helpdialog.show();
			break;
		}
		}
		return true;

	}

	@Override
	protected void onStop() {
		Log.e(TAG, "onStop");

		super.onStop();
	}

	@Override
	protected void onRestart() {
		Log.e(TAG, "onRestart");

		super.onRestart();
	}

	private void settingMenu() {

		mainPref = getSharedPreferences("mainpref", 0);

		settingState = mainPref.getString("setting", null);
		boolean saveCall = false;

		if (settingState != null) {
			saveCall = mainPref.getBoolean("call", true);
		}
		settingdialog = new SettingDialog(KeyboardMouseActivity.this, saveCall,
				myApp.getDeviceNum());
		settingdialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				settingRetrunCall(settingdialog.getcall());
				myApp.setDeviceNum(settingdialog.getNum());
				mChord.setPhoneNumber(settingdialog.getNum());

				sendDeviceNum();
			}
		});
		settingdialog.show();
	}

	public void settingRetrunCall(boolean call) {
		SharedPreferences.Editor editor = mainPref.edit();
		editor.putString("setting", "ok");
		editor.putBoolean("call", call);
		editor.commit();
		callStateReceiver.CallState(call);
	}

	Handler mHandler = new Handler() {
		String str;

		public void handleMessage(Message msg) {
			if (msg.obj != null)
				str = msg.obj.toString() + "\r\n";
			tcp.sendMessage(ProtocalFN + str);
			// Log.e(TAG, str + "   send massage");
			mHandler.sendEmptyMessageDelayed(0, 100);
		}

	};

	Handler scrollHandler = new Handler() {
		String str;

		public void handleMessage(Message msg) {
			if (msg.obj != null)
				str = msg.obj.toString() + "\r\n";
			tcp.sendMessage(ProtocalFN + str);
			Log.e(TAG, "scroll" + str);
			scrollHandler.sendEmptyMessageDelayed(0, 200);
		}

	};

	private void setViewtouch() {

		view_Touch = (View) findViewById(R.id.view_touch);
		view_Touch.setOnTouchListener(new View.OnTouchListener() {

			@SuppressWarnings("deprecation")
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				int action = event.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN: { // 첫번째 터치
					// Log.e(TAG, "ACTION_DOWN");
					final float x = event.getX();
					final float y = event.getY();
					Log.e(TAG, "first " + x + " : " + y);
					// last position
					lastX = x;
					lastY = y;
					// first position
					firstX = x;
					firstY = y;
					// click time

					mode = DRAG;
					clickDown = new Date();
					break;
				}
				case MotionEvent.ACTION_MOVE: {

					if (mode == DRAG) { // 드래그
										// 중일때

						// new position
						final float x1 = event.getX();
						final float y1 = event.getY();

						// get delta
						final float deltax1 = x1 - lastX;
						final float deltay1 = y1 - lastY;
						// set last position
						lastX = x1;
						lastY = y1;

						try {

							byte[] message = ("drag/" + deltax1 + "," + deltay1)
									.getBytes("UTF-8");

							udppacket = new DatagramPacket(message,
									message.length, InetAddress.getByName(myApp
											.getIP()), udp_Mouse_Port);

							udpsocket.send(udppacket);

							Log.e(TAG, " 드래그 mouse" + "deltax:   " + deltax1
									+ "deltay  : " + deltay1);

							Thread.sleep(10);

						} catch (Throwable e) {
							Log.e(TAG, "not send :   " + e);
						}

					} else if (mode == ZOOM) { // 줌일때

						newDist = spacing(event);

						if (newDist - oldDist > 20) { // 줌인

							tcp.sendMessage(ProtocalFN + "zoom_in" + "\r\n");

							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							oldDist = newDist;
							Log.e(TAG, "Zoom in");
						} else if (oldDist - newDist > 20) { // 줌아웃

							tcp.sendMessage(ProtocalFN + "zoom_out" + "\r\n");
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							oldDist = newDist;
							Log.e(TAG, "Zoom out");
						}

					}

					break;
				}

				case MotionEvent.ACTION_UP: {
					// get current position
					final float x2 = event.getX();
					final float y2 = event.getY();
					// get delta
					final float deltaX = firstX - x2;
					final float deltaY = firstY - y2;

					now = new Date();

					byte[] sendClick = null;
					try {
						if (deltaX < tolerance[0] && deltaX > tolerance[1]
								&& deltaY < tolerance[0]
								&& deltaY > tolerance[1]
								&& now.getTime() - clickDown.getTime() >= 1000) {
							sendClick = ("d.click").getBytes("UTF-8");
							Log.e(TAG, "send d.click ");

						} else if (x2 == firstX && y2 == firstY) {
							sendClick = ("click").getBytes("UTF-8");
							Log.e(TAG, "click");
						}

						if (sendClick != null) {
							udppacket = new DatagramPacket(sendClick,
									sendClick.length, InetAddress
											.getByName(myApp.getIP()),
									udp_Mouse_Port);
							// send package

							udpsocket.send(udppacket);

						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
					break;
				}
				case MotionEvent.ACTION_POINTER_UP: {
					Log.e(TAG, "action pointer up");
					mode = NONE;
					break;
				}

				case MotionEvent.ACTION_POINTER_DOWN: { // 멀티터치를 위해서 두번째 손가락이
														// 닿음..
					final float x3 = event.getX();
					final float y3 = event.getY();
					Log.e(TAG, "second " + x3 + " : " + y3);
					// Log.e(TAG, "action pointer down");

					mode = ZOOM;

					newDist = spacing(event);
					oldDist = spacing(event);
					Log.d("zoom", "newDist=" + newDist);
					Log.d("zoom", "oldDist=" + oldDist);
					Log.d("zoom", "mode=ZOOM");

					break;

				}
				case MotionEvent.ACTION_CANCEL:
				default:
					break;
				}
				return true;
			}
		});

	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);

	}

	private void goneCustomKeyboard() { // 키보드 숨기기
		mCustomKeyboard.setVisibility(View.GONE);
		isKtcp = false;
		isFN = false;

	}

	private void visibleHanCustomKeyboard() { // 한글 키보드 보이기
		mCustomKeyboard
				.setActionListenerHanKeyboard(KeyboardMouseActivity.this);
		mCustomKeyboard.setVisibility(View.VISIBLE);
		isKtcp = true;

	}

	private void visibleFNCustomKeyboard() { // 기호 키보드 보이기
		mCustomKeyboard
				.setActionListenerFN_1Keyboard(KeyboardMouseActivity.this);
		mCustomKeyboard.setVisibility(View.VISIBLE);
		isFN = true;

	}

	private void setBasisButton() {

		btn_Keyboard = (ImageButton) findViewById(R.id.keyboard_show);
		btn_Keyboard.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				vibe.vibrate(15);
				if (isKtcp) { // keyboard on 상태
					// btn_Keyboard.setTextColor(Color.BLACK);
					goneCustomKeyboard(); // 키보드 끄기
					isKtcp = false;
				} else {// keyboard off 상태

					// btn_Keyboard.setTextColor(Color.RED);
					visibleHanCustomKeyboard();
					if (isFN) {

						btn_FN.setTextColor(Color.WHITE);
						isFN = false;
					}
					isKtcp = true;
				}

			}
		});

		btn_FN = (Button) findViewById(R.id.keyboard_FN);
		btn_FN.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				vibe.vibrate(15);
				if (isFN) {

					btn_FN.setTextColor(Color.WHITE);
					goneCustomKeyboard();
					isFN = false;
				} else {// FN 꺼져잇엇음
					if (isKtcp) {
						// btn_Keyboard.setTextColor(Color.BLACK);
						isKtcp = false;
					}
					btn_FN.setTextColor(Color.RED);
					visibleFNCustomKeyboard();
					isFN = true;
				}
			}
		});

		btn_copy = (Button) findViewById(R.id.keyboard_Copy);
		btn_copy.setOnClickListener(this);

		btn_paste = (Button) findViewById(R.id.keyboard_Paste);
		btn_paste.setOnClickListener(this);

		btn_shift = (Button) findViewById(R.id.keyboard_Shift);
		btn_shift.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (isShift) {
						isShift = false;
						btn_shift.setTextColor(Color.WHITE); // 검은색변신
						tcp.sendMessage(ProtocalFN + "Shift_off" + "\r\n");
					} else {
						isShift = true;
						btn_shift.setTextColor(Color.RED); // 누른상태에서 빨간색으로 변신
						tcp.sendMessage(ProtocalFN + "Shift_on" + "\r\n");
					}

				} else if (event.getAction() == MotionEvent.ACTION_UP) {

				}
				return false;
			}
		});
		btn_ctrl = (Button) findViewById(R.id.keyboard_Ctrl);
		btn_ctrl.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) { // 패킷보내기 빨갛게
																	// 변하기

					if (isCtrl) {
						isCtrl = false;
						btn_ctrl.setTextColor(Color.WHITE); // 검은색변신
						tcp.sendMessage(ProtocalFN + "Ctrl_off" + "\r\n");
					} else {
						isCtrl = true;
						btn_ctrl.setTextColor(Color.RED); // 누른상태에서 빨간색으로 변신
						tcp.sendMessage(ProtocalFN + "Ctrl_on" + "\r\n");
					}

				} else if (event.getAction() == MotionEvent.ACTION_UP) {

				}
				return false;
			}
		});

		btn_up = (ImageButton) findViewById(R.id.keyboard_Up);
		btn_up.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// Log.e(TAG, "Action down");
					vibe.vibrate(15);
					tcp.sendMessage(ProtocalFN + "Up" + "\r\n");
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// Log.e(TAG, "Action up");
					if (isLong) {
						scrollHandler.removeMessages(0);

						isLong = false;
					}
				}
				return false;
			}
		});
		btn_up.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// Log.e(TAG, "longclick clicked");
				isLong = true;
				Message msg = Message.obtain(scrollHandler, 0, "Scroll_up");
				scrollHandler.sendMessage(msg);
				return false;
			}
		});
		btn_down = (ImageButton) findViewById(R.id.keyboard_Down);
		btn_down.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// Log.e(TAG, "Action down");
					vibe.vibrate(15);
					tcp.sendMessage(ProtocalFN + "Down" + "\r\n");
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// Log.e(TAG, "Action up");
					if (isLong) {
						scrollHandler.removeMessages(0);

						isLong = false;
					}
				}
				return false;
			}
		});
		btn_down.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// Log.e(TAG, "longclick clicked");
				isLong = true;
				Message msg = Message.obtain(scrollHandler, 0, "Scroll_down");
				scrollHandler.sendMessage(msg);
				return false;
			}
		});

		btn_left = (ImageButton) findViewById(R.id.keyboard_left);
		btn_left.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// Log.e(TAG, "Action down");
					vibe.vibrate(15);
					tcp.sendMessage(ProtocalFN + "Left" + "\r\n");
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// Log.e(TAG, "Action up");
					if (isLong) {
						mHandler.removeMessages(0);
						isLong = false;
					}
				}
				return false;
			}
		});
		btn_left.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// Log.e(TAG, "longclick clicked");
				isLong = true;
				Message msg = Message.obtain(mHandler, 0, "Left");
				mHandler.sendMessage(msg);
				return false;
			}
		});
		btn_right = (ImageButton) findViewById(R.id.keyboard_Right);
		btn_right.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// Log.e(TAG, "Action down");
					vibe.vibrate(15);
					tcp.sendMessage(ProtocalFN + "Right" + "\r\n");
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// Log.e(TAG, "Action up");
					if (isLong) {
						mHandler.removeMessages(0);

						isLong = false;
					}
				}
				return false;
			}
		});
		btn_right.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// Log.e(TAG, "longclick clicked");
				isLong = true;
				Message msg = Message.obtain(mHandler, 0, "Right");
				mHandler.sendMessage(msg);
				return false;
			}
		});

		btn_left_click = (ImageButton) findViewById(R.id.mouse_left);
		btn_left_click.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) { // touch 햇다고
																	// 알려주기
					vibe.vibrate(15);
					if (dClick == 0) {
						dClick = 1;
						oneClick = new Date();
						// Log.e(TAG, "Click_on");
						tcp.sendMessage(ProtocalFN + "Click_on" + "\r\n");
					} else if (dClick == 1) {
						twoClick = new Date();
						if (twoClick.getTime() - oneClick.getTime() <= 700) {
							try {
								byte[] msg = ("d.click").getBytes("UTF-8");
								udppacket = new DatagramPacket(msg, msg.length,
										InetAddress.getByName(myApp.getIP()),
										udp_Mouse_Port);
								udpsocket.send(udppacket);

							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {

							tcp.sendMessage(ProtocalFN + "Click_on" + "\r\n");
						}
						dClick = 0;
					}

				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					tcp.sendMessage(ProtocalFN + "Click_off" + "\r\n");
				}
				return false;
			}
		});
		btn_right_click = (ImageButton) findViewById(R.id.mouse_right);
		btn_right_click.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) { // touch 햇다고
					vibe.vibrate(15); // 알려주기

				} else if (event.getAction() == MotionEvent.ACTION_UP) { // touch
																			// 끝낫다고
					// Log.e(TAG, "Rclick"); // 알려주기
					tcp.sendMessage(ProtocalFN + "Rclick" + "\r\n");

				}
				return false;
			}
		});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (isKtcp)
			goneCustomKeyboard();
		callStateReceiver.CallState(true); // 전화오는거 안막는거임
		if (is_Socket_connect) {
			udp_image.Close_Socket();
			tcp.Close_Socket(); // 키보드 닫음 패킷은 보내짐, 이안에서 파일도 닫힘
			is_Socket_connect = false;

		}

		this.udpsocket.close();
		mChord.CloseChord(false);

		finish();
	}

	@Override
	public void onClick(View v) {
		vibe.vibrate(14);

		String str = ((Button) v).getText().toString();
		// Log.e(TAG, "send" + str);
		tcp.sendMessage(ProtocalFN + str + "\r\n");

		if (isCtrl) {
			isCtrl = false;
			btn_ctrl.setTextColor(Color.WHITE); // 검은색변신
			tcp.sendMessage(ProtocalFN + "Ctrl_off" + "\r\n");
		}
		if (isShift) {
			isShift = false;
			btn_shift.setTextColor(Color.WHITE); // 검은색변신
			tcp.sendMessage(ProtocalFN + "Shift_off" + "\r\n");
		}

	}

	@Override
	protected void onUserLeaveHint() {
		// TODO Auto-generated method stub
		super.onUserLeaveHint();
		callStateReceiver.CallState(true); // 전화오는거 안막는거임
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if (isKtcp || isFN) {

			// btn_Keyboard.setTextColor(Color.BLACK);
			btn_FN.setTextColor(Color.BLACK);
			goneCustomKeyboard();
		} else {
			exit_dialog();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isKtcp || isFN) {

				// btn_Keyboard.setTextColor(Color.BLACK);
				btn_FN.setTextColor(Color.WHITE);
				goneCustomKeyboard();
			} else {
				exit_dialog();
			}
			return false;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void exit_dialog() {
		AlertDialog.Builder longDialog = new AlertDialog.Builder(this);
		longDialog.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
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
