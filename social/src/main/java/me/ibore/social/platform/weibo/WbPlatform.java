package me.ibore.social.platform.weibo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.VideoSourceObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;
import com.sina.weibo.sdk.utils.Utility;

import java.io.File;

import bolts.Task;
import me.ibore.social.SocialSDK;
import me.ibore.social.common.SocialConstants;
import me.ibore.social.common.ThumbDataContinuation;
import me.ibore.social.exception.SocialError;
import me.ibore.social.listener.OnLoginListener;
import me.ibore.social.model.ShareObj;
import me.ibore.social.model.SocialSDKConfig;
import me.ibore.social.platform.AbsPlatform;
import me.ibore.social.platform.IPlatform;
import me.ibore.social.platform.PlatformCreator;
import me.ibore.social.platform.Target;
import me.ibore.social.utils.BitmapUtils;
import me.ibore.social.utils.CommonUtils;
import me.ibore.social.utils.FileUtils;
import me.ibore.social.utils.SocialLogUtils;

/**
 * sina平台实现
 * 文本相同的分享不允许重复发送，会发送不出去
 * 分享支持的检测
 * <p>
 * Created by Jeeson on 2018/6/20.
 */
public class WbPlatform extends AbsPlatform {

    private static final String TAG = WbPlatform.class.getSimpleName();

    private WbShareHandler mWbShareHandler;
    private WbLoginHelper mLoginHelper;
    private AuthInfo mAuthInfo;

    public static class Creator implements PlatformCreator {
        @Override
        public IPlatform create(Context context, int target) {

            IPlatform platform = null;
            SocialSDKConfig config = SocialSDK.getConfig();
            if (!CommonUtils.isAnyEmpty(config.getSinaAppId(), config.getAppName()
                    , config.getSinaRedirectUrl(), config.getSinaScope())) {
                platform = new WbPlatform(context, config.getSinaAppId(), config.getAppName()
                        , config.getSinaRedirectUrl(), config.getSinaScope());
            }
            return platform;
        }
    }

    WbPlatform(Context context, String appId, String appName, String redirectUrl, String scope) {
        super(context, appId, appName);
        mAuthInfo = new AuthInfo(context, appId, redirectUrl, scope);
        WbSdk.install(context, mAuthInfo);
    }

    @Override
    public boolean isInstall(Context context) {
        return mAuthInfo != null;
    }

