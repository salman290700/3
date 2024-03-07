package com.example.dietjoggingapp.database.domains

import android.content.Context
import org.tensorflow.contrib.android.TensorFlowInferenceInterface

class ActiivtyClassified(applicationContext: Context) {
    private val MODEL_FILE = "model.pb"
    private val INPUT_NODE = "lstm_1_input"
    private val OUTPUT_NODES = arrayOf("output/Softmax")
    private val OUTPUT_NODE = "output/Softmax"
    private val INPUT_SIZE = longArrayOf(1, 100, 9)
    private val OUTPUT_SIZE = 7

    private var inferenceInterface: TensorFlowInferenceInterface? = null

    fun ActivityClassifier(context: Context) {
        inferenceInterface = TensorFlowInferenceInterface(context.assets, MODEL_FILE)
    }

    fun predictProbabilities(data: FloatArray): FloatArray {
        val result = FloatArray(OUTPUT_SIZE)
        inferenceInterface!!.feed(INPUT_NODE, data, *INPUT_SIZE)
        inferenceInterface!!.run(OUTPUT_NODES)
        inferenceInterface!!.fetch(OUTPUT_NODE, result)
        return result
    }
}