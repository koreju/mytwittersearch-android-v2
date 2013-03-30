package com.example.mytwittersearch.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.mytwittersearch.R;
import com.example.mytwittersearch.fragment.TweetDetailFragment;
import com.example.mytwittersearch.fragment.TweetListFragment;
import com.example.mytwittersearch.fragment.TweetListFragment.OnTweetSelectedListener;

public class MainActivity extends FragmentActivity implements
		OnTweetSelectedListener {
	
	private static final int MENU_EXIT_ID = Menu.FIRST;
	private static final int MENU_ABOUT_ID = MENU_EXIT_ID + 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tweet_container);

		// Check whether the activity is using the layout version with the
		// fragment_container FrameLayout. If so, we must add the first
		// fragment.
		if (findViewById(R.id.fragment_container) != null) {
			// However, if we're being restored from a previous state, then we
			// don't need to do anything and should return or else we could end
			// up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			// Create an instance of ExampleFragment
			TweetListFragment tweetListFragment = new TweetListFragment();

			// In case this activity was started with special instructions from
			// an Intent, pass the Intent's extras to the fragment as arguments.
			tweetListFragment.setArguments(getIntent().getExtras());

			// Add the fragment to the fragment_container FrameLayout
			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragment_container, tweetListFragment).commit();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_EXIT_ID, 0, R.string.menu_exit);
		menu.add(0, MENU_ABOUT_ID, 0, R.string.menu_about);
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_EXIT_ID:
			finish();
			return true;
		case MENU_ABOUT_ID:
			Toast.makeText(getApplicationContext(), "About this app...",
					Toast.LENGTH_LONG).show();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onTweetSelected(int position) {
		// The user selected the headline of an article from the
		// HeadlinesFragment

		// Capture the tweet detail fragment from the activity layout
		TweetDetailFragment tweetDetailFragment = (TweetDetailFragment) getSupportFragmentManager()
				.findFragmentById(R.id.tweet_detail_fragment);

		if (tweetDetailFragment != null) {
			// If tweet detail fragment is available, we're in two-pane layout...

			// Call a method in the TweetDetailFragment to update its content
			tweetDetailFragment.updateTweetView(position);
		} else {
			// If the fragment isn't available, we're in the one-pane layout and
			// must swap fragments...

			// Create fragment and give it an argument for the selected tweet
			TweetDetailFragment newFragment = new TweetDetailFragment();
			Bundle args = new Bundle();
			args.putInt(TweetDetailFragment.ARG_POSITION, position);
			newFragment.setArguments(args);
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();

			// Replace whatever is in the fragment_container view with this
			// fragment, and add the transaction to the back stack so the user
			// can navigate back.
			transaction.replace(R.id.fragment_container, newFragment);
			transaction.addToBackStack(null);
			
			// Commit the transaction
			transaction.commit();
		}
	}
}
