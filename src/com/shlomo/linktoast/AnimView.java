package com.shlomo.linktoast;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.View;

public class AnimView extends View {

    private static final int RED = 0xFFE53935;
    private static final int WHITE = 0xFFFFFFFF;
    private static final float[] PERIOD = {5f, 3.2f, 4f, 9f, 4.6f, 4f, 4.4f, 5f};

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rf = new RectF();
    private final Typeface light = Typeface.create("sans-serif-light", Typeface.NORMAL);
    private final int variant;
    private final long t0;
    private Canvas c;

    public AnimView(Context ctx, int variant) {
        super(ctx);
        this.variant = variant;
        this.t0 = SystemClock.uptimeMillis();
    }

    private static float clamp(float v) { return v < 0 ? 0 : (v > 1 ? 1 : v); }
    private static float prog(float t, float a, float b) { return clamp((t - a) / (b - a)); }
    private static float eo(float q) { float r = 1 - q; return 1 - r * r * r; }
    private static float eio(float q) {
        if (q < 0.5f) return 4 * q * q * q;
        float r = -2 * q + 2;
        return 1 - r * r * r / 2;
    }
    private static float eob(float q) {
        float c1 = 1.70158f, c3 = c1 + 1, r = q - 1;
        return 1 + c3 * r * r * r + c1 * r * r;
    }
    private static int alpha(int color, float a) {
        return (color & 0x00FFFFFF) | (((int) (clamp(a) * 255)) << 24);
    }
    private static float deg(double rad) { return (float) Math.toDegrees(rad); }

    private void fill(int color) { p.setStyle(Paint.Style.FILL); p.setColor(color); }
    private void stroke(int color, float w) {
        p.setStyle(Paint.Style.STROKE);
        p.setColor(color);
        p.setStrokeWidth(w);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
    }
    private void rrect(float x, float y, float w, float h, float r, int color) {
        fill(color); rf.set(x, y, x + w, y + h); c.drawRoundRect(rf, r, r, p);
    }
    private void strokeRR(float x, float y, float w, float h, float r, int color, float sw) {
        stroke(color, sw); rf.set(x, y, x + w, y + h); c.drawRoundRect(rf, r, r, p);
    }
    private void circ(float x, float y, float r, int color) { fill(color); c.drawCircle(x, y, r, p); }
    private void strokeCirc(float x, float y, float r, int color, float w) {
        stroke(color, w); c.drawCircle(x, y, r, p);
    }
    private void oval(float cx, float cy, float rx, float ry, int color) {
        fill(color); rf.set(cx - rx, cy - ry, cx + rx, cy + ry); c.drawOval(rf, p);
    }
    private void strokeOval(float cx, float cy, float rx, float ry, int color, float w) {
        stroke(color, w); rf.set(cx - rx, cy - ry, cx + rx, cy + ry); c.drawOval(rf, p);
    }
    private void line(float x1, float y1, float x2, float y2, int color, float w) {
        stroke(color, w); c.drawLine(x1, y1, x2, y2, p);
    }
    private void poly(int color, float... xy) {
        path.reset();
        path.moveTo(xy[0], xy[1]);
        for (int i = 2; i < xy.length; i += 2) path.lineTo(xy[i], xy[i + 1]);
        path.close();
        fill(color);
        c.drawPath(path, p);
    }
    private void star(float cx, float cy, float ro, float ri, int n, int color) {
        path.reset();
        for (int i = 0; i < n * 2; i++) {
            float r = (i % 2 == 1) ? ri : ro;
            double a = -Math.PI / 2 + i * Math.PI / n;
            float px = cx + (float) Math.cos(a) * r, py = cy + (float) Math.sin(a) * r;
            if (i == 0) path.moveTo(px, py); else path.lineTo(px, py);
        }
        path.close();
        fill(color);
        c.drawPath(path, p);
    }
    private void clipRR(float x, float y, float w, float h, float r) {
        rf.set(x, y, x + w, y + h);
        path.reset();
        path.addRoundRect(rf, r, r, Path.Direction.CW);
        c.clipPath(path);
    }
    private void clipCirc(float x, float y, float r) {
        path.reset();
        path.addCircle(x, y, r, Path.Direction.CW);
        c.clipPath(path);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        c = canvas;
        canvas.drawColor(WHITE);
        float s = getWidth() / 480f;
        canvas.save();
        canvas.scale(s, s);
        canvas.translate(0, -36);
        float t = ((SystemClock.uptimeMillis() - t0) / 1000f) % PERIOD[variant];
        canvas.save();
        canvas.translate(51.5f, 84);
        canvas.scale(1.45f, 1.45f);
        switch (variant) {
            case 0: sceneBarrier(t); break;
            case 1: sceneLock(t); break;
            case 2: sceneGlobe(t); break;
            case 3: sceneSpace(t); break;
            case 4: sceneRobot(t); break;
            case 5: sceneWifi(t); break;
            case 6: sceneUnplug(t); break;
            default: sceneSearch(t); break;
        }
        canvas.restore();
        tp.setStyle(Paint.Style.FILL);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setTypeface(light);
        tp.setTextSize(58);
        tp.setColor(0xFFD32F2F);
        canvas.drawText((variant == 3 || variant == 4 || variant == 7) ? "Page Not Found" : "Access Denied", 240, 432, tp);
        tp.setTypeface(Typeface.DEFAULT);
        tp.setTextSize(28);
        tp.setColor(0xFF49454F);
        canvas.drawText("Your device is restricted", 240, 482, tp);
        canvas.drawText("from browsing the internet.", 240, 520, tp);
        canvas.restore();
        postInvalidateOnAnimation();
    }

