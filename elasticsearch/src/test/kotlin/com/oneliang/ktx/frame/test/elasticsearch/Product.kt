package com.oneliang.ktx.frame.test.elasticsearch

class Product {
    var sku: String? = null
    var name: String? = null
    var price = 0.0

    constructor() {}
    constructor(sku: String?, name: String?, price: Double) {
        this.sku = sku
        this.name = name
        this.price = price
    }
}