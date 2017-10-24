package com.atop.chord;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.atop.main.SurfaceViewMain;
import com.atop.network.NetworkTCP;
import com.example.ssm_test.R;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.chord.Schord;
import com.samsung.android.sdk.chord.SchordChannel;
import com.samsung.android.sdk.chord.SchordManager;
import com.samsung.android.sdk.chord.SchordManager.NetworkListener;

@SuppressLint("ValidFragment")
public class ChordFragment extends Fragment implements OnClickListener {

	static private Bitmap bitmap_image = null;
	private DataSendThread mDataSendThread = null;
	public int phoneNumber = 1;
	NetworkTCP tcp;

	private byte[][] byteImage = new byte[3][];
	private static final String TAG = "Class::Chord";
	private static final String CHORD_CHANNEL = "com.atop.CHORDCHANNEL";
	private static final String CHORD_MESSAGE_TYPE = "com.atop.MESSAGE_TYPE";

	private SchordManager mSchordManager_1 = null;
	private SchordManager mSchordManager_2 = null;
	private SchordManager mSchordManager_3 = null;

	private Button mWifiDirect_startStop_btn = null;

	private boolean mWifiDirect_bStarted = false;
	private boolean sendPlay = false;
	private boolean START = false;

	private HashMap<String, Integer> mNodeNumberMap = new HashMap<String, Integer>();
	private int mNodeNumber = 0;
	private int screenWidth;
	private int screenHeight;
	private float deviceYdp;
	private float smallScreenHeight;
	private int checkChord;
	private SparseIntArray mInterfaceMap = new SparseIntArray();

