package com.example.home.kitchentimersample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
	long _alarmTime;
	Timer _timer;
	MediaPlayer _mediaPlayer;
	Handler _handler = new Handler();

	@Override
	public void onCreate() {
		super.onCreate();
		_mediaPlayer = MediaPlayer.create(this, R.raw.se_maoudamashii_jingle04);
		_mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				stopSelf();
			}
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (_timer != null)
			_timer.cancel();
		clearMediaPlayer();

		_alarmTime = calcAlarmTime(intent);
		startNotification();

		_timer = new Timer();
		_timer.schedule(new MyTask(), 0/*Delay*/, 200/*Interval*/);

		return START_STICKY;	// システムにkillされてもすぐに復活させる
	}

	private void clearMediaPlayer() {
		if (_mediaPlayer.isPlaying()) {
			_mediaPlayer.pause();
			_mediaPlayer.seekTo(0);
		}
	}

	private long calcAlarmTime(Intent intent) {
		ArrayList<String> timeAry = intent.getStringArrayListExtra("time");
		String min = timeAry.get(0) + timeAry.get(1);
		String sec = timeAry.get(2) + timeAry.get(3);
		long lMin = Long.parseLong(min) * 60 * 1000;
		long lSec = Long.parseLong(sec) * 1000;
		return (System.currentTimeMillis() + lMin + lSec);
	}

	private void startNotification() {
		Notification notification = buildNotification();

		// 通知領域を管理
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(1, notification);
		startForeground(1, notification);
	}

	private Notification buildNotification() {
		Intent intent = new Intent(this, MainActivity.class);
		// notificationはいつ使われるかわからないので、pendingIntentを待機させておく
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

		return new NotificationCompat.Builder(this)
				.setTicker("タイマー起動しました")		// 最初に出るメッセージ
				.setContentTitle("キッチンタイマー")
				.setContentText("タイマー作動中")
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentIntent(pendingIntent)
				.build();
	}

	class MyTask extends TimerTask {
		@Override
		public void run() {
			long remainingTime = _alarmTime - System.currentTimeMillis();

			Intent broadCastIntent = new Intent();
			broadCastIntent.putExtra("time", remainingTime);
			broadCastIntent.setAction("com.example.home.kitchentimersample");
			sendBroadcast(broadCastIntent);

			if (remainingTime >= 0)
				return;
			ringAlarm();
		}
	}

	private void ringAlarm() {
		_timer.cancel();
		_mediaPlayer.start();
		_handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "時間です", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_timer.cancel();
		_mediaPlayer.release();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
