package com.almalence.opencam;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import com.almalence.opencam.cameracontroller.CameraProvider;

public class AlarmReceiver extends BroadcastReceiver
{
	private static final String		TAG							= "ALARM_RECIVER";

	private static AlarmManager		alarmMgr;
	private static PendingIntent	alarmIntent;

	private int						pauseBetweenShotsVal		= 0;
	private static long				pauseBetweenShots			= 0;
	private int						pauseBetweenShotsMeasurment	= 0;
	private static boolean			readyToTakePicture			= false;
	private static AlarmReceiver	thiz						= null;
	private static WakeLock			wakeLock					= null;

	public static AlarmReceiver getInstance()
	{
		if (thiz == null)
		{
			thiz = new AlarmReceiver();
			if (wakeLock == null)
			{
				PowerManager pm = (PowerManager) CameraScreenActivity.getInstance().getApplicationContext()
						.getSystemService(Context.POWER_SERVICE);
				wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.ON_AFTER_RELEASE, TAG);
			}
		}
		return thiz;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (wakeLock == null)
		{
			PowerManager pm = (PowerManager) CameraScreenActivity.getInstance().getApplicationContext()
					.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
					| PowerManager.ON_AFTER_RELEASE, TAG);
		}
		if (!wakeLock.isHeld())
		{
			wakeLock.acquire();
		}

		readyToTakePicture = true;

		try
		{
			if (CameraProvider.getInstance().getCamera() == null)
			{
				Intent dialogIntent = new Intent(context, CameraScreenActivity.class);
				dialogIntent.addFlags(Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
						| Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				context.startActivity(dialogIntent);

			} else
			{
				takePicture();
			}
		} catch (NullPointerException ignored){}
	}

	public void takePicture()
	{
		if (!readyToTakePicture)
		{
			return;
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CameraScreenActivity.getMainContext());

		boolean photoTimeLapseActive = prefs.getBoolean(CameraScreenActivity.sPhotoTimeLapseActivePref, false);
		boolean photoTimeLapseIsRunning = prefs.getBoolean(CameraScreenActivity.sPhotoTimeLapseIsRunningPref, false);

		if (!photoTimeLapseActive || !photoTimeLapseIsRunning)
		{
			return;
		}

		PluginManager.getInstance().onShutterClickNotUser();

		readyToTakePicture = false;
	}

	public void setNextAlarm(Context context)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CameraScreenActivity.getMainContext());

		pauseBetweenShotsVal = prefs.getInt(CameraScreenActivity.sPhotoTimeLapseCaptureIntervalPref, -1);
		if (pauseBetweenShotsVal == -1)
		{
			return;
		}

		pauseBetweenShots = 1;

		pauseBetweenShotsMeasurment = prefs.getInt(CameraScreenActivity.sPhotoTimeLapseCaptureIntervalMeasurmentPref, 0);

		switch (pauseBetweenShotsMeasurment)
		{
		case 0:
			pauseBetweenShots = pauseBetweenShots * 1000;
			break;
		case 1:
			pauseBetweenShots = pauseBetweenShots * 60000;
			break;
		case 2:
			pauseBetweenShots = pauseBetweenShots * 60000 * 60;
			break;
		default:
			break;
		}

		Editor e = prefs.edit();
		e.putInt(CameraScreenActivity.sPhotoTimeLapseCount, prefs.getInt(CameraScreenActivity.sPhotoTimeLapseCount, 0) + 1);
		e.apply();
		this.setAlarm(context, pauseBetweenShots);

		ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
		PackageManager pm = context.getPackageManager();

		pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);

		if (wakeLock.isHeld())
			wakeLock.release();
	}

	@SuppressLint("NewApi")
	public void setAlarm(Context context, long intervalMillis)
	{
		alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		if (Build.VERSION.SDK_INT < 19)
		{
			alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + intervalMillis, alarmIntent);
		} else
		{
			alarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + intervalMillis, alarmIntent);
		}
	}

	public static void cancelAlarm(Context context)
	{
		if (alarmMgr != null && alarmIntent != null)
		{
			alarmMgr.cancel(alarmIntent);
		}

		if (wakeLock.isHeld())
			wakeLock.release();
	}
}