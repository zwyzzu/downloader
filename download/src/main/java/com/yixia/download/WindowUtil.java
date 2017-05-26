package com.yixia.download;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

public class WindowUtil {

    public static Dialog createAlertDialog(Context ctx, int titleId, String message, int onOkId, OnClickListener onOKClick, int onCancelId, OnClickListener onCancelClick) {
        return createAlertDialog(ctx, titleId, message, ctx.getString(onOkId), onOKClick, ctx.getString(onCancelId), onCancelClick);
    }

    public static Dialog createAlertDialog(Context ctx, int titleId, String message, String onOKText, OnClickListener onOKClick, String onCancelText, OnClickListener onCancelClick) {
        return new AlertDialog.Builder(ctx)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(ctx.getString(titleId))
                .setMessage(message)
                .setPositiveButton(onOKText, onOKClick)
                .setNegativeButton(onCancelText, onCancelClick)
                .create();
    }

    public static Dialog createSimpleTipAlertDialog(Context ctx, int titleId, String message) {
        return createAlertDialog(ctx, titleId, message, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public static Dialog createAlertDialog(Context ctx, int titleId, String message, OnClickListener onOKClick) {
        return createAlertDialog(ctx, titleId, message, onOKClick, true);
    }

    public static Dialog createAlertDialog(Context ctx, int titleId, String message, OnClickListener onOKClick, boolean isOk) {
        return new AlertDialog.Builder(ctx)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(ctx.getString(titleId))
                .setMessage(message)
                .setPositiveButton(isOk ? R.string.ok : R.string.dialog_close, onOKClick)
                .create();
    }

    public static Dialog createAlertDialog(Context ctx, int titleId, View view, OnClickListener onOKClick, OnClickListener onCancelClick) {
        return createAlertDialog(ctx, titleId, view, onOKClick, R.string.ok, onCancelClick, R.string.cancel);
    }

    public static Dialog createAlertDialog(Context ctx, int titleId, View view, OnClickListener onOKClick, int okId, OnClickListener onCancelClick, int cancelId) {
        return new AlertDialog.Builder(ctx).setIcon(R.drawable.alert_dialog_icon)
                .setTitle(ctx.getString(titleId)).setView(view)
                .setPositiveButton(okId == 0 ? R.string.ok : okId, onOKClick)
                .setNegativeButton(cancelId == 0 ? R.string.cancel : cancelId, onCancelClick)
                .create();
    }

    public static ProgressDialog createProgressDialog(Context ctx, int titleId, int messageId, boolean indeterminate, boolean cancelable) {
        return createProgressDialog(ctx, titleId, ctx.getResources().getString(messageId),
                indeterminate, cancelable);
    }

    public static ProgressDialog createProgressDialog(Context ctx, int titleId, String message, boolean indeterminate, boolean cancelable) {
        String title = titleId == 0 ? "" : ctx.getString(titleId);
        ProgressDialog dialog = new ProgressDialog(ctx);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(cancelable);
        return dialog;
    }
}
