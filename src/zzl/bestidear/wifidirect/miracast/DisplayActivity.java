package zzl.bestidear.wifidirect.miracast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import zzl.bestidear.wifidirect.miracast.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class DisplayActivity extends Activity {

	private SurfaceView surfaceview;
	private SurfaceHolder surfaceholder;

	private SinkPlayer sinkPlayer;
	private String ip;
	private String port;
	private WakeLock mWakeLock;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.display_activity);
		
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Miracast");
		
		surfaceview = (SurfaceView) findViewById(R.id.surfaceview);
		surfaceholder = surfaceview.getHolder();
		surfaceholder.addCallback(new Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void surfaceCreated(SurfaceHolder arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}
		});

		IntentFilter intentFilter = new IntentFilter(
				WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		registerReceiver(mReceiver, intentFilter);

		Intent intent = getIntent();

		ip = intent.getStringExtra(MiracastService.DNSMASQ_IP_EXTRA);
		port = intent.getStringExtra(MiracastService.DNSMASQ_PORT_EXTRA);

		setSystemUIStatusHide(true);
		Log.d("zzl:::", "DisplayActivity" + ip + port);

		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				sinkPlayer = new SinkPlayer();
				sinkPlayer.setHostAndPort(ip, Integer.parseInt(port));
				setSinkParameters(true);
				sinkPlayer.startRtsp();
			}
		}.start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		setSystemUIStatusHide(false);
		setSinkParameters(false);
		if (sinkPlayer != null)
		sinkPlayer.stopRtsp();
		unregisterReceiver(mReceiver);
		Intent intent = new Intent(MiracastService.CANCEL_WIFI_CONNECTED);
		sendBroadcast(intent);
		finish();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
		{
			Log.d("zzl:::", "back/home key up");
			if (sinkPlayer != null)
				sinkPlayer.stopRtsp();
			finish();
		}
		default:
			break;
		}
		return super.onKeyUp(keyCode, event);
		
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action
					.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)) {
				NetworkInfo networkInfo = (NetworkInfo) intent
						.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

				if (!networkInfo.isConnected()) {
					if (sinkPlayer != null)
						sinkPlayer.stopRtsp();
					finish();
				}
			}
		}
	};

	private void setSinkParameters(boolean start) {
		if (start) {
			writeSysfs("/sys/class/vfm/map", "rm default");
			writeSysfs("/sys/class/vfm/map", "add default decoder amvideo");
		} else {
			writeSysfs("/sys/class/vfm/map", "rm default");
			writeSysfs("/sys/class/vfm/map",
					"add default decoder ppmgr amvideo");
		}
	}

	public static int writeSysfs(String path, String val) {
		if (!new File(path).exists()) {
			return 1;
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(path), 64);
			try {
				if(writer != null)
					writer.write(val);
			} finally {
				if(writer != null)
					writer.close();
			}
			return 0;

		} catch (IOException e) {
			return 1;
		}
	}

	private void setSystemUIStatusHide(boolean hide) {
		SystemProperties.set("vplayer.hideStatusBar.enable",
				(hide == true ? "true" : "false"));
		if(hide == true){
			mWakeLock.acquire();
		}else{
			if(mWakeLock.isHeld())
				mWakeLock.release();
		}
	}

}
