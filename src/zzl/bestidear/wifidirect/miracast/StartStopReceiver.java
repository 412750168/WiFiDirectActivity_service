package zzl.bestidear.wifidirect.miracast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

public class StartStopReceiver extends BroadcastReceiver {

	private int flag; // 0:close 1:open
	private static final String DEBUG = "zzl:::";
	private final static int CLOSE = 0;
	private final static int OPEN = 1;
	public final static String STARTSTOPRECEIVER = "net.bestidear.miracast.action.CONTROL";
	public final static String GLOBAL_MIRACAST_ON = "miracast_on";

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub

		Log.d("zzl:::", "startstopmiracast");

		if (arg1.getAction().equals(STARTSTOPRECEIVER)) {
			flag = arg1.getIntExtra("flag", CLOSE);

			onFlag(flag, arg0);
		}

		if (arg1.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

			try {
				flag = Settings.Global.getInt(arg0.getContentResolver(),
						GLOBAL_MIRACAST_ON);
			} catch (SettingNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			onFlag(flag, arg0);

		}

	}

	private void onFlag(int flag, Context context) {

		if (flag == OPEN) {
			Intent start_intent = new Intent(context, MiracastService.class);
			Log.d(DEBUG, "Miracast-receive start");
			context.startService(start_intent);
		} else {

			Intent stop_intent = new Intent(
					MiracastService.DESTORY_MIRACAST_SERVICE);
			context.sendBroadcast(stop_intent);

		}
	}
}
