package com.bravem.app.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bravem.app.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

public class DialogUtils {

    public interface DialogCallback {
        void onConfirm();
    }

    public interface MenuCallback {
        void onItemSelected(int index);
    }

    public static void showConfirmation(Context context, String title, String message, String positiveText, DialogCallback callback) {
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.Theme_BraveM_BottomSheetDialog);
        dialog.setContentView(R.layout.layout_modern_dialog_confirmation);

        TextView titleView = dialog.findViewById(R.id.dialog_title);
        TextView messageView = dialog.findViewById(R.id.dialog_message);
        MaterialButton btnPositive = dialog.findViewById(R.id.btn_positive);
        MaterialButton btnNegative = dialog.findViewById(R.id.btn_negative);

        if (titleView != null) titleView.setText(title);
        if (messageView != null) messageView.setText(message);
        if (btnPositive != null) {
            btnPositive.setText(positiveText);
            btnPositive.setOnClickListener(v -> {
                dialog.dismiss();
                if (callback != null) callback.onConfirm();
            });
        }

        if (btnNegative != null) {
            btnNegative.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    public static void showOptions(Context context, String title, String[] options, int[] icons, MenuCallback callback) {
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.Theme_BraveM_BottomSheetDialog);
        dialog.setContentView(R.layout.layout_modern_dialog_options);

        TextView titleView = dialog.findViewById(R.id.dialog_title);
        LinearLayout optionsContainer = dialog.findViewById(R.id.options_container);

        if (titleView != null) titleView.setText(title);

        if (optionsContainer != null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            for (int i = 0; i < options.length; i++) {
                MaterialButton button = (MaterialButton) inflater.inflate(R.layout.view_modern_option_button, optionsContainer, false);
                button.setText(options[i]);
                if (icons != null && i < icons.length && icons[i] != 0) {
                    button.setIconResource(icons[i]);
                }
                
                final int index = i;
                button.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (callback != null) callback.onItemSelected(index);
                });
                
                optionsContainer.addView(button);
            }
        }

        dialog.show();
    }
}
