package com.skystreamtv.element_ez_stream.updater.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.skystreamtv.element_ez_stream.updater.R;

public class Dialogs {

    public static AlertDialog buildErrorDialog(Context context, String title, String message, final int action) {
        final Activity this_activity = (Activity) context;
        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this_activity);
        dialog_builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNeutralButton(R.string.close_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        ((PlayerUpdaterActivity)this_activity).errorAction(action);
                    }
                });
        return dialog_builder.create();
    }
}
