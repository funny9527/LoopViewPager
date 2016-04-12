/**
 * 
 */
package com.example.pagertest;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * @author szy
 *
 */
public class ScrollPanel extends ViewGroup {
	
	public static final String TAG = "test";
	private int state = -1;
	private static final int STATE_SCROLLING = 1;
	private static final int MIN_LENGTH_FOR_FLING = 25;
	
	private float mDownX = -1;
	private float mLastX = -1;
	
	private Scroller mScroller;
	
	private int mChildWidth = -1;
	private int mPageCount = 0;
	
	private int mCurrentPage = 0;
	
	private int mUnboundedScrollX = 0;
	
	private boolean isLoopEnabled = false;
	
	private VelocityTracker mVelocityTracker;
	private int mMaximumVelocity;
	protected int mFlingThresholdVelocity;
	protected float mDensity;
	
	private static final int FLING_THRESHOLD_VELOCITY = 500;
	
	private static final float SNAP_RATIO = 0.6f;
	
	private static final int ANIM_DURATION = 400;
	
	private int TouchSlop = 16;
	
	private boolean smooth = false;
	
	private Interpolator mInterpolator;

	public ScrollPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ScrollPanel, 0, 0);
		smooth = a.getBoolean(R.styleable.ScrollPanel_smooth, false);
		isLoopEnabled = a.getBoolean(R.styleable.ScrollPanel_loop, false);
		a.recycle();
		
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		TouchSlop = configuration.getScaledPagingTouchSlop();
		
		mDensity = getResources().getDisplayMetrics().density;
		mFlingThresholdVelocity = (int) (FLING_THRESHOLD_VELOCITY * mDensity);
		
		if (smooth) {
			mInterpolator = new SmoothInterpolator();
		} else {
			mInterpolator = new ScrollInterpolator();
		}
		mScroller = new Scroller(context, mInterpolator);
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        int childCount = getChildCount();
        
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        }
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		
		if (changed) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = getChildAt(i);
                childView.layout(i * childView.getMeasuredWidth(), 0, (i + 1) * childView.getMeasuredWidth(), childView.getMeasuredHeight());
            }
            
            if (childCount > 0) {
            	mChildWidth = getChildAt(0).getWidth();
            	mPageCount = childCount;
            }
        }
	}
	
	@Override
    public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			mUnboundedScrollX = mScroller.getCurrX(); 
            scrollTo(mUnboundedScrollX, mScroller.getCurrY());
            invalidate();
        }
	}
	
	private static class ScrollInterpolator implements Interpolator {
        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
        	t -= 1.0f;
        	return t*t*t*t*t + 1;
        }
    }
	
	private static class SmoothInterpolator implements Interpolator {
		private float mTension = 1.3f;
        public SmoothInterpolator() {
        }

        public float getInterpolation(float t) {
        	t -= 1.0f;
            return t * t * ((mTension + 1) * t + mTension) + 1.0f;
        }
    }

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		
		acquireVelocityTrackerAndAddMovement(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownX = event.getX();
			mLastX = mDownX;
			break;
		case MotionEvent.ACTION_MOVE:
			float cx = event.getX();
			float dx = Math.abs(cx - mDownX);
			if (dx > TouchSlop) {
				state = STATE_SCROLLING;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			releaseVelocityTracker();
			break;
		}
		return state == STATE_SCROLLING;
	}
	
	@Override 
	public boolean onTouchEvent(MotionEvent event) {
		acquireVelocityTrackerAndAddMovement(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownX = event.getX();
			mLastX = mDownX;
			if (!mScroller.isFinished()) {
				state = -1;
                mScroller.abortAnimation();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			float cx = event.getX();
			if (state == STATE_SCROLLING) {

				if (cx - mLastX > 4) {
					scrollBy((int) (mLastX - cx), 0);
					mUnboundedScrollX += mLastX - cx;
					mLastX = cx;
					invalidate();
				} else if (mLastX - cx > 4) {
					scrollBy((int) (mLastX - cx), 0);
					mUnboundedScrollX += mLastX - cx;
					mLastX = cx;
					invalidate();
				}
			} else {
				float dx = Math.abs(cx - mDownX);
				if (dx > TouchSlop) {
					state = STATE_SCROLLING;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (state == STATE_SCROLLING) {
				float upx = event.getX();
				float delx = upx - mDownX;
				
				final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();
                boolean isFling = Math.abs(delx) > MIN_LENGTH_FOR_FLING &&
                        Math.abs(velocityX) > mFlingThresholdVelocity;
				
				if (isLoopEnabled && mPageCount > 1) {
					if (delx > 0) {
						if (isFling || Math.abs(delx) > mChildWidth * SNAP_RATIO) {
							if (mCurrentPage > 0) {
							    mScroller.startScroll(mUnboundedScrollX, 0, (int)((mCurrentPage - 1) * mChildWidth - mUnboundedScrollX), 0, ANIM_DURATION);
							    mCurrentPage--;
							    invalidate();
							} else {
								mScroller.startScroll(mUnboundedScrollX, 0, (int)((mCurrentPage - 1) * mChildWidth - mUnboundedScrollX), 0, ANIM_DURATION);
							    mCurrentPage = mPageCount - 1;
							    invalidate();
							}
						} else {
							mScroller.startScroll(mUnboundedScrollX, 0, (int) ((mCurrentPage) * mChildWidth - mUnboundedScrollX), 0, ANIM_DURATION);
							invalidate();
						}
					} else {
						if (isFling || Math.abs(delx) > mChildWidth * SNAP_RATIO) {
							if (mCurrentPage < mPageCount - 1) {
							    mScroller.startScroll(mUnboundedScrollX, 0, (int) ((mCurrentPage + 1) * mChildWidth - mUnboundedScrollX), 0, ANIM_DURATION);
							    mCurrentPage++;
							    invalidate();
							} else {
								mScroller.startScroll(mUnboundedScrollX, 0, (int) ((mCurrentPage + 1) * mChildWidth - mUnboundedScrollX), 0, ANIM_DURATION);
							    mCurrentPage = 0;
							    invalidate();
							}
						} else {
							mScroller.startScroll(mUnboundedScrollX, 0, (int) ((mCurrentPage) * mChildWidth - mUnboundedScrollX), 0, ANIM_DURATION);
							invalidate();
						}
					}
				} else {
					if (delx > 0) {
						if ((isFling || Math.abs(delx) > mChildWidth * SNAP_RATIO) && mCurrentPage > 0) {
							mScroller.startScroll(mUnboundedScrollX, 0, (int)((mCurrentPage - 1) * mChildWidth - mUnboundedScrollX), 0, ANIM_DURATION);
							mCurrentPage--;
							invalidate();
						} else {
							mScroller.startScroll(mUnboundedScrollX, 0, (int) ((mCurrentPage) * mChildWidth - mUnboundedScrollX), 0, ANIM_DURATION);
							invalidate();
						}
					} else {
						if ((isFling || Math.abs(delx) > mChildWidth * SNAP_RATIO) && mCurrentPage < mPageCount - 1) {
							mScroller.startScroll(mUnboundedScrollX, 0, (int) ((mCurrentPage + 1) * mChildWidth - mUnboundedScrollX), 0, ANIM_DURATION);
							mCurrentPage++;
							invalidate();
						} else {
							mScroller.startScroll(mUnboundedScrollX, 0, (int) ((mCurrentPage) * mChildWidth - mUnboundedScrollX), 0, ANIM_DURATION);
							invalidate();
						}
					}
				}
				
				
			    state = -1;
			    releaseVelocityTracker();
			}
			break;
		}
		return true;
	}
	
	
	@Override
    protected void dispatchDraw(Canvas canvas) {
		int[] range = new int[2];
		getScreenRange(range);
        
		int left = range[0];
		int right = range[1];
		int offset = mPageCount * mChildWidth;
		
		canvas.save();
        canvas.clipRect(getScrollX(), getScrollY(), getScrollX() + getRight() - getLeft(),
                getScrollY() + getBottom() - getTop());
		
		if (isLoopEnabled && mPageCount > 1) {
			if (right >= mPageCount) {
				drawChild(canvas, getChildAt(mPageCount - 1), getDrawingTime());
				canvas.translate(offset, 0);
				drawChild(canvas, getChildAt(0), getDrawingTime());
				if (smooth) {
				    drawChild(canvas, getChildAt(1), getDrawingTime());
				}
				canvas.translate(-offset, 0);
				
				if (mCurrentPage <= 0) {
				    canvas.translate(offset, 0);
				    mUnboundedScrollX = 0;
				    scrollTo(0, 0);
				}
			} else if (left < 0) {
				canvas.translate(-offset, 0);
				if (smooth) {
				    drawChild(canvas, getChildAt(mPageCount - 2), getDrawingTime());
				}
				drawChild(canvas, getChildAt(mPageCount - 1), getDrawingTime());
				canvas.translate(offset, 0);
				drawChild(canvas, getChildAt(right), getDrawingTime());
				
				if (mCurrentPage >= mPageCount - 1) {
				    canvas.translate(-offset, 0);
				    mUnboundedScrollX = offset - mChildWidth;
				    scrollTo(offset - mChildWidth, 0);
				}
			} else {
				for (int i = right; i >= left; i--) {
					if (i >= 0 && i < mPageCount) {
		                drawChild(canvas, getChildAt(i), getDrawingTime());
					}
		        }
			}
		} else {
			for (int i = right; i >= left; i--) {
				if (i >= 0 && i < mPageCount) {
	                drawChild(canvas, getChildAt(i), getDrawingTime());
				}
	        }
		}
        canvas.restore();
	}
	
	private void getScreenRange(int[] range) {
		
		int left = 0;
		int right = 0;
		
		int offset = mUnboundedScrollX;
		
		if (offset > 0) {
		    left = offset / mChildWidth;
		    right = left + 1;
		} else {
			left = -1;
			right = 0;
		}
		
		range[0] = left;
		range[1] = right;
	}
	
	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }
	
	private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
	
	public void setInterpolator(Interpolator interpolator) {
		mInterpolator = interpolator;
	}
	
	public void setSmoothEnabled(boolean smooth) {
		this.smooth = smooth;
	}
	
	public void setLoopEnabled(boolean loop) {
		isLoopEnabled = loop;
	}
	
	public int getCurrentPageIndex() {
		return mCurrentPage;
	}
	
	public Pager addPage(int layout) {
		Pager pager = (Pager) LayoutInflater.from(getContext()).inflate(layout, this, false);
		addView(pager);
		return pager;
	}
}
