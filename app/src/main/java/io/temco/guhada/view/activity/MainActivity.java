package io.temco.guhada.view.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.auth0.android.jwt.JWT;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import io.temco.guhada.R;
import io.temco.guhada.common.Flag;
import io.temco.guhada.common.Info;
import io.temco.guhada.common.Preferences;
import io.temco.guhada.common.ProductBridge;
import io.temco.guhada.common.Type;
import io.temco.guhada.common.listener.OnBrandListener;
import io.temco.guhada.common.listener.OnMainListener;
import io.temco.guhada.common.util.CommonUtil;
import io.temco.guhada.common.util.LoadingIndicatorUtil;
import io.temco.guhada.common.util.ToastUtil;
import io.temco.guhada.data.model.Brand;
import io.temco.guhada.data.model.ProductByList;
import io.temco.guhada.data.model.Token;
import io.temco.guhada.data.server.ProductServer;
import io.temco.guhada.databinding.ActivityMainBinding;
import io.temco.guhada.view.activity.base.BindActivity;
import io.temco.guhada.view.adapter.MainPagerAdapter;
import io.temco.guhada.view.adapter.category.SideMenuCategoryFirstListAdapter;
import io.temco.guhada.view.custom.dialog.BrandListDialog;
import io.temco.guhada.view.custom.dialog.CategoryListDialog;
import io.temco.guhada.view.fragment.productdetail.ProductDetailFragment;

public class MainActivity extends BindActivity<ActivityMainBinding> implements View.OnClickListener, OnMainListener {

    // -------- LOCAL VALUE --------
    private final int REQUEST_CODE_LOGIN = 101;
    private final int REQUEST_CODE_CATEGORY = 201;
    private final int REQUEST_CODE_BRAND = 202;
    //

    private MainPagerAdapter mPagerAdapter;
    private SideMenuCategoryFirstListAdapter mSideMenuCategoryAdapter;
    private CategoryListDialog mCategoryListDialog;
    private BrandListDialog mBrandListDialog;
    private ProductDetailFragment mProductDetailFragment;
    private LoadingIndicatorUtil mLoadingIndicatorUtil;
    // NFC
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private final String TAG_COMPANY = "TAG_COMPANY";
    private final String TAG_ID = "TAG_ID";
    private final String COMPANY_NAME = "GUHADA";
    // -----------------------------

    ////////////////////////////////////////////////
    // OVERRIDE
    ////////////////////////////////////////////////

