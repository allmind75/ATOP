package com.atop.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.graphics.Bitmap;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

public class NetworkTCP extends Thread {
	private final String TAG = "Class::NetworkTCP";
	private final String ProtocalFN = "특";
	private Socket socket;

	private static String server_ip;
	private static final int server_port = 8000;

	private Bitmap bitmap; // 비트맵만들어짐
	private byte[] data;
	private String outData;
	private InputStream im;
	private BufferedReader br;
	private boolean socketConnect = false;

	private boolean connectFileSocket = false;
	private FileTransfer filetransfer;

	public NetworkTCP(String ip) { // 생성자.
		this.server_ip = ip;

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
			Log.e(TAG, " tcp new socket ");

			im = socket.getInputStream();
			br = new BufferedReader(new InputStreamReader(im));

			while (socketConnect) { // 여기서 file 이라는 걸 받을꺼야

				outData = br.readLine();

				outData = new String(Base64.decode(outData, 0));
				Log.e(TAG, "file : " + outData);

				if (outData.equalsIgnoreCase("File")) {
					if (!connectFileSocket) {

						filetransfer = new FileTransfer(server_ip);
						filetransfer.start();
						connectFileSocket = true;
					} else {
						filetransfer.sendMessage("Open"); // 소켓이 열리는걸 알림
						Log.e(TAG, "Open");
					}
				}

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

	public void sendMessage(String message) { // output stream

		try {
			socket.getOutputStream().write(message.getBytes());
		} catch (IOException e) {
			Log.e(TAG, "패킷 전송 실패." + e);

		}
	}

	public void Close_Socket() {
		try {
			if (connectFileSocket)
				filetransfer.Close_Socket();
			socketConnect = false;
			sendMessage(ProtocalFN + "quit" + "\r\n");
			socket.close();
		} catch (IOException e) {
			Log.d(TAG, "Socket Close Failed");
			e.printStackTrace();
		}
	}

}
