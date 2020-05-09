package com.decodedhealth.flutter_zoom_plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import us.zoom.sdk.JoinMeetingParams;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.ZoomError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKInitParams;
import us.zoom.sdk.ZoomSDKInitializeListener;
import us.zoom.sdk.ZoomSDKRawDataMemoryMode;

/**
 * Created by Anatoliy Mizyakin on 08.05.2020.
 */
public class MeetingRoomActivity extends Activity implements ZoomSDKInitializeListener, MeetingServiceListener {
    private final static String EXTRA_MEETING_NUMBER = "meeting_number";
    private final static String EXTRA_MEETING_PASSWORD = "meeting_password";
    private final static String EXTRA_APP_KEY = "app_key";
    private final static String EXTRA_APP_SECRET = "app_secret";
    private final static String EXTRA_WEB_DOMAIN = "web_domain";
    private final static String EXTRA_NAME = "name";

    private final static String TAG = MeetingRoomActivity.class.getSimpleName();

    private String meetingNumber;
    private String meetingPassword;
    private String appKey;
    private String appSecret;
    private String webDomain;
    private String name;

    public static Intent getLaunchIntent(final Context context,
                                         String meetingNumber,
                                         String meetingPassword,
                                         String sdkKey,
                                         String sdkSecret,
                                         String webDomain,
                                         String name) {
        Intent intent = new Intent(context, MeetingRoomActivity.class);
        intent.putExtra(EXTRA_MEETING_NUMBER, meetingNumber);
        intent.putExtra(EXTRA_MEETING_PASSWORD, meetingPassword);
        intent.putExtra(EXTRA_APP_KEY, sdkKey);
        intent.putExtra(EXTRA_APP_SECRET, sdkSecret);
        intent.putExtra(EXTRA_WEB_DOMAIN, webDomain);
        intent.putExtra(EXTRA_NAME, name);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_meeting_room);

        Log.i(TAG, "getArguments");
        getArguments();

        ZoomSDK.getInstance().getMeetingService().addListener(this);
        ZoomSDK.getInstance().getMeetingSettingsHelper().enable720p(true);
        if (ZoomSDK.getInstance().isInitialized()) {
            Log.i(TAG, "onClickJoin");
            join();
        } else {
            Log.i(TAG, "initSDK");
            initSDK();
        }
    }

    private void getArguments() {
        meetingNumber = getIntent().getExtras().getString(EXTRA_MEETING_NUMBER);
        meetingPassword = getIntent().getExtras().getString(EXTRA_MEETING_PASSWORD);
        appKey = getIntent().getExtras().getString(EXTRA_APP_KEY);
        appSecret = getIntent().getExtras().getString(EXTRA_APP_SECRET);
        webDomain = getIntent().getExtras().getString(EXTRA_WEB_DOMAIN);
        name = getIntent().getExtras().getString(EXTRA_NAME);
    }

    public void initSDK() {
        if (!ZoomSDK.getInstance().isInitialized()) {
            ZoomSDK.getInstance().getMeetingSettingsHelper().setCustomizedMeetingUIEnabled(true);
            ZoomSDKInitParams initParams = new ZoomSDKInitParams();
            initParams.appKey = appKey;
            initParams.appSecret = appSecret;
            initParams.enableLog = true;
            initParams.logSize = 50;
            initParams.domain = webDomain;
            initParams.videoRawDataMemoryMode = ZoomSDKRawDataMemoryMode.ZoomSDKRawDataMemoryModeStack;
            ZoomSDK.getInstance().initialize(this, this, initParams);
        }
    }

    @Override
    public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
        if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
            Log.i(TAG, "Failed to initialize Zoom SDK. Error: " + errorCode + ", internalErrorCode=" + internalErrorCode);
        } else {
            ZoomSDK.getInstance().getMeetingSettingsHelper().enable720p(false);
            ZoomSDK.getInstance().getMeetingService().addListener(this);
            Log.i(TAG, "Initialize Zoom SDK successfully.");
            join();
        }
    }

    public void join() {
        ZoomSDK.getInstance().getSmsService().enableZoomAuthRealNameMeetingUIShown(false);

        JoinMeetingParams params = new JoinMeetingParams();
        params.meetingNo = meetingNumber;
        params.password = meetingPassword;
        params.displayName = name;
        ZoomSDK.getInstance().getMeetingService().joinMeetingWithParams(this, params);
    }

    @Override
    public void onMeetingStatusChanged(MeetingStatus meetingStatus, int errorCode, int internalErrorCode) {
        Log.d(TAG, "onMeetingStatusChanged " + meetingStatus);
        if (meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
            showMeetingUi();
        }
    }

    private void showMeetingUi() {
        Intent intent = new Intent(this, MyMeetingActivity.class);
        intent.putExtra("from", 3);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        this.startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ZoomSDK.getInstance().getMeetingService() != null) {
            ZoomSDK.getInstance().getMeetingService().removeListener(this);
        }
    }

    @Override
    public void onZoomAuthIdentityExpired() {
    }
}
