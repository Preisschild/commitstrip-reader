package com.commitstrip.commitstripreader.strip;

import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.commitstrip.commitstripreader.BaseActivity;
import com.commitstrip.commitstripreader.MyApp;
import com.commitstrip.commitstripreader.R;
import com.commitstrip.commitstripreader.data.source.StripRepositorySingleton;
import com.commitstrip.commitstripreader.service.SyncLocalDatabaseService;
import com.commitstrip.commitstripreader.util.ActivityUtils;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class StripActivity extends BaseActivity {

    @Inject
    StripPresenter mStripPresenter;

    @NonNull public static String ARGUMENT_STRIP_ID = "ARGUMENT_STRIP_ID";
    private StripFragment mStripFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shouldLaunchOnFirstRun();

        // Create the fragment
        mStripFragment = (StripFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (mStripFragment == null) {
            Bundle extras = getIntent().getExtras();

            if (extras != null && extras.containsKey(ARGUMENT_STRIP_ID))
                mStripFragment = StripFragment.newInstance(extras.getLong(ARGUMENT_STRIP_ID));
            else
                mStripFragment = StripFragment.newInstance();

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mStripFragment, R.id.contentFrame);
        }

        // Create the presenter
        DaggerStripComponent.builder()
                .stripRepositoryComponent(StripRepositorySingleton.getInstance(getApplicationContext()).getStripRepositoryComponent())
                .stripPresenterModule(new StripPresenterModule(mStripFragment)).build()
                .inject(this);
    }

    private static final String JOB_ID = com.commitstrip.commitstripreader.configuration.Configuration.JOB_ID_SYNC_LOCAL_DATABASE;

    private void shouldLaunchOnFirstRun() {
        // Get user preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // If it our first run, we schedule a job for synchronize the database.
        if (!sharedPreferences.getBoolean("firstrun", false)) {

            // Ask Firebase to register the device in the topic. Used for receiving notification.
            FirebaseApp.initializeApp(getApplicationContext());
            FirebaseMessaging.getInstance().subscribeToTopic(com.commitstrip.commitstripreader.configuration.Configuration.TOPIC_NAME);

            // Create a new dispatcher using the Google Play driver.
            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));

            // Schedule the job
            Job myJob = dispatcher.newJobBuilder()
                    .setService(SyncLocalDatabaseService.class)
                    .setTag(JOB_ID)
                    .setRecurring(false)
                    .setLifetime(Lifetime.FOREVER)
                    .setTrigger(Trigger.executionWindow(0, 60))
                    .setConstraints(Constraint.ON_ANY_NETWORK)
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    .build();

            dispatcher.mustSchedule(myJob);

            startService(new Intent(this, SyncLocalDatabaseService.class));

            // Not our first run now !
            sharedPreferences.edit().putBoolean("firstrun", false).apply();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){

            if (mStripFragment == null) {
                return false;
            }
            else {
                return mStripFragment.onKeyVolumeDown(keyCode, event);
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){

            if (mStripFragment == null) {
                return false;
            }
            else {
                return mStripFragment.onKeyVolumeUp(keyCode, event);
            }
        }
        else {
            return false;
        }
    }

}
