package com.example.home.kitchentimersample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

	MyReceiver _receiver;
	TextView _tvTime;
	ArrayList<String> _strTimeAry = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		_tvTime = (TextView) findViewById(R.id.textView);
		clearStrTime();

		setButtonStartListener();
		setButtonStopListener();
	}

	private void setButtonStopListener() {
		Button btnStop = (Button) findViewById(R.id.buttonStop);
		btnStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), MyService.class);
				stopService(intent);
			}
		});
	}

	private void setButtonStartListener() {
		Button btnStart = (Button) findViewById(R.id.buttonStart);
		btnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), MyService.class);
				intent.putStringArrayListExtra("time", _strTimeAry);
				startService(intent);
			}
		});
	}

	private void clearStrTime() {
		for (int i = 0; i < 4; i++)
			_strTimeAry.add("0");
	}

	@Override
	protected void onResume() {
		super.onResume();
		_receiver = new MyReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.example.home.kitchentimersample");
		registerReceiver(_receiver, intentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(_receiver);
	}

	public void numClick(View view) {
		Button bt = (Button) view;
		_strTimeAry.add(bt.getText().toString());
		_strTimeAry.remove(0);
		_tvTime.setText(_strTimeAry.get(0) + _strTimeAry.get(1) + "分" + _strTimeAry.get(2) + _strTimeAry.get(3) + "秒");
	}

	class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			long remainingTime = intent.getLongExtra("time", 0);
			long min = remainingTime / (1000 * 60);
			long sec = remainingTime % (1000 * 60) / 1000;
			_tvTime.setText(min + "分" + sec + "秒");
		}
	}
}
