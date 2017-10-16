package cn.tianya.light.pulltorefresh;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import cn.tianya.light.R;

/**
 * listview item滑动动作监听
 * @author wudeng
 *
 */
public class SwipeListViewTouchListener implements View.OnTouchListener {
	static String tag = SwipeListViewTouchListener.class.getSimpleName();
	private VelocityTracker velocityTracker;
	private SwipeListView slideListView = null;
	private IsAllowSwipeAtPosition isAllowSwipeAtPosition;
	private View showView = null;
	private View hideView = null;

	private Rect rect = new Rect();
	private float moveX = 0;
	private float downX;
	private long animationTime = 200;

	private int touchSlop = 0;
	private int downPosition = ListView.INVALID_POSITION;
	private int viewWidth = 1;

	private List<Boolean> opened = new ArrayList<Boolean>();
	private boolean swiping;
	private boolean paused;
	private boolean ignore ;
	private int prePosition=ListView.INVALID_POSITION;

	public SwipeListViewTouchListener(SwipeListView slideListView, int touchSlop) {
		this.slideListView = slideListView;
		this.touchSlop = touchSlop;
	}

	public IsAllowSwipeAtPosition getIsAllowSwipeAtPosition() {
		return isAllowSwipeAtPosition;
	}
	public void setIsAllowSwipeAtPosition(
			IsAllowSwipeAtPosition isAllowSwipeAtPosition) {
		this.isAllowSwipeAtPosition = isAllowSwipeAtPosition;
	}



	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (viewWidth < 2) {
			viewWidth = slideListView.getWidth();
		}

