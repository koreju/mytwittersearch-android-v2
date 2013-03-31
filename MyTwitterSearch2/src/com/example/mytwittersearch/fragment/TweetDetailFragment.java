package com.example.mytwittersearch.fragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mytwittersearch.R;
import com.example.mytwittersearch.manager.TweetListManager;
import com.example.mytwittersearch.model.Tweet;

public class TweetDetailFragment extends Fragment {
	// Pattern for matching from_user
	private final Pattern mFromUserPattern = Pattern.compile("^[A-Za-z0-9_]+");

	// Pattern for matching @mention
	private final Pattern mMentionPattern = Pattern.compile("@[A-Za-z0-9_]+");

	// Pattern for matching #hash
	private final Pattern mHashPattern = Pattern.compile("#[A-Za-z0-9_]+");

	// Pattern for matching URL, Linkify.WEB_URLS seem to overdo the
	// substitution
	private final Pattern mUrlPattern = Pattern.compile("http://[^ ]+");

	private final String mTwitterUserURL = "http://twitter.com/";
	private final String mTwitterSearchURL = "http://search.twitter.com/search?q=";

	private Linkify.TransformFilter noAtSign = new Linkify.TransformFilter() {
		// A filter to remove the @ character before the user name
		@Override
		public String transformUrl(Matcher match, String user) {
			return user.substring(1);
		}
	};

	private Linkify.TransformFilter keywordEncoder = new Linkify.TransformFilter() {
		// encode the search keyword
		@Override
		public String transformUrl(Matcher match, String keyword) {
			return Uri.encode(keyword);
		}
	};
	
	public final static String ARG_POSITION = "positoin";
	private int mCurrentPositon = -1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// If activity recreated (such as from screen rotate), restore the
		// previous article selection set by onSaveInstanceState().
		// This is primarily necessary when in the two pane layout.
		if (savedInstanceState != null) {
			mCurrentPositon = savedInstanceState.getInt(ARG_POSITION);
		}
		return inflater.inflate(R.layout.tweet_detail, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		// During startup, check if there are arguments passed to the fragment.
		// onStart() is a good place to do this because the layout has already
		// been applied to the fragment at this point so we can
		// safely call the method below that sets the article text.
		Bundle args = getArguments();
		if (args != null) {
			// Set the article based on argument passed in
			updateTweetView(args.getInt(ARG_POSITION));
		} else if (mCurrentPositon != -1) {
			// Set article based on saved instance sate defined in onCreateView()
			updateTweetView(mCurrentPositon);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_POSITION, mCurrentPositon);
	}
	
	public void updateTweetView(int position) {
		Tweet tweet = TweetListManager.getInstance().getTweet(position);
		ImageView imageView = (ImageView) getActivity().findViewById(R.id.tweet_user_photo);
		imageView.setImageDrawable(TweetListManager.getInstance().getImage(tweet.getImageUrl()));
		TextView textView = (TextView) getActivity().findViewById(R.id.tweet_text);
		textView.setText(tweet.getFromUser() + ": " + tweet.getText());
		Linkify.addLinks(textView, mFromUserPattern, mTwitterUserURL);
		Linkify.addLinks(textView, mMentionPattern, mTwitterUserURL, null,
				noAtSign);
		Linkify.addLinks(textView, mHashPattern, mTwitterSearchURL, null,
				keywordEncoder);
		Linkify.addLinks(textView, mUrlPattern, "");
		textView = (TextView) getActivity().findViewById(R.id.tweet_created_at);
		textView.setText(tweet.getCreatedAt());

		mCurrentPositon = position;
	}

}
