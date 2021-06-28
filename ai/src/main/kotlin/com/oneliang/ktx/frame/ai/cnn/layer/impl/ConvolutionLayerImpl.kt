package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.activation.sigmoid
import com.oneliang.ktx.frame.ai.cnn.layer.ConvolutionLayer
import com.oneliang.ktx.util.common.doubleIteration
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.common.toNewArray
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.math.matrix.add
import com.oneliang.ktx.util.math.matrix.innerProduct
import com.oneliang.ktx.util.math.matrix.operate
import com.oneliang.ktx.util.math.matrix.rotate180

class ConvolutionLayerImpl(
    previousLayerMapDepth: Int,//1
    mapDepth: Int,//32
    inX: Int,
    inY: Int,
    x: Int,
    y: Int,
    padding: Int = 0,
    stride: Int = 1
) : ConvolutionLayer<Array<Array<Array<Float>>>, Array<Array<Array<Float>>>, Array<Array<Array<Float>>>>(previousLayerMapDepth, mapDepth, inX, inY, x, y, padding, stride) {

    companion object {
        private const val DERIVED_FILTERS_KEY = "derivedFilters"
        private const val SUM_BIAS_KEY = "sumBias"
    }

    private var derivedFilters = AtomicMap<String, Array<Array<Array<Array<Float>>?>>>()
    private var sumBias = AtomicMap<String, Array<Float>>()

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Float>>>, y: Float, training: Boolean): Array<Array<Array<Float>>> {
        val outputNeuron = Array(this.mapDepth) { Array(this.outY) { Array(this.outX) { 0.0f } } }
        singleIteration(this.mapDepth) { mapIndex ->
            var mapValuesSum: Array<Array<Float>>? = null
            //先计算上一层每个图层与卷积的内积(convolutional()含了内积),然后将上一层各个图层卷积后的内积值求和
            singleIteration(this.previousLayerMapDepth) { parentMapIndex ->
                val maps = inputNeuron[parentMapIndex]
                val filters = this.filters[parentMapIndex][mapIndex]
                val rows = this.outY //28-5+1=24
                val columns: Int = this.outX //28-5+1=24
                val values = convolutional(rows, columns, maps, filters)
                mapValuesSum = mapValuesSum?.add(values) ?: values
            }
            //求和后的每个值进行一次sigmoid
            outputNeuron[mapIndex] = sigmoidBias(mapValuesSum!!, this.biases[mapIndex])
        }
//        println("output data size:[%s][%s][%s]".format(outputNeuron.size, outputNeuron[0].size, outputNeuron[0][0].size))
        return outputNeuron
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Float>>>, y: Float) {
        val nextLayerLoss = getNextLayerInputNeuronLoss(dataId)

        //input loss=previous output loss
        val sumAllPreviousMapValues: Array<Array<Array<Float>>?> = arrayOfNulls(this.previousLayerMapDepth)
        singleIteration(this.previousLayerMapDepth) { previousMapIndex ->
            var sumMapValues: Array<Array<Float>>? = null
            singleIteration(this.mapDepth) { mapIndex ->
                val values = fullConvolution(nextLayerLoss[mapIndex], rotate180(this.filters[previousMapIndex][mapIndex]))
//                println("i:%s, j%s, aaaaa:%s".format(previousMapIndex, mapIndex, values.toJson().MD5String()))
                sumMapValues = sumMapValues?.add(values) ?: values
            }
            sumAllPreviousMapValues[previousMapIndex] = sumMapValues
        }

        this.inputNeuronLoss[dataId] = sumAllPreviousMapValues.toNewArray { it!! }

        //derived filters
        this.derivedFilters.operate(DERIVED_FILTERS_KEY, create = {
            val filterLoss: Array<Array<Array<Array<Float>>?>> = Array(this.previousLayerMapDepth) { arrayOfNulls(this.mapDepth) }
            calculateFiltersLoss(inputNeuron, nextLayerLoss, filterLoss)
            filterLoss
        }, update = {
            val filterLoss: Array<Array<Array<Array<Float>>?>> = Array(this.previousLayerMapDepth) { arrayOfNulls(this.mapDepth) }
            calculateFiltersLoss(inputNeuron, nextLayerLoss, filterLoss, it)
            filterLoss
        })

        //sum bias
        this.sumBias.operate(SUM_BIAS_KEY, create = {
            val mapSumBias = Array(this.mapDepth) { 0.0f }
            singleIteration(this.mapDepth) { mapIndex ->
                mapSumBias[mapIndex] = sumLoss(nextLayerLoss[mapIndex])
            }
            mapSumBias
        }, update = {
            val mapSumBias = Array(this.mapDepth) { 0.0f }
            singleIteration(this.mapDepth) { mapIndex ->
                mapSumBias[mapIndex] = it[mapIndex] + sumLoss(nextLayerLoss[mapIndex])
            }
            mapSumBias
        })
    }

    override fun forwardResetImpl(dataId: Long) {
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
        updateFilter(totalDataSize, learningRate)
        updateBias(totalDataSize, learningRate)
    }

    override fun initializeLayerModelDataImpl(data: String) {
    }

    override fun saveLayerModelDataImpl(): String {
        return Constants.String.BLANK
    }

    private fun convolutional(rows: Int, columns: Int, maps: Array<Array<Float>>, filters: Array<Array<Float>>, originalResult: Array<Array<Float>>? = null): Array<Array<Float>> { //map是原图,filters是卷积核,过滤器
        val result = Array(rows) { Array(columns) { 0.0f } }
        doubleIteration(rows, columns) { row, column ->
            result[row][column] = maps.innerProduct(filters, row, column) + if (originalResult != null) {
                originalResult[row][column]
            } else {
                0.0f
            }
        }
        return result
    }

    private fun sigmoidBias(mapValues: Array<Array<Float>>, bias: Float): Array<Array<Float>> {
        doubleIteration(mapValues.size, mapValues[0].size) { row, column ->
            val value = mapValues[row][column] + bias
            mapValues[row][column] = sigmoid(value)//1 / (1 + exp(-bias - m[row][column]))
        }
        return mapValues
    }

    private fun fullConvolution(maps: Array<Array<Float>>, filters: Array<Array<Float>>): Array<Array<Float>> {
        val rows = maps.size + 2 * filters.size - 2//放大一倍,正常是filters.size-1
        val columns = maps[0].size + 2 * filters[0].size - 2
        val results = Array(rows) { Array(columns) { 0.0f } }
        doubleIteration(maps.size, maps[0].size) { row, column ->
            results[row + filters.size - 1][column + filters[0].size - 1] = maps[row][column]
        }
//        println("aaaaa:%s, %s".format(results.toJson().MD5String(), maps.toJson()))
//        println(results.toJson())
        val iterateRows = rows - filters.size + 1
        val iterateColumns = columns - filters.size + 1
        return convolutional(iterateRows, iterateColumns, results, filters)
    }

    private fun rotate180(values: Array<Array<Float>>): Array<Array<Float>> {
        return values.rotate180()
    }

    private fun calculateFiltersLoss(inputNeuron: Array<Array<Array<Float>>>, nextLayerLoss: Array<Array<Array<Float>>>, filtersLoss: Array<Array<Array<Array<Float>>?>>, originalFiltersLoss: Array<Array<Array<Array<Float>>?>>? = null) {
        singleIteration(this.mapDepth) { mapIndex ->
            singleIteration(this.previousLayerMapDepth) { previousMapIndex ->
                val maps = inputNeuron[previousMapIndex]
                val loss = nextLayerLoss[mapIndex]
                val rows = maps.size - loss.size + 1
                val columns = maps[0].size - loss[0].size + 1
                val lossValues = convolutional(rows, columns, maps, loss)//loss卷积后变成误差值,尺寸跟filter一样
                //然后将filter里面的数据进行误差计算
                filtersLoss[previousMapIndex][mapIndex] = lossValues
            }
        }
    }

    //统计收集所有的loss求和再除以样本总数再进行卷积核更新,maps-filters+1=loss->maps-loss+1=filters
    private fun updateFilter(totalDataSize: Long, learningRate: Float) {
        val derivedWeights = this.derivedFilters[DERIVED_FILTERS_KEY] ?: emptyArray()
        singleIteration(this.mapDepth) { mapIndex ->
            singleIteration(this.previousLayerMapDepth) { previousMapIndex ->
                val filtersLossValues = derivedWeights[this.mapDepth][this.previousLayerMapDepth]!!
                //然后将filter里面的数据进行误差计算
                this.filters[previousMapIndex][mapIndex] = mul3(this.filters[previousMapIndex][mapIndex], div(filtersLossValues, totalDataSize), learningRate)
            }
        }
        this.derivedFilters.clear()//clear it after update
    }

    private fun updateBias(totalDataSize: Long, learningRate: Float) {
        val sumBias = this.sumBias[SUM_BIAS_KEY] ?: emptyArray()
        singleIteration(this.mapDepth) { mapIndex ->
            this.biases[mapIndex] += learningRate * sumBias[mapIndex] / totalDataSize//batchSize
        }
        this.sumBias.clear()//clear it after update
    }

    //用filters的每个值+loss*学习率再重新赋值给loss,其实是为了避免覆盖filter,拿新loss(其实已经不是原来的loss了)重新在方法调用层赋值回给filter
    private fun mul3(
        values: Array<Array<Float>>,
        lossValues: Array<Array<Float>>,
        learningRate: Float
    ): Array<Array<Float>> {
        lossValues.operate { row, column, value ->
            values[row][column] + value * learningRate
        }
        return lossValues
    }

    private fun div(results: Array<Array<Float>>, totalDataSize: Long): Array<Array<Float>> {
        return results.operate { _, _, value ->
            value / totalDataSize
        }
    }

    private fun sumLoss(eachLoss: Array<Array<Float>>): Float {
        var sum = 0.0f
        doubleIteration(eachLoss.size, eachLoss[0].size) { row, column ->
            sum += eachLoss[row][column]
        }
        return sum
    }
}