		switch (MotionEventCompat.getActionMasked(motionEvent)) {
		case MotionEvent.ACTION_DOWN: {
			if (paused && downPosition != ListView.INVALID_POSITION) {
				return false;
			}
			boolean hasOpened = false;
			if (checkOpened()) {
				hasOpened = true;
				closeOpenedItems();
//				return true;
			}
			if(ignore){
				return true;
			}
			int childCount = slideListView.getChildCount();
			int[] listViewCoords = new int[2];
			slideListView.getLocationOnScreen(listViewCoords);
			int x = (int) motionEvent.getRawX() - listViewCoords[0];
			int y = (int) motionEvent.getRawY() - listViewCoords[1];
			View child;
			for (int i = 0; i < childCount; i++) {
				child = slideListView.getChildAt(i);
				child.getHitRect(rect);

				int childPosition = slideListView.getPositionForView(child);

				boolean allowSwipe = slideListView.getAdapter().isEnabled(
						childPosition)
						&& slideListView.getAdapter().getItemViewType(
								childPosition) >= 0;
				if(isAllowSwipeAtPosition!=null){
					allowSwipe = allowSwipe && isAllowSwipeAtPosition.isAllowSwipeAtPosition(childPosition);
				}
				if (allowSwipe && rect.contains(x, y)) {
					// 向右滑动关闭hideview时，忽略一小段时间的touch事件，避免关闭hideview时还响应click事件
					if(hasOpened){
						if(childPosition==prePosition){
							ignore = true;
							slideListView.postDelayed(new Runnable() {

								@Override
								public void run() {
									ignore = false;

								}
							}, 200);
							prePosition = ListView.INVALID_POSITION;
							return true;
						}else{
							prePosition = ListView.INVALID_POSITION;
							return false;
						}
					}

					showView = child.findViewById(R.id.left_view);
					hideView = child.findViewById(R.id.right_view);

					downX = motionEvent.getRawX();
					downPosition = childPosition;
					prePosition =downPosition;
					velocityTracker = VelocityTracker.obtain();
					velocityTracker.addMovement(motionEvent);
					break;
				}
			}
			view.onTouchEvent(motionEvent);
			return true;
		}

		case MotionEvent.ACTION_UP: {
			if(ignore){
				return true;
			}
			if (velocityTracker == null || !swiping
					|| downPosition == ListView.INVALID_POSITION) {
				break;
			}


			velocityTracker.addMovement(motionEvent);
			velocityTracker.computeCurrentVelocity(1000);
			moveTo(downPosition);
			velocityTracker.recycle();
			velocityTracker = null;
			downX = 0;
			swiping = false;
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			if(ignore){
				return true;
			}
			if (velocityTracker == null || paused
					|| downPosition == ListView.INVALID_POSITION) {
				break;
			}

			velocityTracker.addMovement(motionEvent);
			velocityTracker.computeCurrentVelocity(1000);
			float velocityX = Math.abs(velocityTracker.getXVelocity());
			float velocityY = Math.abs(velocityTracker.getYVelocity());

			float deltaX = motionEvent.getRawX() - downX;
			float deltaMode = Math.abs(deltaX);

			if (deltaMode > touchSlop && velocityY < velocityX) {
				swiping = true;
				slideListView.requestDisallowInterceptTouchEvent(true);
				MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
				cancelEvent
						.setAction(MotionEvent.ACTION_CANCEL
								| (MotionEventCompat
										.getActionIndex(motionEvent) << MotionEventCompat.ACTION_POINTER_INDEX_SHIFT));
				slideListView.onTouchEvent(cancelEvent);
			}

			if (swiping && downPosition != ListView.INVALID_POSITION) {
				if (deltaX >= 0) {
					deltaX = 0;
				}

				if (deltaX <= -hideView.getWidth()) {
					deltaX = -hideView.getWidth();
				}
				showView.setTranslationX(deltaX);
				hideView.setTranslationX(deltaX);
				return true;
			}
			break;
		}

		}
		return false;
	}

	private void moveTo(final int downPosition) {
		final int hideViewWidth = hideView.getWidth();
		final float translationX = showView.getTranslationX();
		float deltaX = 0;

		if (translationX == -hideViewWidth) {
			animationTime = 0;
		} else {
			animationTime = 200;
		}

		if (translationX > -hideViewWidth / 2) {
			deltaX = 0;
		} else if (translationX <= -hideViewWidth / 2) {
			deltaX = -hideViewWidth;
		}
		moveX += deltaX;
		if ((moveX >= 0) || (deltaX == 0)) {
			moveX = 0;
		} else if (moveX <= -hideViewWidth) {
			moveX = -hideViewWidth;
		}
		showView.animate().translationX(deltaX)
				.setDuration(animationTime).setListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator arg0) {
					}

					@Override
					public void onAnimationRepeat(Animator arg0) {
					}

					@Override
					public void onAnimationEnd(Animator arg0) {
						boolean aux = !opened.get(downPosition);
						opened.set(downPosition, aux);
						resetCell();
					}

					@Override
					public void onAnimationCancel(Animator arg0) {
					}
				});
		hideView.animate().translationX(deltaX)
				.setDuration(animationTime);
	}

	private void moveToRight(final int downPosition) {
		float deltaX = 0;
		animationTime = 100;
		showView.animate().translationX(deltaX)
				.setDuration(animationTime).setListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator arg0) {
					}

					@Override
					public void onAnimationRepeat(Animator arg0) {
					}

					@Override
					public void onAnimationEnd(Animator arg0) {
						boolean aux = !opened.get(downPosition);
						opened.set(downPosition, aux);
						resetCell();
					}

					@Override
					public void onAnimationCancel(Animator arg0) {
					}
				});
		hideView.animate().translationX(deltaX)
				.setDuration(animationTime);
	}

	public void resetItems() {
		if (slideListView.getAdapter() != null) {
			int count = slideListView.getAdapter().getCount();
			for (int i = opened.size(); i <= count; i++) {
				opened.add(false);
			}
		}
	}

	private void closeOpenedItems() {
		if (opened != null) {
			int start = slideListView.getFirstVisiblePosition();
			int end = slideListView.getLastVisiblePosition();
			for (int i = start; i <= end; i++) {
				if (opened.get(i)) {
					moveToRight(i);
				}
			}
		}
	}

	private boolean checkOpened() {
		int i = 0;
		for (boolean open : opened) {
			if (open) {
				return true;
			}

			i++;
		}

		return false;
	}

	public void close(){
		closeOpenedItems();
	}

	public boolean isOpened(){
		return checkOpened();
	}

	private void resetCell() {
		downPosition = ListView.INVALID_POSITION;

	}

	public void setEnabled(boolean enabled) {
		paused = !enabled;
	}

	/**
	 * Return ScrollListener for ListView
	 *
	 * @return OnScrollListener
	 */
	public AbsListView.OnScrollListener makeScrollListener() {
		return new AbsListView.OnScrollListener() {

			private boolean isFirstItem = false;
			private boolean isLastItem = false;

			@Override
			public void onScrollStateChanged(AbsListView absListView,
					int scrollState) {
				setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
				if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
					setEnabled(false);
				}
				if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_FLING
						&& scrollState != SCROLL_STATE_TOUCH_SCROLL) {
					downPosition = ListView.INVALID_POSITION;
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							setEnabled(true);
						}
					}, 500);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (isFirstItem) {
					boolean onSecondItemList = firstVisibleItem == 1;
					if (onSecondItemList) {
						isFirstItem = false;
					}
				} else {
					boolean onFirstItemList = firstVisibleItem == 0;
					if (onFirstItemList) {
						isFirstItem = true;
					}
				}
				if (isLastItem) {
					boolean onBeforeLastItemList = firstVisibleItem
							+ visibleItemCount == totalItemCount - 1;
					if (onBeforeLastItemList) {
						isLastItem = false;
					}
				} else {
					boolean onLastItemList = firstVisibleItem
							+ visibleItemCount >= totalItemCount;
					if (onLastItemList) {
						isLastItem = true;
					}
				}
			}
		};
	}
	/**
	 * position代表的项是否可以左右滑动
	 * @author zengyp@staff.tianya.cn
	 *
	 */
	public interface IsAllowSwipeAtPosition{
		public boolean isAllowSwipeAtPosition(int position);
	}
}
