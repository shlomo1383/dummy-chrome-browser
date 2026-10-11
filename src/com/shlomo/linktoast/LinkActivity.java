package com.shlomo.linktoast;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

public class LinkActivity extends Activity {

    private int variant = 0;

    private void slide(String enter, String exit) {
        int e = getResources().getIdentifier(enter, "anim", getPackageName());
        int x = getResources().getIdentifier(exit, "anim", getPackageName());
        if (e != 0 && x != 0) overridePendingTransition(e, x);
    }

    @Override
    public void finish() {
        super.finish();
        slide("slide_in_left_slight", "slide_out_right");
    }

    private void killMenuBar() {
        final android.view.View root = getWindow().getDecorView();
        hideMenuBar(root);
        root.getViewTreeObserver().addOnGlobalLayoutListener(
            new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                @Override public void onGlobalLayout() { hideMenuBar(root); }
            });
    }

    private void hideMenuBar(android.view.View v) {
        if (v == null) return;
        if ("android.widget.MenuBar".equals(v.getClass().getName())) {
            if (v.getVisibility() != android.view.View.GONE) v.setVisibility(android.view.View.GONE);
            return;
        }
        if (v instanceof android.view.ViewGroup) {
            android.view.ViewGroup g = (android.view.ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) hideMenuBar(g.getChildAt(i));
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideBars();
    }

    private void hideBars() {
        getWindow().setNavigationBarColor(android.graphics.Color.WHITE);
        getWindow().setNavigationBarContrastEnforced(false);
        getWindow().setNavigationBarDividerColor(android.graphics.Color.WHITE);
        getWindow().getDecorView().setBackgroundColor(android.graphics.Color.WHITE);
        getWindow().setDecorFitsSystemWindows(false);
        android.view.WindowInsetsController wc = getWindow().getInsetsController();
        if (wc != null) {
            wc.setSystemBarsAppearance(
                android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
            wc.hide(android.view.WindowInsets.Type.navigationBars());
            wc.setSystemBarsBehavior(
                android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
        getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt("v", variant);
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (b != null) {
            variant = b.getInt("v", 0);
        } else {
            SharedPreferences p = getSharedPreferences("toast", MODE_PRIVATE);
            int n = p.getInt("k", 0);
            variant = n % 8;
            p.edit().putInt("k", n + 1).commit();
        }
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().setNavigationBarDividerColor(Color.WHITE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(new AnimView(this, variant));
        killMenuBar();
        slide("slide_in_right", "slide_out_left_slight");
    }
}
