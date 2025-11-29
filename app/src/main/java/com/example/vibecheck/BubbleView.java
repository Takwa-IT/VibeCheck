package com.example.vibecheck;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BubbleView extends View {
    private List<Bubble> bubbles = new ArrayList<>();
    private Random random = new Random();
    private Paint paint = new Paint();
    private ValueAnimator floatAnimator;
    private BubbleListener listener;

    // Couleurs pour les bulles
    private final int[] BUBBLE_COLORS = {
            Color.parseColor("#FF9575D9"), // soft_purple
            Color.parseColor("#FFEC7BAD"), // soft_pink
            Color.parseColor("#FF4CAF50"), // green
            Color.parseColor("#FFFFC107"), // yellow
            Color.parseColor("#FF2196F3")  // blue
    };

    public interface BubbleListener {
        void onBubblePopped();
        void onAllBubblesPopped();
    }

    public BubbleView(Context context) {
        super(context);
        init();
    }

    public BubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BubbleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setupPaint();
    }

    private void setupPaint() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setBubbleListener(BubbleListener listener) {
        this.listener = listener;
    }

    public void generateBubbles(int count) {
        bubbles.clear();

        for (int i = 0; i < count; i++) {
            float x = random.nextFloat() * (getWidth() - 100) + 50;
            float y = random.nextFloat() * (getHeight() - 200) + 100;
            float radius = 30 + random.nextInt(40);
            int color = BUBBLE_COLORS[random.nextInt(BUBBLE_COLORS.length)];

            Bubble bubble = new Bubble(x, y, radius, color);
            bubble.floatOffset = random.nextFloat() * 10 - 5;
            bubbles.add(bubble);
        }
        startFloatingAnimation();
        invalidate();
    }

    private void startFloatingAnimation() {
        if (floatAnimator != null) floatAnimator.cancel();

        floatAnimator = ValueAnimator.ofFloat(0, 1);
        floatAnimator.setDuration(2000);
        floatAnimator.setRepeatCount(ValueAnimator.INFINITE);
        floatAnimator.setRepeatMode(ValueAnimator.REVERSE);
        floatAnimator.addUpdateListener(animation -> {
            float value = (Float) animation.getAnimatedValue();
            for (Bubble bubble : bubbles) {
                if (!bubble.popped) {
                    bubble.y += Math.sin(value * Math.PI * 2) * 0.5f + bubble.floatOffset * 0.1f;
                }
            }
            invalidate();
        });
        floatAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Bubble bubble : bubbles) {
            if (!bubble.popped) {
                // Effet de brillance
                paint.setColor(bubble.color);
                canvas.drawCircle(bubble.x, bubble.y, bubble.radius, paint);

                paint.setColor(Color.argb(100, 255, 255, 255));
                canvas.drawCircle(bubble.x - bubble.radius/3, bubble.y - bubble.radius/3,
                        bubble.radius/3, paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            for (Bubble bubble : bubbles) {
                if (!bubble.popped && isTouchInBubble(touchX, touchY, bubble)) {
                    bubble.popped = true;
                    animatePop(bubble);

                    if (listener != null) {
                        listener.onBubblePopped();
                    }

                    if (allPopped() && listener != null) {
                        listener.onAllBubblesPopped();
                    }

                    invalidate();
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isTouchInBubble(float x, float y, Bubble bubble) {
        float distance = (float) Math.sqrt(Math.pow(x - bubble.x, 2) + Math.pow(y - bubble.y, 2));
        return distance < bubble.radius;
    }

    private void animatePop(final Bubble bubble) {
        ValueAnimator popAnimator = ValueAnimator.ofFloat(1, 0);
        popAnimator.setDuration(200);
        popAnimator.addUpdateListener(animation -> {
            float scale = (Float) animation.getAnimatedValue();
            bubble.radius *= scale;
            invalidate();
        });
        popAnimator.start();
    }

    private boolean allPopped() {
        for (Bubble b : bubbles) {
            if (!b.popped) return false;
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (floatAnimator != null) {
            floatAnimator.cancel();
        }
    }

    private static class Bubble {
        float x, y, radius;
        int color;
        boolean popped = false;
        float floatOffset;

        Bubble(float x, float y, float radius, int color) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = color;
        }
    }
}