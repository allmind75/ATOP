/**
 * Copyright 2013 Maarten Pennings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * If you use this software in a product, an acknowledgment in the product
 * documentation would be appreciated but is not required.
 */

package com.atop.keyboard;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

import com.atop.network.NetworkTCP;
import com.example.ssm_test.R;

/**
 * When an activity hosts a keyboardView, this class allows several EditText's
 * to register for it.
 * 
 * @author Maarten Pennings
 * @date 2012 December 23
 */
class CustomKeyboard extends KeyboardView {
	private final String TAG = "Class::CustomKeyboard";

	private final String ProtocalFN = "특";
	private final String ProtocalSum = "조";
	private final String ProtocalHan = "한";
	private final String ProtocalEng = "영";

	CustomOnKeyboardActionListener hanKey, engKey, big_engKey, sym1Key,
			sym2Key, fn_1Key, big_hanKey, fn_2Key;
	Keyboard han = null, big_han = null, eng = null, big_eng = null,
			sym1 = null, fn_1 = null, fn_2 = null;
	Context context;
	NetworkTCP tcp;
	private String state; // 지금 무슨상태인지 알아오는 친구

	private boolean isLong = false;

	public CustomKeyboard(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		han = new Keyboard(context, R.xml.han);
		big_han = new Keyboard(context, R.xml.bighan);
		eng = new Keyboard(context, R.xml.eng);
		big_eng = new Keyboard(context, R.xml.bigeng);
		sym1 = new Keyboard(context, R.xml.sym_1);
		fn_1 = new Keyboard(context, R.xml.fn_1);
		fn_2 = new Keyboard(context, R.xml.fn_2);
		setPreviewEnabled(false); // 팝업 사라짐 미리 보기 사라짐
	}

	public void setTCP(NetworkTCP tcp) {
		this.tcp = tcp;
	}

	public void setActionListenerHanKeyboard(Activity act) { // 한글 세팅

		state = "Han";
		this.clearFocus();
		hanKey = new CustomOnKeyboardActionListener(act);
		this.setOnKeyboardActionListener(hanKey);
		this.setKeyboard(han);
	}

	public void setActionListenerBigHanKeyboard(Activity act) { // 한글 쉬프트 세팅

		state = "BigHan";
		this.clearFocus();
		big_hanKey = new CustomOnKeyboardActionListener(act);
		this.setOnKeyboardActionListener(big_hanKey);
		this.setKeyboard(big_han);
	}

	public void setActionListenerEngKeyboard(Activity act) { // 소문자영어세팅

		state = "Eng";
		this.clearFocus();
		engKey = new CustomOnKeyboardActionListener(act);
		this.setOnKeyboardActionListener(engKey);
		this.setKeyboard(eng);
	}

	public void setActionListenerBigEngKeyboard(Activity act) { // 대문자영어

		state = "BigEng";
		this.clearFocus();
		big_engKey = new CustomOnKeyboardActionListener(act);
		this.setOnKeyboardActionListener(big_engKey);
		this.setKeyboard(big_eng);
	}

	public void setActionListenerSym1Keyboard(Activity act) { // 기호1번

		state = "Sym1";
		this.clearFocus();
		sym1Key = new CustomOnKeyboardActionListener(act);
		this.setOnKeyboardActionListener(sym1Key);
		this.setKeyboard(sym1);
	}

	public void setActionListenerFN_1Keyboard(Activity act) { // FN1

		state = "FN_1";
		this.clearFocus();
		fn_1Key = new CustomOnKeyboardActionListener(act);
		this.setOnKeyboardActionListener(fn_1Key);
		this.setKeyboard(fn_1);
	}

	public void setActionListenerFN_2Keyboard(Activity act) { // FN2

		state = "FN_2";
		this.clearFocus();
		fn_2Key = new CustomOnKeyboardActionListener(act);
		this.setOnKeyboardActionListener(fn_2Key);
		this.setKeyboard(fn_2);
	}

	@Override
	public boolean isInEditMode() {
		return true;
	};

