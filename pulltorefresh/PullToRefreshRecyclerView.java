/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package cn.tianya.light.pulltorefresh;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.lidroid.xutils.view.annotation.event.OnScroll;

public class PullToRefreshRecyclerView extends PullToRefreshBase<RecyclerView>  {
    private static final int NO_POSITION = 0;
    private RecyclerView.OnScrollListener mOnScrollListener;
    private RecyclerView.OnScrollListener mOnViewScrollListener=new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (null != mOnScrollListener) {
                mOnScrollListener.onScrollStateChanged(recyclerView, newState);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (null!=mOnScrollListener){
                mOnScrollListener.onScrolled(recyclerView,dx,dy);
            }
        }
    };
    public PullToRefreshRecyclerView(Context context) {
        super(context);
        mRefreshableView.setOnScrollListener(mOnViewScrollListener);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRefreshableView.setOnScrollListener(mOnViewScrollListener);
    }

    public PullToRefreshRecyclerView(Context context, Mode mode) {
        super(context, mode);
        mRefreshableView.setOnScrollListener(mOnViewScrollListener);
    }

    public PullToRefreshRecyclerView(Context context, Mode mode, AnimationStyle style) {
        super(context, mode, style);
        mRefreshableView.setOnScrollListener(mOnViewScrollListener);
    }

    public void setOnscrollListener(RecyclerView.OnScrollListener onscrollListener){
        this.mOnScrollListener=onscrollListener;
    }

    @Override
    public final Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected RecyclerView createRefreshableView(Context context,
                                                 AttributeSet attrs) {
        RecyclerView recyclerView = new RecyclerView(context, attrs);
        return recyclerView;
    }

    @Override
    protected boolean isReadyForPullStart() {
        return isFirstItemVisible();
    }

    @Override
    protected boolean isReadyForPullEnd() {
        return isLastItemVisible();
    }

    private boolean isFirstItemVisible() {
        final RecyclerView.Adapter<?> adapter = getRefreshableView().getAdapter();

        // 如果未设置Adapter或者Adapter没有数据可以下拉刷新
        if (null == adapter || adapter.getItemCount() == 0) {
            if (DEBUG) {
                Log.d(LOG_TAG, "isFirstItemVisible. Empty View.");
            }
            return true;

        } else {
            // 第一个条目完全展示,可以刷新
            if (getFirstVisiblePosition() == 0) {
                return mRefreshableView.getChildAt(0).getTop() >= mRefreshableView
                        .getTop();
            }
        }

        return false;
    }

    public int getFirstVisiblePosition() {
        View firstVisibleChild = mRefreshableView.getChildAt(0);
        return firstVisibleChild != null ? mRefreshableView
                .getChildAdapterPosition(firstVisibleChild) : -1;
    }

    public boolean isLastItemVisible() {
        final RecyclerView.Adapter<?> adapter = getRefreshableView().getAdapter();

        // 如果未设置Adapter或者Adapter没有数据可以上拉刷新
        if (null == adapter || adapter.getItemCount() == 0) {
            if (DEBUG) {
                Log.d(LOG_TAG, "isLastItemVisible. Empty View.");
            }
            return true;

        } else {
            // 最后一个条目View完全展示,可以刷新
            int lastVisiblePosition = getLastVisiblePosition();
            if (lastVisiblePosition >= mRefreshableView.getAdapter().getItemCount() - 1) {
                return mRefreshableView.getChildAt(
                        mRefreshableView.getChildCount() - 1).getBottom() <= mRefreshableView
                        .getBottom();
            }
        }

        return false;
    }


    public int getLastVisiblePosition() {
        View lastVisibleChild = mRefreshableView.getChildAt(mRefreshableView
                .getChildCount() - 1);
        return lastVisibleChild != null ? mRefreshableView
                .getChildAdapterPosition(lastVisibleChild) : -1;
    }

    public int getLastVisibleItemPosition(){
        RecyclerView.LayoutManager manager = mRefreshableView.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).findLastVisibleItemPosition();
        }
        return NO_POSITION;
    }
    public boolean isBottomViewVisible() {
        int lastVisibleItem = getLastVisibleItemPosition();
        if (lastVisibleItem > NO_POSITION && lastVisibleItem == mRefreshableView.getAdapter().getItemCount() - 1){
            return mRefreshableView.getChildAt(mRefreshableView.getChildCount() - 1).getBottom() <= mRefreshableView
                    .getBottom();
        }
        return false;
    }
}