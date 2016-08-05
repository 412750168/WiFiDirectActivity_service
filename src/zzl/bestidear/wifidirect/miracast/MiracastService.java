package zzl.bestidear.wifidirect.miracast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MiracastService extends Service implements ChannelListener ,PeerListListener ,ConnectionInfoListener{

	public static final String TAG = "wifidirectdemo";
	private WifiP2pManager manager;
	private boolean isWifiP2pEnabled = false;
	private boolean retryChannel = false;

	private final IntentFilter intentFilter = new IntentFilter();
	private Channel channel;
	private BroadcastReceiver receiver = null;

	public static final String DNSMASQ_IP_ADDR_ACTION = "android.net.wifidns.IP_ADDR";
	public static final String DESTORY_MIRACAST_SERVICE = "zzl.bestidear.wifidirect.miracast.Service";
	public static final String CANCEL_WIFI_CONNECTED = "zzl.bestidear.wifidirect.miracast.connected";
	public static final String DNSMASQ_MAC_EXTRA = "MAC_EXTRA";
	public static final String DNSMASQ_IP_EXTRA = "IP_EXTRA";
	public static final String DNSMASQ_PORT_EXTRA = "PORT_EXTRA";
	
	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	private WifiP2pDevice device;
	public static int ctrolPort = 7236;
	//ProgressDialog progressDialog = null;
	
	private final String FB0_BLANK = "/sys/class/graphics/fb0/blank";
	private final String ipFilePath = "/data/misc/adb/dnsmasq.txt";
	private Handler handler_ip;
	
	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		this.isWifiP2pEnabled = isWifiP2pEnabled;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d("zzl:::","Miracast service is created!!");
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		intentFilter.addAction(DNSMASQ_IP_ADDR_ACTION);
		intentFilter.addAction(DESTORY_MIRACAST_SERVICE);
		intentFilter.addAction(CANCEL_WIFI_CONNECTED);
		
		handler_ip = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				new Thread() {
					@Override
					public void run() {
						// TODO Auto-generated method stub						
						parseDnsmasqAddr(ipFilePath);
					}
				}.start();
			}
		};
				

		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);
		
		receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
		registerReceiver(receiver, intentFilter);
	
		wifiStatus();
	}
	
	private void wifiStatus()
	{
		WifiManager wifimanager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if(wifimanager != null)
		{
			if(!wifimanager.isWifiEnabled())
			{
				Toast.makeText(this, "Please open wifi(p2p)!", Toast.LENGTH_LONG).show();
			}else{
				discoverWifiPeers();
			}
		}
		
	}
	private boolean discoverWifiPeers()
	{
		
		manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

			@Override
			public void onSuccess() {
			//	Toast.makeText(MiracastService.this,
			//			"Discovery Initiated", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(int reasonCode) {
			//	Toast.makeText(MiracastService.this,
			//			"Discovery Failed : " + reasonCode,
			//			Toast.LENGTH_SHORT).show();
			}
		});
		return true;
	}
	
	public void resetData() {
		peers.clear();
		discoverWifiPeers();
	}

	public void startSink(String host, String port2) {

		Intent intent = new Intent(MiracastService.this, DisplayActivity.class);
		intent.putExtra(MiracastService.DNSMASQ_IP_EXTRA, host);
		intent.putExtra(MiracastService.DNSMASQ_PORT_EXTRA, port2);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);

	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		manager.removeGroup(channel, null);
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onChannelDisconnected() {
		// TODO Auto-generated method stub
		// we will try once more
				if (manager != null && !retryChannel) {
					//Toast.makeText(this, "Channel lost. Trying again",
					//		Toast.LENGTH_LONG).show();
					resetData();
					retryChannel = true;
					manager.initialize(this, getMainLooper(), this);
				} else {
					//Toast.makeText(
					//		this,
					//		"Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
					//		Toast.LENGTH_LONG).show();
					retryChannel = false;
				}
		
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList arg0) {
		// TODO Auto-generated method stub
		peers.clear();
		peers.addAll(arg0.getDeviceList());
		if (peers.size() == 0) {
			Log.d(MiracastService.TAG, "No devices found");
			return;
		}else{
			for(int i=0;i<peers.size();i++)
				if(peers.get(i).status == WifiP2pDevice.CONNECTED)
				{
					getDeviceStatus(peers.get(i).status);
					ctrolPort = peers.get(i).wfdInfo.getControlPort();
				}
		}

		
	}
	
	private String getDeviceStatus(int deviceStatus) {
		Log.d(MiracastService.TAG, "Peer status :" + deviceStatus);
		switch (deviceStatus) {
		case WifiP2pDevice.AVAILABLE:
			return "Available";
		case WifiP2pDevice.INVITED:
		/*	progressDialog = ProgressDialog.show(this,
					"Please wait!", "Wifi direct is connecting!", true, true,
					new DialogInterface.OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {

						}
					});*/
			return "Invited";
		case WifiP2pDevice.CONNECTED:
			//if(progressDialog != null)
			//	progressDialog.dismiss();
			return "Connected";
		case WifiP2pDevice.FAILED:
			return "Failed";
		case WifiP2pDevice.UNAVAILABLE:
			return "Unavailable";
		default:
			return "Unknown";

		}
	}

	private void parseDnsmasqAddr(String fullname) {
		// TODO Auto-generated method stub
		File file = new File(fullname);
		BufferedReader reader = null;
		String mac = new String();
		String ip = new String();

		try {
			reader = new BufferedReader(new FileReader(file));
			mac = reader.readLine();
			ip = reader.readLine();
			Log.d("zzl:::", "mac" + mac + ":" + "ip" + ip);
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		Intent intent = new Intent(DNSMASQ_IP_ADDR_ACTION);
		intent.putExtra(MiracastService.DNSMASQ_IP_EXTRA, ip);
		intent.putExtra(MiracastService.DNSMASQ_PORT_EXTRA, String.valueOf(ctrolPort));
		sendBroadcast(intent);

	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo arg0) {
		// TODO Auto-generated method stub
		TimerTask task = new TimerTask() {
			public void run() {
				// execute the task
				Message msg = new Message();
				handler_ip.sendMessage(msg);
				//progressDialog.dismiss();

			}
		};
		Timer timer = new Timer();
		timer.schedule(task, 10000);
//		timer.cancel();

		//progressDialog = ProgressDialog.show(this,
		//		"Open Miracast Function", "Waiting Source data", true, true);	
		
	}
	
	private String getWifiMac(){
		WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		WifiInfo wf = wm.getConnectionInfo();
		if(wf != null)
			return wf.getMacAddress();
		return null;
	}
	
	public String setDevicename()
	{
		String deviceName = Build.MODEL+"_"+"Miracast"+" "+getWifiMac();
		return deviceName;
	}

}
