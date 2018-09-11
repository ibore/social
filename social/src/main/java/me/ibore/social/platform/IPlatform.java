package me.ibore.social.platform;

import android.app.Activity;
import android.content.Context;

import me.ibore.social.listener.OnLoginListener;
import me.ibore.social.listener.OnShareListener;
import me.ibore.social.listener.PlatformLifeCircle;
import me.ibore.social.model.ShareObj;

/**
 * 平台接口协议
 * Created by Jeeson on 2018/6/20.
 */
public interface IPlatform extends PlatformLifeCircle {

    // 检测参数配置
    boolean checkPlatformConfig();

    // 初始化分享监听
    void initOnShareListener(OnShareListener listener);

    // 是否安装
    boolean isInstall(Context context);

    // 发起登录
    void login(Activity activity, OnLoginListener onLoginListener);

    // 发起分享
    void share(Activity activity, int shareTarget, ShareObj shareMediaObj);

    int getPlatformType();
}
