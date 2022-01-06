package com.github.horitaku1124.ml_advisor

import com.github.horitaku1124.ml_advisor.models.KNearestNeighbor
import com.github.horitaku1124.util.CSVReader
import org.junit.jupiter.api.Test

class RegressionTest {
  private fun variance(ary: List<Float>): Double {
    var sum = 0.0

    for (i in ary.indices) {
      sum += ary[i]
    }
    val mean = sum / ary.size

    var devsq = 0.0
    for (i in ary.indices) {
      val diff = ary[i] - mean
      devsq += diff * diff
    }
    return devsq / ary.size
  }

  data class DataAndLabel<L, R>(var data: L, var label: R)

  @Test
  fun testIrisClassify() {
    val allData = CSVReader("./src/test/resources/iris.csv", 1).use { csvReader ->
      csvReader.readClassAll { s ->
        s.split(",").let {
          DataAndLabel(
            doubleArrayOf(
              it[0].toDouble(),
              it[1].toDouble(),
              it[2].toDouble(),
              it[3].toDouble(),
            ),
            it[4]
          )
        }
      }
    }

    val vectorsByLabel = HashMap<String, ArrayList<DoubleArray>>()

    for (row in allData) {
      val label = row.label
      (if (vectorsByLabel.containsKey(label)) {
        vectorsByLabel[label]
      } else {
        val list = arrayListOf<DoubleArray>()
        vectorsByLabel[label] = list
        list
      })!!.add(row.data)
    }

    val model = KNearestNeighbor<String>()
    val trainedData = model.train(vectorsByLabel)
    println(trainedData)

    var correctRate = 0.0
    for(row in allData) {
      val inferred = model.inference(trainedData, row.data)
      println(inferred + " " + row.label)
      correctRate += (if (inferred == row.label) 1 else 0)
    }
    println("correctRate=" + (correctRate / allData.size * 100))
  }
}