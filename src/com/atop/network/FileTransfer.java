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

	private String dirPath = "/sdcard/ATOP/"; // ����� ���� ���

	private FileOutputStream fos; // ���� �������
	private BufferedOutputStream bos; // ���ϸ���ģ��

	private byte[] fileNameByte; // ���� �̸� �޾ƿ��� ����Ʈ
	private int fileNameSize; // ���� ����Ʈũ��
	private String fileName;
	private byte[] fileSizeByte; // ���� ũ�� �޾ƿ��� ����Ʈ
	private int fileSize; // ���� ����Ʈ ũ��
	private String strSize; // �޾Ƽ� ���ڵ��ؾ���
	private long fileAllSize; // ���� ��ü ũ��

	public FileTransfer(String ip) { // ������.
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
			sendMessage("Open"); // ������ �����°� �˸�
			while (true) {

				fileIm = new DataInputStream(fileSocket.getInputStream());

				fileNameByte = new byte[1024];

				fileNameSize = fileIm.read(fileNameByte); // ���⼭ ��⸦ �ϰ���

				fileName = new String(fileNameByte, 0, fileNameSize, "UTF-8");

				Log.e(TAG, "file �̸� : " + fileName);

				File receiveFile = new File(dirPath + fileName);
				fos = new FileOutputStream(receiveFile);
				bos = new BufferedOutputStream(fos); // ���� ������

				Log.e(TAG, "file ����");

				sendMessage("Size"); // �غ�������˸�

				fileSizeByte = new byte[1024];
				fileSize = fileIm.read(fileSizeByte);

				strSize = new String(fileSizeByte, 0, fileSize, "UTF-8");
				Log.e(TAG, "file size : " + strSize);
				fileAllSize = Long.parseLong(strSize);

				sendMessage("Ready"); // �غ�������˸�

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
					Log.e(TAG, "len ���� : " + len);
					bos.write(data, 0, readSize);
				}
				long end = System.currentTimeMillis();
				Log.e(TAG, "���� �ð� " + (end - start) / 1000.0);

				bos.flush();
				bos.close();
				fos.close(); // ���ϴݱ�

			}

		} catch (IOException e) {

			Log.e(TAG, "Socket Connect Exception2 : " + e); // ���� IoException

		} catch (Exception e) { // ���� Exception

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
			Log.e(TAG, "��Ŷ ���� ����." + e);

		}
	}
}
