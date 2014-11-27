package com.gos.veleta;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kskkbys.rate.RateThisApp;

public class MyActivity extends ActionBarActivity implements OnClickListener {

	static String haveAnAppUrl = "http://veleta.api.haveanapp.com/weather?token=Mfsd_2489hf*__6";

	static final int ANIMATION_DURATION_MIL = 2 * 1000;
	static final int ANIMATION_TOTAL_LAPS = 4;
	private static final int ARROW_INITIAL_ROTATION = -90;

	private static final String SPACE = " ";

	private static final String COMMA = "," + SPACE;

	private static final String ENTER = "\n";
	private ImageView image;
	private TextView text;

	private WeatherAsynctask asyncTask;
	WindInfo windInfo;

	private LocationManager locationManager;

	Location location;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_my);

		ActionBar actionBar = getSupportActionBar();
		actionBar.hide();

		View mainLayout = (View) findViewById(R.id.mainlayout);
		image = (ImageView) findViewById(R.id.image);

		image.setVisibility(View.INVISIBLE);

		text = (TextView) findViewById(R.id.text);
		if (screenIsBig(this)) {
			text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		}

		// AppRater.app_launched(this);
		RateThisApp.onStart(this);
		RateThisApp.showRateDialogIfNeeded(this);

		mainLayout.setOnClickListener(this);
		// new SimpleEula(this).show();

		sendTrack("/home");

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE); // default
		criteria.setCostAllowed(false);
		// get the best provider depending on the criteria
		String provider = locationManager.getBestProvider(criteria, false);
		location = locationManager.getLastKnownLocation(provider);

		if (location == null) {
			// location updates: at least 1 meter and 200millsecs change
			MyLocationListener mylistener = new MyLocationListener();
			locationManager
					.requestLocationUpdates(provider, 200, 1, mylistener);
		}
		asyncTask = startAsyncTask();
	}

	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location2) {
			// Initialize the location fields
			if (location2 != null) {
				location = location2;
				locationManager.removeUpdates(this);
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {
		}
	}

	private void sendTrack(String viewName) {
		// Get tracker.
		Tracker t = getTracker(TrackerName.APP_TRACKER);

		// Set screen name.
		// Where path is a String representing the screen name.
		t.setScreenName(viewName);

		// Send a screen view.
		t.send(new HitBuilders.AppViewBuilder().build());
	}

	private boolean screenIsBig(MyActivity myActivity) {
		int w = getWindowManager().getDefaultDisplay().getWidth();
		return w > 800;
	}

	public static class PlaceholderFragment extends Fragment {
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_my, container,
					false);
			return rootView;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onClick(View v) {

		refreshAdd();

		if (windInfo == null) {
			windInfo = getAsyncResultSilent(asyncTask);

			if (windInfo == null || isTooOld(windInfo) || windInfo.hasError()) {
				asyncTask = startAsyncTask();
				windInfo = getAsyncResultSilent(asyncTask);

			}
		}
		text.setVisibility(View.INVISIBLE);

		int rot = 0;
		if (windInfo.getDegrees() != null) {
			rot = windInfo.getDegrees() - ARROW_INITIAL_ROTATION + 180;
			// 180 because arrow should point where the wind comes from
		}

		RotateAnimation animation = new RotateAnimation(0, 360
				* ANIMATION_TOTAL_LAPS + rot, image.getWidth() / 2,
				image.getHeight() / 2);
		animation.setDuration(ANIMATION_DURATION_MIL);
		animation.setFillAfter(true);
		image.startAnimation(animation);

		text.postDelayed(new Runnable() {

			@Override
			public void run() {

				if (windInfo.hasError()) {
					Toast.makeText(text.getContext(),
							windInfo.getError().getMessage(getResources()),
							Toast.LENGTH_LONG).show();
					image.setVisibility(View.INVISIBLE);
				} else {
					// image.setRotation(rot);

					if (windInfo.getSpeedKm() != null) {
						String msg = getUserMessage(windInfo);
						text.setText(msg);

						text.setVisibility(View.VISIBLE);
					}
				}

			}

			private String getUserMessage(final WindInfo windInfo) {
				Locale locale = Locale.getDefault();
				Integer speed = null;
				if (isMPH(locale)) {
					speed = windInfo.getSpeedMiles();
				} else {
					speed = windInfo.getSpeedKm();
				}

				String msg = getResources().getString(R.string.SPEED_PREFIX)
						+ SPACE + String.valueOf(speed) + SPACE
						+ getResources().getString(R.string.SPEED_SUFFIX)
						+ ENTER + getLocation(windInfo);

				windInfo.setProvider("http://www.worldweatheronline.com");
				if (windInfo.getProvider() != null) {
					msg += ENTER
							+ getResources().getString(R.string.PROVIDED_BY)
							+ SPACE + windInfo.getProvider();
				}
				return msg;
			}

			private boolean isMPH(Locale locale) {
				// TODO Auto-generated method stub
				return Locale.UK.equals(locale) || Locale.US.equals(locale);
			}
		}, ANIMATION_DURATION_MIL);

	}

	Long lastAddRefresh = null;

	private void refreshAdd() {
		if (lastAddRefresh == null
				|| (System.currentTimeMillis() - lastAddRefresh) > (1000 * 3)) {
			lastAddRefresh = System.currentTimeMillis();
			AdView adView = (AdView) findViewById(R.id.adView);

			Builder builder = new AdRequest.Builder();
			if (location != null) {
				builder.setLocation(location);
			}
			adView.loadAd(builder.build());
		}
	}

	private boolean isTooOld(WindInfo windInfo2) {
		return System.currentTimeMillis() - windInfo2.getTimeStamp() > 1000 * 60;

	}

	private WeatherAsynctask startAsyncTask() {
		WeatherAsynctask asyncTask = new WeatherAsynctask(this);

		String url = null;
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			url = haveAnAppUrl + "&v=" + pInfo.versionName;
			if (location != null) {
				url += "&lat=" + location.getLatitude() + "&lon="
						+ location.getLongitude();
			}
		} catch (NameNotFoundException e) {
			url = haveAnAppUrl;
		}
		asyncTask.execute(url);
		return asyncTask;
	}

	private String getLocation(WindInfo windInfo) {

		return getResources().getString(R.string.LOCATION_PREFIX) + SPACE
				+ windInfo.getAreaName() + COMMA + windInfo.getRegion() + COMMA
				+ windInfo.getCountry();
	}

	private WindInfo getAsyncResultSilent(WeatherAsynctask asyncTask) {
		WindInfo windInfo = new WindInfo();
		try {
			windInfo = asyncTask.get();
		} catch (InterruptedException | ExecutionException e) {

			ErrorInfo error = new ErrorInfo(R.string.ERR_CANT_GET_HTTP_INFO);
			windInfo.setError(error);
		}
		windInfo.setTimeStamp(System.currentTimeMillis());
		return windInfo;
	}

	/**
	 * This class makes the ad request and loads the ad.
	 */
	public static class AdFragment extends Fragment {
		private AdView mAdView;

		public AdFragment() {
		}

		@Override
		public void onActivityCreated(Bundle bundle) {
			super.onActivityCreated(bundle);
			// Gets the ad view defined in layout/ad_fragment.xml with ad unit
			// ID set in
			// values/strings.xml.
			mAdView = (AdView) getView().findViewById(R.id.adView);
			// Create an ad request. Check logcat output for the hashed device
			// ID to
			// get test ads on a physical device. e.g.
			// "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
			AdRequest adRequest = new AdRequest.Builder().addTestDevice(
					AdRequest.DEVICE_ID_EMULATOR).build();
			// Start loading the ad in the background.
			mAdView.loadAd(adRequest);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_ad, container, false);
		}

		/** Called when leaving the activity */
		@Override
		public void onPause() {
			if (mAdView != null) {
				mAdView.pause();
			}
			super.onPause();
		}

		/** Called when returning to the activity */
		@Override
		public void onResume() {
			super.onResume();
			if (mAdView != null) {
				mAdView.resume();
			}
		}

		/** Called before the activity is destroyed */
		@Override
		public void onDestroy() {
			if (mAdView != null) {
				mAdView.destroy();
			}
			super.onDestroy();
		}
	}

	/**
	 * Enum used to identify the tracker that needs to be used for tracking.
	 * 
	 * A single tracker is usually enough for most purposes. In case you do need
	 * multiple trackers, storing them all in Application object helps ensure
	 * that they are created only once per application instance.
	 */
	public enum TrackerName {
		APP_TRACKER // Tracker used only in this app.

	}

	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
	// The following line should be changed to include the correct property id.
	private static final String PROPERTY_ID = "UA-56449357-1";

	synchronized Tracker getTracker(TrackerName trackerId) {
		if (!mTrackers.containsKey(trackerId)) {

			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);

			Tracker t = analytics.newTracker(PROPERTY_ID);
			mTrackers.put(trackerId, t);

		}
		return mTrackers.get(trackerId);
	}

}
