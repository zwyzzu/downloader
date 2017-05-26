package com.yixia.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Author: zhangwy(张维亚)
 * Email:  zhangweiya@yixia.com
 * 创建时间:2017/5/3 下午3:49
 * 修改时间:2017/5/3 下午3:49
 * Description:
 */

public class Screen {

    private static DisplayMetrics getDisplayMetrics(Context ctx) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics;
    }

    /**
     * 获取手机分辨率
     */
    public static String getResolution(Context ctx) {
        DisplayMetrics metrics = getDisplayMetrics(ctx);
        return metrics.widthPixels + "*" + metrics.heightPixels;
    }

    public static int getScreenWidth(Context ctx) {
        return getDisplayMetrics(ctx).widthPixels;
    }

    public static int getScreenHeight(Context ctx) {
        return getDisplayMetrics(ctx).heightPixels;
    }

    public static float getScreenDensity(Context ctx) {
        return getDisplayMetrics(ctx).density;
    }

    public static float getScreenScaledDensity(Context ctx) {
        DisplayMetrics dm = getDisplayMetrics(ctx);
        return dm.scaledDensity;
    }

    public static float getScreenDpi(Context ctx) {
        return getDisplayMetrics(ctx).densityDpi;
    }

    /**
     * According to the resolution of the phone from the dp unit will become a px (pixels)
     */
    public static int dip2px(Context ctx, int dip) {
        float density = getScreenDensity(ctx);
        return (int) (dip * density + 0.5f);
    }

    /**
     * According to the resolution of the phone from the dp unit will become a px (pixels)
     */
    public static float dip2px(Context ctx, float dip) {
        float density = getScreenDensity(ctx);
        return (dip * density + 0.5f);
    }

    /**
     * Turn from the units of px (pixels) become dp according to phone resolution
     */
    public static int px2dip(Context ctx, float px) {
        float density = getScreenDensity(ctx);
        return (int) (px / density + 0.5f);
    }

    public static int px2sp(Context ctx, float px) {
        float scale = getScreenScaledDensity(ctx);
        return (int) (px / scale + 0.5f);
    }

    public static int sp2px(Context ctx, int sp) {
        float scale = getScreenScaledDensity(ctx);
        return (int) (sp * scale + 0.5f);
    }
}
