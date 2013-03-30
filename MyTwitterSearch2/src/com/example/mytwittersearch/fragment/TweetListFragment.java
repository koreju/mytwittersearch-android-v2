package com.example.mytwittersearch.fragment;

import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
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
import android.widget.Toast;

import com.example.mytwittersearch.R;
import com.example.mytwittersearch.adapter.JsonAdapter;
import com.example.mytwittersearch.database.DatabaseMetaData;
import com.example.mytwittersearch.operation.DownloadTask;
import com.example.mytwittersearch.utils.ConstantValues;

public class TweetListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private EditText mKeywordText = null;
	private ImageButton mSearchBtn = null;
	private TextView mTweetHeader = null;
	private InputMethodManager mInputMethodManager = null;

	private JsonAdapter mJsonAdapter = null;

	private ContentResolver mContentResolver = null;
	private Cursor mCursor = null;
	private OnTweetSelectedListener mTweetSelectedListener = null;

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
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);
		mTweetSelectedListener.onTweetSelected(position);
		getListView().setItemChecked(position, true);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mJsonAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mJsonAdapter.swapCursor(null);
	}
	
	private void initialize() {
		mKeywordText = (EditText) getActivity().findViewById(R.id.search_key);
		mSearchBtn = (ImageButton) getActivity().findViewById(R.id.btn_search);
		mTweetHeader = (TextView) getActivity().findViewById(R.id.tweet_header);

		mKeywordText.setOnKeyListener(mEnterKeyHandler);
		mSearchBtn.setOnClickListener(mSearchHandler);

		mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

		mJsonAdapter = new JsonAdapter(getActivity(), mCursor);
		setListAdapter(mJsonAdapter);

		getLoaderManager().initLoader(0, null, this);
		mContentResolver = getActivity().getContentResolver();
		mContentResolver.registerContentObserver(
				Uri.parse("content://" + DatabaseMetaData.AUTHORITY), true,
				new TweetObserver(new Handler()));
	}

	private void onSearchStart() {
		// Hide the soft keyboard
		mInputMethodManager.hideSoftInputFromWindow(
				mKeywordText.getWindowToken(), 0);
		DownloadTask downloadTask = new DownloadTask(mContentResolver);
		downloadTask.execute("?rpp=50&q="
				+ Uri.encode(mKeywordText.getText().toString()));
		try {
			downloadTask.get(ConstantValues.TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			downloadTask.cancel(true);
			alert("Cannot retrieve tweets...");
		}
		Toast.makeText(getActivity().getApplicationContext(), mKeywordText.getText(),
				Toast.LENGTH_LONG).show();
	}

	private void alert(String message) {
		Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG)
				.show();
	}

	private final class TweetObserver extends ContentObserver {

		public TweetObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			mCursor = mContentResolver.query(
					DatabaseMetaData.TweetTableMetaData.CONTENT_URI, null,
					null, null, null);
			mTweetHeader.setText(mCursor.getCount() + " tweets");
			mJsonAdapter.swapCursor(mCursor);
		}
	}
}
