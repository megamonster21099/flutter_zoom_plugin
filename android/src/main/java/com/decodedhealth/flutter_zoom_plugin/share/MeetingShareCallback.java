package com.decodedhealth.flutter_zoom_plugin.share;

import us.zoom.sdk.InMeetingShareController;
import us.zoom.sdk.ZoomSDK;
import com.decodedhealth.flutter_zoom_plugin.BaseCallback;
import com.decodedhealth.flutter_zoom_plugin.BaseEvent;

public class MeetingShareCallback extends BaseCallback<MeetingShareCallback.ShareEvent> {

    public interface ShareEvent extends BaseEvent {

        void onShareActiveUser(long userId);

        void onShareUserReceivingStatus(long userId);

    }

    static MeetingShareCallback instance;

    private MeetingShareCallback() {
        init();
    }


    protected void init() {
        ZoomSDK.getInstance().getInMeetingService().getInMeetingShareController().addListener(shareListener);
    }

    public static MeetingShareCallback getInstance() {
        if (null == instance) {
            synchronized (MeetingShareCallback.class) {
                if (null == instance) {
                    instance = new MeetingShareCallback();
                }
            }
        }
        return instance;
    }

    InMeetingShareController.InMeetingShareListener shareListener = new InMeetingShareController.InMeetingShareListener() {
        @Override
        public void onShareActiveUser(long userId) {

            for (ShareEvent event : callbacks) {
                event.onShareActiveUser(userId);
            }
        }

        @Override
        public void onShareUserReceivingStatus(long userId) {

            for (ShareEvent event : callbacks) {
                event.onShareUserReceivingStatus(userId);
            }
        }
    };

}
