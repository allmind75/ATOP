package com.atop.network;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.zip.GZIPInputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.util.Log;

import com.atop.chord.ChordFragment;

public class NetworkUDP_Image extends Thread {
	private final String TAG = "Class::NetworkUDP_Image";

	private DatagramSocket socket;

	private static String server_ip;
	private static final int server_port = 2000;
	private DatagramPacket receiveImage;
	private int bufferSize = 60000;
	private boolean isconnet = false; // 나중에 연결됫는지 확인하려구
	private Bitmap bitmap;
	private byte[] receivebuffer;
	byte[] returnBuffer;
	private ChordFragment chord;
	private boolean gzipSuccess = true;

	public NetworkUDP_Image(ChordFragment chord, String ip) { // 생성자.
		this.server_ip = ip;
		this.chord = chord;
		try {
			socket = new DatagramSocket(server_port);
		} catch (SocketException e) {

			Log.e(TAG, "���ϻ��� ����" + e);
		}
	}

	public boolean isConnet() {
		return isconnet;

	}

	public void run() {
		Message msg;

		try {

			isconnet = true;

			receivebuffer = new byte[bufferSize];

			while (isconnet) {
				long start = System.currentTimeMillis();
				receivebuffer = new byte[bufferSize];

				receiveImage = new DatagramPacket(receivebuffer,
						receivebuffer.length);

				int size = 0;
				socket.receive(receiveImage);

				size = receiveImage.getLength();

				byte[] buffer = new byte[size];

				System.arraycopy(receivebuffer, 0, buffer, 0, size);

				// Log.e(TAG, "받은크기 : " + size);

				try {
					returnBuffer = decompress(buffer); // GZip 압축 해제.
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

					Thread.sleep(100);
					
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

		} catch (Exception e) {
			isconnet = false;
			Log.e(TAG, "Exception : " + e);
		} finally {
			isconnet = false;
			// socket.close();
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

	public void Close_Socket() {
		try {
			// tcp.sendMessage("quit" + "\r\n");
			isconnet = false;
			socket.close();
		} catch (Exception e) {
			Log.d(TAG, "Socket Close Failed");
			e.printStackTrace();
		}
	}
}