    private void sceneWifi(float t) {
        float cx = 130, by = 150;
        float fade = t > 3.4f ? 1 - prog(t, 3.4f, 3.9f) : 1;
        circ(cx, by, 9, alpha(0xFF42A5F5, fade));
        float[] rr = {30, 55, 80};
        for (int i = 0; i < 3; i++) {
            float a = (t < 1.6f ? prog(t, 0.2f + i * 0.3f, 0.5f + i * 0.3f) : 0.3f) * fade;
            stroke(alpha(0xFF42A5F5, a), 12);
            rf.set(cx - rr[i], by - rr[i], cx + rr[i], by + rr[i]);
            c.drawArc(rf, 225, 90, false, p);
        }
        float s = 0;
        if (t > 1.6f && t < 3.4f) s = eob(prog(t, 1.6f, 2.0f));
        else if (t >= 3.4f) s = 1 - eio(prog(t, 3.4f, 3.9f));
        if (s > 0) {
            c.save();
            c.translate(130, 105);
            c.scale(s, s);
            c.rotate(t < 2.6f ? deg(Math.sin(t * 20) * 0.03) : 0);
            circ(0, 0, 66, alpha(WHITE, 0.92f));
            strokeCirc(0, 0, 60, RED, 12);
            line(-42, -42, 42, 42, RED, 12);
            c.restore();
        }
    }

