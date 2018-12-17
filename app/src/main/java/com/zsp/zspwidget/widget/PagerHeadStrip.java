package com.zsp.zspwidget.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewParent;

import com.zsp.zspwidget.R;
import com.zsp.zspwidget.util.DensityUtil;

/**
 * Created by zsp on 2018/9/14.
 * PagerHeadStrip
 */

@ViewPager.DecorView
public class PagerHeadStrip extends View {

    /**
     * item宽度默认等于（文字宽度+2* @paddingLR)
     * AUTO 自动适配 当超出屏幕宽度使用FULL，否则使用EQUALLY
     * FULL 根据内容从左到右排列显示
     * EQUALLY item平分控件宽度  注意：文字长度没做适配 当item长度小于文字长度时，文字会重叠
     * EQUIVALENT item宽度相等（取最长item的宽度）；
     */
    public static final int AUTO = 0;
    public static final int EQUALLY = 1;
    public static final int FULL = 2;
    public static final int EQUIVALENT = 3;

    public boolean isFlow;

    private ViewPager mPager;
    private PagerAdapter mAdapter;
    private PagerListener mPagerListener;

    private Paint mNormalTextPaint;
    private int mNormalTextSize;
    private int mNormalTextColor;

    private Paint mCurrentTextPaint;
    private int mCurrentTextColor;
    private int mCurrentTextSize;

    private Paint mLinePaint;
    private int mLineSize;
    private int mLineWidth;//设置后 不自动适配line的长度(适配每个item文字的长度)
    private int mLineColor;//line背景色
    private int linePaddingB;//line下间距
    private Drawable mLineDrawable;//line背景,设置后mLineColor无效

    private int partitionType;//item长度类型
    private int mWidth;// 控件总宽度,在屏幕里呈现的宽度
    private int mMaxTitleWidth;// title宽度(每个itemWidth=mMaxItemWidth)
    private int mMaxItemWidth;// item最大宽度
    private int paddingLR;//文字左右间距

    private int moveX;//移动的距离
    private int mCurrentPosition = 0;//当前显示的title位置
    private int mOffSetPosition = 0;//偏移的position
    private float positionOffSet = 0;//偏移量

    private ValueAnimator moveAnim;

    private int lastX;//ACTION_DOWN 时x值
    private float downX;//ACTION_DOWN 时y值
    private int minMoveX = DensityUtil.getSize(5);//最小有效滑动距离
    private boolean isMove = false;//当次事件是否是滑动事件
    private VelocityTracker mVelocityTracker;//惯性滑动速率计算

    public PagerHeadStrip(Context context) {
        super(context);
        setDefaultValue();
        init();
    }

