package com.xiaomakj.bdvoice.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

/**
 * Created by Administrator on 2016/6/15.
 */
public class MyScrollView extends ScrollView {
    private View innerView;
    private float y;

    private Rect normal = new Rect();

    private boolean animationFinish = true;

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 当我们的xml布局已经被系统变成view的时候
     */
    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            innerView = getChildAt(0);
        }
        super.onFinishInflate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (innerView == null) {
            return super.onTouchEvent(ev);
        } else {
            //定义自己的touch处理
            commonTouchEvent(ev);
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 自己的touch处理
     *
     * @param ev
     */
    private void commonTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (animationFinish) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    y = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    //记录一下一段移动距离的2个Y点的值
                    float preY = y == 0 ? ev.getY() : y;
                    float nowY = ev.getY();
                    //detailY作为innerview移动的距离的参考值
                    int detailY = (int) (preY - nowY);
                    y = nowY;
                    if (isNeedMove()) {
                        //移动之前把正常位置记录一下
                        if (normal.isEmpty()) {
                            normal.set(innerView.getLeft(), innerView.getTop(), innerView.getRight(), innerView.getBottom());
                        }
                        //移动view的位置
                        innerView.layout(innerView.getLeft(), innerView.getTop() - detailY / 2, innerView.getRight(), innerView.getBottom() - detailY / 2);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    y = 0;
                    if (isNeedAnimaton()) {
                        animation();
                    }
                    break;

            }
        }
    }

    private void animation() {
        TranslateAnimation ta = new TranslateAnimation(0, 0, 0, normal.top - innerView.getTop());
        ta.setDuration(200);
        ta.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animationFinish = false;

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                innerView.clearAnimation();
                innerView.layout(normal.left, normal.top, normal.right, normal.bottom);
                normal.setEmpty();
                animationFinish = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        innerView.startAnimation(ta);


    }

    private boolean isNeedAnimaton() {
        if (!normal.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isNeedMove() {
        int offset = innerView.getMeasuredHeight() - getHeight();
        int scrollY = getScrollY();
        if (scrollY == 0 || scrollY == offset) {
            return true;
        }
        return false;
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    private ScrollViewListener scrollViewListener = null;

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        View contentView = getChildAt(0);
        if (contentView != null && contentView.getMeasuredHeight() <= getScrollY() + getHeight()) {
            if (scrollViewListener != null) {
                scrollViewListener.onBottom();
            }
        } else if (getScrollY() == 0) {
            if (scrollViewListener != null) {
                scrollViewListener.onTop();
            }
        } else {
            if (scrollViewListener != null) {
                int i = y - oldy;
                scrollViewListener.onscroll(i);
            }
        }
    }

    public interface ScrollViewListener {
        void onscroll(int y);

        void onBottom();

        void onTop();
    }
}
