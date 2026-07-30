package com.bravem.app.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bravem.app.R;
import com.bravem.app.databinding.LayoutModernDialogConfirmationBinding;
import com.bravem.app.databinding.LayoutModernDialogOptionsBinding;
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
        LayoutModernDialogConfirmationBinding binding = LayoutModernDialogConfirmationBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        binding.dialogTitle.setText(title);
        binding.dialogMessage.setText(message);
        binding.btnPositive.setText(positiveText);

        binding.btnNegative.setOnClickListener(v -> dialog.dismiss());
        binding.btnPositive.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) callback.onConfirm();
        });

        dialog.show();
    }

    public static void showOptions(Context context, String title, String[] options, int[] icons, MenuCallback callback) {
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.Theme_BraveM_BottomSheetDialog);
        LayoutModernDialogOptionsBinding binding = LayoutModernDialogOptionsBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        binding.dialogTitle.setText(title);

        LayoutInflater inflater = LayoutInflater.from(context);
        for (int i = 0; i < options.length; i++) {
            MaterialButton button = (MaterialButton) inflater.inflate(R.layout.view_modern_option_button, binding.optionsContainer, false);
            button.setText(options[i]);
            if (icons != null && i < icons.length && icons[i] != 0) {
                button.setIconResource(icons[i]);
            }
            
            final int index = i;
            button.setOnClickListener(v -> {
                dialog.dismiss();
                if (callback != null) callback.onItemSelected(index);
            });
            
            binding.optionsContainer.addView(button);
        }

        dialog.show();
    }
}
