package com.example.demo

import com.github.horitaku1124.blog_manager.util.MorphologicalAnalysis
import com.github.horitaku1124.blog_manager.util.WordFrequent
import com.github.horitaku1124.ml_advisor.models.KNearestNeighbor
import org.junit.jupiter.api.Test

class RegressionTest2 {
  val data1 = """
Mozlila/5.0 (Linux; Android 7.0; SM-G892A Bulid/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/60.0.3112.107 Moblie Safari/537.36
Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.75 Safari/537.36
Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36
Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36
Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3864.0 Safari/537.36
Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36
Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100
Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36
  """.trimIndent()
  val data2 = """
Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:65.0) Gecko/20100101 Firefox/65.0
Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:62.0) Gecko/20100101 Firefox/62.0
Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:67.0) Gecko/20100101 Firefox/67.0
Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:68.0) Gecko/20100101 Firefox/68.0
Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0
Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0
Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:57.0) Gecko/20100101 Firefox/57.0
  """.trimIndent()

  @Test
  fun test1() {
    val mapByLabel = hashMapOf<Int, List<String>>()
    mapByLabel[1] = data1.split("\n")
    mapByLabel[2] = data2.split("\n")

    val preUniqueWords = arrayListOf<String>()
    val allDocsByLines = arrayListOf<List<String>>()
    val labelByLine = arrayListOf<Int>()

    mapByLabel.forEach { (idx, lines) ->
      for (line in lines) {
        val cols = MorphologicalAnalysis.parse(line)
        preUniqueWords.addAll(cols)
        allDocsByLines.add(cols)
        labelByLine.add(idx)
      }
    }

    val vectorsByLabel = HashMap<Int, ArrayList<DoubleArray>>()
    val uniqueWords = preUniqueWords.distinct()
    val wf = WordFrequent(allDocsByLines)
    for (i in 0 until allDocsByLines.size) {
      val vec = wf.toScoreVec2(i, uniqueWords)
      val label = labelByLine[i]
      if (vectorsByLabel.containsKey(label)) {
        vectorsByLabel[label]!!.add(vec)
      } else {
        vectorsByLabel[label] = arrayListOf(vec)
      }
    }
    val model = KNearestNeighbor<Int>()
    val trainedData = model.train(vectorsByLabel)

    for (i in allDocsByLines.indices) {
      val cols = allDocsByLines[i]
      val vecs = wf.testScore2(cols, uniqueWords)
      val label = model.inference(trainedData, vecs)
      println(labelByLine[i].toString() + " - " + label)
    }
  }
}