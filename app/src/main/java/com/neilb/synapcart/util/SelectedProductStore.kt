package com.neilb.synapcart.util

import com.neilb.synapcart.data.model.ProductDTO

/**
 * Hands the tapped product from a list screen to the product-details screen without
 * serialising it into the navigation route. If the process was killed in between, [product]
 * is null and the details screen simply closes.
 */
class SelectedProductStore {
    @Volatile
    var product: ProductDTO? = null
}
