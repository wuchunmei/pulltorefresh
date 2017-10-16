//package cn.tianya.light.pulltorefresh;
//
//import android.content.Context;
//import android.util.AttributeSet;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.LinearLayout;
//
///**
// * handle scroll conflict about PullToRefreshListView with view
// *
// * @author zhanggx
// *
// */
//
//public class ScrollConflictHelperLayout extends LinearLayout {
//	private final static int HORIZONTAL_SCROLL_DISTANCE = 20;
//	private PullToRefreshListView pullToRefreshListView;
//	private View mScrollConflictView;
//	private int mScrollConflictPosition;
//	float mDownX = 0.0f;
//	float mDownY = 0.0f;
//
//	public ScrollConflictHelperLayout(Context context) {
//		super(context);
//	}
//
//	public ScrollConflictHelperLayout(Context context, AttributeSet attrs) {
//		super(context, attrs);
//	}
//
//	public void setPullToRefreshListView(
//			PullToRefreshListView pullToRefreshListView) {
//		this.pullToRefreshListView = pullToRefreshListView;
//	}
//
//	public void setConflictView(View view) {
//		this.mScrollConflictView = view;
//	}
//
//	public void setConflictPosition(int position) {
//		this.mScrollConflictPosition = position;
//	}
//
//	@Override
//	public boolean onInterceptTouchEvent(MotionEvent ev) {
//
//		switch (ev.getAction()) {
//		case MotionEvent.ACTION_DOWN:
//			mDownX = ev.getX();
//			mDownY = ev.getY();
//			break;
//		case MotionEvent.ACTION_MOVE:
//			final int x = (int) ev.getX();
//			final int y = (int) ev.getY();
//			int motionPosition = pullToRefreshListView.getRefreshableView()
//					.pointToPosition(x, y);
//			if (motionPosition == mScrollConflictPosition) {
//				final int dx = Math.abs((int) (x - mDownX));
//				final int dy = Math.abs((int) (y - mDownY));
//				if (dx > HORIZONTAL_SCROLL_DISTANCE && dx > dy) {
//					return true;
//				}
//			}
//			break;
//		}
//
//		return super.onInterceptTouchEvent(ev);
//
//	}
//
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		final int x = (int) event.getX();
//		final int y = (int) event.getY();
//		int motionPosition = pullToRefreshListView.getRefreshableView()
//				.pointToPosition(x, y);
//		if (motionPosition == mScrollConflictPosition) {
//			return mScrollConflictView.onTouchEvent(event);
//		}
//		return super.onTouchEvent(event);
//	}
//}
