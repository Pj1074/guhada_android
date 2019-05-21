package io.temco.guhada.view.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.widget.Toast;

import io.temco.guhada.R;
import io.temco.guhada.common.Type;
import io.temco.guhada.common.listener.OnFindAccountListener;
import io.temco.guhada.common.util.CommonUtil;
import io.temco.guhada.data.viewmodel.FindAccountViewModel;
import io.temco.guhada.databinding.ActivityFindaccountBinding;
import io.temco.guhada.view.activity.base.BindActivity;
import io.temco.guhada.view.adapter.FindAccountPagerAdapter;
import io.temco.guhada.view.fragment.findaccount.FindIdFragment;

public class FindAccountActivity extends BindActivity<ActivityFindaccountBinding> {
    private FindAccountViewModel mViewModel;

    @Override
    protected String getBaseTag() {
        return FindAccountActivity.class.getSimpleName();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_findaccount;
    }

    @Override
    protected Type.View getViewType() {
        return Type.View.FIND_ACCOUNT;
    }

    @Override
    protected void init() {
        initViewModel();
        initFragmentPager();
        mBinding.tablayoutFindaccount.setTabTextColors(getResources().getColor(R.color.common_black), getResources().getColor(R.color.colorPrimary));
        mBinding.executePendingBindings();
    }

    private void initViewModel() {
        mViewModel = new FindAccountViewModel();
        mViewModel.setFindAccountListener(new OnFindAccountListener() {
            @Override
            public void showSnackBar(String message) {
                CommonUtil.showSnackBar(mBinding.linearlayoutFiindaccountContainer, message, getResources().getColor(R.color.colorPrimary), (int) getResources().getDimension(R.dimen.height_header));
            }

            @Override
            public void showMessage(String message) {
                Toast.makeText(FindAccountActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void closeActivity() {
                finish();
            }

            @Override
            public void hideKeyboard() {
                CommonUtil.hideKeyboard(getApplicationContext(), mBinding.linearlayoutFiindaccountContainer);
            }

            @Override
            public void redirectSignUpActivity() {
                startActivity(new Intent(getApplicationContext(), JoinActivity.class));
                finish();
            }

            @Override
            public void redirectSignInActivity() {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }

            @Override
            public void copyToClipboard(String text) {
                ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("email", text);
                manager.setPrimaryClip(clip);
                Toast.makeText(FindAccountActivity.this, "아이디가 클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        mBinding.setViewModel(mViewModel);
        mBinding.includeFindaccountHeader.setViewModel(mViewModel);
    }

    private void initFragmentPager() {
        FindAccountPagerAdapter adapter = new FindAccountPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FindIdFragment(mViewModel));
        mBinding.viewpagerFindaccount.setAdapter(adapter);
    }

}
