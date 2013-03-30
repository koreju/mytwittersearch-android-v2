package com.example.mytwittersearch.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.graphics.drawable.Drawable;

public class TweetListManager {
	private static TweetListManager instance = null;
	private List<Tweet> mTweetList = null;
	private Map<String, Drawable> mImageCache = null;

	public static TweetListManager getInstance() {
		if (instance == null) {
			instance = new TweetListManager();
		}
		return instance;
	}

	private TweetListManager() {
		mTweetList = new ArrayList<Tweet>();
		mImageCache = new HashMap<String, Drawable>();
	}

	/**
	 * @return the mTweetList
	 */
	public List<Tweet> getTweetList() {
		return mTweetList;
	}

	public int getSize() {
		return mTweetList.size();
	}

	public void add(Tweet tweet) {
		mTweetList.add(tweet);
	}

	public Tweet getTweet(int position) {
		return mTweetList.get(position);
	}

	public void clear() {
		mTweetList.clear();
	}
	
	public void addImage(String url, Drawable image) {
		mImageCache.put(url, image);
	}
	
	public Drawable getImage(String url) {
		return mImageCache.get(url);
	}
	
	public boolean containsImage(String url) {
		return mImageCache.containsKey(url);
	}

	public ContentValues[] toContentValues() {
		if (mTweetList == null) {
			return null;
		}

		ContentValues[] values = new ContentValues[mTweetList.size()];
		for (int i = 0; i < values.length; ++i) {
			if (mTweetList.get(i) != null) {
				values[i] = mTweetList.get(i).toContentValues();
			}
		}
		return values;
	}
}
