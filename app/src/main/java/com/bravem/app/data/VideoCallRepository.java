package com.bravem.app.data;

import android.content.Context;
import android.util.Log;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class VideoCallRepository {
    private static final String TAG = "VideoCallRepository";
    private static final String APP_ID = "48398f5ff29a4a25ad15c2be812431d5";

    private RtcEngine mRtcEngine;
    private final Context mContext;

    public VideoCallRepository(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void initEngine(IRtcEngineEventHandler handler) {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = mContext;
            config.mAppId = APP_ID;
            config.mEventHandler = handler;
            mRtcEngine = RtcEngine.create(config);
            
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
            
            mRtcEngine.enableVideo();
            mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
            ));
        } catch (Exception e) {
            Log.e(TAG, "RtcEngine creation failed", e);
        }
    }

    public RtcEngine getEngine() {
        return mRtcEngine;
    }

    public void joinChannel(String channelId, String token, int uid) {
        if (mRtcEngine != null) {
            ChannelMediaOptions options = new ChannelMediaOptions();
            options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
            options.autoSubscribeAudio = true;
            options.autoSubscribeVideo = true;
            options.publishCameraTrack = true;
            options.publishMicrophoneTrack = true;
            mRtcEngine.joinChannel(token, channelId, uid, options);
        }
    }

    public void leaveChannel() {
        if (mRtcEngine != null) {
            mRtcEngine.stopPreview();
            mRtcEngine.leaveChannel();
        }
    }

    public void startPreview() {
        if (mRtcEngine != null) {
            mRtcEngine.startPreview();
        }
    }

    public void destroy() {
        if (mRtcEngine != null) {
            RtcEngine.destroy();
            mRtcEngine = null;
        }
    }
}
