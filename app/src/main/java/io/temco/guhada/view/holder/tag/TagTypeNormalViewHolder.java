package io.temco.guhada.view.holder.tag;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import io.temco.guhada.data.Tag;
import io.temco.guhada.databinding.ItemTagTypeNormalBinding;
import io.temco.guhada.view.holder.base.BaseTagViewHolder;

public class TagTypeNormalViewHolder extends BaseTagViewHolder<ItemTagTypeNormalBinding> {

    ////////////////////////////////////////////////
    // CONSTRUCTOR
    ////////////////////////////////////////////////

    public TagTypeNormalViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    ////////////////////////////////////////////////
    // OVERRIDE
    ////////////////////////////////////////////////


    @Override
    public void init(Context context, Tag data) {
        if (data != null) {
            // Title
            if (!TextUtils.isEmpty(data.title)) {
                mBinding.setTitle(data.title);
            }
        }
    }


    ////////////////////////////////////////////////
}