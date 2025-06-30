package com.surpasslike.welcomate.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast工具类
 * 提供统一的Toast显示方法，简化代码
 */
public class ToastUtils {
    
    /**
     * 显示短时间Toast
     * 
     * @param context 上下文
     * @param message 要显示的消息
     */
    public static void showShort(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 显示长时间Toast
     * 
     * @param context 上下文
     * @param message 要显示的消息
     */
    public static void showLong(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * 显示短时间Toast（通过资源ID）
     * 
     * @param context 上下文
     * @param messageResId 字符串资源ID
     */
    public static void showShort(Context context, int messageResId) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 显示长时间Toast（通过资源ID）
     * 
     * @param context 上下文
     * @param messageResId 字符串资源ID
     */
    public static void showLong(Context context, int messageResId) {
        Toast.makeText(context, messageResId, Toast.LENGTH_LONG).show();
    }
}