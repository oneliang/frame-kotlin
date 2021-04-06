package com.oneliang.ktx.frame.ai.dnn

import com.oneliang.ktx.Constants

class Model(var times: Int, var layerModels: Array<LayerModel>) {
    constructor() : this(0, emptyArray())

    class LayerModel {
        var index = 0
        var data = Constants.String.BLANK
    }
}