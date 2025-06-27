package com.surpasslike.welcomate.utils;

import android.text.TextUtils;

import com.surpasslike.welcomate.constants.AppConstants;

/**
 * 输入验证工具类
 * 提供统一的输入验证方法
 */
public class ValidationUtils {
    
    /**
     * 验证用户名是否有效
     * 
     * @param username 用户名
     * @return true表示有效，false表示无效
     */
    public static boolean isValidUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            return false;
        }
        
        int length = username.trim().length();
        return length >= AppConstants.TextLimit.USERNAME_MIN_LENGTH && 
               length <= AppConstants.TextLimit.USERNAME_MAX_LENGTH;
    }
    
    /**
     * 验证密码是否有效
     * 
     * @param password 密码
     * @return true表示有效，false表示无效
     */
    public static boolean isValidPassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }
        
        int length = password.length();
        return length >= AppConstants.TextLimit.PASSWORD_MIN_LENGTH && 
               length <= AppConstants.TextLimit.PASSWORD_MAX_LENGTH;
    }
    
    /**
     * 验证账号是否有效（非空）
     * 
     * @param account 账号
     * @return true表示有效，false表示无效
     */
    public static boolean isValidAccount(String account) {
        return !TextUtils.isEmpty(account) && !TextUtils.isEmpty(account.trim());
    }
}