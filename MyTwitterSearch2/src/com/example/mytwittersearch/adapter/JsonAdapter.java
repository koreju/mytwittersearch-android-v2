package com.example.mytwittersearch.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mytwittersearch.R;
import com.example.mytwittersearch.database.DatabaseMetaData;
import com.example.mytwittersearch.operation.DownloadImageTask;

public class JsonAdapter extends CursorAdapter {
	private LayoutInflater mInflater = null;

	public JsonAdapter(Context context, Cursor cursor) {
		super(context, cursor, 0);
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView photoImageView = (ImageView) view.findViewById(R.id.user_photo);
		String image_url = cursor.getString(cursor
				.getColumnIndex(DatabaseMetaData.TweetTableMetaData.Columns.IMAGE_URL));
		photoImageView.setTag(image_url);
		new DownloadImageTask().loadImage(photoImageView);
		
		String author = cursor.getString(cursor
				.getColumnIndex(DatabaseMetaData.TweetTableMetaData.Columns.FROM_USER));
		String text = cursor.getString(cursor
				.getColumnIndex(DatabaseMetaData.TweetTableMetaData.Columns.TEXT));
		TextView contentTextView = (TextView) view.findViewById(R.id.text);
		contentTextView.setText(author + ": " + text);

		String time = cursor.getString(cursor
				.getColumnIndex(DatabaseMetaData.TweetTableMetaData.Columns.CREATED_AT));
		TextView timeTextView = (TextView) view.findViewById(R.id.created_at);
		timeTextView.setText(time);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(R.layout.tweet_list_item, parent, false);
	}
}
