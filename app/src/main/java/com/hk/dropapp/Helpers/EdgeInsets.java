package com.hk.dropapp.Helpers;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Android 15+ uygulamaları kenardan kenara (edge-to-edge) çizer.
 * Araç çubuğunu durum çubuğunun altına, içeriği gezinme çubuğunun ve klavyenin üstüne iter.
 */
public final class EdgeInsets {
    private EdgeInsets() {
    }

    public static void apply(View root, View appBar) {
        final int tbTop = appBar.getPaddingTop();
        final int rootBottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            appBar.setPadding(appBar.getPaddingLeft(), tbTop + bars.top,
                    appBar.getPaddingRight(), appBar.getPaddingBottom());
            v.setPadding(bars.left, v.getPaddingTop(), bars.right,
                    rootBottom + Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
