package com.bravem.app.utils;

import android.view.View;
import android.view.ViewGroup;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UiUtils {

    public static void applyEdgeToEdge(ComponentActivity activity) {
        EdgeToEdge.enable(activity);
    }

    public static void handleTopInset(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    public static void handleBottomInset(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });
    }

    public static void handleFabBottomInset(View view, int baseMarginDp) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            float density = v.getResources().getDisplayMetrics().density;
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.bottomMargin = (int) (baseMarginDp * density) + systemBars.bottom;
            v.setLayoutParams(lp);
            return insets;
        });
    }
}
