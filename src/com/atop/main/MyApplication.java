package com.atop.main;

import android.app.Application;

import com.atop.chord.ChordFragment;
import com.atop.network.NetworkTCP;
import com.atop.network.NetworkTCP_Image;
import com.atop.network.NetworkUDP_Image;

public class MyApplication extends Application {

	private NetworkTCP tcpclient;
	private NetworkUDP_Image udp_image;
	private ChordFragment mChord;
	private String serverIP;
	private int DeviceNum = 1;
	private boolean isCall;

	public boolean getCall() {
		return isCall;
	}

	public void setCall(boolean call) {
		this.isCall = call;
	}

	public void setTCP(NetworkTCP tcp) {
		this.tcpclient = tcp;
	}

	public NetworkTCP getTCP() {
		return tcpclient;
	}

	public void setImageUDP(NetworkUDP_Image image) {
		this.udp_image = image;
	}

	public NetworkUDP_Image getImageUDP() {
		return udp_image;
	}

	public void setIP(String ip) {
		this.serverIP = ip;
	}

	public String getIP() {
		return serverIP;
	}

	public void setChord(ChordFragment mChord) {
		this.mChord = mChord;
	}

	public ChordFragment getChord() {
		return mChord;
	}

	public void setDeviceNum(int DeviceNum) {
		this.DeviceNum = DeviceNum;
	}

	public int getDeviceNum() {
		return DeviceNum;
	}

}