	public SurfaceViewMain surfaceviewmain;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.chord_fragment, null);

		mWifiDirect_startStop_btn = (Button) view
				.findViewById(R.id.wifiDirect_start_stop_btn);
		mWifiDirect_startStop_btn.setOnClickListener(this);
		mWifiDirect_startStop_btn.setEnabled(false);

		return view;
	}

	public ChordFragment(SurfaceViewMain surfaceviewmain) {

		this.surfaceviewmain = surfaceviewmain;

	}

	@Override
	public void onResume() {
		super.onResume();

		if (mSchordManager_1 == null) {
			initChord();

		}
	}

	@Override
	public void onDestroy() {
		if (mSchordManager_1 != null) {
			mSchordManager_1.setNetworkListener(null);
			mSchordManager_1.close();
			mSchordManager_1 = null;
		}

		if (mSchordManager_2 != null) {
			mSchordManager_2.close();
			mSchordManager_2 = null;
		}
		if (mSchordManager_3 != null) {
			mSchordManager_3.close();
			mSchordManager_3 = null;
		}

		mNodeNumberMap.clear();
		mInterfaceMap.clear();

		if (bitmap_image != null) {
			bitmap_image.recycle();
			bitmap_image = null;
		}

		super.onDestroy();
	}

	// 추가
	@Override
	public void onDetach() {
		if (mSchordManager_1 != null) {
			mSchordManager_1.setNetworkListener(null);
			mSchordManager_1.close();
			mSchordManager_1 = null;
		}

		if (mSchordManager_2 != null) {
			mSchordManager_2.close();
			mSchordManager_2 = null;
		}
		if (mSchordManager_3 != null) {
			mSchordManager_3.close();
			mSchordManager_3 = null;
		}

		mNodeNumberMap.clear();
		mInterfaceMap.clear();

		super.onDetach();
	}

	@Override
	public void onDestroyView() {
		if (mSchordManager_1 != null) {
			mSchordManager_1.setNetworkListener(null);
			mSchordManager_1.close();
			mSchordManager_1 = null;
		}

		if (mSchordManager_2 != null) {
			mSchordManager_2.close();
			mSchordManager_2 = null;
		}
		if (mSchordManager_3 != null) {
			mSchordManager_3.close();
			mSchordManager_3 = null;
		}

		mNodeNumberMap.clear();
		mInterfaceMap.clear();

		super.onDestroyView();
	}

	//

	@Override
	public void onClick(View v) {

		boolean bStarted = false;
		int ifc = -1;

		switch (v.getId()) {
		case R.id.wifiDirect_start_stop_btn:
			bStarted = mWifiDirect_bStarted;
			ifc = SchordManager.INTERFACE_TYPE_WIFI_P2P;
			break;
		}
		if (!bStarted) {
			Log.d(TAG, "START Chord");
			// -----------------StartChord---------------------//
			startChord(ifc);
			// ------------------------------------------------//
		} else {
			Log.d(TAG, "STOP Chord");
			stopChord(ifc);
		}
	}

	// Initialize
	private void initChord() {

		Schord chord = new Schord();
		try {
			chord.initialize(getActivity());

		} catch (SsdkUnsupportedException e) {
			if (e.getType() == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED) {
				return;
			}

		}

		mSchordManager_1 = new SchordManager(getActivity());
		mSchordManager_1.setLooper(getActivity().getMainLooper());

		mSchordManager_1.setNetworkListener(new NetworkListener() {

			@Override
			public void onConnected(int interfaceType) {
				refreshInterfaceStatus(interfaceType, true);

			}

			@Override
			public void onDisconnected(int interfaceType) {
				refreshInterfaceStatus(interfaceType, false);

			}
		});

		List<Integer> ifcList = mSchordManager_1.getAvailableInterfaceTypes();
		if (ifcList.isEmpty()) {
			// No Connection
			return;
		}

		for (Integer ifc : ifcList) {
			refreshInterfaceStatus(ifc, true);
		}
	}

	private void refreshInterfaceStatus(int interfaceType, boolean bConnected) {
		if (!bConnected) {

			if (interfaceType == SchordManager.INTERFACE_TYPE_WIFI_P2P) {
				mWifiDirect_startStop_btn.setEnabled(false);
			}
		} else {

			if (interfaceType == SchordManager.INTERFACE_TYPE_WIFI_P2P) {
				mWifiDirect_startStop_btn.setEnabled(true);
			}
		}
	}

	public void startChord(int interfaceType) {

		Log.e(TAG, "phoneNumber : " + phoneNumber);

		int managerIndex = 0;
		SchordManager startManager = null;

		if (mInterfaceMap.get(interfaceType) == 0) {
			managerIndex = mInterfaceMap.size() + 1;
			mInterfaceMap.put(interfaceType, managerIndex);
		} else {
			managerIndex = mInterfaceMap.get(interfaceType);
		}

		switch (managerIndex) {
		case 1:
			startManager = mSchordManager_1;
			break;
		case 2:
			mSchordManager_2 = new SchordManager(getActivity());
			startManager = mSchordManager_2;
			break;
		case 3:
			mSchordManager_3 = new SchordManager(getActivity());
			startManager = mSchordManager_3;
			break;
		}

		try {
			Log.d(TAG, "Chord start(" + getInterfaceName(interfaceType)
					+ ") with the SchordManager number : " + managerIndex);

			startManager.setLooper(getActivity().getMainLooper());

			switch (interfaceType) {
			case SchordManager.INTERFACE_TYPE_WIFI_P2P:
				startManager.start(interfaceType, mWifiDirect_ManagerListener);
				mWifiDirect_startStop_btn.setEnabled(true);
				break;
			}
			Log.d(TAG, "start - " + getInterfaceName(interfaceType));
		} catch (Exception e) {
			Log.d(TAG, "Fail to start - " + e.getMessage());
			mInterfaceMap.delete(interfaceType);
		}

		Log.e(TAG, "Manager : " + managerIndex);

	}

	private SchordManager.StatusListener mWifiDirect_ManagerListener = new SchordManager.StatusListener() {

		@Override
		public void onStarted(String nodeName, int reason) {
			mWifiDirect_bStarted = true;

			mWifiDirect_startStop_btn.setText(R.string.stop);
			mWifiDirect_startStop_btn.setEnabled(true);

			if (reason == STARTED_BY_USER) {
				// Success to start by calling start() method
				Log.d(TAG, "onStarted");
				joinTestChannel(SchordManager.INTERFACE_TYPE_WIFI_P2P);
			} else if (reason == STARTED_BY_RECONNECTION) {
				// Re-start by network re-connection.
			}

		}

		@Override
		public void onStopped(int reason) {
			mWifiDirect_bStarted = false;

			mWifiDirect_startStop_btn.setText(R.string.start);

			if (STOPPED_BY_USER == reason) {
				// Success to stop by calling stop() method
				mWifiDirect_startStop_btn.setEnabled(true);

			} else if (NETWORK_DISCONNECTED == reason) {
				// Stopped by network disconnected
				mWifiDirect_startStop_btn.setEnabled(false);
			}
		}
	};

	private void joinTestChannel(int interfaceType) {

		Log.d(TAG, interfaceType + " - JoinChannel");

		SchordChannel channel = null;
		SchordManager currentManager = null;

		currentManager = getSchordManager(interfaceType);

		switch (interfaceType) {
		case SchordManager.INTERFACE_TYPE_WIFI_P2P:
			channel = currentManager.joinChannel(CHORD_CHANNEL,
					mWifiDirect_ChannelListener);
			break;
		}
		if (channel == null) {
			Log.e(TAG, interfaceType + "Fail to joinChannel");
		}

	}

	public void stopChord(int ifc) {

		SchordManager currentManager = null;

		currentManager = getSchordManager(ifc);

		if (currentManager == null)
			return;

		currentManager.stop();

		switch (ifc) {
		case SchordManager.INTERFACE_TYPE_WIFI_P2P:
			mWifiDirect_startStop_btn.setEnabled(false);
			break;
		}
		Log.e(TAG, "StopChord - " + getInterfaceName(ifc));
	}

	private SchordManager getSchordManager(int interfaceType) {

		int managerIndex = 0;
		SchordManager manager = null;

		managerIndex = mInterfaceMap.get(interfaceType);

		switch (managerIndex) {
		case 1:
			manager = mSchordManager_1;
			break;
		case 2:
			manager = mSchordManager_2;
			break;
		case 3:
			manager = mSchordManager_3;
			break;
		}
		return manager;
	}

	private SchordChannel.StatusListener mWifiDirect_ChannelListener = new SchordChannel.StatusListener() {

		int interfaceType = SchordManager.INTERFACE_TYPE_WIFI_P2P;

		@Override
		public void onNodeLeft(String fromNode, String fromChannel) {
			Bitmap image;

			Log.d(TAG, "onNodeLeft::");

			image = BitmapFactory.decodeResource(getResources(),
					R.drawable.chord_disconnect_black);

			if (bitmap_image != null) {
				Log.e(TAG, "STOP Chord Thread");

				mDataSendThread.stopThread(false);
			}

			if (!START)
				surfaceviewmain.setThreadimageBitmap(image, smallScreenHeight
						* deviceYdp);
		}

		@Override
		public void onNodeJoined(String fromNode, String fromChannel) {

			Log.d(TAG, "onNodeJoined::");
			DisplaySize();

			// AP에서만 sendthread 동작
			if (bitmap_image != null) {
				Log.e(TAG, "Start Chord Send Thread");
				mDataSendThread = new DataSendThread(true, interfaceType,
						fromNode);
				mDataSendThread.start();
				START = true;
			} else {
				onNodeCallbackCommon(interfaceType, fromNode);

			}

		}

		@Override
		public void onDataReceived(String fromNode, String Channel,
				String payloadType, byte[][] payload) {

			String Check = new String(payload[0]);
			String temp;
			float floatImageHeight;

			long start = System.currentTimeMillis();

			if (Check.equals("I")) {
				try {
					if (payload[1] != null) {
						Log.d(TAG, "Data Receive");
						if (checkChord == 1) {
							startChord(SchordManager.INTERFACE_TYPE_WIFI_P2P);
							checkChord = 0;
						}
						Log.e(TAG, "Height" + smallScreenHeight * deviceYdp);
						Log.e(TAG, smallScreenHeight + " / " + deviceYdp);
						surfaceviewmain.setThreadimage(payload[1],
								payload[1].length, smallScreenHeight
										* deviceYdp);	//작은화면사이즈의 height

					} else {
						Log.d(TAG, "Data NULL");

						checkChord = 1;
						surfaceviewmain.setThreadimageBitmap(bitmap_image,
								smallScreenHeight * deviceYdp);
						stopChord(SchordManager.INTERFACE_TYPE_WIFI_P2P);

					}
				} catch (Exception e) {
					return;
				}
			} else if (Check.equals("S")) {
				temp = new String(payload[1]);
				floatImageHeight = Float.parseFloat(temp);
				if (floatImageHeight < smallScreenHeight)
					smallScreenHeight = floatImageHeight;

				Log.e(TAG, "smallScreenHeight : " + floatImageHeight);
			}

			long end = System.currentTimeMillis();

			Log.d(TAG, "receiveChord time  " + (end - start) / 1000.0);

			// if (payloadType.equals(CHORD_MESSAGE_TYPE)) {
			// Log.d(TAG,
			// "onDataReceive - Node "
			// + mNodeNumberMap.get(interfaceName + fromNode));
			//
			// }
		}

		@Override
		public void onFileChunkReceived(String arg0, String arg1, String arg2,
				String arg3, String arg4, String arg5, long arg6, long arg7) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onFileChunkSent(String arg0, String arg1, String arg2,
				String arg3, String arg4, String arg5, long arg6, long arg7,
				long arg8) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onFileFailed(String arg0, String arg1, String arg2,
				String arg3, String arg4, int arg5) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onFileReceived(String arg0, String arg1, String arg2,
				String arg3, String arg4, String arg5, long arg6, String arg7) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onFileSent(String arg0, String arg1, String arg2,
				String arg3, String arg4, String arg5) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onFileWillReceive(String arg0, String arg1, String arg2,
				String arg3, String arg4, String arg5, long arg6) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMultiFilesChunkReceived(String arg0, String arg1,
				String arg2, String arg3, int arg4, String arg5, long arg6,
				long arg7) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMultiFilesChunkSent(String arg0, String arg1,
				String arg2, String arg3, int arg4, String arg5, long arg6,
				long arg7, long arg8) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMultiFilesFailed(String arg0, String arg1, String arg2,
				String arg3, int arg4, int arg5) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMultiFilesFinished(String arg0, String arg1, String arg2,
				int arg3) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMultiFilesReceived(String arg0, String arg1, String arg2,
				String arg3, int arg4, String arg5, long arg6, String arg7) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMultiFilesSent(String arg0, String arg1, String arg2,
				String arg3, int arg4, String arg5) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMultiFilesWillReceive(String arg0, String arg1,
				String arg2, String arg3, int arg4, String arg5, long arg6) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUdpDataDelivered(String arg0, String arg1, String arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUdpDataReceived(String arg0, String arg1, String arg2,
				byte[][] arg3, String arg4) {
			// TODO Auto-generated method stub

		}
	};

	private String getInterfaceName(int interfaceType) {
		if (SchordManager.INTERFACE_TYPE_WIFI_P2P == interfaceType)
			return "Wi-Fi Direct";

		return "UNKNOWN";
	}

	private SchordChannel getJoinedChannelByIfcType(int ifcType) {

		int managerIndex = 0;
		SchordChannel channel = null;

		managerIndex = mInterfaceMap.get(ifcType);

		switch (managerIndex) {

		case 1:
			channel = mSchordManager_1.getJoinedChannel(CHORD_CHANNEL);
			break;
		case 2:
			channel = mSchordManager_2.getJoinedChannel(CHORD_CHANNEL);
			break;
		case 3:
			channel = mSchordManager_3.getJoinedChannel(CHORD_CHANNEL);
			break;
		}

		return channel;
	}

	// Layout Device Number
	public void setPhoneNumber(int num) {

		phoneNumber = num;
	}

	// UDP에서 bitmap 받음
	public synchronized void setThreadimage(Bitmap bitmap) {

		bitmap_image = bitmap;

	}

	private byte[] bitmapToByteArray(Bitmap bitMapImage) {

		byte[] ImageArray = null;
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		bitMapImage.compress(CompressFormat.JPEG, 80, byteArray);
		ImageArray = byteArray.toByteArray();

		bitMapImage.recycle();
		bitMapImage = null;

		return ImageArray;

	}

	// Bitmap 나누기
	private void SplitBitmap(Bitmap originalBm) {

		long start = System.currentTimeMillis();

		switch (phoneNumber) {

		// Device 2 - 1AP, 1Client
		case 1:
			// originalBm = GetRotatedBitmap(originalBm, 90);
			byteImage[0] = bitmapToByteArray(originalBm);
			break;

		// Device 3 - 1AP, 2Client 가로모드
		case 2:
			// originalBm = GetRotatedBitmap(originalBm, 90);
			byteImage[0] = bitmapToByteArray(Bitmap.createBitmap(originalBm, 0,
					0, (originalBm.getWidth() / 2), originalBm.getHeight()));
			byteImage[1] = bitmapToByteArray(Bitmap.createBitmap(originalBm,
					(originalBm.getWidth() / 2), 0,
					(originalBm.getWidth() / 2), originalBm.getHeight()));
			break;
		// Device 3 - 1AP, 2Client 세로모드
		case 3:
			byteImage[0] = bitmapToByteArray(Bitmap.createBitmap(originalBm, 0,
					0, (originalBm.getWidth() / 2), originalBm.getHeight()));
			byteImage[1] = bitmapToByteArray(Bitmap.createBitmap(originalBm,
					(originalBm.getWidth() / 2), 0,
					(originalBm.getWidth() / 2), originalBm.getHeight()));
			break;

		// Device 4 - 1AP, 3Client 가로모드
		case 4:
			// originalBm = GetRotatedBitmap(originalBm, 90);
			byteImage[0] = bitmapToByteArray(Bitmap.createBitmap(originalBm, 0,
					0, (originalBm.getWidth() / 3), originalBm.getHeight()));
			byteImage[1] = bitmapToByteArray(Bitmap.createBitmap(originalBm,
					(originalBm.getWidth() / 3), 0,
					(originalBm.getWidth() / 3), originalBm.getHeight()));
			byteImage[2] = bitmapToByteArray(Bitmap.createBitmap(originalBm,
					(originalBm.getWidth() / 3) * 2, 0,
					(originalBm.getWidth() / 3), originalBm.getHeight()));
			break;

		// Device 4 - 1AP, 3Client 세로모드
		case 5:
			byteImage[0] = bitmapToByteArray(Bitmap.createBitmap(originalBm, 0,
					0, (originalBm.getWidth() / 3), originalBm.getHeight()));
			byteImage[1] = bitmapToByteArray(Bitmap.createBitmap(originalBm,
					(originalBm.getWidth() / 3), 0,
					(originalBm.getWidth() / 3), originalBm.getHeight()));
			byteImage[2] = bitmapToByteArray(Bitmap.createBitmap(originalBm,
					(originalBm.getWidth() / 3) * 2, 0,
					(originalBm.getWidth() / 3), originalBm.getHeight()));
			break;

		}

		originalBm.recycle();
		originalBm = null;

		long end = System.currentTimeMillis();
		Log.d(TAG, "Split_Chord time : " + (end - start) / 1000.0);
	}

	//
	// private Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees) {
	//
	// if (degrees != 0 && bitmap != null) {
	// Matrix m = new Matrix();
	// m.setRotate(degrees, (float) bitmap.getWidth() / 2,
	// (float) bitmap.getHeight() / 2);
	// try {
	// Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0,
	// bitmap.getWidth(), bitmap.getHeight(), m, true);
	// if (bitmap != b2) {
	// bitmap.recycle();
	// bitmap = b2;
	// }
	// } catch (OutOfMemoryError ex) {
	// // We have no memory to rotate. Return the original bitmap.
	// }
	// }
	//
	// return bitmap;
	// }

	// 화면 크기 구하기
	public void DisplaySize() {

		DisplayMetrics display = this.getResources().getDisplayMetrics();
		int width = display.widthPixels;
		int height = display.heightPixels;
		// int Dpi = display.densityDpi;
		// float xdp = display.xdpi;
		float ydp = display.ydpi;

		// (display.widthPixels / display.xdpi) * 2.54 : 스마트폰 액정 폭(cm)
		// (display.heightPixels / display.ydpi) * 2.54 : 스마트폰 액정 높이(cm)

		// double x = Math.pow(display.widthPixels / display.xdpi, 2);
		// double y = Math.pow(display.heightPixels / display.ydpi, 2);
		// double screenInches = Math.sqrt(x + y);

		// float screenHeight = getResources().getDisplayMetrics().heightPixels;
		// // 화면사이즈가져옴
		// float screenWidth = getResources().getDisplayMetrics().widthPixels;

		//
		screenWidth = width;
		screenHeight = height;
		deviceYdp = ydp;
		smallScreenHeight = display.heightPixels / display.ydpi;
		// Log.e(TAG, "X : " + display.widthPixels / display.xdpi + "Y : "
		// + ;
		// Log.e(TAG, "screenHeight : " + screenHeight + "screenWidth : "
		// + screenWidth);
		// Log.e(TAG, "DPI : " + Dpi);
		// Log.e(TAG, "xdp : " + xdp);
		// Log.e(TAG, "ydp : " + ydp);
		// Log.e(TAG, "screenInches : " + screenInches);

	}

	public void CloseChord(boolean isPlay) {

		if (sendPlay)
			mDataSendThread.stopThread(isPlay);

	}

	// 화면사이즈 Node로 전송
	private void onNodeCallbackCommon(int interfaceType, String fromNode) {

		Log.e(TAG, "onNodeCallbackCommon");
		byte[][] payload = new byte[2][];
		String interfaceName = getInterfaceName(interfaceType);
		String stringFloat;
		DisplayMetrics display = this.getResources().getDisplayMetrics();
		SchordChannel channel = getJoinedChannelByIfcType(interfaceType);

		if (channel == null) {

			return;
		}

		try {
			stringFloat = String.valueOf((display.heightPixels / display.ydpi)); // heght

			payload[0] = "S".getBytes();
			payload[1] = stringFloat.getBytes();

			channel.sendDataToAll(CHORD_MESSAGE_TYPE, payload);

		} catch (Exception e) {
			Log.e(TAG, "dvice height send fail");
			return;
		}

	}

	class DataSendThread extends Thread implements Runnable {

		private boolean isPlay = false;
		boolean isJoin;
		boolean isFirst = true;
		int interfaceType;
		String fromNode;
		String interfaceName = getInterfaceName(interfaceType);
		byte[] ImageArray;

		public DataSendThread(boolean isJoin, int interfaceType, String fromNode) {

			isPlay = true;
			sendPlay = true;
			this.isJoin = isJoin;
			this.interfaceType = interfaceType;
			this.fromNode = fromNode;

		}

		public void isThreadState(boolean isPlay) {
			this.isPlay = isPlay;
		}

		public void stopThread(boolean isJoin) {
			isPlay = !isPlay;
			sendPlay = false;
		}

		@Override
		public void run() {
			super.run();

			while (isPlay) {

				if (isJoin) {

					if (mNodeNumberMap.containsKey(interfaceName + fromNode)) {

					} else {

						mNodeNumber++;
						mNodeNumberMap.put(interfaceName + fromNode,
								mNodeNumber);
					}

					byte[][] payload = new byte[3][];
					SchordChannel channel = getJoinedChannelByIfcType(interfaceType);

					if (channel == null) {

						Log.e(TAG, "Fail to get the joined Channel");
						return;
					}

					try {

						SplitBitmap(bitmap_image); // Image 분할

						Log.d(TAG,
								"mNodeNumberMap : "
										+ mNodeNumberMap.get(interfaceName
												+ fromNode));
						switch (phoneNumber) {

						case 1:
							switch (mNodeNumberMap
									.get(interfaceName + fromNode)) {
							case 1:
								ImageArray = byteImage[0];
								break;
							default:
								ImageArray = null;
								mDataSendThread.stopThread(false);

							}
							break;
						case 2:
							switch (mNodeNumberMap
									.get(interfaceName + fromNode)) {

							case 1:
								ImageArray = byteImage[0];
								break;
							case 2:
								ImageArray = byteImage[1];
								break;
							default:
								ImageArray = null;
								mDataSendThread.stopThread(false);
								break;
							}
							break;
						case 3:
							switch (mNodeNumberMap
									.get(interfaceName + fromNode)) {

							case 1:
								ImageArray = byteImage[0];
								break;
							case 2:
								ImageArray = byteImage[1];
								break;
							default:
								ImageArray = null;
								mDataSendThread.stopThread(false);
								break;
							}
							break;

						case 4:
							switch (mNodeNumberMap
									.get(interfaceName + fromNode)) {

							case 1:
								ImageArray = byteImage[0];
								break;
							case 2:
								ImageArray = byteImage[1];
								break;
							case 3:
								ImageArray = byteImage[2];
								break;
							default:
								ImageArray = null;
								mDataSendThread.stopThread(false);
								break;
							}
							break;
						case 5:
							switch (mNodeNumberMap
									.get(interfaceName + fromNode)) {

							case 1:
								ImageArray = byteImage[0];
								break;
							case 2:
								ImageArray = byteImage[1];
								break;
							case 3:
								ImageArray = byteImage[2];
								break;
							default:
								ImageArray = null;
								mDataSendThread.stopThread(false);
								break;
							}

							break;
						default:
							ImageArray = null;
							break;
						}

						// payload[0] = "I".getBytes();
						// payload[1] = ImageArray;

						// channel.sendDataToAll(CHORD_MESSAGE_TYPE, payload);
						// if(isFirst) {
						// payload[0] = "S".getBytes();
						// // payload[1] = Float.toString((display.heightPixels
						// / display.ydpi))
						// // .getBytes();
						// Log.e(TAG, "0 :" + payload[0].toString());
						// Log.e(TAG, "1 :" + payload[1].toString());
						// isFirst = false;
						// } else {
						payload[0] = "I".getBytes();
						payload[1] = ImageArray;
						channel.sendData(fromNode, CHORD_MESSAGE_TYPE, payload);

						// }
						// channel.sendData(fromNode, CHORD_MESSAGE_TYPE,
						// payload);

						Log.d(TAG, "Send Data length : " + payload[0].length);
						Log.d(TAG, "FromNode : " + fromNode);

					} catch (Exception e) {
						// return;
					}
				} else {
					Log.d(TAG,
							"onNodeLeft : "
									+ mNodeNumberMap.get(interfaceName
											+ fromNode));
					Log.d(TAG, "FromNode : " + fromNode);
				}

				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

}
