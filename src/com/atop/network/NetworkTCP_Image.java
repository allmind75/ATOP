package com.atop.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.zip.GZIPInputStream;

import com.atop.chord.ChordFragment;

import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

public class NetworkTCP_Image extends Thread {
	private final String TAG = "Class::NetworkTCP_Image";

	private Socket socket;

	private static String server_ip;
	private static final int server_port = 7000;

	private Bitmap bitmap; // 비트맵만들어짐
	private byte[] outdata;
	private byte[] byteSize;
	private String sizeString;
	private int intSize;
	private InputStream im;
	private boolean socketConnect = false;
	private boolean gzipSuccess = false;
	private byte[] returnBuffer;
	private ChordFragment chord;
	private ByteArrayOutputStream bos;

	public NetworkTCP_Image(ChordFragment chord, String ip) { // 생성자.
		this.server_ip = ip;
		this.chord = chord;
	}

	public boolean istcpConnet() {
		return socketConnect;
	}

	public void run() {
		Message msg;

		try {
			socket = new Socket();
			InetSocketAddress socketAddr = new InetSocketAddress(server_ip,
					server_port);
			socket.connect(socketAddr, 5000);

			socketConnect = true;
			Log.e(TAG, " tcp image new socket ");

			im = socket.getInputStream();

			while (socketConnect) { // 여기서 file 이라는 걸 받을꺼야
				long start = System.currentTimeMillis();
				gzipSuccess = true;
				sendMessage("Size");

				byteSize = new byte[100];

				intSize = im.read(byteSize); // 여기서 대기를 하겟지

				sizeString = new String(byteSize, 0, intSize, "UTF-8");

				intSize = Integer.parseInt(sizeString);

				sendMessage("Data");

				int len = 0;
				int size = 7168;
				byte[] data = new byte[size];

				bos = new ByteArrayOutputStream();

				while (len < intSize) {
					int readSize = im.read(data);
					len = len + readSize;
					bos.write(data, 0, readSize);
				}

				outdata = bos.toByteArray();

				bos.flush();
				bos.close();

				try {
					returnBuffer = decompress(outdata); // GZip 압축 해제.
				} catch (Exception e) {
					gzipSuccess = false;
					Log.e(TAG, "gzip 실패");
				}

				if (returnBuffer == null) {
					gzipSuccess = false;
					Log.e(TAG, "gzip 실패");
				}

				if (gzipSuccess) {

					// Log.e(TAG, "압축풀어서 받은크기 " + returnBuffer.length);

					bitmap = BitmapFactory.decodeByteArray(returnBuffer, 0,
							returnBuffer.length);
					if (bitmap == null) {
						Log.e(TAG, "bitmap null");
					}
					if (chord == null) {
						Log.e(TAG, "chord null");
					} else {
						chord.setThreadimage(bitmap);
						// chord.setThreadimage2(returnBuffer, true);

					}
				} else {
					gzipSuccess = true;
				}

				long end = System.currentTimeMillis();
				Log.e(TAG, "사진 한장 받아서 그리는 시간 " + (end - start) / 1000.0);

			}
		} catch (IOException e) {
			socketConnect = false;
			Log.e(TAG, "Socket Connect Exception2 : " + e); // 소켓 IoException

		} catch (Exception e) { // 소켓 Exception
			socketConnect = false;
			Log.e(TAG, "Exception : " + e);
		} finally { // 소켓 ㅂㅇ
			try {
				sendMessage("quit" + "\r\n");
				socketConnect = false;
				socket.close();
			} catch (IOException e) {
				Log.e(TAG, "socket close : " + e);
			}
		}
	} // end of run

	public byte[] decompress(byte[] value) {

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		GZIPInputStream gzipInStream;
		try {
			gzipInStream = new GZIPInputStream(new BufferedInputStream(
					new ByteArrayInputStream(value)));
			int size = 0;
			byte[] buffer = new byte[1024];
			while ((size = gzipInStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, size);
			}
			outStream.flush();
			outStream.close();
			gzipInStream.close();

		} catch (IOException e) {
			Log.e(TAG, "gzip 오류 : " + e);
			return null;
		}

		return outStream.toByteArray();
	}

	public void sendMessage(String message) { // output stream

		try {
			socket.getOutputStream().write(message.getBytes());
		} catch (IOException e) {
			Log.e(TAG, "패킷 전송 실패." + e);

		}
	}

	public void Close_Socket() {
		try {
			socketConnect = false;
			sendMessage("quit" + "\r\n");
			socket.close();
		} catch (IOException e) {
			Log.d(TAG, "Socket Close Failed");
			e.printStackTrace();
		}
	}

}
