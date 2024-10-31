package com.gitgle.utils;

import org.apache.commons.codec.digest.DigestUtils;

public class Md5Util {

    public final static String md5Key = "gitgle";

    public static String md5(String strPwd, String Key) {
        return DigestUtils.md5Hex(strPwd + Key);
    }


    public static boolean passwordVerify(String pwdStr, String oldPwd, String key) {
        String md5Pwd= md5(pwdStr, key);
        return md5Pwd.equalsIgnoreCase(oldPwd);
    }

}

