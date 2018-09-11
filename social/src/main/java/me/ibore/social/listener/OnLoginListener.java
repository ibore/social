package me.ibore.social.listener;

import me.ibore.social.exception.SocialError;
import me.ibore.social.model.LoginResult;

/**
 * 登陆监听回调
 * Created by Jeeson on 2018/6/20.
 */
public interface OnLoginListener {

    void onStart();

    void onSuccess(LoginResult loginResult);

    void onCancel();

    void onFailure(SocialError e);
}
