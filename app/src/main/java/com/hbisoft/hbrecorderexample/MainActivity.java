package com.hbisoft.hbrecorderexample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatRadioButton;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.hbisoft.hbrecorder.HBRecorder;
import com.hbisoft.hbrecorder.HBRecorderListener;

import java.io.File;


/**
 * Created by HBiSoft on 13 Aug 2019
 * Copyright (c) 2019 . All rights reserved.
 * Last modified 13 Aug 2019
 */

/*
* Implementation Steps
*
* 1. Implement HBRecorderListener by calling implements HBRecorderListener
*    After this you have to implement the methods by pressing (Alt + Enter)
*    This will create a method called HBRecorderOnComplete()
*    This method will be called once the recording is done.
*
* 2. Declare HBRecorder
*
* 3. Init implements HBRecorderListener by calling hbRecorder = new HBRecorder(this, this);
*
* 4. Set adjust provided settings
*
* 5. Start recording by first calling:
* MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
  Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
  startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE);

* 6. Then in onActivityResult call hbRecorder.onActivityResult(resultCode, data, this);
*
* 7. Then you can start recording by calling hbRecorder.startScreenRecording(data);
*
* */

public class MainActivity extends AppCompatActivity implements HBRecorderListener {
    //Permissions
    private static final int SCREEN_RECORD_REQUEST_CODE = 777;
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    private boolean hasPermissions = false;

    //Declare HBRecorder
    private HBRecorder hbRecorder;

    //Start/Stop Button
    private Button startbtn;

    //HD/SD quality
    private AppCompatRadioButton hdRadio;
    private AppCompatRadioButton sdRadio;
    private RadioGroup radioGroup;

    //Should record/show audio/notification
    private CheckBox recordAudioCheckBox;
    private CheckBox notificationCheckbox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createFolder();
        initViews();
        mOnClickListeners();
        radioButtonColorStates();
        radioGroupCheckListener();
        recordAudioCheckBoxListener();
        notificationCheckboxListener();

        //Init HBRecorder
        hbRecorder = new HBRecorder(this, this);
        hbRecorder.setOutputPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) +"/HBRecorder");
        hbRecorder.setAudioBitrate(128000);
        hbRecorder.setAudioSamplingRate(44100);


        //When the user returns to the application, some UI changes might be necessary,
        //check if recording is in progress and make changes accordingly
        if (hbRecorder.isBusyRecording()) {
            startbtn.setText(R.string.stop_recording);
        }

    }

    //Create Folder
    private void createFolder() {
        File f1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "HBRecorder");
        if (!f1.exists()) {
            if (f1.mkdirs()){
                Log.i("Folder ","created");
            }
        }
    }

    //Init Views
    private void initViews() {
        startbtn = findViewById(R.id.button_start);
        radioGroup = findViewById(R.id.radio_group);
        hdRadio = findViewById(R.id.hd_button);
        sdRadio = findViewById(R.id.sd_button);
        recordAudioCheckBox = findViewById(R.id.audio_check_box);
        notificationCheckbox = findViewById(R.id.notification_check_box);
    }

    //Start Button OnClickListener
    private void mOnClickListeners() {
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first check if permissions was granted
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE)) {
                    hasPermissions = true;
                }
                if (hasPermissions) {
                    //check if recording is in progress
                    //and stop it if it is
                    if (hbRecorder.isBusyRecording()) {
                        hbRecorder.stopScreenRecording();
                        startbtn.setText(R.string.start_recording);

                    }
                    //else start recording
                    else {
                        startRecordingScreen();
                    }
                }
            }
        });
    }

    //Change color states of radio buttons
    private void radioButtonColorStates() {
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        ContextCompat.getColor(this, R.color.white)
                        , ContextCompat.getColor(this, R.color.colorAccent),
                }
        );

        hdRadio.setButtonTintList(colorStateList);
        sdRadio.setButtonTintList(colorStateList);

    }

    //Check if HD/SD Video should be recorded
    private void radioGroupCheckListener() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.hd_button:
                        //Ser HBRecorder to HD
                        hbRecorder.recordHDVideo(true);
                        break;
                    case R.id.sd_button:
                        //Ser HBRecorder to SD
                        hbRecorder.recordHDVideo(false);
                        break;
                }
            }
        });
    }

    //Check if audio should be recorded
    private void recordAudioCheckBoxListener() {
        recordAudioCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    //Enable audio
                    hbRecorder.isAudioEnabled(true);
                } else {
                    //Disable audio
                    hbRecorder.isAudioEnabled(false);
                }
            }
        });
    }

    //Check if notifications should be shown
    private void notificationCheckboxListener() {
        notificationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //Enable notifications
                if (isChecked) {
                    hbRecorder.shouldShowNotification(true);
                    hbRecorder.setNotificationSmallIcon(R.drawable.icon);
                    hbRecorder.setNotificationTitle("Recording your screen");
                    hbRecorder.setNotificationDescription("Drag down to stop the recording");
                }
                //Disable notifications
                else {
                    hbRecorder.shouldShowNotification(false);
                }
            }
        });
    }

    //Listener for when the recording is saved successfully
    //This will be called after the file was created
    @Override
    public void HBRecorderOnComplete() {
        startbtn.setText(R.string.start_recording);
        showLongToast("Saved Successfully");

    }

    //Start recording screen
    //It is important to call it like this
    //hbRecorder.startScreenRecording(data); should only be called in onActivityResult
    private void startRecordingScreen() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
        startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE);
        startbtn.setText(R.string.stop_recording);
    }

    //Check if permissions was granted
    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }

    //Handle permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE);
                } else {
                    hasPermissions = false;
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                }
                break;
            case PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasPermissions = true;
                    //Permissions was provided
                    //Start screen recording
                    startRecordingScreen();
                } else {
                    hasPermissions = false;
                    showLongToast("No permission for " + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //It is important to call this before starting the recording
                hbRecorder.onActivityResult(resultCode, data, this);
                //Start screen recording
                hbRecorder.startScreenRecording(data);

            }
        }
    }

    //Show Toast
    private void showLongToast(final String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}