    private void sceneUnplug(float t) {
        rrect(190, 60, 64, 80, 12, 0xFF37474F);
        rrect(196, 84, 16, 8, 3, 0xFF111111);
        rrect(196, 108, 16, 8, 3, 0xFF111111);
        float px;
        if (t < 1.2f) px = -50 + 180 * eo(prog(t, 0, 1.2f));
        else if (t < 3.0f) px = 130 - 16 * (float) Math.abs(Math.sin((t - 1.2f) * 4)) * (1 - prog(t, 1.2f, 3.0f));
        else if (t < 3.2f) px = 130;
        else px = 130 - 180 * eio(prog(t, 3.2f, 4.2f));
        stroke(0xFF90A4AE, 6);
        path.reset();
        path.moveTo(px, 100);
        path.quadTo(px * 0.5f - 10, 175, -30, 150);
        c.drawPath(path, p);
        rrect(px, 78, 40, 44, 8, 0xFF42A5F5);
        rrect(px + 40, 85, 18, 7, 2, 0xFFCFD8DC);
        rrect(px + 40, 109, 18, 7, 2, 0xFFCFD8DC);
        if (t > 1.2f && t < 3.0f && px > 118 && Math.sin(t * 30) > -0.2) {
            star(190, 100, 11, 4, 6, 0xFFFFC933);
            star(184, 88, 6, 2.5f, 5, 0xFFFFC933);
        }
        if (t > 1.4f && t < 3.4f) {
            float sc = t < 1.8f ? eob(prog(t, 1.4f, 1.8f)) : (t > 3.1f ? 1 - prog(t, 3.1f, 3.4f) : 1);
            c.save();
            c.translate(222, 34);
            c.scale(sc, sc);
            circ(0, 0, 15, RED);
            line(-6, -6, 6, 6, WHITE, 3.5f);
            line(6, -6, -6, 6, WHITE, 3.5f);
            c.restore();
        }
    }

    private void sceneSearch(float t) {
        float w = (float) (2 * Math.PI / 5);
        rrect(80, 30, 100, 130, 10, 0xFFF5F7FA);
        strokeRR(80, 30, 100, 130, 10, 0xFFCFD4D9, 3);
        for (int i = 0; i < 5; i++) rrect(95, 50 + i * 20, 70 - (i % 2) * 24, 7, 3, 0xFFCFD4D9);
        float mx = 130 + 62 * (float) Math.sin(w * t);
        float my = 100 + 24 * (float) Math.sin(2 * w * t + 1);
        line(mx + 19, my + 19, mx + 44, my + 44, 0xFF263238, 10);
        circ(mx, my, 27, alpha(WHITE, 0.55f));
        strokeCirc(mx, my, 27, 0xFF42A5F5, 7);
        if (t > 1.2f && t < 4.7f) {
            float sc = t < 1.6f ? eob(prog(t, 1.2f, 1.6f)) : (t > 4.3f ? 1 - prog(t, 4.3f, 4.7f) : 1);
            c.save();
            c.translate(mx, my - 52 + 3 * (float) Math.sin(t * 6));
            c.scale(sc, sc);
            circ(0, 0, 20, RED);
            tp.setStyle(Paint.Style.FILL);
            tp.setTextAlign(Paint.Align.CENTER);
            tp.setTypeface(Typeface.DEFAULT_BOLD);
            tp.setTextSize(36);
            tp.setColor(WHITE);
            c.drawText("?", 0, 13, tp);
            c.restore();
        }
    }

