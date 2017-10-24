package com.atop.network;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.os.Message;
import android.util.Log;

public class FileTransfer extends Thread {
	private final String TAG = "Class::FileTranse";

	private Socket fileSocket;

	private static String server_ip;
	private static final int server_port = 6000;
	private DataInputStream fileIm;

	private String dirPath = "/sdcard/ATOP/"; // 저장될 파일 경로

	private FileOutputStream fos; // 파일 만들려고
	private BufferedOutputStream bos; // 파일만들친구

	private byte[] fileNameByte; // 파일 이름 받아오는 바이트
	private int fileNameSize; // 위의 바이트크기
	private String fileName;
	private byte[] fileSizeByte; // 파일 크기 받아오는 바이트
	private int fileSize; // 위의 바이트 크기
	private String strSize; // 받아서 디코딩해야함
	private long fileAllSize; // 파일 전체 크기

	public FileTransfer(String ip) { // 생성자.
		this.server_ip = ip;
	}

	public void run() {
		Message msg;

		try {

			fileSocket = new Socket();
			InetSocketAddress socketAddr = new InetSocketAddress(server_ip,
					server_port);
			fileSocket.connect(socketAddr, 5000);

			Log.e(TAG, "file new socket ");
			sendMessage("Open"); // 소켓이 열리는걸 알림
			while (true) {

				fileIm = new DataInputStream(fileSocket.getInputStream());

				fileNameByte = new byte[1024];

				fileNameSize = fileIm.read(fileNameByte); // 여기서 대기를 하겟지

				fileName = new String(fileNameByte, 0, fileNameSize, "UTF-8");

				Log.e(TAG, "file 이름 : " + fileName);

				File receiveFile = new File(dirPath + fileName);
				fos = new FileOutputStream(receiveFile);
				bos = new BufferedOutputStream(fos); // 파일 생성됨

				Log.e(TAG, "file 생성");

				sendMessage("Size"); // 준비됫음을알림

				fileSizeByte = new byte[1024];
				fileSize = fileIm.read(fileSizeByte);

				strSize = new String(fileSizeByte, 0, fileSize, "UTF-8");
				Log.e(TAG, "file size : " + strSize);
				fileAllSize = Long.parseLong(strSize);

				sendMessage("Ready"); // 준비됫음을알림

				long len = 0;
				int size = 4096;
				byte[] data = new byte[size];

				long cut = (fileAllSize / 4);
				long start = System.currentTimeMillis();
				while (len != fileAllSize) {
					int readSize = fileIm.read(data);
					len = len + readSize;
					if (len > cut) {
						cut = cut + len;
						bos.write(data, 0, readSize);
						bos.flush();
						continue;
					}
					Log.e(TAG, "len 길이 : " + len);
					bos.write(data, 0, readSize);
				}
				long end = System.currentTimeMillis();
				Log.e(TAG, "파일 시간 " + (end - start) / 1000.0);

				bos.flush();
				bos.close();
				fos.close(); // 파일닫기

			}

		} catch (IOException e) {

			Log.e(TAG, "Socket Connect Exception2 : " + e); // 소켓 IoException

		} catch (Exception e) { // 소켓 Exception

			Log.e(TAG, "Exception : " + e);
		} finally {
			// sendMessage("Fail");
			try {
				fileIm.close();
				Log.e(TAG, "End");
				// fileSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	} // end of run

	public void Close_Socket() {
		try {
			sendMessage("End" + "\r\n");
			fileSocket.close();
		} catch (IOException e) {
			Log.d(TAG, "Socket Close Failed");
			e.printStackTrace();
		}
	}

	public void sendMessage(String message) { // output stream

		try {
			fileSocket.getOutputStream().write(message.getBytes());
		} catch (IOException e) {
			Log.e(TAG, "패킷 전송 실패." + e);

		}
	}
}
