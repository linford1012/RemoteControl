package server.yf.com.remotecontrolserver_as.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import server.yf.com.remotecontrolserver_as.ui.serice.MouseService;

public class RemoteControlMouseKeyReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		Intent newIntent = new Intent(context, MouseService.class);
		context.startService(newIntent);
		Log.i("RemoteControlMouseKeyReceiver", "启动");
		//String act =intent.getAction(); 
//		Log.d("RemoteControlMouseKeyReceiver","1onReceive...............................");
//		if (act.equals("com.yf.server.ui.service.MouseService.start")) {
//			Log.d("RemoteControlMouseKeyReceiver","2onReceive...............................");
			
//		}
//			 com.yf.server.ui.service.MouseService.start
	}
}