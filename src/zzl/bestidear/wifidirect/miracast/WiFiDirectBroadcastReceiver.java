/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zzl.bestidear.wifidirect.miracast;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import zzl.bestidear.wifidirect.miracast.R;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

	private WifiP2pManager manager;
	private Channel channel;
	//private WiFiDirectActivity activity;
	private MiracastService service;

	/**
	 * @param manager
	 *            WifiP2pManager system service
	 * @param channel
	 *            Wifi p2p channel
	 * @param activity
	 *            activity associated with the receiver
	 */
	public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
			 MiracastService service) {
		super();
		this.manager = manager;
		this.channel = channel;
		this.service = service;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

			// UI update to indicate wifi p2p status.
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				// Wifi Direct mode is enabled
				service.setIsWifiP2pEnabled(true);
			} else {
				service.setIsWifiP2pEnabled(false);
				service.resetData();
			}
			
			WifiP2pWfdInfo wfdInfo = new WifiP2pWfdInfo();
			wfdInfo.setWfdEnabled(true);
			wfdInfo.setDeviceType(WifiP2pWfdInfo.PRIMARY_SINK);
			wfdInfo.setSessionAvailable(true);
		//	wfdInfo.setControlPort(7236);
			wfdInfo.setMaxThroughput(50);

			manager.setWFDInfo(channel, wfdInfo, new ActionListener() {

				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
					Log.d("zzl:::", "wifi-sink" + "success");
				}

				@Override
				public void onFailure(int arg0) {
					// TODO Auto-generated method stub
				}
			});
			
			String devName = service.setDevicename();
			manager.setDeviceName(channel, devName, new ActionListener() {
				
				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onFailure(int arg0) {
					// TODO Auto-generated method stub
					
				}
			});
			
			Log.d(MiracastService.TAG, "P2P state changed - " + state);
		
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

			// request available peers from the wifi p2p manager. This is an
			// asynchronous call and the calling activity is notified with a
			// callback on PeerListListener.onPeersAvailable()
			if (manager != null) {
				manager.requestPeers(channel, (PeerListListener)service);
			}
			Log.d(MiracastService.TAG, "P2P peers changed");
			
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
				.equals(action)) {

			if (manager == null) {
				return;
			}

			NetworkInfo networkInfo = (NetworkInfo) intent
					.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

			if (networkInfo.isConnected()) {

				// we are connected with the other device, request connection
				// info to find group owner IP
				manager.requestConnectionInfo(channel, service);

			} else {
				// It's a disconnect
				service.resetData();
			}
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
				.equals(action)) {
			
		} else if (MiracastService.DNSMASQ_IP_ADDR_ACTION.equals(action)) {
			String ip = intent
					.getStringExtra(MiracastService.DNSMASQ_IP_EXTRA);
			String port = intent
					.getStringExtra(MiracastService.DNSMASQ_PORT_EXTRA);
			Log.d("zzl:::", "Get ¶Ô·½µÄ ip and port ::" + ip + "   " + port);

			service.startSink(ip, port);
		} else if(MiracastService.DESTORY_MIRACAST_SERVICE.equals(action)){
			manager.removeGroup(channel, null);
			manager.cancelConnect(channel, null);
			manager.clearLocalServices(channel, null);
			manager.clearServiceRequests(channel, null);
			service.stopSelf();
			
		} else if(MiracastService.CANCEL_WIFI_CONNECTED.equals(action)){
			manager.removeGroup(channel, null);
		}
		
	}
}
