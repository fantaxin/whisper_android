package com.example.Whisper.widget;

import android.content.Context;

import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


public class FloatingDraftButton extends FloatingActionButton implements View.OnTouchListener {

    int lastX, lastY;
    int originX, originY;
    final int screenWidth;
    final int screenHeight;
    /**
     * 可设置距离边界的距离，正负值均可，单位为dp
     */
    //private int hideSize = 10;

    private ArrayList<FloatingActionButton> floatingActionButtons = new ArrayList<FloatingActionButton>();

    public FloatingDraftButton(Context context) {
        this(context, null);
    }

    public FloatingDraftButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingDraftButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        screenWidth = ScreenUtil.getScreenWidth(context);//获取屏幕宽度
        screenHeight = ScreenUtil.getContentHeight(context);//获取屏幕高度
        setOnTouchListener(this);
        //hideSize = dip2px(context, hideSize);
    }

    //注册归属的FloatingActionButton
    public void registerButton(FloatingActionButton floatingActionButton) {
        floatingActionButtons.add(floatingActionButton);
    }

    public ArrayList<FloatingActionButton> getButtons() {
        return floatingActionButtons;
    }

    public int getButtonSize() {
        return floatingActionButtons.size();
    }

    //是否可拖拽  一旦展开则不允许拖拽
    public boolean isDraftable() {
        for (FloatingActionButton btn : floatingActionButtons) {
            if (btn.getVisibility() == View.VISIBLE) {
                return false;
            }
        }
        return true;
    }

    //当被拖拽后其所属的FloatingActionButton 也要改变位置
    private void slideButton(int l, int t, int r, int b) {
        for (FloatingActionButton floatingActionButton : floatingActionButtons) {
            floatingActionButton.layout(l, t, r, b);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isDraftable()) {
            return false;
        }
        int ea = event.getAction();
        switch (ea) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getRawX();// 获取触摸事件触摸位置的原始X坐标
                lastY = (int) event.getRawY();
                originX = lastX;
                originY = lastY;
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) event.getRawX() - lastX;
                int dy = (int) event.getRawY() - lastY;
                int l = v.getLeft() + dx;
                int b = v.getBottom() + dy;
                int r = v.getRight() + dx;
                int t = v.getTop() + dy;
                // 下面判断移动是否超出屏幕
                if (l < 0) {
                    l = 0;
                    r = l + v.getWidth();
                }
                if (t < 0) {
                    t = 0;
                    b = t + v.getHeight();
                }
                if (r > screenWidth) {
                    r = screenWidth;
                    l = r - v.getWidth();
                }
                if (b > screenHeight) {
                    b = screenHeight;
                    t = b - v.getHeight();
                }//如何防止出屏幕
                v.layout(l, t, r, b);
                slideButton(l, t, r, b);
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                v.postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                int maxDuration = 500;
                int duration;
                int distance = (int) event.getRawX() - originX + (int) event.getRawY() - originY;
                Log.e("DIstance", distance + "");
                if (Math.abs(distance) < 20) {
                    //当变化太小的时候什么都不做 OnClick执行
                    //可以靠边，效果不咋好
                } else {
                    return true;
                }
                break;
        }
        return false;

    }
    /**private void animSlide(final View view, final int leftFrom, int leftTo, int duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(leftFrom, leftTo);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int viewLeft = (int) valueAnimator.getAnimatedValue();
                view.layout(viewLeft, view.getTop(), viewLeft + view.getWidth(), view.getBottom());
            }
        });
        //为防止溢出边界时，duration时间为负值，做下0判断
        valueAnimator.setDuration(duration < 0 ? 0 : duration);
        valueAnimator.start();
    }

    public int dip2px(Context paramContext, float paramFloat) {
        return (int) (0.5F + paramFloat
                * paramContext.getResources().getDisplayMetrics().density);
    }**/
}
