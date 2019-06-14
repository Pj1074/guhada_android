package io.temco.guhada.view.adapter.holder;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import io.temco.guhada.data.model.Brand;
import io.temco.guhada.databinding.ItemBrandListContentsBinding;
import io.temco.guhada.databinding.ItemDetailSearchBrandListContentsBinding;
import io.temco.guhada.view.adapter.base.BaseBrandViewHolder;

public class DetailSearchBrandListContentsViewHolder extends BaseBrandViewHolder<ItemDetailSearchBrandListContentsBinding> {

    ////////////////////////////////////////////////
    // CONSTRUCTOR
    ////////////////////////////////////////////////

    public DetailSearchBrandListContentsViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    ////////////////////////////////////////////////
    // OVERRIDE
    ////////////////////////////////////////////////

    @Override
    public void init(Context context, Brand data, boolean isAlphabet) {
        if (data != null) {
            // Title
            mBinding.setTitle(isAlphabet ? data.nameEn : data.nameKo);
            // Selected
            mBinding.getRoot().setSelected(data.isSelected);
        }
    }

    ////////////////////////////////////////////////
}