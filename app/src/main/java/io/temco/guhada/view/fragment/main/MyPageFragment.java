package io.temco.guhada.view.fragment.main;

import android.view.View;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import io.temco.guhada.R;
import io.temco.guhada.common.listener.OnDrawerLayoutListener;
import io.temco.guhada.common.util.CommonUtil;
import io.temco.guhada.databinding.FragmentMainMyPageBinding;
import io.temco.guhada.view.adapter.MyPagePagerAdapter;
import io.temco.guhada.view.fragment.base.BaseFragment;

public class MyPageFragment extends BaseFragment<FragmentMainMyPageBinding> implements View.OnClickListener {

    // -------- LOCAL VALUE --------
    private OnDrawerLayoutListener mDrawerListener;
    private MyPagePagerAdapter mPagerAdapter;
    // -----------------------------

    ////////////////////////////////////////////////
    // OVERRIDE
    ////////////////////////////////////////////////

    @Override
    protected String getBaseTag() {
        return MyPageFragment.class.getSimpleName();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main_my_page;
    }

    protected void init() {
        initHeader();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_side_menu:
                if (mDrawerListener != null) {
                    mDrawerListener.onDrawerEvnet(true);
                }
                break;

            case R.id.image_search:
                CommonUtil.debug("image_search");
                break;

            case R.id.image_shop_cart:
                CommonUtil.debug("image_shop_cart");
                break;

        }
    }

    ////////////////////////////////////////////////
    // PUBLIC
    ////////////////////////////////////////////////

    public void setOnDrawerLayoutListener(OnDrawerLayoutListener listener) {
        mDrawerListener = listener;
    }

    ////////////////////////////////////////////////
    // PRIVATE
    ////////////////////////////////////////////////

    private void initHeader() {
        mBinding.layoutHeader.setClickListener(this);

        // Tab
        setTabLayout();
    }


    private void setTabLayout() {
        // Remove
        if (mBinding.layoutHeader.layoutTab.getChildCount() > 0) {
            mBinding.layoutHeader.layoutTab.removeAllTabs();
        }
        // Adapter
        if (mPagerAdapter == null)
            mPagerAdapter = new MyPagePagerAdapter(getChildFragmentManager());
        // Pager
        mBinding.pager.setAdapter(mPagerAdapter);
        mBinding.pager.setSwipeLocked(false);
        mBinding.pager.setOffscreenPageLimit(mPagerAdapter.getCount());
        mBinding.pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.layoutHeader.layoutTab));
        // Tab
        // mBinding.layoutHeader.layoutTab.addOnTabSelectedListener();
        // Add Tab
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            switch (i) {
                case 0: // 주문 배송
                    addCustomTabs(getString(R.string.mypage_title_order_shipping), true);
                    break;

                case 1: // 취소/교환/반품
                    // addCustomTabs(getString(R.string.mypage_title_cancel_swap_return), true);
                    break;
            }
        }
    }

    private void addCustomTabs(String title, boolean isSelect) {
        if (getContext() != null) {
            View v = getLayoutInflater().inflate(R.layout.layout_tab_category, null);
            ((TextView) v.findViewById(R.id.text_title)).setText(title);
            TabLayout.Tab tab = mBinding.layoutHeader.layoutTab.newTab().setCustomView(v);
            mBinding.layoutHeader.layoutTab.addTab(tab);
            if (isSelect) tab.select();
        }
    }

    ////////////////////////////////////////////////
}