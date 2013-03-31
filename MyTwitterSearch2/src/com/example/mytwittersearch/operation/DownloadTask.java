package com.example.mytwittersearch.operation;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import com.example.mytwittersearch.database.DatabaseMetaData;
import com.example.mytwittersearch.json.JsonParser;
import com.example.mytwittersearch.manager.TweetFragmentManager;
import com.example.mytwittersearch.manager.TweetListManager;
import com.example.mytwittersearch.network.NetworkRequestExecutor;
import com.example.mytwittersearch.network.NetworkRequestExecutor.DownloadException;
import com.example.mytwittersearch.utils.ConstantValues;

public class DownloadTask extends AsyncTask<String, Void, String> {

	public interface OnDownloadFinishedListener {
		public void onDownloadFinished();
	}

	private boolean mIsNewDownload = false;

	public DownloadTask(boolean isNewDownload) {
		super();
		mIsNewDownload = isNewDownload;
	}

	@Override
	protected String doInBackground(String... params) {
		try {
			return NetworkRequestExecutor.downloadFromServer(params[0]);
		} catch (DownloadException e) {
			return "";
		}
	}

	@Override
	protected void onPostExecute(String result) {
		if (TweetFragmentManager.getInstance().getFragment() == null) {
			Log.e(ConstantValues.LOG_TAG,
					"onPostExecute(): ContentResolver is null");
			return;
		}

		if (result == null || result.length() == 0) {
			Log.e(ConstantValues.LOG_TAG,
					"onPostExecute(): Result is null/empty");
			return;
		}

		// Load data into the tweet list
		JsonParser.parseJson(result);
		// Insert into the database
		ContentValues[] values = TweetListManager.getInstance()
				.toContentValues();
		ContentResolver contentResolver = TweetFragmentManager.getInstance()
				.getFragment().getActivity().getContentResolver();
		if (mIsNewDownload) {
			// If it's a new search, clear the table
			contentResolver
					.delete(DatabaseMetaData.TweetTableMetaData.CONTENT_URI,
							null, null);
		}
		// Insert new data
		contentResolver.bulkInsert(
				DatabaseMetaData.TweetTableMetaData.CONTENT_URI, values);
		TweetFragmentManager.getInstance().getFragment().onDownloadFinished();
	}
}
