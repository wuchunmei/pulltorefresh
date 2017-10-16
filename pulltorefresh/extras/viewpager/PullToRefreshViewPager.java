package cn.tianya.light.pulltorefresh.extras.viewpager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import cn.tianya.light.R;
import cn.tianya.light.pulltorefresh.PullToRefreshBase;
import cn.tianya.light.view.ViewPagerFixed;


/**
 * Created by Page on 13-12-28.
 */
    public class PullToRefreshViewPager extends PullToRefreshBase<ViewPagerFixed> {

    public PullToRefreshViewPager(Context context) {
        super(context);
    }

    public PullToRefreshViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public final Orientation getPullToRefreshScrollDirection() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected ViewPagerFixed createRefreshableView(Context context, AttributeSet attrs) {
        ViewPagerFixed viewPager = new ViewPagerFixed(context, attrs);
        viewPager.setId(R.id.viewpager);
        return viewPager;
    }

    @Override
    protected boolean isReadyForPullStart() {
        ViewPager refreshableView = getRefreshableView();

        PagerAdapter adapter = refreshableView.getAdapter();
        if (null != adapter) {
            return refreshableView.getCurrentItem() == 0;
        }

        return false;
    }

    @Override
    protected boolean isReadyForPullEnd() {
        ViewPager refreshableView = getRefreshableView();

        PagerAdapter adapter = refreshableView.getAdapter();
        if (null != adapter) {
            return refreshableView.getCurrentItem() == adapter.getCount() - 1;
        }

        return false;
    }
}