    private void sceneBarrier(float t) {
        float g = 160;
        line(0, g + 2, 260, g + 2, 0xFFCFD4D9, 4);
        float raised;
        if (t < 0.5f) raised = 1;
        else if (t < 1.1f) raised = 1 - eio(prog(t, 0.5f, 1.1f));
        else if (t < 4.0f) raised = 0;
        else if (t < 4.6f) raised = eio(prog(t, 4.0f, 4.6f));
        else raised = 1;
        float x, lurch = 0;
        if (t < 1.5f) x = -170 + 194 * eo(prog(t, 0, 1.5f));
        else if (t < 3.8f) {
            x = 24;
            lurch = (float) (Math.sin((t - 1.5f) * 18) * Math.exp(-(t - 1.5f) * 3.2) * 0.05);
        }
        else if (t < 4.8f) x = 24 - 194 * eio(prog(t, 3.8f, 4.8f));
        else x = -170;

        rrect(214, 118, 14, 42, 3, 0xFF37474F);
        c.save();
        c.translate(221, 128);
        c.rotate(raised * 70);
        rrect(-100, -5, 100, 10, 3, WHITE);
        c.save();
        clipRR(-100, -5, 100, 10, 3);
        fill(RED);
        for (int i = 0; i < 8; i += 2) c.drawRect(-100 + i * 14, -6, -100 + i * 14 + 14, 6, p);
        c.restore();
        c.restore();
        circ(221, 128, 7, RED);
        circ(221, 128, 3, WHITE);

        if (t > 1.1f && t < 2.1f) {
            float q = prog(t, 1.1f, 2.1f);
            for (int i = 0; i < 3; i++)
                circ(x + 8 - q * (14 + i * 10), 154 - q * (8 + i * 5), 3 + q * (5 + i * 2),
                     alpha(0xFFB0BEC5, (1 - q) * 0.7f));
        }

        c.save();
        c.translate(x + 40, g);
        c.rotate(deg(lurch));
        c.translate(-(x + 40), -g);
        rrect(x, 128, 80, 24, 8, 0xFF42A5F5);
        poly(0xFF42A5F5, x + 14, 128, x + 24, 110, x + 54, 110, x + 66, 128);
        poly(0xFFE3F2FD, x + 20, 128, x + 27, 114, x + 38, 114, x + 38, 128);
        poly(0xFFE3F2FD, x + 43, 128, x + 43, 114, x + 52, 114, x + 60, 128);
        circ(x + 77, 136, 3.5f, 0xFFFFC933);
        boolean braking = t > 1.2f && t < 3.8f;
        boolean on = braking && Math.sin(t * 16) > -0.3;
        if (on) circ(x + 3, 136, 9, alpha(0xFFFF1744, 0.3f));
        circ(x + 3, 136, 3.5f, on ? 0xFFFF1744 : 0xFFB0BEC5);
        float a = x / 9;
        float[] wxs = {x + 17, x + 63};
        for (float wx : wxs) {
            circ(wx, 151, 9, 0xFF263238);
            circ(wx, 151, 4, 0xFFB0BEC5);
            float ca = (float) Math.cos(a) * 3.5f, sa = (float) Math.sin(a) * 3.5f;
            line(wx - ca, 151 - sa, wx + ca, 151 + sa, 0xFF263238, 1.5f);
            line(wx - sa, 151 + ca, wx + sa, 151 - ca, 0xFF263238, 1.5f);
        }
        c.restore();

        if (t > 1.7f && t < 3.9f) {
            float sc = t < 2.1f ? eob(prog(t, 1.7f, 2.1f)) : (t > 3.6f ? 1 - prog(t, 3.6f, 3.9f) : 1);
            c.save();
            c.translate(x + 40, 82);
            c.scale(sc, sc);
            circ(0, 0, 15, RED);
            rrect(-2.5f, -8, 5, 10, 2, WHITE);
            circ(0, 6, 2.8f, WHITE);
            c.restore();
        }
    }

    private void sceneLock(float t) {
        float cx = 130, by = 178;
        oval(130, 186, 62, 7, 0xFFECEFF1);
        float[] rs = {0.45f, 0.75f, 1.05f, 2.05f, 2.35f};
        for (float s : rs) {
            float q = prog(t, s, s + 0.8f);
            if (q > 0 && q < 1) strokeCirc(cx, 132, 48 + q * 62, alpha(RED, 0.5f * (1 - q)), 3);
        }
        float ang = 0, lift = 0;
        float[][] ws = {{0.4f, 1.4f}, {2.0f, 2.8f}};
        for (float[] w : ws) {
            if (t > w[0] && t < w[1]) {
                float q = prog(t, w[0], w[1]);
                ang = (float) (Math.sin((t - w[0]) * 38) * 0.16 * (1 - q));
                lift = (float) (7 * Math.sin(Math.min(1f, q * 2) * Math.PI));
            }
        }
        c.save();
        c.translate(cx, by);
        c.rotate(deg(ang));
        c.translate(-cx, -by);
        stroke(0xFF90A4AE, 14);
        path.reset();
        path.moveTo(100, 100 - lift);
        path.lineTo(100, 74 - lift);
        rf.set(100, 44 - lift, 160, 104 - lift);
        path.arcTo(rf, 180, 180);
        path.lineTo(160, 100 - lift);
        c.drawPath(path, p);
        rrect(78, 100, 104, 78, 14, RED);
        rrect(86, 108, 10, 62, 5, 0x2EFFFFFF);
        circ(130, 130, 10, 0xFF7F1D1D);
        poly(0xFF7F1D1D, 125, 134, 135, 134, 138, 156, 122, 156);
        c.restore();
    }

