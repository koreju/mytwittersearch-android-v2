package com.example.mytwittersearch.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mytwittersearch.R;
import com.example.mytwittersearch.adapter.JsonAdapter;
import com.example.mytwittersearch.database.DatabaseMetaData;
import com.example.mytwittersearch.manager.TweetFragmentManager;
import com.example.mytwittersearch.manager.TweetListManager;
import com.example.mytwittersearch.model.Tweet;
import com.example.mytwittersearch.operation.DownloadTask.OnDownloadFinishedListener;
import com.example.mytwittersearch.utils.ConstantValues;

public class TweetListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor>, OnDownloadFinishedListener {

	private EditText mKeywordText = null;
	private ImageButton mSearchBtn = null;
	private TextView mTweetHeader = null;
	private InputMethodManager mInputMethodManager = null;

	private JsonAdapter mJsonAdapter = null;

	private CursorLoader mCursorLoader = null;
	private OnTweetSelectedListener mTweetSelectedListener = null;
	private Intent mIntent = null;

	public interface OnTweetSelectedListener {
		public void onTweetSelected(int position);
	}

	private OnClickListener mSearchHandler = new OnClickListener() {
		@Override
		public void onClick(View v) {
			onSearchStart();
		}
	};

	private OnKeyListener mEnterKeyHandler = new OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ENTER) {
				onSearchStart();
				return true;
			}
			return false;
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented the
		// callback interface. If not, it throws and exception.
		try {
			mTweetSelectedListener = (OnTweetSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnTweetSelectedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tweet_list, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initialize();
	}

	@Override
	public void onStart() {
		super.onStart();

		// When in two-pane layout, set the listview to highlight the selected
		// list item.
		// (We do this during onStart() because at the point the listview is
		// available.)
		if (getFragmentManager().findFragmentById(R.id.tweet_detail_fragment) != null) {
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
	}

	@Override
	public void onDestroy() {
		stopBackgroundDownloadService();
		super.onDestroy();
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);
		mTweetSelectedListener.onTweetSelected(position);
		getListView().setItemChecked(position, true);
	}

	@SuppressLint("NewApi")
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		mCursorLoader = new CursorLoader(getActivity().getApplicationContext(),
				DatabaseMetaData.TweetTableMetaData.CONTENT_URI, null, null,
				null, DatabaseMetaData.TweetTableMetaData.Columns.CREATED_AT
						+ " DESC");
		return mCursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		TweetListManager.getInstance().clear();
		while (cursor.moveToNext()) {
			Tweet tweet = new Tweet(
					cursor.getString(cursor
							.getColumnIndex(DatabaseMetaData.TweetTableMetaData.Columns.FROM_USER)),
					cursor.getString(cursor
							.getColumnIndex(DatabaseMetaData.TweetTableMetaData.Columns.CREATED_AT)),
					cursor.getString(cursor
							.getColumnIndex(DatabaseMetaData.TweetTableMetaData.Columns.IMAGE_URL)),
					cursor.getString(cursor
							.getColumnIndex(DatabaseMetaData.TweetTableMetaData.Columns.TEXT)));
			TweetListManager.getInstance().add(tweet);
		}
		mJsonAdapter.swapCursor(cursor);
		mTweetHeader.setText(cursor.getCount() + " tweets");
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mJsonAdapter.swapCursor(null);
		TweetListManager.getInstance().clear();
	}

	@Override
	public void onDownloadFinished() {
		if (getLoaderManager().getLoader(ConstantValues.LOADER_ID) == null) {
			getLoaderManager().initLoader(ConstantValues.LOADER_ID, null, this);
		} else {
			getLoaderManager().restartLoader(ConstantValues.LOADER_ID, null,
					this);
		}
	}

	private void initialize() {
		mKeywordText = (EditText) getActivity().findViewById(R.id.search_key);
		mSearchBtn = (ImageButton) getActivity().findViewById(R.id.btn_search);
		mTweetHeader = (TextView) getActivity().findViewById(R.id.tweet_header);

		mKeywordText.setOnKeyListener(mEnterKeyHandler);
		mSearchBtn.setOnClickListener(mSearchHandler);

		mInputMethodManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		mJsonAdapter = new JsonAdapter(getActivity(), null);
		setListAdapter(mJsonAdapter);
		TweetFragmentManager.getInstance().setFragment(this);
	}

	private void onSearchStart() {
		// Hide the soft keyboard
		mInputMethodManager.hideSoftInputFromWindow(
				mKeywordText.getWindowToken(), 0);
		String searchUrl = "?rpp=" + ConstantValues.NUM_TWEETS_PER_FETCH
				+ "&q=" + Uri.encode(mKeywordText.getText().toString());
		stopBackgroundDownloadService();
		startBackgroundDownloadService(searchUrl);
	}

	private void startBackgroundDownloadService(String searchUrl) {
		mIntent = new Intent();
		mIntent.setAction(getResources().getString(R.string.download_service));
		mIntent.putExtra(ConstantValues.SEARCH_URL, searchUrl);
		getActivity().startService(mIntent);
	}

	private void stopBackgroundDownloadService() {
		if (mIntent != null) {
			getActivity().stopService(mIntent);
		}
	}
}
