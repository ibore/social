package me.ibore.social.listener.impl;

import me.ibore.social.exception.SocialError;
import me.ibore.social.listener.OnLoginListener;
import me.ibore.social.model.LoginResult;

/**
 * 简化版本登录监听
 * Created by Jeeson on 2018/6/20.
 */
public class SimpleLoginListener  implements OnLoginListener {

    @Override
    public void onStart() {

    }

    @Override
    public void onSuccess(LoginResult loginResult) {

    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onFailure(SocialError e) {

    }
}