    private void sceneGlobe(float t) {
        float cx = 118, cy = 100, R = 64;
        circ(cx, cy, R, 0xFF42A5F5);
        c.save();
        clipCirc(cx, cy, R);
        float[][] blobs = {{0, -18, 20, 12}, {70, 14, 26, 14}, {110, -34, 16, 9}, {150, 30, 18, 10}};
        for (float[] b : blobs) {
            float bx = cx - R - 30 + ((t * 26 + b[0]) % (2 * R + 60));
            oval(bx, cy + b[1], b[2], b[3], 0xFF66BB6A);
        }
        int wl = 0xBFFFFFFF;
        for (int k = 0; k < 4; k++) {
            float rx = (float) Math.abs(Math.cos(t * 1.1 + k * Math.PI / 4)) * R;
            strokeOval(cx, cy, Math.max(rx, 0.5f), R, wl, 2.5f);
        }
        float[] fs = {-0.5f, 0f, 0.5f};
        for (float f : fs) {
            float y = cy + f * R;
            float hw = (float) Math.sqrt(R * R - f * f * R * R);
            line(cx - hw, y, cx + hw, y, wl, 2.5f);
        }
        c.restore();
        strokeCirc(cx, cy, R, 0xFF1E88E5, 4);

        float s = 0;
        if (t > 0.5f && t < 3.5f)
            s = t < 1.0f ? eob(prog(t, 0.5f, 1.0f)) : (float) (1 + 0.05 * Math.sin((t - 1) * 6));
        else if (t >= 3.5f) s = 1 - eio(prog(t, 3.5f, 4.0f));
        if (t > 1.0f && t < 3.5f) {
            float q = ((t - 1) % 1.2f) / 1.2f;
            strokeCirc(176, 138, 40 + q * 26, alpha(RED, 0.45f * (1 - q)), 3);
        }
        if (s > 0) {
            c.save();
            c.translate(176, 138);
            c.scale(s, s);
            circ(0, 0, 42, WHITE);
            circ(0, 0, 36, RED);
            rrect(-20, -6, 40, 12, 4, WHITE);
            c.restore();
        }
    }

