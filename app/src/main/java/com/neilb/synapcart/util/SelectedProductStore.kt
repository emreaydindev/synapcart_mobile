package com.neilb.synapcart.util

import com.neilb.synapcart.data.model.ProductDTO

/**
 * Hands products to the detail and compare screens without serializing them into routes.
 * In-memory only: after process death the screens find it empty and close themselves.
 */
class SelectedProductStore {
    var product: ProductDTO? = null

    /** The results shown together with [product]; "Compare" on the detail screen uses them. */
    var siblings: List<ProductDTO> = emptyList()

    /** 2–4 products picked for the compare screen. */
    var compareProducts: List<ProductDTO> = emptyList()
}
