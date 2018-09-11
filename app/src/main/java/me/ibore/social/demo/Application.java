package me.ibore.social.demo;

import me.ibore.social.SocialSDK;
import me.ibore.social.common.SocialConstants;
import me.ibore.social.model.SocialSDKConfig;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        String qqAppId = "xxx";
        String wxAppId = "xxx";
        String wxSecretKey = "xxx";
        String sinaAppId = "xxx";

        SocialSDKConfig config = new SocialSDKConfig(this)
                // 配置qq
                .qq(qqAppId)
                // 配置wx
                .wechat(wxAppId, wxSecretKey)
                // 配置sina
                .sina(sinaAppId)
                // 配置Sina授权scope,有默认值，默认值 all
                .sinaScope(SocialConstants.SCOPE);
        // 👮 添加 config 数据，必须
        SocialSDK.init(config);
        // 👮 添加自定义的 json 解析，必须
        SocialSDK.setJsonAdapter(new SocialSDKJsonAdapter());
        // 👮 添加自定义的网络请求，非必须(可切换为Glide下载图片)
        SocialSDK.setRequestAdapter(null);
    }
}