	private class CustomOnKeyboardActionListener implements
			OnKeyboardActionListener {
		Activity owner;

		public CustomOnKeyboardActionListener(Activity activity) {
			owner = activity;
		}

		public void onKey(int primaryCode, int[] keyCodes) {

			long eventTime = System.currentTimeMillis();
			// 한글일때 영어일때 기호일때 패킷 바꿔서 보내는거 가능

			// 구분해야하는거 한/영 쉬프트 alt

			if (isLong) {
				Log.e(TAG, "알트탭 오프");
				altTabHandler.removeMessages(0);
				tcp.sendMessage(ProtocalSum + "Alt_Off" + "\r\n");
				isLong = false;
			}

			Log.e(TAG, "primartCode :" + primaryCode);
			if (!state.equals("FN_1") && !state.equals("FN_2")) { // 기호가 아니면 이러한
																	// 것들을 한다

				switch (primaryCode) {
				case 10: {
					if (state.equals("Han")) {
						setActionListenerBigHanKeyboard(owner); // 쉬프트 한글로 바꿈
					} else if (state.equals("BigHan")) {
						setActionListenerHanKeyboard(owner); // 한글로 바꿈
					} else if (state.equals("Eng")) {
						setActionListenerBigEngKeyboard(owner); // 대문자
					} else if (state.equals("BigEng")) {
						setActionListenerEngKeyboard(owner); // 소문자
					}
					break;
				}
				case 11: {// 백버튼누름
					tcp.sendMessage(ProtocalFN + "Back" + "\r\n");
					break;
				}
				case 12: { // 기호/abc 누름
					if (state.equals("Sym1") || state.equals("Sym2")) { // 기호
																		// 상태라면
						setActionListenerHanKeyboard(owner); // 한글로 넘어감
					} else {
						setActionListenerSym1Keyboard(owner); // 기호로 넘어감
					}
					break;
				}
				case 13: { // 한/영
					if (!state.equals("Han")) { // 한글이 아니면
						setActionListenerHanKeyboard(owner); // 한글로 바꿈
					} else {
						setActionListenerEngKeyboard(owner); // 소문자로 바꿈
					}
					break;
				}
				case 14: {// 스페이스 누름
					tcp.sendMessage(ProtocalFN + "Space" + "\r\n");
					break;
				}
				case 15: {// 엔터누름
					tcp.sendMessage(ProtocalFN + "Ent" + "\r\n");
					break;
				}
				case 33: {
					tcp.sendMessage(ProtocalFN + "!" + "\r\n");
					break;
				}
				case 46: {
					tcp.sendMessage(ProtocalFN + "." + "\r\n");
					break;
				}
				case 44: {
					tcp.sendMessage(ProtocalFN + "," + "\r\n");
					break;
				}
				case 63: {
					tcp.sendMessage(ProtocalFN + "?" + "\r\n");
					break;
				}
				default:

					KeyEvent event = new KeyEvent(eventTime, eventTime,
							KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0, 0,
							KeyEvent.FLAG_SOFT_KEYBOARD
									| KeyEvent.FLAG_KEEP_TOUCH_MODE);

					char c = (char) primaryCode;
					String s = Character.toString(c);

					if (state.equals("Han") || state.equals("BigHan")) {
						Log.e(TAG, "sned String :" + s);
						tcp.sendMessage(ProtocalHan + s + "\r\n");

						if (state.equals("BigHan")) {
							setActionListenerHanKeyboard(owner); // 한글로 바꿈
						}

					} else if (state.equals("Eng") || state.equals("BigEng")) {
						Log.e(TAG, "sned String :" + s);
						tcp.sendMessage(ProtocalEng + s + "\r\n");
					} else if (state.equals("Sym1")) {
						Log.e(TAG, "sned String :" + s);
						tcp.sendMessage(ProtocalFN + s + "\r\n");
					}

					break;
				}
			} else { // 요건 FN이 사용될 때만 돌아갈꺼

				switch (primaryCode) {

				case 300: {
					if (state.equals("FN_1")) {
						setActionListenerFN_2Keyboard(owner);
					} else if (state.equals("FN_2")) {
						setActionListenerFN_1Keyboard(owner);
					}
					break;
				}
				case 161: {
					tcp.sendMessage(ProtocalFN + "F1" + "\r\n");
					break;
				}
				case 162: {
					tcp.sendMessage(ProtocalFN + "F2" + "\r\n");
					break;
				}
				case 163: {
					tcp.sendMessage(ProtocalFN + "F3" + "\r\n");
					break;
				}
				case 164: {
					tcp.sendMessage(ProtocalFN + "F4" + "\r\n");
					break;
				}
				case 165: {
					tcp.sendMessage(ProtocalFN + "F5" + "\r\n");
					break;
				}
				case 166: {
					tcp.sendMessage(ProtocalFN + "F6" + "\r\n");
					break;
				}
				case 167: {
					tcp.sendMessage(ProtocalFN + "F7" + "\r\n");
					break;
				}
				case 168: {
					tcp.sendMessage(ProtocalFN + "F8" + "\r\n");
					break;
				}
				case 169: {
					tcp.sendMessage(ProtocalFN + "F9" + "\r\n");
					break;
				}
				case 170: {
					tcp.sendMessage(ProtocalFN + "F10" + "\r\n");
					break;
				}
				case 171: {
					tcp.sendMessage(ProtocalFN + "F11" + "\r\n");
					break;
				}
				case 172: {
					tcp.sendMessage(ProtocalFN + "F12" + "\r\n");
					break;
				}
				case 173: {
					tcp.sendMessage(ProtocalFN + "Tab" + "\r\n");
					break;
				}
				case 174: {
					tcp.sendMessage(ProtocalFN + "WIN" + "\r\n");
					break;
				}
				case 175: {
					tcp.sendMessage(ProtocalFN + "한자" + "\r\n");
					break;
				}
				case 176: {
					tcp.sendMessage(ProtocalFN + "Alt" + "\r\n");
					break;
				}
				case 177: {
					tcp.sendMessage(ProtocalFN + "Del" + "\r\n");
					break;
				}
				case 178: {
					tcp.sendMessage(ProtocalFN + "Insert" + "\r\n");
					break;
				}
				case 179: {
					tcp.sendMessage(ProtocalFN + "Home" + "\r\n");
					break;
				}
				case 180: {
					tcp.sendMessage(ProtocalFN + "End" + "\r\n");
					break;
				}
				case 181: {
					tcp.sendMessage(ProtocalFN + "PgUp" + "\r\n");
					break;
				}
				case 182: {
					tcp.sendMessage(ProtocalFN + "PgDn" + "\r\n");
					break;
				}
				case 183: {
					tcp.sendMessage(ProtocalFN + "PrtSc" + "\r\n");
					break;
				}
				case 184: {
					tcp.sendMessage(ProtocalFN + "ScrLK" + "\r\n");
					break;
				}
				case 185: {
					tcp.sendMessage(ProtocalFN + "Pause" + "\r\n");
					break;
				}
				case 186: {
					tcp.sendMessage(ProtocalSum + "바탕화면" + "\r\n");
					break;
				}
				case 187: {
					tcp.sendMessage(ProtocalSum + "실행" + "\r\n");
					break;
				}
				case 188: {
					tcp.sendMessage(ProtocalSum + "종료" + "\r\n");
					break;
				}
				case 189: {
					tcp.sendMessage(ProtocalSum + "주소창" + "\r\n");
					break;
				}
				case 191: {
					tcp.sendMessage(ProtocalSum + "창최소" + "\r\n");
					break;
				}
				case 192: {
					tcp.sendMessage(ProtocalSum + "창최대" + "\r\n");
					break;
				}
				case 193: {
					tcp.sendMessage(ProtocalSum + "창이전" + "\r\n");
					break;
				}
				case 195: {
					tcp.sendMessage(ProtocalSum + "저장" + "\r\n");
					break;
				}
				case 196: {
					tcp.sendMessage(ProtocalSum + "모두선택" + "\r\n");
					break;
				}
				case 197: {
					tcp.sendMessage(ProtocalSum + "실행취소" + "\r\n");
					break;
				}
				case 198: {
					tcp.sendMessage(ProtocalSum + "찾기" + "\r\n");
					break;
				}
				case 199: {
					tcp.sendMessage(ProtocalFN + "ESC" + "\r\n");
					break;
				}
				case 201: {
					tcp.sendMessage(ProtocalSum + "좌단어" + "\r\n");
					break;
				}
				case 202: {
					tcp.sendMessage(ProtocalSum + "우단어" + "\r\n");
					break;
				}
				case 204: {
					tcp.sendMessage(ProtocalSum + "좌페이지" + "\r\n");
					break;
				}
				case 205: {
					tcp.sendMessage(ProtocalSum + "우페이지" + "\r\n");
					break;
				}
				case 207: {
					tcp.sendMessage(ProtocalSum + "완앞" + "\r\n");
					break;
				}
				case 208: {
					tcp.sendMessage(ProtocalSum + "완뒤" + "\r\n");
					break;
				}
				case 210: {
					tcp.sendMessage(ProtocalSum + "좌정렬" + "\r\n");
					break;
				}
				case 211: {
					tcp.sendMessage(ProtocalSum + "우정렬" + "\r\n");
					break;
				}
				case 212: {
					tcp.sendMessage(ProtocalSum + "작업" + "\r\n");
					break;
				}
				default:
					break;
				}

			}
		}

		@Override
		public void onPress(int primaryCode) {
			if (primaryCode == 194) {
				isLong = true;
				Log.e(TAG, "alt+tab on");
				tcp.sendMessage(ProtocalSum + "Alt_On" + "\r\n");
				Message msg = Message.obtain(altTabHandler, 0, "Alt_Move");
				altTabHandler.sendMessage(msg);
			}
		}

		@Override
		public void onRelease(int primaryCode) {
			if (primaryCode == 194) {

				if (isLong) {
					Log.e(TAG, "alt+tab off");
					tcp.sendMessage(ProtocalSum + "Alt_Off" + "\r\n");
					altTabHandler.removeMessages(0);

					isLong = false;

				}

			}
		}

		@Override
		public void onText(CharSequence text) {
		}

		@Override
		public void swipeDown() {
		}

		@Override
		public void swipeLeft() { // 여기서 스와이프를 처리해야할듯

		}

		@Override
		public void swipeRight() {

		}

		@Override
		public void swipeUp() {
		}

		Handler altTabHandler = new Handler() {
			String str;

			public void handleMessage(Message msg) {
				if (msg.obj != null)
					str = msg.obj.toString() + "\r\n";
				tcp.sendMessage(ProtocalSum + str);
				Log.e(TAG, "alt+tab " + str);
				altTabHandler.sendEmptyMessageDelayed(0, 1000);
			}

		};
	}

}