    @Override
    protected String getBaseTag() {
        return MainActivity.class.getSimpleName();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected Type.View getViewType() {
        return Type.View.MAIN;
    }

    @Override
    protected void init() {
        // [2019.06.26] 임시 브릿지
        ProductBridge.Companion.setMainActivity(this);
        mLoadingIndicatorUtil = new LoadingIndicatorUtil(this);

        // Init
        initNfc();
        setFullWideDrawerLayout();
        initMainPager();
        initSideMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableNfc();
        // Check
        checkLogin();
    }

    @Override
    protected void onPause() {
        disableNfc();
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) readData(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            ////////////////////////////////////////////////
            // Side Menu
            case R.id.image_home:
                CommonUtil.debug(getBaseTag(), "image_home");
                break;

            case R.id.image_setting:
                CommonUtil.debug(getBaseTag(), "image_setting");
                break;

            case R.id.image_close:
                changeDrawerLayout(false, false);
                break;

            case R.id.layout_brand:
                BrandSubActivity.startActivityForResult(this, REQUEST_CODE_BRAND);
                break;

            // Sub Menu
            case R.id.layout_sale:
                CommonUtil.debug(getBaseTag(), "layout_sale");
                break;

            case R.id.layout_new_product:
                CommonUtil.debug(getBaseTag(), "layout_new_product");
                break;

            case R.id.layout_power_deal:
                CommonUtil.debug(getBaseTag(), "layout_power_deal");
                break;

            case R.id.layout_time_deal:
                CommonUtil.debug(getBaseTag(), "layout_time_deal");
                break;

            case R.id.layout_limit_price:
                CommonUtil.debug(getBaseTag(), "layout_limit_price");
                break;

            case R.id.layout_community:
                CommonUtil.debug(getBaseTag(), "layout_community");
                break;

            ////////////////////////////////////////////////
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_LOGIN:
                    changeLoginStatus(checkToken());

                    // [상품 상세] 문의 리프레시
                    if (mProductDetailFragment != null) {
                        mProductDetailFragment.refreshIsMyClaimsVisible();
                        mProductDetailFragment.refreshClaims();
                    }

                    break;

                case REQUEST_CODE_CATEGORY:
                    changeDrawerLayout(false, false);
                    if (data != null) {
                        mPagerAdapter.removeAll();
                        Type.Category type = (Type.Category) data.getSerializableExtra(Info.INTENT_CATEGORY_TYPE);
                        int[] hierarchies = data.getIntArrayExtra(Info.INTENT_CATEGORY_HIERARCHIES);
                        startCategoryScreen(type, hierarchies);
                    }
                    break;

                case REQUEST_CODE_BRAND:
                    changeDrawerLayout(false, false);
                    if (data != null) {
                        mPagerAdapter.removeAll();
                        Brand b = (Brand) data.getSerializableExtra(Info.INTENT_BRAND_DATA);
                        mPagerAdapter.setProductBrandData(b);

                        // [2019.06.26] 임시
                        ProductBridge.Companion.removeProductDetailFragment();
                    }
                    break;

                case Flag.RequestCode.WRITE_CLAIM:
                    mProductDetailFragment.refreshClaims();
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);

            switch (requestCode) {
                case Flag.RequestCode.WRITE_CLAIM:
                    ToastUtil.showMessage(getResources().getString(R.string.common_message_error));
                    break;
            }
        }
    }

    ////////////////////////////////////////////////
    // PUBLIC
    ////////////////////////////////////////////////

    ////////////////////////////////////////////////
    // PRIVATE
    ////////////////////////////////////////////////

    private void checkLogin() {
        if (checkToken()) {
            changeLoginStatus(true);
        } else {
            // startLoginActivity();
            changeLoginStatus(false);
        }
    }

    /**
     * [사이드 메뉴] 상단 로그인/로그아웃
     *
     * @param isLogin
     */
    private void changeLoginStatus(boolean isLogin) {
        if (mBinding != null) {
            if (isLogin) {
                mBinding.layoutSideMenu.layoutHeader.textLogin.setText(getString(R.string.side_menu_login_out));
                mBinding.layoutSideMenu.layoutHeader.layoutLogin.setOnClickListener(v -> {
                    //   startLoginActivity();
                    Preferences.clearToken();
                    changeLoginStatus(false);

                    // [상품 상세] 문의 리스트 리프레시
                    if (mProductDetailFragment != null) {
                        mProductDetailFragment.refreshIsMyClaimsVisible();
                        mProductDetailFragment.refreshClaims();
                    }
                });
            } else {
                mBinding.layoutSideMenu.layoutHeader.textLogin.setText(getString(R.string.side_menu_login_need));
                mBinding.layoutSideMenu.layoutHeader.layoutLogin.setOnClickListener(v -> {
                    startLoginActivity();
                });
            }
        }
    }

    private boolean checkToken() {
        Token token = Preferences.getToken();
        if (token == null) return false;
        int current = (int) (System.currentTimeMillis() / 1000L);
        int exp = new JWT(token.getAccessToken()).getClaim("exp").asInt();
        if (exp > current) {
            return true;
        } else {
            Preferences.clearToken();
            return false;
        }
    }

    private void startLoginActivity() {
        startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_CODE_LOGIN);
    }

    // Drawer
    private void setFullWideDrawerLayout() {
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mBinding.layoutSideMenu.getRoot().getLayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels;
        mBinding.layoutSideMenu.getRoot().setLayoutParams(params);
    }

    private void changeDrawerLayout(boolean isOpen) {
        changeDrawerLayout(isOpen, true);
    }

    private void changeDrawerLayout(boolean isOpen, boolean animate) {
        if (isOpen &&
                !mBinding.layoutDrawer.isDrawerOpen(mBinding.layoutSideMenu.getRoot())) {
            mBinding.layoutDrawer.openDrawer(mBinding.layoutSideMenu.getRoot(), animate);
        } else {
            mBinding.layoutDrawer.closeDrawer(mBinding.layoutSideMenu.getRoot(), animate);
        }
    }

    // Main Pager
    private void initMainPager() {
        // Adapter
        if (mPagerAdapter == null)
            mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.setOnDrawerLayoutListener(isOpen -> changeDrawerLayout(isOpen));
        // Pager
        mBinding.layoutContents.layoutPager.setAdapter(mPagerAdapter);
        mBinding.layoutContents.layoutPager.setSwipeLocked(true);
        mBinding.layoutContents.layoutPager.setOffscreenPageLimit(mPagerAdapter.getCount());
        // Tab
        addCustomTabs(3); // Home
        mBinding.layoutContents.layoutTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectTab(tab.getPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                selectTab(tab.getPosition(), true);
            }
        });
    }

    private void addCustomTabs(int current) {
        // Add Custom Tab
        for (int i = 1; i <= 5; i++) {
            View v = getLayoutInflater().inflate(R.layout.layout_tab_main, null);
            switch (i) {
                case 1: // Category
                    v.findViewById(R.id.icon).setBackgroundResource(R.drawable.selector_tab_main_category);
                    ((TextView) v.findViewById(R.id.title)).setText(R.string.common_category);
                    break;

                case 2: // Brand
                    v.findViewById(R.id.icon).setBackgroundResource(R.drawable.selector_tab_main_brand);
                    ((TextView) v.findViewById(R.id.title)).setText(R.string.common_brand);
                    break;

                case 3: // Home
                    v.findViewById(R.id.icon).setBackgroundResource(R.drawable.selector_tab_main_home);
                    ((TextView) v.findViewById(R.id.title)).setText(R.string.common_home);
                    break;

                case 4: // Community
                    v.findViewById(R.id.icon).setBackgroundResource(R.drawable.selector_tab_main_community);
                    ((TextView) v.findViewById(R.id.title)).setText(R.string.common_community);
                    break;

                case 5: // My Page
                    v.findViewById(R.id.icon).setBackgroundResource(R.drawable.selector_tab_main_my_page);
                    ((TextView) v.findViewById(R.id.title)).setText(R.string.common_my_page);
                    break;
            }
            // Tab
            TabLayout.Tab tab = mBinding.layoutContents.layoutTab.newTab().setCustomView(v);
            mBinding.layoutContents.layoutTab.addTab(tab);
            if (i == current) {
                tab.select();
            }
        }
    }

    private void selectTab(int position, boolean isReselected) {
        switch (position) {
            case 0: // Category
                showCategoryListDialog();
                break;

            case 1: // Brand
                showBrandListDialog();
                break;

            case 2: // Home
                mPagerAdapter.removeProduct();
                if (!isReselected) mBinding.layoutContents.layoutPager.setCurrentItem(0);
                break;

            case 3: // Community
                mPagerAdapter.removeProduct();
                if (!isReselected) mBinding.layoutContents.layoutPager.setCurrentItem(1);
                break;

            case 4: // My Page
                mPagerAdapter.removeProduct();
                if (!isReselected) {
                    if (false) {
                        // if login
                    } else {
                        mBinding.layoutContents.layoutPager.setCurrentItem(2);
                    }
                }
                break;
        }
    }

    // Side Menu
    private void initSideMenu() {
        // Listener
        mBinding.layoutSideMenu.setClickListener(this);
        mBinding.layoutSideMenu.layoutHeader.setClickListener(this);
        mBinding.layoutSideMenu.layoutSubMenu.setClickListener(this);
        mBinding.layoutDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (mSideMenuCategoryAdapter != null) mSideMenuCategoryAdapter.collapseAll();
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        // Category
        mBinding.layoutSideMenu.listContents.setLayoutManager(new LinearLayoutManager(this));
        mSideMenuCategoryAdapter = new SideMenuCategoryFirstListAdapter(this);
        mSideMenuCategoryAdapter.setOnCategoryListener(category -> startCategoryByHierarchy(category.type, category.hierarchies));
        mSideMenuCategoryAdapter.setItems(Preferences.getCategories());
        mBinding.layoutSideMenu.listContents.setAdapter(mSideMenuCategoryAdapter);
    }

    // Category
    private void startCategoryByHierarchy(Type.Category type, int[] hierarchies) {
        switch (type) {
            case ALL:
                mPagerAdapter.removeAll();
                changeDrawerLayout(false, false);
                startCategoryScreen(type, hierarchies);
                break;

            default:
                if (hierarchies != null && hierarchies.length >= 2) {
                    CategorySubActivity.startActivityForResult(this, hierarchies[1], REQUEST_CODE_CATEGORY);
                }
        }
    }

    private void startCategoryScreen(Type.Category type, int[] hierarchies) {
        mPagerAdapter.addProductCategoryData(type, hierarchies);

        // [2019.06.26] 임시 브릿지
        removeProductDetailFragment();
    }

    // Dialog
    private void showCategoryListDialog() {
        if (getSupportFragmentManager() != null) {
            if (mCategoryListDialog == null) {
                mCategoryListDialog = new CategoryListDialog();
                mCategoryListDialog.setOnCategoryListener(category -> startCategoryScreen(category.type, category.hierarchies));
            }
            mCategoryListDialog.show(getSupportFragmentManager(), getBaseTag());
        }
    }

    private void dismissCategoryListDialog() {
        if (mCategoryListDialog != null) {
            mCategoryListDialog.dismiss();
        }
    }

    private void showBrandListDialog() {
        if (getSupportFragmentManager() != null) {
            if (mBrandListDialog == null) {
                mBrandListDialog = new BrandListDialog();
                mBrandListDialog.setOnBrandListener(mBrandListener);
            }
            mBrandListDialog.show(getSupportFragmentManager(), getBaseTag());
        }
    }

    private void dismissBrandListDialog() {
        if (mBrandListDialog != null) {
            mBrandListDialog.dismiss();
        }
    }

    ////////////////////////////////////////////////
    // NFC
    private void initNfc() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            // 미지원 기기
            showToast(R.string.common_message_nfc_unsupported);
        } else {
            if (mNfcAdapter.isEnabled()) {
                Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            } else {
                showToast(R.string.common_message_nfc_off);
            }
        }
    }

    private void enableNfc() {
        if (mNfcAdapter != null && mPendingIntent != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    private void disableNfc() {
        if (mNfcAdapter != null && mPendingIntent != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void readData(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (messages == null) return;
            for (Parcelable message : messages) showBlockChainProduct((NdefMessage) message);
        }
    }

    private void showBlockChainProduct(NdefMessage msg) {
        try {
            for (NdefRecord r : msg.getRecords()) {
                JSONObject p = new JSONObject(new String(r.getPayload()));  // (*) 중요!
                if (p.getString(TAG_COMPANY).equals(COMPANY_NAME)) {
                    String id = p.getString(TAG_ID); // get productId
                    getProductData(id);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showToast(@StringRes int id) {
        Toast.makeText(this, getString(id), Toast.LENGTH_SHORT).show();
    }

    ////////////////////////////////////////////////
    // LISTENER
    ////////////////////////////////////////////////

    private OnBrandListener mBrandListener = new OnBrandListener() {
        @Override
        public void onEvent(Brand brand) {
            if (brand != null) mPagerAdapter.setProductBrandData(brand);
        }
    };

    ////////////////////////////////////////////////
    // SERVER
    ////////////////////////////////////////////////

    private void getProductData(String id) {
        ProductServer.getProductByList(id, (success, o) -> {
            if (success) {
                BlockChainHistoryActivity.startActivity(this, (ProductByList) o);
            }
        });
    }

    ////////////////////////////////////////////////


    // [2019.06.26] 임시 브릿지
    public void addProductDetailView(Long dealId) {
        mBinding.viewMainProductdetail.bringToFront();
        mBinding.viewMainProductdetail.setVisibility(View.VISIBLE);

        if (mProductDetailFragment != null) {
            mProductDetailFragment = new ProductDetailFragment(dealId, this);
            getSupportFragmentManager().beginTransaction().replace(mBinding.viewMainProductdetail.getId(), mProductDetailFragment).addToBackStack(null).commitAllowingStateLoss();
        } else {
            mProductDetailFragment = new ProductDetailFragment(dealId, this);
            getSupportFragmentManager().beginTransaction().add(mBinding.viewMainProductdetail.getId(), mProductDetailFragment).addToBackStack(null).commitAllowingStateLoss();
        }
    }

    // [2019.06.26] 임시 브릿지
    @Override
    public void removeProductDetailFragment() {
        mBinding.layoutSideMenu.linearlayoutSidemenuContainer.bringToFront();
        mBinding.viewMainProductdetail.setVisibility(View.GONE);
        if (mProductDetailFragment != null && mProductDetailFragment.isAdded())
            getSupportFragmentManager().beginTransaction().remove(mProductDetailFragment).commitAllowingStateLoss();

        mLoadingIndicatorUtil.hide();
    }

    // [2019.06.26] 임시 브릿지
    @Override
    public void showSideMenu(boolean isOpen) {
        if (isOpen) mBinding.layoutSideMenu.linearlayoutSidemenuContainer.bringToFront();
        changeDrawerLayout(isOpen);
    }

    // [2019.06.26] 임시 브릿지
    @Override
    public void setBrandProductList(Brand brand) {
        mPagerAdapter.setProductBrandData(brand);
    }

    @Override
    public void removeProductFragment() {
        mLoadingIndicatorUtil.show();
        mPagerAdapter.removeProduct();
        removeProductDetailFragment();
    }

    @Override
    public void onBackPressed() {
        if (mProductDetailFragment != null && mProductDetailFragment.isVisible()) {
            removeProductDetailFragment();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (mLoadingIndicatorUtil != null) mLoadingIndicatorUtil.dismiss();
        super.onDestroy();
    }
}