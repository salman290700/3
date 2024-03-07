package com.example.dietjoggingapp.database.domains

import android.content.Context
import org.tensorflow.contrib.android.TensorFlowInferenceInterface

import kotlin.collections.ArrayList

class ActivityClassified (context: Context) {
    private val MODEL_FILE = "model.pb"
    private val INPUT_NODE = "lstm_1_input"
    private val OUTPUT_NODE = "output/Softmax"
    private val OUTPUT_NODES = arrayOf("output/softmax")
    private val INPUT_SIZE = longArrayOf(1, 100, 9)
    private val OUTPUT_SIZE = 7

    private var inferenceInterface: TensorFlowInferenceInterface? = null

    fun activityClassifier(context: Context) {
        inferenceInterface = TensorFlowInferenceInterface(context.assets, MODEL_FILE)
    }

    fun predictProbability(data: FloatArray): FloatArray {
        val result = FloatArray(OUTPUT_SIZE)
        var floatArray: FloatArray = FloatArray(data!!.size)

        inferenceInterface!!.feed(INPUT_NODE, floatArray, *INPUT_SIZE)
        inferenceInterface!!.run(OUTPUT_NODES)
        inferenceInterface!!.fetch(OUTPUT_NODE, result)
        return result
    }
}