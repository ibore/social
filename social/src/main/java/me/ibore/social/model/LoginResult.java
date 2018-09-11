package me.ibore.social.model;

/**
 * 登陆结果
 * Created by Jeeson on 2018/6/20.
 */
public class LoginResult {

    // 登陆的类型，对应 Target.LOGIN_QQ 等。。。
    private int type;
    // 返回的基本用户信息
    // 针对登录类型可强转为 WbUser,WxUser,QQUser 来获取更加丰富的信息
    private SocialUser mBaseUser;
    // 本次登陆的 token 信息，openid,unionid,token,expires_in
    private AccessToken mBaseToken;

    public LoginResult(int type, SocialUser baseUser, AccessToken baseToken) {
        this.type = type;
        mBaseUser = baseUser;
        mBaseToken = baseToken;
    }

    public int getType() {
        return type;
    }

    public SocialUser getBaseUser() {
        return mBaseUser;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setBaseUser(SocialUser baseUser) {
        mBaseUser = baseUser;
    }

    public AccessToken getBaseToken() {
        return mBaseToken;
    }

    public void setBaseToken(AccessToken baseToken) {
        mBaseToken = baseToken;
    }

    @Override
    public String toString() {
        return "LoginResult{" +
                "type=" + type +
                ", mBaseUser=" + mBaseUser.toString() +
                '}';
    }
}