    public PagerHeadStrip(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setDefaultValue();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PagerHeadStrip);
        if (array != null) {
            mNormalTextColor = array.getColor(R.styleable.PagerHeadStrip_mPHSNormalTextColor, mNormalTextColor);
            mCurrentTextColor = array.getColor(R.styleable.PagerHeadStrip_mPHSCurrentTextColor, mCurrentTextColor);
            mLineColor = array.getColor(R.styleable.PagerHeadStrip_mPHSLineColor, mLineColor);
            partitionType = array.getInt(R.styleable.PagerHeadStrip_mPHSPartitionType, AUTO);
            mLineSize = array.getDimensionPixelSize(R.styleable.PagerHeadStrip_mPHSLineSize, mLineSize);
            mNormalTextSize = array.getDimensionPixelSize(R.styleable.PagerHeadStrip_mPHSTextSize, mNormalTextSize);
            mCurrentTextSize = array.getDimensionPixelSize(R.styleable.PagerHeadStrip_mPHSCurrentTextSize, mCurrentTextSize);
            paddingLR = array.getDimensionPixelSize(R.styleable.PagerHeadStrip_mPHSPaddingLR, paddingLR);
            mLineWidth = array.getDimensionPixelSize(R.styleable.PagerHeadStrip_mPHSLineWidth, mLineWidth);
            mLineDrawable = array.getDrawable(R.styleable.PagerHeadStrip_mPHSLineDrawable);
            linePaddingB = array.getDimensionPixelOffset(R.styleable.PagerHeadStrip_mPHSLinePaddingB,linePaddingB);
            isFlow = array.getBoolean(R.styleable.PagerHeadStrip_mPHSIsFlow,false);
            array.recycle();
        }
        init();
    }

    public PagerHeadStrip(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDefaultValue();
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final ViewParent parent = getParent();
        if (!(parent instanceof ViewPager)) {
            return;
        }
        final ViewPager pager = (ViewPager) parent;
        mAdapter = pager.getAdapter();
        mPager = pager;
        if (mPagerListener == null) {
            mPagerListener = new PagerListener();
        }
        mPager.addOnPageChangeListener(mPagerListener);
        mPager.addOnAdapterChangeListener(mPagerListener);
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mPagerListener);
        }
        moveX = 0;
        move(getStartX(mCurrentPosition));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPager != null) {
            mPager.removeOnAdapterChangeListener(mPagerListener);
            mPager.removeOnPageChangeListener(mPagerListener);
            if (mAdapter != null) {
                mAdapter.unregisterDataSetObserver(mPagerListener);
            }
        }

    }

    private void setDefaultValue() {
        mNormalTextSize = getRealPixelSize(17);
        mNormalTextColor = Color.GRAY;
        mCurrentTextColor = Color.BLACK;
        mCurrentTextSize = getRealPixelSize(18);

        mLineSize = getRealPixelSize(2);
        mLineColor = mCurrentTextColor;
        mLineWidth = -1;
        paddingLR = getRealPixelSize(10);
        linePaddingB=0;
    }

    private int getRealPixelSize(int size){
        DisplayMetrics displayMetrics=getContext().getResources().getDisplayMetrics();
        float density=displayMetrics.density;
        return (int)(density*size);
    }

    private void init() {
        mNormalTextPaint = new Paint();
        mNormalTextPaint.setAntiAlias(true);
        mNormalTextPaint.setTypeface(Typeface.MONOSPACE);
        mNormalTextPaint.setTextSize(mNormalTextSize);
        mNormalTextPaint.setColor(mNormalTextColor);

        mCurrentTextPaint = new Paint();
        mCurrentTextPaint.setAntiAlias(true);
        mCurrentTextPaint.setTypeface(Typeface.MONOSPACE);
        mCurrentTextPaint.setTextSize(mCurrentTextSize);
        mCurrentTextPaint.setColor(mCurrentTextColor);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(mLineSize);
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);

    }


    @Nullable
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SaveStatus saveStatus = new SaveStatus(parcelable);
        saveStatus.setPosition(mCurrentPosition);
        return saveStatus;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SaveStatus) {
            SaveStatus saveStatus = (SaveStatus) state;
            super.onRestoreInstanceState(saveStatus.getSuperState());
            mCurrentPosition = saveStatus.getPosition();
        }
    }


    /**
     * 计算title宽度
     */
    private void initWidth() {
        float maxItemWidth = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            float w = measureItemWidth(i, mNormalTextPaint);
            maxItemWidth = Math.max(maxItemWidth, w);
        }
        mMaxTitleWidth = (int) maxItemWidth * mAdapter.getCount();
        mMaxItemWidth = (int) maxItemWidth;
    }

    /**
     * 获取position 的宽度
     *
     * @param position 位置
     * @return 宽
     */
    private int getItemWidth(int position) {
        if (mMaxItemWidth == 0) {
            initWidth();
        }
        if (partitionType == EQUALLY || (partitionType == AUTO && mMaxTitleWidth <= mWidth)) {
            return mWidth / mAdapter.getCount();
        }
        if (partitionType == FULL || (partitionType == AUTO && mMaxTitleWidth > mWidth)) {
            return measureItemWidth(position, mNormalTextPaint);
        }
        if (partitionType == EQUIVALENT) {
            return mMaxItemWidth;
        }
        return 0;
    }


    /**
     * 获取position 的开始位置
     *
     * @param position 位置
     * @return 开始的横坐标
     */
    private int getStartX(int position) {
        int x = 0;
        for (int i = 0; i < position; i++) {
            x += getItemWidth(i);
        }
        return x;
    }

    /**
     * 测量item宽度(适配文字宽度)
     *
     * @param position position
     * @param paint    paint
     * @return int item width
     */
    private int measureItemWidth(int position, Paint paint) {
        int width = (int) measureTextWidth(position, paint);
        return width > 0 ? width + paddingLR * 2 : 0;
    }

    /**
     * 测量文字宽度
     *
     * @param position position
     * @param paint    paint
     * @return int  text width
     */
    private float measureTextWidth(int position, Paint paint) {
        int width = 0;
        if (mAdapter != null && position >= 0 && position < mAdapter.getCount()) {
            CharSequence cs = mAdapter.getPageTitle(position);
            if (!TextUtils.isEmpty(cs)) {
                return (int) paint.measureText(cs.toString());
            }
        }
        return width;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mAdapter == null) {
            return;
        }
        final int count = mAdapter.getCount();
        if (count <= 0) {
            return;
        }
        canvas.translate(-moveX, 0);
        drawBackGroundLine(canvas);

        float startX;
        float startY = (getHeight() + mNormalTextSize*2/3) / 2;
        for (int i = 0; i < count; i++) {
            float textWidth = mCurrentPosition == i ? measureTextWidth(i, mCurrentTextPaint) : measureTextWidth(i, mNormalTextPaint);
            startX = getStartX(i) + (getItemWidth(i) - textWidth) / 2;
            if (mCurrentPosition == i) {
                drawItemText(i, startX, startY, mCurrentTextPaint, canvas);
            } else {
                drawItemText(i, startX, startY, mNormalTextPaint, canvas);
            }
        }
    }

    private void drawBackGroundLine(Canvas canvas){
        float lineStartX;
        float lineStopX;
        float lineWidth = measureTextWidth(mOffSetPosition, mCurrentTextPaint);
        float nextLineWidth = measureTextWidth(mOffSetPosition + 1, mCurrentTextPaint);
        if(!isFlow){
            if (mLineWidth == -1) {//逐渐改变长度
                lineWidth = lineWidth + (nextLineWidth - lineWidth) * positionOffSet;
            } else {
                lineWidth = mLineWidth;
            }

            lineStartX =getStartX(mOffSetPosition) + (getItemWidth(mOffSetPosition) - lineWidth) / 2+ (getItemWidth(mOffSetPosition) + getItemWidth(mOffSetPosition + 1)) / 2 * positionOffSet;
            lineStopX = lineStartX + lineWidth;
        }else {

            if (mLineWidth != -1) {
                lineWidth=mLineWidth;
                nextLineWidth=mLineWidth;
            }

            if(positionOffSet<0.5){
                lineStartX=getStartX(mOffSetPosition)+(getItemWidth(mOffSetPosition)-lineWidth)/2;
                lineStopX=lineStartX+lineWidth+(getStartX(mOffSetPosition+1)+(getItemWidth(mOffSetPosition+1)-nextLineWidth)/2+nextLineWidth-lineStartX-lineWidth)*2*positionOffSet;
            }else {
                lineStartX=getStartX(mOffSetPosition)+(getItemWidth(mOffSetPosition)-lineWidth)/2;
                lineStartX=lineStartX+(getStartX(mOffSetPosition+1)+(getItemWidth(mOffSetPosition+1)-nextLineWidth)/2-lineStartX)*2*(positionOffSet-0.5f);
                lineStopX=getStartX(mOffSetPosition+1)+(getItemWidth(mOffSetPosition+1)-nextLineWidth)/2+nextLineWidth;
            }
        }

        if (mLineDrawable != null) {
            float lineY = getHeight() - mLineSize-linePaddingB;
            mLineDrawable.setBounds((int) lineStartX, (int) lineY, (int) lineStopX, (int) lineY + mLineSize);
            mLineDrawable.draw(canvas);
        } else {
            float lineY = getHeight() - (mLineSize/2+linePaddingB);
            canvas.drawLine(lineStartX, lineY, lineStopX, lineY, mLinePaint);
        }
    }

    private void drawItemText(int position, float startX, float startY, Paint paint, Canvas canvas) {
        if (mAdapter != null && position >= 0 && position < mAdapter.getCount()) {
            CharSequence cs = mAdapter.getPageTitle(position);
            if (!TextUtils.isEmpty(cs)) {
                canvas.drawText(cs.toString(), startX, startY, paint);
            }
        }
    }

    /**
     * 移动画布
     *
     * @param x 移动的距离
     */
    private void move(int x) {
        int maxMoveX = getStartX(mAdapter.getCount() - 1) + getItemWidth(mAdapter.getCount() - 1) - getWidth();
        maxMoveX = maxMoveX > 0 ? maxMoveX : 0;
        moveX = moveX + x;
        if (moveX < 0) {
            moveX = 0;
        } else if (moveX > maxMoveX) {
            moveX = maxMoveX;
        }
    }


    private void moveByAnim(int x) {
        if (moveAnim != null) {
            moveAnim.cancel();
        }
        moveAnim = ValueAnimator.ofInt(0, x);
        moveAnim.setDuration(300);
        moveAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int lastX = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                move((int) animation.getAnimatedValue() - lastX);
                invalidate();
                lastX = (int) animation.getAnimatedValue();
            }
        });
        moveAnim.start();
    }

    /**
     * 将当前的选中的title移动到中间位置
     */
    private void moveTitleToCenter() {
        int cPositionX = getStartX(mCurrentPosition) + getItemWidth(mCurrentPosition) / 2;
        int cMoveX = cPositionX - getWidth() / 2;
        moveByAnim(cMoveX - moveX);
    }

    private void changeCurrentPosition(int position) {
        if (position != mCurrentPosition) {
            mCurrentPosition = position;
        }
        moveTitleToCenter();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        initVelocityTracker();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getRawX();
                mVelocityTracker.clear();
                downX = event.getRawX();
                mVelocityTracker.addMovement(event);
                isMove = false;
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                if (Math.abs(downX - event.getRawX()) > minMoveX) {
                    isMove = true;
                    int m = (int) event.getRawX() - lastX;
                    move(-m);
                    lastX = (int) event.getRawX();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isMove) {
                    mVelocityTracker.computeCurrentVelocity(1000);
                    float x = mVelocityTracker.getXVelocity();
                    post(new MoveRunnable(-x));
                } else {
                    int eX = (int) event.getX() + moveX;
                    int position = 0;
                    int x = 0;
                    for (int i = 0; i < mAdapter.getCount(); i++) {
                        x += getItemWidth(i);
                        if (eX < x) {
                            position = i;
                            break;
                        }
                    }
                    if (mPager != null) {
                        mPager.setCurrentItem(position);
                    }
                }
                performClick();
                break;
        }
        invalidate();
        getParent().requestDisallowInterceptTouchEvent(true);
        return true;
    }

    private void initVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private class MoveRunnable implements Runnable {
        private float velocity;

        MoveRunnable(float velocity) {
            this.velocity = velocity;
        }

        @Override
        public void run() {
            if (Math.abs(velocity) < 20) {
                return;
            }
            move((int) velocity / 40);
            velocity /= 1.0666666;
            postInvalidate();
            postDelayed(this, 10);
        }
    }


    private class PagerListener extends DataSetObserver implements ViewPager.OnPageChangeListener, ViewPager.OnAdapterChangeListener {

        PagerListener() {

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            mOffSetPosition = position;
            PagerHeadStrip.this.positionOffSet = positionOffset;
            mCurrentPosition = mPager.getCurrentItem();
            invalidate();
        }

        @Override
        public void onPageSelected(int position) {
            changeCurrentPosition(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onAdapterChanged(@NonNull ViewPager viewPager, PagerAdapter oldAdapter,
                                     PagerAdapter newAdapter) {
            if (mAdapter != null) {
                mAdapter.unregisterDataSetObserver(mPagerListener);
            }
            mAdapter = newAdapter;
            mAdapter.registerDataSetObserver(mPagerListener);
            mOffSetPosition = viewPager.getCurrentItem();
            changeCurrentPosition(mOffSetPosition);
            initWidth();
            invalidate();
        }

        @Override
        public void onChanged() {
            initWidth();
            invalidate();
        }
    }

    private static class SaveStatus extends BaseSavedState {
        private int position;

        private int getPosition() {
            return position;
        }

        private void setPosition(int cPosition) {
            this.position = cPosition;
        }


        private SaveStatus(Parcel source) {
            super(source);
            position = source.readInt();
        }

        private SaveStatus(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(position);
        }

        public static final Creator<SaveStatus> CREATOR = new Creator<SaveStatus>() {

            @Override
            public SaveStatus createFromParcel(Parcel source) {
                return new SaveStatus(source);
            }

            @Override
            public SaveStatus[] newArray(int size) {
                return new SaveStatus[size];
            }
        };
    }
}
