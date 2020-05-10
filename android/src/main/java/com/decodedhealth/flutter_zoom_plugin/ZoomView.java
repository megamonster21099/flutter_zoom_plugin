package com.decodedhealth.flutter_zoom_plugin;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;
import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.JoinMeetingParams;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.ZoomError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKAuthenticationListener;
import us.zoom.sdk.ZoomSDKInitParams;
import us.zoom.sdk.ZoomSDKInitializeListener;
import us.zoom.sdk.ZoomSDKRawDataMemoryMode;

public class ZoomView implements PlatformView,
        MethodChannel.MethodCallHandler,
        ZoomSDKAuthenticationListener {
    private static final String TAG = ZoomView.class.getSimpleName();
    private final TextView textView;
    private final MethodChannel methodChannel;
    private final Context context;
    private final EventChannel meetingStatusChannel;
    private final Activity activity;

    ZoomView(Context context, Activity activity, BinaryMessenger messenger, int id) {
        textView = new TextView(context);
        this.activity = activity;
        this.context = context;

        methodChannel = new MethodChannel(messenger, "com.decodedhealth/flutter_zoom_plugin");
        methodChannel.setMethodCallHandler(this);

        meetingStatusChannel = new EventChannel(messenger, "com.decodedhealth/zoom_event_stream");
    }

    @Override
    public View getView() {
        return textView;
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case "init":
                init(methodCall, result);
                break;
            case "join":
                joinMeeting(methodCall, result);
                break;
            case "meeting_status":
                meetingStatus(result);
                break;
            default:
                result.notImplemented();
        }

    }

    private void init(final MethodCall methodCall, final MethodChannel.Result result) {

        Map<String, String> options = methodCall.arguments();

        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if (zoomSDK.isInitialized()) {
            List<Integer> response = Arrays.asList(0, 0);
            result.success(response);
            return;
        }

        ZoomSDKInitParams initParams = new ZoomSDKInitParams();
        initParams.appKey = options.get("appKey");
        initParams.appSecret = options.get("appSecret");
        initParams.domain = options.get("domain");
        initParams.enableLog = true;
        initParams.logSize = 50;
        initParams.videoRawDataMemoryMode = ZoomSDKRawDataMemoryMode.ZoomSDKRawDataMemoryModeStack;
        zoomSDK.initialize(
                context,
                new ZoomSDKInitializeListener() {
                    @Override
                    public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
                        List<Integer> response = Arrays.asList(errorCode, internalErrorCode);

                        if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
                            System.out.println("Failed to initialize Zoom SDK");
                            result.success(response);
                            return;
                        }

                        ZoomSDK zoomSDK = ZoomSDK.getInstance();
                        MeetingService meetingService = zoomSDK.getMeetingService();
                        zoomSDK.getMeetingSettingsHelper().setCustomizedMeetingUIEnabled(true);
                        ZoomSDK.getInstance().getSmsService().enableZoomAuthRealNameMeetingUIShown(false);
                        ZoomSDK.getInstance().getMeetingSettingsHelper().enable720p(false);
                        meetingStatusChannel.setStreamHandler(new StatusStreamHandler(meetingService));
                        result.success(response);
                    }

                    @Override
                    public void onZoomAuthIdentityExpired() {

                    }
                },
                initParams);
    }

    private void joinMeeting(MethodCall methodCall, final MethodChannel.Result result) {
        final Map<String, String> options = methodCall.arguments();

        ZoomSDK.getInstance().getMeetingService().addListener(new MeetingServiceListener() {
            @Override
            public void onMeetingStatusChanged(MeetingStatus meetingStatus, int errorCode, int internalErrorCode) {
                Log.d(TAG, "onMeetingStatusChanged " + meetingStatus);
                if (meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
                    activity.startActivity(MyMeetingActivity.getLaunchIntent(activity));
                    //result.success(true);
                }
            }
        });

        JoinMeetingParams params = new JoinMeetingParams();
        params.meetingNo = options.get("meetingId");
        params.password = options.get("meetingPassword");
        params.displayName = options.get("name");
        ZoomSDK.getInstance().getMeetingService().joinMeetingWithParams(context, params);
    }

    private boolean parseBoolean(Map<String, String> options, String property, boolean defaultValue) {
        return options.get(property) == null ? defaultValue : Boolean.parseBoolean(options.get(property));
    }


    private void meetingStatus(MethodChannel.Result result) {

        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if (!zoomSDK.isInitialized()) {
            System.out.println("Not initialized!!!!!!");
            result.success(Arrays.asList("MEETING_STATUS_UNKNOWN", "SDK not initialized"));
            return;
        }

        MeetingService meetingService = zoomSDK.getMeetingService();

        if (meetingService == null) {
            result.success(Arrays.asList("MEETING_STATUS_UNKNOWN", "No status available"));
            return;
        }

        MeetingStatus status = meetingService.getMeetingStatus();
        result.success(status != null ? Arrays.asList(status.name(), "") : Arrays.asList("MEETING_STATUS_UNKNOWN", "No status available"));
    }

    @Override
    public void dispose() {
    }


    @Override
    public void onZoomSDKLoginResult(long result) {

    }

    @Override
    public void onZoomSDKLogoutResult(long result) {

    }

    @Override
    public void onZoomIdentityExpired() {

    }

    @Override
    public void onZoomAuthIdentityExpired() {

    }
}