    private void sceneSpace(float t) {
        float w = (float) (2 * Math.PI / 9);
        c.save();
        clipRR(0, 0, 260, 200, 22);
        fill(0xFF1B1F3B);
        c.drawRect(0, 0, 260, 200, p);
        long sd = 7;
        for (int i = 0; i < 26; i++) {
            sd = (sd * 9301 + 49297) % 233280;
            float sx = sd / 233280f * 260;
            sd = (sd * 9301 + 49297) % 233280;
            float sy = sd / 233280f * 200;
            sd = (sd * 9301 + 49297) % 233280;
            float sr = 0.8f + sd / 233280f * 1.4f;
            float al = (float) (0.55 + 0.45 * Math.sin(w * t * (3 + i % 4) + i));
            circ(sx, sy, sr, alpha(WHITE, al));
        }
        float[][] shoots = {{3.0f, 3.7f}, {6.2f, 6.9f}};
        for (float[] sh : shoots) {
            float q = prog(t, sh[0], sh[1]);
            if (q > 0 && q < 1) {
                float x0 = 250 - 200 * q, y0 = 10 + 80 * q;
                line(x0, y0, x0 + 30, y0 - 15, alpha(WHITE, 1 - q), 2);
            }
        }
        tp.setStyle(Paint.Style.FILL);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setTypeface(Typeface.DEFAULT_BOLD);
        tp.setTextSize(96);
        tp.setColor(WHITE);
        c.drawText("4", 52, 128, tp);
        c.drawText("4", 208, 128, tp);

        float py = 96 + 3 * (float) Math.sin(w * t * 2);
        c.save();
        c.translate(130, py);
        c.rotate(deg(-0.32));
        rf.set(-60, -15, 60, 15);
        stroke(0xFFFFD180, 6);
        c.drawArc(rf, 180, 180, false, p);
        c.restore();
        circ(130, py, 36, 0xFFFF8A3D);
        c.save();
        clipCirc(130, py, 36);
        fill(0xFFF57C00);
        c.drawRect(90, py - 14, 170, py - 6, p);
        c.drawRect(90, py + 6, 170, py + 16, p);
        c.restore();
        c.save();
        c.translate(130, py);
        c.rotate(deg(-0.32));
        rf.set(-60, -15, 60, 15);
        stroke(0xFFFFD180, 6);
        c.drawArc(rf, 0, 180, false, p);
        c.restore();

        float ax = 130 + 78 * (float) Math.sin(w * t);
        float ay = 152 + 20 * (float) Math.sin(2 * w * t);
        float rot = 0.35f * (float) Math.sin(w * t + 1);
        stroke(alpha(WHITE, 0.45f), 1.5f);
        path.reset();
        path.moveTo(130, py + 36);
        path.quadTo((ax + 130) / 2, ay + 34, ax, ay + 2);
        c.drawPath(path, p);
        c.save();
        c.translate(ax, ay);
        c.rotate(deg(rot));
        rrect(-15, -8, 30, 22, 5, 0xFF90A4AE);
        rrect(-10, -6, 20, 24, 6, 0xFFECEFF1);
        line(-9, 0, -17, 6 + 3 * (float) Math.sin(w * t * 4), 0xFFECEFF1, 5);
        line(9, 0, 17, -4 + 3 * (float) Math.sin(w * t * 4 + 1), 0xFFECEFF1, 5);
        line(-5, 16, -8, 27, 0xFFECEFF1, 5);
        line(5, 16, 9, 26, 0xFFECEFF1, 5);
        circ(0, -14, 13, WHITE);
        oval(2, -14, 8.5f, 6.5f, 0xFF263259);
        circ(-1, -16, 1.8f, WHITE);
        c.restore();
        c.restore();
    }

