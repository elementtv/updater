package com.skystreamtv.element_ez_stream.updater.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.background.FailureReason;
import com.skystreamtv.element_ez_stream.updater.background.PlayerUpdaterService;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;

public class UpdateActivity extends AppCompatActivity implements PlayerUpdaterActivity, ServiceConnection {

    private static final String TAG = "UpdateActivity";

    protected Messenger service_messenger = null;
    protected boolean is_service_bound;

    private TextView statusMessageTextView;
    private ProgressBar updateProgressBar;
    private Button retryButton;

    private static final String RETRY = "retry", TITLE = "title", STATE_TEXT = "state_text",
            PROGRESS = "progress";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        statusMessageTextView = (TextView) findViewById(R.id.statusMessageTextView);
        updateProgressBar = (ProgressBar) findViewById(R.id.updateProgressBar);
        retryButton = (Button) findViewById(R.id.retryButton);

        if (savedInstanceState == null) {
            startServiceReset(false);
        } else {
            setTitle(savedInstanceState.getString(TITLE));
            statusMessageTextView.setText(savedInstanceState.getString(STATE_TEXT));
            updateProgressBar.setProgress(savedInstanceState.getInt(PROGRESS));
            int retry = savedInstanceState.getInt(RETRY);

            switch (retry) {
                case View.VISIBLE:
                    retryButton.setVisibility(View.VISIBLE);
                    break;
                case View.GONE:
                    retryButton.setVisibility(View.GONE);
                    break;
                case View.INVISIBLE:
                    retryButton.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    }

    protected void startServiceReset(boolean retrying) {
        try {
            Log.d(TAG, "UpdateActivity.startServiceReset()");
            Intent startIntent = getIntent();
            Log.d(TAG, "Get skin");
            Skin skin = startIntent.getParcelableExtra(Constants.SKINS);
            setTitle(String.format(getString(R.string.updating_brand), skin.getName()));
            Log.d(TAG, "Reset view");
            resetView();
            Log.d(TAG, "Create service intent");
            Intent serviceIntent = new Intent(this, PlayerUpdaterService.class);
            serviceIntent.putExtra(Constants.SERVICE_RESET, startIntent.getBooleanExtra(Constants.SERVICE_RESET, retrying));
            serviceIntent.putExtra(Constants.SKINS, skin);
            startService(serviceIntent);
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TITLE, (String)getTitle());
        outState.putString(STATE_TEXT, statusMessageTextView.getText().toString());
        outState.putInt(PROGRESS, updateProgressBar.getProgress());
        outState.putInt(RETRY, retryButton.getVisibility());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent start_intent  = getIntent();
        if (start_intent.getBooleanExtra("update_ready", false))
            hideRetryButton();
        else {
            String failure_reason = start_intent.getStringExtra("error_message");
            if (failure_reason != null && !failure_reason.equals(""))
                showFailure(failure_reason);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Called OnResume()");
        Intent service_intent = new Intent(this, PlayerUpdaterService.class);
        bindService(service_intent, this, BIND_AUTO_CREATE);
        is_service_bound = true;
    }

    protected final Messenger update_activity_messenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PlayerUpdaterService.MSG_UPDATE_READY:
                    hideRetryButton();
                    break;
                case PlayerUpdaterService.MSG_UPDATE_PROGRESS:
                    updateProgressBarPercent(msg.arg1);
                    break;
                case PlayerUpdaterService.MSG_UPDATE_CANCELLED:
                    updateCancelled(msg);
                    break;
                case PlayerUpdaterService.MSG_UPDATE_COMPLETED:
                    updateDone();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    });

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        Log.d(TAG, "Called onServiceConnected()");
        service_messenger = new Messenger(binder);
        try{
            Message msg = Message.obtain(null, PlayerUpdaterService.MSG_REGISTER_CLIENT);
            msg.replyTo = update_activity_messenger;
            service_messenger.send(msg);
        } catch (RemoteException | NullPointerException e) {
            Log.d(TAG, "Service disconnected while registering update activity.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Called OnPause()");
        if (is_service_bound && service_messenger != null) {
            try {
                Message msg = Message.obtain(null, PlayerUpdaterService.MSG_UNREGISTER_CLIENT);
                msg.replyTo = update_activity_messenger;
                service_messenger.send(msg);
            } catch (RemoteException | NullPointerException e) {
                Log.d(TAG,  "The service was already disconnected");
            }
        }
        unbindService(this);
        is_service_bound = false;

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d("PlayerUpdater", "Called onServiceDisconnected()");
        service_messenger = null;
    }

    protected void setStatusText(final String text) {
        statusMessageTextView.setText(text);
    }

    protected void showErrorDialog(final String title, final String message) {
        AlertDialog error_dialog = Dialogs.buildErrorDialog(this, title, message, 0);
        error_dialog.show();
    }

    protected void resetView() {
        hideRetryButton();
        setStatusText(getResources().getString(R.string.starting_download));
        updateProgressBar.setProgress(0);
    }

    protected void updateProgressBarPercent(int percent) {
        if (percent <= 0)
            setStatusText(getString(R.string.starting_download));
        else if (percent > 0  && percent <= 50)
            setStatusText(String.format(getString(R.string.downloading_new_version),
                    getString(R.string.player_name)));
        else if (percent > 50  && percent <= 98)
            setStatusText(getString(R.string.decompressing_update));
        else if (percent == 99)
            setStatusText(getString(R.string.applying_update));
        else if (percent == 100)
            setStatusText(getString(R.string.update_done));
        if (updateProgressBar.getProgress() < percent)
            updateProgressBar.setProgress(percent);
    }

    @Override
    public void errorAction(int action) {}

    protected void hideRetryButton() {
        Button retryButton = (Button) findViewById(R.id.retryButton);
        retryButton.setVisibility(View.INVISIBLE);
    }

    protected void updateCancelled(Message remoteMessage) {
        Log.d(TAG, "Call UpdateActivity.updateCancelled()");
        Bundle remoteData = remoteMessage.getData();
        remoteData.setClassLoader(getClassLoader());
        FailureReason failureReason = remoteData.getParcelable("failure_reason");
        String failString;
        if (failureReason != null)
            failString = failureReason.getFailureReason();
        else
            failString = getResources().getString(R.string.unknown_error);
        showFailure(failString);
    }

    protected void updateDone() {
        Intent finish_intent = new Intent(getApplicationContext(), DisclaimerActivity.class);
        finish_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish_intent.putExtra("EXIT", true);
        startActivity(finish_intent);
    }

    protected void showFailure(String failure_reason) {
        showErrorDialog(getResources().getString(R.string.download_error), failure_reason);
        setStatusText(getResources().getString(R.string.update_failed));
        Button retryButton = (Button) findViewById(R.id.retryButton);
        retryButton.setVisibility(View.VISIBLE);
    }

    public void onRetryButtonClick(View view) {
        Log.d(TAG, "Call UpdateActivity.onRetryButtonClick()");
        startServiceReset(true);
    }
}
