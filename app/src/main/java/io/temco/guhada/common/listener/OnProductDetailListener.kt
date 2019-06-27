package io.temco.guhada.common.listener

import io.temco.guhada.data.model.Brand

interface OnProductDetailListener {
    fun scrollToElement(pos: Int)
    fun showMessage(message: String)
    fun showMenu()
    fun redirectPaymentActivity(isOptionPopupSelected: Boolean)
    fun hideLoadingIndicator()
    fun closeActivity()
    fun showSideMenu()
    fun setBrandProductList(brand: Brand)
}