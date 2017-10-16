package cn.tianya.light.pulltorefresh;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.widget.ListAdapter;
import android.widget.ListView;
import cn.tianya.light.pulltorefresh.SwipeListViewTouchListener.IsAllowSwipeAtPosition;
/**
 * 可左滑listview
 * @author wudeng
 *
 */
public class SwipeListView extends ListView {
	private SwipeListViewTouchListener touchListener;
	public SwipeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		final ViewConfiguration configuration = ViewConfiguration
				.get(getContext());
		final int touchSlop = ViewConfigurationCompat
				.getScaledPagingTouchSlop(configuration);
		touchListener = new SwipeListViewTouchListener(this, touchSlop);
		setOnTouchListener(touchListener);
		setOnScrollListener(touchListener.makeScrollListener());
	}
	/**
	 * 设置某项是否可以左右滑动
	 * @param isAllowSwipeAtPosition
	 */
	public void setIsAllowSwipeAtPosition(IsAllowSwipeAtPosition isAllowSwipeAtPosition){
		touchListener.setIsAllowSwipeAtPosition(isAllowSwipeAtPosition);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		touchListener.resetItems();
		adapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				touchListener.resetItems();
			}
		});
	}

	public void closeOpenedItems(){
		if(touchListener != null){
			touchListener.close();
		}
	}

	public boolean isOpened(){
		if(touchListener != null){
			return touchListener.isOpened();
		}

		return false;
	}

	public boolean checkedAndClosedItems(){
		if(isOpened()){
			closeOpenedItems();
			return true;
		}

		return false;
	}
}