    private void sceneRobot(float t) {
        float g = 160;
        float x, face = 1, wob = 0, sq = 0;
        boolean dizzy = false, moving = false;
        if (t < 1.7f) { x = -130 + 284 * (t / 1.7f); moving = true; }
        else if (t < 2.0f) {
            x = 154 - 14 * eo(prog(t, 1.7f, 2.0f));
            sq = (float) Math.sin(prog(t, 1.7f, 2.05f) * Math.PI);
        }
        else if (t < 3.3f) { x = 140; dizzy = true; wob = (float) (0.06 * Math.sin(t * 10)); }
        else { x = 140 - 270 * (float) Math.pow(prog(t, 3.3f, 4.6f), 1.5); face = -1; moving = true; }
        float sh = 0;
        if (t > 1.7f && t < 2.1f) sh = (float) (3 * Math.sin((t - 1.7f) * 60) * (1 - prog(t, 1.7f, 2.1f)));

        line(0, g + 2, 260, g + 2, 0xFFCFD4D9, 4);
        c.save();
        c.translate(sh, 0);
        fill(0xFFFF8A3D);
        c.drawRect(206, 64, 266, 160, p);
        for (int r = 0; r <= 5; r++) {
            float y = 64 + r * 19.2f;
            line(206, y, 266, y, 0xFFFFD3A8, 2);
            if (r < 5) {
                for (float vx = 206 + (r % 2) * 15; vx < 266; vx += 30)
                    line(vx, y, vx, y + 19.2f, 0xFFFFD3A8, 2);
            }
        }
        c.restore();

        c.save();
        c.translate(x + 26, g);
        c.rotate(deg(wob));
        c.scale(face, 1);
        c.translate(-(x + 26), -g);
        float ph = t * 14;
        float sw = moving ? (float) Math.sin(ph) * 6 : 0;
        float liftL = moving ? (float) Math.max(0, Math.sin(ph)) * 3 : 0;
        float liftR = moving ? (float) Math.max(0, -Math.sin(ph)) * 3 : 0;
        line(x + 16, 146, x + 16 + sw, 158 - liftL, 0xFF455A64, 5);
        line(x + 36, 146, x + 36 - sw, 158 - liftR, 0xFF455A64, 5);
        line(x + 6, 118, x - 2, 130 + sw * 0.5f, 0xFF78909C, 5);
        line(x + 46, 118, x + 54, 130 - sw * 0.5f, 0xFF78909C, 5);
        rrect(x + 6, 112, 40, 36, 8, 0xFF78909C);
        rrect(x + 16, 121, 20, 12, 3, 0xFFCFD8DC);
        boolean blink = Math.sin(t * 9) > 0;
        circ(x + 26, 127, 3, blink ? 0xFFFF5252 : 0xFFB0BEC5);
        c.save();
        c.translate(x + 26, 110);
        c.scale(1 + sq * 0.12f, 1 - sq * 0.15f);
        c.translate(-(x + 26), -110);
        line(x + 26, 70, x + 26, 58, 0xFF455A64, 3);
        circ(x + 26, 55, 4.5f, blink ? RED : 0xFFB0BEC5);
        rrect(x + 2, 70, 48, 40, 10, 0xFFECEFF1);
        strokeRR(x + 2, 70, 48, 40, 10, 0xFF455A64, 3);
        if (dizzy || sq > 0.2f) {
            float[] exs = {x + 18, x + 36};
            for (float ex : exs) {
                line(ex - 4, 84, ex + 4, 92, 0xFF263238, 2.5f);
                line(ex + 4, 84, ex - 4, 92, 0xFF263238, 2.5f);
            }
            line(x + 18, 102, x + 22, 99, 0xFF263238, 2.5f);
            line(x + 22, 99, x + 26, 102, 0xFF263238, 2.5f);
            line(x + 26, 102, x + 30, 99, 0xFF263238, 2.5f);
            line(x + 30, 99, x + 34, 102, 0xFF263238, 2.5f);
        } else {
            circ(x + 19, 88, 5, 0xFF263238);
            circ(x + 37, 88, 5, 0xFF263238);
            line(x + 20, 100, x + 32, 100, 0xFF263238, 2.5f);
        }
        c.restore();
        c.restore();

        if (dizzy) {
            for (int i = 0; i < 3; i++) {
                float a = t * 6 + i * 2.094f;
                star(x + 26 + (float) Math.cos(a) * 24, 62 + (float) Math.sin(a) * 7, 6, 2.6f, 5, 0xFFFFC933);
            }
        }
        if (t > 1.7f && t < 2.4f) {
            float bs = eob(prog(t, 1.7f, 1.95f)) * (t > 2.2f ? 1 - prog(t, 2.2f, 2.4f) : 1);
            c.save();
            c.translate(196, 98);
            c.scale(bs, bs);
            star(0, 0, 24, 14, 10, 0xFFFFC933);
            tp.setStyle(Paint.Style.FILL);
            tp.setTextAlign(Paint.Align.CENTER);
            tp.setTypeface(Typeface.DEFAULT_BOLD);
            tp.setTextSize(12);
            tp.setColor(0xFFBF360C);
            c.drawText("bonk", 0, 4.2f, tp);
            c.restore();
        }
    }
}
