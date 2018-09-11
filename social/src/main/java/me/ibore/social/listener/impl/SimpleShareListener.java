package me.ibore.social.listener.impl;

import me.ibore.social.exception.SocialError;
import me.ibore.social.listener.OnShareListener;
import me.ibore.social.model.ShareObj;

/**
 * 简化版本分享监听
 *
 * Created by Jeeson on 2018/6/20.
 */
public class SimpleShareListener implements OnShareListener {

    @Override
    public void onStart(int shareTarget, ShareObj obj) {
    }

    @Override
    public ShareObj onPrepareInBackground(int shareTarget, ShareObj obj) throws Exception {
        return obj;
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure(SocialError e) {

    }

    @Override
    public void onCancel() {

    }
}
