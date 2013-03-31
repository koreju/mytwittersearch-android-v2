package com.example.mytwittersearch.manager;

import com.example.mytwittersearch.fragment.TweetListFragment;


public class TweetFragmentManager {

	private static TweetFragmentManager kInstance;

	private TweetListFragment mFragment;

	public static TweetFragmentManager getInstance() {
		if (kInstance == null) {
			kInstance = new TweetFragmentManager();
		}
		return kInstance;
	}

	private TweetFragmentManager() {
	}

	public TweetListFragment getFragment() {
		return mFragment;
	}

	public void setFragment(TweetListFragment fragment) {
		mFragment = fragment;
	}
}
