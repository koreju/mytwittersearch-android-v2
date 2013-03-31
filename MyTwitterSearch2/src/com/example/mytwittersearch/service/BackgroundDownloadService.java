package com.example.mytwittersearch.service;

import java.util.concurrent.TimeUnit;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.example.mytwittersearch.operation.DownloadTask;
import com.example.mytwittersearch.utils.ConstantValues;

public class BackgroundDownloadService extends IntentService {

	private boolean mQuit = false;

	public BackgroundDownloadService() {
		super("DownloadIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean isNewDownload = true;
		while (!mQuit) {
			Log.v(ConstantValues.LOG_TAG,
					"--- Starting fetching new tweets ---");
			DownloadTask downloadTask = new DownloadTask(isNewDownload);
			downloadTask.execute(intent
					.getStringExtra(ConstantValues.SEARCH_URL));
			try {
				downloadTask.get(ConstantValues.TIMEOUT, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				downloadTask.cancel(true);
			}
			Log.v(ConstantValues.LOG_TAG, "--- Waiting for "
					+ ConstantValues.DOWNLOAD_INTERVAL
					+ " ms to start fetching new tweets ---");
			SystemClock.sleep(ConstantValues.DOWNLOAD_INTERVAL);
			isNewDownload = false;
		}
	}

	@Override
	public void onDestroy() {
		mQuit = true;
		super.onDestroy();
	}
}
