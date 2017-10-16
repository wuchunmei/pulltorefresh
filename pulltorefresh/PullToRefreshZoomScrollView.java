package cn.tianya.light.pulltorefresh;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import cn.tianya.light.R;

import com.google.zxing.util.Log;

/**
 * 一个可以支持actionbar随滚动渐变，图片随下拉伸缩的下拉刷新scrollview
 * @author xuwei
 *
 */
public class PullToRefreshZoomScrollView extends PullToRefreshBase<ScrollView> {
	private final static String TAG=PullToRefreshZoomScrollView.class.getSimpleName();
	private boolean isCustomHeaderHeight = false;
	private FrameLayout mHeaderContainer;
	private LinearLayout mRootContainer;
	private View mContentView;
	private int mHeaderHeight;
	private ScalingRunnable mScalingRunnable;
	private static final Interpolator sInterpolator = new Interpolator() {
		public float getInterpolation(float paramAnonymousFloat) {
			float f = paramAnonymousFloat - 1.0F;
			return 1.0F + f * (f * (f * (f * f)));
		}
	};

	protected View mHeaderView;// 头部View
	protected View mZoomView;// 缩放拉伸View
	private boolean isZoomEnabled = true;
	private boolean isParallax = true;
	private boolean isZooming = false;
	private boolean isHideHeader = false;
	public PullToRefreshZoomScrollView(Context context) {
		super(context);
		init();
	}

	public PullToRefreshZoomScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PullToRefreshZoomScrollView(Context context, Mode mode) {
		this(context, mode, null);
	}

	public PullToRefreshZoomScrollView(Context context, Mode mode, AnimationStyle style) {
		super(context, mode, style);
		init();
	}

	private void init() {
		mScalingRunnable = new ScalingRunnable();
		((InternalScrollView) mRefreshableView)
				.setOnScrollViewChangedListener(new OnScrollViewChangedListener() {
					@Override
					public void onInternalScrollChanged(int left, int top,
							int oldLeft, int oldTop) {
						if (isZoomEnabled && isParallax) {
							 Log.d(TAG, "onScrollChanged --> getScrollY() = "
							+ mRefreshableView.getScrollY());
							float f = mHeaderHeight
									- mHeaderContainer.getBottom()
									+ mRefreshableView.getScrollY();
							// Log.d(TAG, "onScrollChanged --> f = " + f);
							if ((f > 0.0F) && (f < mHeaderHeight)) {
								int i = (int) (0.65D * f);
								mHeaderContainer.scrollTo(0, -i);
							} else if (mHeaderContainer.getScrollY() != 0) {
								mHeaderContainer.scrollTo(0, 0);
							}
						}
					}
				});
	}

	@Override
	public Orientation getPullToRefreshScrollDirection() {
		// TODO Auto-generated method stub
		return Orientation.VERTICAL;
	}

	@Override
	protected ScrollView createRefreshableView(Context context,
			AttributeSet attrs) {
		// TODO Auto-generated method stub
		ScrollView scrollView;
		if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
			scrollView = new InternalScrollViewSDK9(context, attrs);
		} else {
			scrollView = new InternalScrollView(context, attrs);
		}

		scrollView.setId(R.id.scrollview);
		return scrollView;
	}

	@Override
	protected boolean isReadyForPullEnd() {
		// TODO Auto-generated method stub
		View scrollViewChild = mRefreshableView.getChildAt(0);
		if (null != scrollViewChild) {
			return mRefreshableView.getScrollY() >= (scrollViewChild
					.getHeight() - getHeight());
		}
		return false;
	}

	@Override
	protected boolean isReadyForPullStart() {
		// TODO Auto-generated method stub
		return mRefreshableView.getScrollY() == 0;
	}
	
	/**
	 * 添加actionbar渐变效果
	 * 
	 * @param activity
	 * @param actionbarBg
	 */
	public void addFadingActionbarEffect(final Activity activity,
			final Drawable actionbarBg) {
		if (mRefreshableView != null) {
			((InternalScrollView) mRefreshableView)
					.setOnCustomScrollChangedListener(new OnCustomScrollChangedListener() {
						@Override
						public void onCustomScrollChanged(int left, int top,
								int oldLeft, int oldTop) {
							ActionBar actionBar = null;
							if (activity instanceof ActionBarActivity) {
								actionBar = ((ActionBarActivity) activity)
										.getSupportActionBar();
							}
							if (mZoomView != null && actionBar != null) {
								int headerHeight = mZoomView.getHeight()
										- actionBar.getHeight();
								final float ratio = (float) Math.min(
										Math.max(top, 0), headerHeight)
										/ headerHeight;
								final int newAlpha = (int) (ratio * 255);
								actionbarBg.setAlpha(newAlpha);
							}
						}
					});
		}
	}

	/**
	 * 重写pulltoRefreshBase的handleStyledAttributes，在init的时候加入头图
	 */
	@Override
	protected void handleStyledAttributes(TypedArray a) {
		// =======================zoomView=================================
		LayoutInflater mLayoutInflater = LayoutInflater.from(getContext());

		int zoomViewResId = a.getResourceId(R.styleable.PullToRefresh_zoomView,
				0);
		if (zoomViewResId > 0) {
			mZoomView = mLayoutInflater.inflate(zoomViewResId, null, false);
		}

		int headerViewResId = a.getResourceId(
				R.styleable.PullToRefresh_headerView, 0);
		if (headerViewResId > 0) {
			mHeaderView = mLayoutInflater.inflate(headerViewResId, null, false);
		}
		isParallax = a.getBoolean(R.styleable.PullToRefresh_isHeaderParallax,
				true);

		mRootContainer = new LinearLayout(getContext());
		mRootContainer.setOrientation(LinearLayout.VERTICAL);
		mHeaderContainer = new FrameLayout(getContext());

		if (mZoomView != null) {
			mHeaderContainer.addView(mZoomView);
		}
		if (mHeaderView != null) {
			mHeaderContainer.addView(mHeaderView);
		}
		int contentViewResId = a.getResourceId(
				R.styleable.PullToRefresh_contentView, 0);
		if (contentViewResId > 0) {
			mContentView = mLayoutInflater.inflate(contentViewResId, null,
					false);
		}

		mRootContainer.addView(mHeaderContainer);
		if (mContentView != null) {
			mRootContainer.addView(mContentView);
		}

		mRootContainer.setClipChildren(false);
		mHeaderContainer.setClipChildren(false);

		mRefreshableView.addView(mRootContainer);
	}

