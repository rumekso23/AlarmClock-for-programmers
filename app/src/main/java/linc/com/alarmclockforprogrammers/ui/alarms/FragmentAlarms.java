package linc.com.alarmclockforprogrammers.ui.alarms;

import android.Manifest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.Explode;
import android.support.transition.Transition;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import linc.com.alarmclockforprogrammers.AlarmApp;
import linc.com.alarmclockforprogrammers.data.database.AppDatabase;
import linc.com.alarmclockforprogrammers.data.entity.AlarmEntity;
import linc.com.alarmclockforprogrammers.R;
import linc.com.alarmclockforprogrammers.data.mapper.AlarmEntityMapper;
import linc.com.alarmclockforprogrammers.data.preferences.PreferencesAlarm;
import linc.com.alarmclockforprogrammers.domain.interactor.alarms.InteractorAlarms;
import linc.com.alarmclockforprogrammers.data.repository.RepositoryAlarms;
import linc.com.alarmclockforprogrammers.domain.model.Alarm;
import linc.com.alarmclockforprogrammers.infrastructure.AlarmHandler;
import linc.com.alarmclockforprogrammers.ui.activities.main.MainActivity;
import linc.com.alarmclockforprogrammers.ui.alarms.adapters.AdapterAlarms;
import linc.com.alarmclockforprogrammers.ui.alarmsettings.FragmentAlarmSettings;
import linc.com.alarmclockforprogrammers.ui.base.BaseFragment;
import linc.com.alarmclockforprogrammers.ui.mapper.AlarmViewModelMapper;
import linc.com.alarmclockforprogrammers.ui.viewmodel.AlarmViewModel;
import linc.com.alarmclockforprogrammers.utils.JsonUtil;
import linc.com.alarmclockforprogrammers.utils.ResUtil;


public class FragmentAlarms extends BaseFragment implements AdapterAlarms.OnAlarmClicked,
        View.OnClickListener, FragmentBottomDialog.BottomDialogStateListener, ViewAlarms {

    private TextView balance;
    private AdapterAlarms adapterAlarms;
    private PresenterAlarms presenter;

    private Transition enterAnimation;
    private Transition returnAnimation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppDatabase database = AlarmApp.getInstance().getDatabase();

        if(presenter == null) {
            this.presenter = new PresenterAlarms(new InteractorAlarms(
                    new RepositoryAlarms(
                            database.alarmDao(),
                            database.questionsDao(),
                            new PreferencesAlarm(getActivity()),
                            new JsonUtil<>(new Gson()),
                            new AlarmEntityMapper()),
                    new AlarmHandler(getActivity())
            ), new AlarmViewModelMapper());
        }

        this.enterAnimation = new Explode()
                .setInterpolator(new FastOutSlowInInterpolator())
                .setStartDelay(500)
                .setDuration(1000);

        this.returnAnimation = new Explode()
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(1000);

        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.WAKE_LOCK,
                        Manifest.permission.SET_ALARM,
                        Manifest.permission.RECEIVE_BOOT_COMPLETED
                }, 1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);

        RecyclerView alarmsListRV = view.findViewById(R.id.alarms__list_of_alarms);
        FloatingActionButton fab = view.findViewById(R.id.alarms__add_alarm);
        this.balance = view.findViewById(R.id.alarms__balance);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        SnapHelper snapHelper = new LinearSnapHelper();
        this.adapterAlarms = new AdapterAlarms(this);

        snapHelper.attachToRecyclerView(alarmsListRV);
        alarmsListRV.setHasFixedSize(true);
        alarmsListRV.setLayoutManager(layoutManager);
        alarmsListRV.setAdapter(adapterAlarms);
        fab.setOnClickListener(this);

        presenter.bind(this);
        Log.d("CREATE_FRAG", "onCreateView: ");

        return view;
    }

    @Override
    public void setAlarmsData(Map<Integer, AlarmViewModel> alarms) {
        this.adapterAlarms.setAlarms(alarms);
        Log.d("DATA_RESET", "setAlarmsData: ");
    }

    @Override
    public void setBalance(int balance) {
        this.balance.setText(String.valueOf(balance));
    }

    @Override
    public void setDrawerState(boolean isEnable) {
        ((MainActivity) getActivity()).setDrawerEnabled(isEnable);
    }

    @Override
    public void openAlarmEditor(int alarmId) {
        FragmentAlarmSettings alarmSettings = new FragmentAlarmSettings();
        Bundle data = new Bundle();
        data.putInt("alarm_id", alarmId);

        /*alarmSettings.setEnterTransition(enterAnimation);
        alarmSettings.setReturnTransition(returnAnimation);
*/
        alarmSettings.setArguments(data);

        getFragmentManager().beginTransaction()
                .replace(R.id.alarms_container, alarmSettings)
                .addToBackStack(null)
                .commit();
    }

    // todo merge

    @Override
    public void openAlarmCreator() {
        FragmentAlarmSettings alarmSettings = new FragmentAlarmSettings();

/*        alarmSettings.setEnterTransition(enterAnimation);
        alarmSettings.setReturnTransition(returnAnimation);*/

        getFragmentManager().beginTransaction()
                .replace(R.id.alarms_container, alarmSettings)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void openBottomSheetDialog(AlarmViewModel alarm) {
        FragmentBottomDialog bottomDialog = new FragmentBottomDialog();
        bottomDialog.setBottomDialogClickListener(this);
        bottomDialog.setAlarm(alarm);
        bottomDialog.show(getFragmentManager(), "DIALOG");
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.alarms__add_alarm) {
            this.presenter.openAlarmCreator();
        }
    }

    @Override
    public void hideDeletedItem(int id) {
        adapterAlarms.removeAlarm(id);
    }

    @Override
    public void highlightEnable(AlarmViewModel alarmViewModel) {
        adapterAlarms.updateAlarm(alarmViewModel);
    }

    @Override
    public void onAlarmClicked(int position) {
        this.presenter.openAlarmEditor(position);
    }

    @Override
    public void onHold(int position) {
        this.presenter.alarmSelected(position);
    }

    @Override
    public void onDeleteClicked(int id) {
        this.presenter.deleteAlarm(id);
    }

    @Override
    public void onSwitchClicked(int id, boolean enable) {
        this.presenter.enableAlarm(id, enable);
    }

    @Override
    public void onBackPressed() {
        getActivity().finish();
    }
}
