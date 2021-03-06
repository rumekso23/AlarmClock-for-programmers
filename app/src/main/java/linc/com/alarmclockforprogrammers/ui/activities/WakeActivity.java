package linc.com.alarmclockforprogrammers.ui.activities;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.google.gson.Gson;

import linc.com.alarmclockforprogrammers.AlarmApp;
import linc.com.alarmclockforprogrammers.R;
import linc.com.alarmclockforprogrammers.data.preferences.LocalPreferencesManager;
import linc.com.alarmclockforprogrammers.data.repository.RepositoryWakeActivityImpl;
import linc.com.alarmclockforprogrammers.domain.interactor.implementation.InteractorWakeActivityImpl;
import linc.com.alarmclockforprogrammers.domain.models.Alarm;
import linc.com.alarmclockforprogrammers.ui.fragments.FragmentAlarmDismiss;
import linc.com.alarmclockforprogrammers.ui.fragments.FragmentTest;
import linc.com.alarmclockforprogrammers.ui.presenters.PresenterWakeActivity;
import linc.com.alarmclockforprogrammers.ui.views.ViewWakeActivity;
import linc.com.alarmclockforprogrammers.utils.Consts;


public class WakeActivity extends AppCompatActivity implements ViewWakeActivity {

    private PresenterWakeActivity presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake);

        if(presenter == null) {
            this.presenter = new PresenterWakeActivity(
                    this,
                    new InteractorWakeActivityImpl(
                            new RepositoryWakeActivityImpl(new LocalPreferencesManager(this))
                    )
            );
        }
        this.presenter.setData();

        Bundle data = new Bundle();
        Alarm alarm = new Gson().fromJson(
                getIntent().getStringExtra(Consts.ALARM_JSON), Alarm.class);

        Fragment wakeFragment;

        if(!alarm.isContainsTask()) {
            wakeFragment = new FragmentAlarmDismiss();
        }else {
            wakeFragment = new FragmentTest();
        }

        data.putInt(Consts.ALARM_ID, alarm.getId());
        wakeFragment.setArguments(data);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.wake_container, wakeFragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AlarmApp.getInstance().setCurrentActivity(this);
    }

    @Override
    public void setAppTheme(int theme) {
        setTheme(theme);
    }

    @Override
    public void screenTurnOn() {
        // Check for turned on screen
        if( !((PowerManager) getSystemService(POWER_SERVICE)).isInteractive() ) {

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setTurnScreenOn(true);
                setShowWhenLocked(true);
                getSystemService(KeyguardManager.class)
                        .requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {});
            } else {
                getWindow().addFlags( WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON );
            }
        }
    }

    @Override
    public void returnToActivity() {
        Intent intent = getIntent().
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    @Override
    protected void onStop() {
        super.onStop();
        this.presenter.checkTestCompleteness();
    }

    @Override
    public void finishTask() {
        this.presenter.finish();
        finish();
    }
}
