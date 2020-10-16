package com.nidhikamath.appupdatedemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private AppUpdateManager appUpdateManager;
    private final int UPDATE_REQUEST = 21;
    private final int DAYS_FOR_FLEXIBLE_UPDATE = 5;
    private InstallStateUpdatedListener stateUpdatedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //updateAppFlexible();
        updateAppImmediate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_REQUEST) {
            if (resultCode != RESULT_OK) {
                //update failed
            } else if (resultCode == RESULT_CANCELED) {
                //user has cancelled the update
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //monitorFlexibleUpdate();
        monitorImmediateUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterFlexibleUpdate();
        unregisterImmediateUpdate();
    }

    private void updateAppFlexible() {
        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && result.clientVersionStalenessDays() != null
                        && result.clientVersionStalenessDays() >= 5
                        && result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.FLEXIBLE, MainActivity.this, UPDATE_REQUEST);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void updateAppImmediate() {
        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.IMMEDIATE, MainActivity.this, UPDATE_REQUEST);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void monitorFlexibleUpdate() {
        stateUpdatedListener = state -> {
            if (state.installStatus() == InstallStatus.DOWNLOADING) {
                long totalBytesToDownload = state.totalBytesToDownload();
                long bytesDownloaded = state.bytesDownloaded();
                //to implement progress bar if needed
            } else if (state.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForUpdateCompleted();
            }
        };

        appUpdateManager.registerListener(stateUpdatedListener);
    }

    private void monitorImmediateUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.IMMEDIATE, MainActivity.this, UPDATE_REQUEST);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void popupSnackbarForUpdateCompleted() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.appupdate), "An update has just been downloaded.", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.purple_200));
        snackbar.show();
    }

    private void unregisterFlexibleUpdate() {
        appUpdateManager.unregisterListener(stateUpdatedListener);
    }

    private void unregisterImmediateUpdate() {
        appUpdateManager.unregisterListener(stateUpdatedListener);
    }


}