//	@Override
//	public boolean isPullToRefreshEnabled() {
//		if (isZoomEnabled) {
//			return true;
//		} else {
//			return super.isPullToRefreshEnabled();
//		}
//	}

	
	@Override
	protected void onZoomFinished(Mode mode){
	   smoothScrollToTop();
	}

	@Override
	protected void setHeaderZoom(int scaleValue) {
		pullHeaderToZoom(scaleValue);
	}
	
	/**
	 * 默认不支持放大
	 * @return
	 */
	@Override
	protected boolean isReadyForZoomStart(){
		return isZoomEnabled&&mRefreshableView.getScrollY() == 0;
	}
	
	/**
	 * 头图放大
	 * 
	 * @param newScrollValue
	 */
	protected void pullHeaderToZoom(int newScrollValue) {
		// TODO Auto-generated method stub
		// Log.d(TAG, "pullHeaderToZoom --> newScrollValue = " +
		// newScrollValue);
		// Log.d(TAG, "pullHeaderToZoom --> mHeaderHeight = " + mHeaderHeight);
		if (mScalingRunnable != null && !mScalingRunnable.isFinished()) {
			mScalingRunnable.abortAnimation();
		}

		ViewGroup.LayoutParams localLayoutParams = mHeaderContainer
				.getLayoutParams();
		localLayoutParams.height = Math.abs(newScrollValue) + mHeaderHeight;
		mHeaderContainer.setLayoutParams(localLayoutParams);

		if (isCustomHeaderHeight) {
			ViewGroup.LayoutParams zoomLayoutParams = mZoomView
					.getLayoutParams();
			zoomLayoutParams.height = Math.abs(newScrollValue) + mHeaderHeight;
			mZoomView.setLayoutParams(zoomLayoutParams);
		}
	}

	protected void smoothScrollToTop() {
		Log.d(TAG, "smoothScrollToTop --> ");
		mScalingRunnable.startAnimation(200L);
	}
	
	class ScalingRunnable implements Runnable {
		protected long mDuration;
		protected boolean mIsFinished = true;
		protected float mScale;
		protected long mStartTime;

		ScalingRunnable() {
		}

		public void abortAnimation() {
			mIsFinished = true;
		}

		public boolean isFinished() {
			return mIsFinished;
		}

		public void run() {
			if (mZoomView != null) {
				float f2;
				ViewGroup.LayoutParams localLayoutParams;
				if ((!mIsFinished) && (mScale > 1.0D)) {
					float f1 = ((float) SystemClock.currentThreadTimeMillis() - (float) mStartTime)
							/ (float) mDuration;
					f2 = mScale
							- (mScale - 1.0F)
							* PullToRefreshZoomScrollView.sInterpolator
									.getInterpolation(f1);
					localLayoutParams = mHeaderContainer.getLayoutParams();
					// Log.d(TAG, "ScalingRunnable --> f2 = " + f2);
					if (f2 > 1.0F) {
						localLayoutParams.height = ((int) (f2 * mHeaderHeight));
						mHeaderContainer.setLayoutParams(localLayoutParams);
						if (isCustomHeaderHeight) {
							ViewGroup.LayoutParams zoomLayoutParams;
							zoomLayoutParams = mZoomView.getLayoutParams();
							zoomLayoutParams.height = ((int) (f2 * mHeaderHeight));
							mZoomView.setLayoutParams(zoomLayoutParams);
						}
						post(this);
						return;
					}
					mIsFinished = true;
				}
			}
		}

		public void startAnimation(long paramLong) {
			if (mZoomView != null) {
				mStartTime = SystemClock.currentThreadTimeMillis();
				mDuration = paramLong;
				mScale = ((float) (mHeaderContainer.getBottom()) / mHeaderHeight);
				mIsFinished = false;
				post(this);
			}
		}
	}

	@TargetApi(9)
	final class InternalScrollViewSDK9 extends InternalScrollView {

		public InternalScrollViewSDK9(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
				int scrollY, int scrollRangeX, int scrollRangeY,
				int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

			final boolean returnValue = super.overScrollBy(deltaX, deltaY,
					scrollX, scrollY, scrollRangeX, scrollRangeY,
					maxOverScrollX, maxOverScrollY, isTouchEvent);

			// Does all of the hard work...
			OverscrollHelper.overScrollBy(PullToRefreshZoomScrollView.this, deltaX,
					scrollX, deltaY, scrollY, getScrollRange(), isTouchEvent);
			return returnValue;
		}

		/**
		 * Taken from the AOSP ScrollView source
		 */
		private int getScrollRange() {
			int scrollRange = 0;
			if (getChildCount() > 0) {
				View child = getChildAt(0);
				Log.d("======myRefreshview", "child at 0:" + child.toString()
						+ "child at 0 id:" + child.getId());
				scrollRange = Math.max(0, child.getHeight()
						- (getHeight() - getPaddingBottom() - getPaddingTop()));
			}
			return scrollRange;
		}
	}

	/**
	 * 可监听滚动的ScrollView
	 */
	protected class InternalScrollView extends ScrollView {
		private OnScrollViewChangedListener onScrollViewChangedListener;
		private OnCustomScrollChangedListener onCustomScrollChangedListener;

		public InternalScrollView(Context context) {
			this(context, null);
		}

		public InternalScrollView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public void setOnScrollViewChangedListener(
				OnScrollViewChangedListener onScrollViewChangedListener) {
			this.onScrollViewChangedListener = onScrollViewChangedListener;
		}

		public void setOnCustomScrollChangedListener(
				OnCustomScrollChangedListener listener) {
			this.onCustomScrollChangedListener = listener;
		}

		@Override
		protected void onScrollChanged(int l, int t, int oldl, int oldt) {
			super.onScrollChanged(l, t, oldl, oldt);
			if (onScrollViewChangedListener != null) {
				onScrollViewChangedListener.onInternalScrollChanged(l, t, oldl,
						oldt);
			}
			if (onCustomScrollChangedListener != null) {
				onCustomScrollChangedListener.onCustomScrollChanged(l, t, oldl,
						oldt);
			}
		}
	}

	protected interface OnScrollViewChangedListener {
		public void onInternalScrollChanged(int left, int top, int oldLeft,
				int oldTop);
	}

	protected interface OnCustomScrollChangedListener {
		public void onCustomScrollChanged(int left, int top, int oldLeft,
				int oldTop);
	}

	protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2,
			int paramInt3, int paramInt4) {
		super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
		// Log.d(TAG, "onLayout --> ");
		if (mHeaderHeight == 0 && mZoomView != null) {
			mHeaderHeight = mHeaderContainer.getHeight();
		}
	}

	public void setHeaderView(View headerView) {
		if (headerView != null) {
			mHeaderView = headerView;
			updateHeaderView();
		}
	}

	public void setZoomView(View zoomView) {
		if (zoomView != null) {
			mZoomView = zoomView;
			updateHeaderView();
		}
	}

	private void updateHeaderView() {
		if (mHeaderContainer != null) {
			mHeaderContainer.removeAllViews();

			if (mZoomView != null) {
				mHeaderContainer.addView(mZoomView);
			}

			if (mHeaderView != null) {
				mHeaderContainer.addView(mHeaderView);
			}
		}
	}

	public void setScrollContentView(View contentView) {
		if (contentView != null) {
			if (mContentView != null) {
				mRootContainer.removeView(mContentView);
			}
			mContentView = contentView;
			mRootContainer.addView(mContentView);
		}
	}

	/**
	 * 设置HeaderView高度
	 *
	 * @param width
	 * @param height
	 */
	public void setHeaderViewSize(int width, int height) {
		if (mHeaderContainer != null) {
			Object localObject = mHeaderContainer.getLayoutParams();
			if (localObject == null) {
				localObject = new ViewGroup.LayoutParams(width, height);
			}
			((ViewGroup.LayoutParams) localObject).width = width;
			((ViewGroup.LayoutParams) localObject).height = height;
			mHeaderContainer
					.setLayoutParams((ViewGroup.LayoutParams) localObject);
			mHeaderHeight = height;
			isCustomHeaderHeight = true;
		}
	}

	/**
	 * 设置HeaderView LayoutParams
	 *
	 * @param layoutParams
	 *            LayoutParams
	 */
	public void setHeaderLayoutParams(LayoutParams layoutParams) {
		if (mHeaderContainer != null) {
			mHeaderContainer.setLayoutParams(layoutParams);
			mHeaderHeight = layoutParams.height;
			isCustomHeaderHeight = true;
		}
	}

	public void enbaleZoom() {
		this.isZoomEnabled = true;
	}

	public void disableZoom() {
		this.isZoomEnabled = false;
	}

}
