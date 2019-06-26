package io.temco.guhada.view.adapter;

import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import io.temco.guhada.common.listener.OnAddCategoryListener;
import io.temco.guhada.common.listener.OnChangeTitleListener;
import io.temco.guhada.data.model.Brand;
import io.temco.guhada.data.model.Category;
import io.temco.guhada.view.fragment.product.ProductListFragment;

public class ProductListPagerAdapter extends FragmentStatePagerAdapter {

    // -------- LOCAL VALUE --------
    private final String TAG = ProductListPagerAdapter.class.getSimpleName();
    private OnChangeTitleListener mChangeTitleListener;
    private OnAddCategoryListener mCategoryListener;
    private List<Category> mCategoryList;
    private List<ProductListFragment> mFragmentList;
    // -----------------------------

    ////////////////////////////////////////////////
    // CONSTRUCTOR
    ////////////////////////////////////////////////

    public ProductListPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
        mCategoryList = new ArrayList<>();
        mFragmentList = new ArrayList<>();
    }

    ////////////////////////////////////////////////
    // OVERRIDE
    ////////////////////////////////////////////////

    @Override
    public int getItemPosition(@NonNull Object object) {
        if (object instanceof ProductListFragment) {
            ProductListFragment frag = (ProductListFragment) object;
            if (frag.getPosition() < mCategoryList.size()) {
                Category data = mCategoryList.get(frag.getPosition());
                if (data != null) {
                    frag.onUpdate(data);
                }
            } else if (frag.getPosition() == mCategoryList.size()) {
                frag.onReset();
            }
        }
        return super.getItemPosition(object);
        // return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mFragmentList == null ? 0 : mFragmentList.size();
    }

    @NonNull
    @Override
    public ProductListFragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    ////////////////////////////////////////////////
    // PUBLIC
    ////////////////////////////////////////////////

    public void addCategoryFragment(Category data) {
        // Fragment
        ProductListFragment f = new ProductListFragment();
        f.setIsCategory(true);
        f.setCategoryData(data);
        f.setOnAddCategoryListener(mCategoryListener);
        // Add
        mFragmentList.add(f);
        mCategoryList.add(data);
        // Position
        f.setPosition(mFragmentList.size() - 1);
        // Title
        if (mChangeTitleListener != null) {
            mChangeTitleListener.onSet(data.title);
        }
        notifyDataSetChanged();
    }

    public void addBrandFragment(Brand data) {
        // Fragment
        ProductListFragment f = new ProductListFragment();
        f.setIsCategory(false);
        f.setBrandData(data);
        // Add
        mFragmentList.add(f);
        // Position
        f.setPosition(mFragmentList.size() - 1);
        // Title
        notifyDataSetChanged();
    }

    public boolean removeFragment() {
        int changeCount = getCount() - 1;
        // Title
        if (mChangeTitleListener != null && getCategoryCount() > 0) {
            String title = mCategoryList.get(changeCount - 1).title;
            mChangeTitleListener.onSet(title);
        }
        // Remove
        if (getCount() > 1) {
            mFragmentList.remove(changeCount);
            if (getCategoryCount() > 1) mCategoryList.remove(changeCount);
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    public void removeAll() {
        mFragmentList.clear();
        mCategoryList.clear();
        notifyDataSetChanged();
    }

    public void setOnChangeTitleListener(OnChangeTitleListener listener) {
        mChangeTitleListener = listener;
    }

    public void setOnAddCategoryListener(OnAddCategoryListener listener) {
        mCategoryListener = listener;
    }

    ////////////////////////////////////////////////
    // PRIVATE
    ////////////////////////////////////////////////

    private int getCategoryCount() {
        return mCategoryList == null ? 0 : mCategoryList.size();
    }

    ////////////////////////////////////////////////
}