    @Override
    public void recycle() {
        super.recycle();
        mWbShareHandler = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mLoginHelper != null)
            mLoginHelper.authorizeCallBack(requestCode, resultCode, data);
    }

    @Override
    public void handleIntent(Activity activity) {
        if (mOnShareListener != null && activity instanceof WbShareCallback) {
            mWbShareHandler.doResultIntent(activity.getIntent(), (WbShareCallback) activity);
        }
    }

    @Override
    public void onResponse(Object resp) {
        if (resp instanceof Integer && mOnShareListener != null) {
            switch ((int) resp) {
                case WBConstants.ErrorCode.ERR_OK:
                    // 分享成功
                    mOnShareListener.onSuccess();
                    break;
                case WBConstants.ErrorCode.ERR_CANCEL:
                    // 分享取消
                    mOnShareListener.onCancel();
                    break;
                case WBConstants.ErrorCode.ERR_FAIL:
                    // 分享失败
                    mOnShareListener.onFailure(new SocialError("微博分享失败"));
                    break;
            }
        }
    }


    @Override
    public void login(Activity activity, OnLoginListener loginListener) {
        makeLoginHelper(activity).login(activity, loginListener);
    }

    private WbLoginHelper makeLoginHelper(Activity activity) {
        if (mLoginHelper == null) {
            mLoginHelper = new WbLoginHelper(activity);
        }
        return mLoginHelper;
    }

    @Override
    public int getPlatformType() {
        return Target.PLATFORM_WB;
    }

    @Override
    protected void shareOpenApp(int shareTarget, Activity activity, ShareObj obj) {
        boolean rst = CommonUtils.openApp(activity, SocialConstants.SINA_PKG);
        if (rst) {
            mOnShareListener.onSuccess();
        } else {
            mOnShareListener.onFailure(new SocialError("open app error"));
        }
    }

    @Override
    public void shareText(int shareTarget, Activity activity, final ShareObj obj) {
        WeiboMultiMessage multiMessage = new WeiboMultiMessage();
        multiMessage.textObject = getTextObj(obj.getSummary());
        sendWeiboMultiMsg(activity, multiMessage);
    }

    @Override
    public void shareImage(int shareTarget, final Activity activity, final ShareObj obj) {
        BitmapUtils.getStaticSizeBitmapByteByPathTask(obj.getThumbImagePath(), THUMB_IMAGE_SIZE)
                .continueWith(new ThumbDataContinuation(TAG, "shareImage", mOnShareListener) {
                    @Override
                    public void onSuccess(byte[] thumbData) {
                        if (thumbData != null && thumbData.length > THUMB_IMAGE_SIZE) {
                            mOnShareListener.onFailure(new SocialError("图片太大，分享失败。"));
                            return;
                        }
                        WeiboMultiMessage multiMessage = new WeiboMultiMessage();
                        multiMessage.imageObject = getImageObj(obj.getThumbImagePath(), thumbData);
                        multiMessage.textObject = getTextObj(obj.getSummary());
                        sendWeiboMultiMsg(activity, multiMessage);
                    }
                }, Task.UI_THREAD_EXECUTOR);

    }

    @Override
    public void shareApp(int shareTarget, Activity activity, ShareObj obj) {
        SocialLogUtils.e(TAG, "sina不支持app分享，将以web形式分享");
        shareWeb(shareTarget, activity, obj);
    }

    @Override
    public void shareWeb(int shareTarget, final Activity activity, final ShareObj obj) {
        BitmapUtils.getStaticSizeBitmapByteByPathTask(obj.getThumbImagePath(), THUMB_IMAGE_SIZE)
                .continueWith(new ThumbDataContinuation(TAG, "shareWeb", mOnShareListener) {
                    @Override
                    public void onSuccess(byte[] thumbData) {
                        WeiboMultiMessage multiMessage = new WeiboMultiMessage();
                        multiMessage.mediaObject = getWebObj(obj, thumbData);
                        sendWeiboMultiMsg(activity, multiMessage);
                    }
                }, Task.UI_THREAD_EXECUTOR);
    }

    @Override
    public void shareMusic(int shareTarget, final Activity activity, final ShareObj obj) {
        shareWeb(shareTarget, activity, obj);
    }

    @Override
    public void shareVideo(int shareTarget, final Activity activity, final ShareObj obj) {
        String mediaPath = obj.getMediaPath();
        if (FileUtils.isExist(mediaPath)) {
            WeiboMultiMessage multiMessage = new WeiboMultiMessage();
            multiMessage.videoSourceObject = getVideoObj(obj, null);
            sendWeiboMultiMsg(activity, multiMessage);
        } else {
            shareWeb(shareTarget, activity, obj);
        }
    }


    private void sendWeiboMultiMsg(Activity activity, WeiboMultiMessage message) {
        if (mWbShareHandler == null) {
            mWbShareHandler = new WbShareHandler(activity);
            mWbShareHandler.registerApp();
        }
        if (mWbShareHandler != null && message != null) {
            mWbShareHandler.shareMessage(message, false);
        }
    }


    /**
     * 根据ShareMediaObj配置来检测是不是添加文字和照片
     *
     * @param thumbData    图片数组
     * @param multiMessage msg
     * @param obj          share
     */
    private void checkAddTextAndImageObj(WeiboMultiMessage multiMessage, ShareObj obj, byte[] thumbData) {
        if (obj.isSinaWithPicture())
            multiMessage.imageObject = getImageObj(obj.getThumbImagePath(), thumbData);
        if (obj.isSinaWithSummary())
            multiMessage.textObject = getTextObj(obj.getSummary());
    }


    private TextObject getTextObj(String summary) {
        if (TextUtils.isEmpty(summary)) return null;

        TextObject textObject = new TextObject();
        textObject.text = summary;
        return textObject;
    }


    private ImageObject getImageObj(String localPath, byte[] data) {
        if (TextUtils.isEmpty(localPath) || data == null) return null;

        ImageObject imageObject = new ImageObject();
        //设置缩略图。 注意：最终压缩过的缩略图大小不得超过 32kb。
        imageObject.imageData = data;
        imageObject.imagePath = localPath;
        return imageObject;
    }


    private WebpageObject getWebObj(ShareObj obj, byte[] thumbData) {
        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = obj.getTitle();
        mediaObject.description = obj.getSummary();
        // 注意：最终压缩过的缩略图大小不得超过 32kb。
        mediaObject.thumbData = thumbData;
        mediaObject.actionUrl = obj.getTargetUrl();
        mediaObject.defaultText = obj.getSummary();
        return mediaObject;
    }


    private VideoSourceObject getVideoObj(ShareObj obj, byte[] thumbData) {
        VideoSourceObject mediaObject = new VideoSourceObject();
        mediaObject.videoPath = Uri.fromFile(new File(obj.getMediaPath()));
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = obj.getTitle();
        mediaObject.description = obj.getSummary();
        // 注意：最终压缩过的缩略图大小不得超过 32kb。
//        mediaObject.thumbData = thumbData;
        mediaObject.actionUrl = obj.getTargetUrl();
        mediaObject.during = obj.getDuration() == 0 ? 10 : obj.getDuration();
        return mediaObject;
    }
}
