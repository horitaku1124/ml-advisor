package com.github.horitaku1124.ml_advisor.controller

import com.github.horitaku1124.blog_manager.util.MorphologicalAnalysis
import com.github.horitaku1124.blog_manager.util.VectorUtil
import com.github.horitaku1124.blog_manager.util.WordFrequent
import com.github.horitaku1124.ml_advisor.dao.TrainDataDao
import com.github.horitaku1124.ml_advisor.dao.TrainLabelDao
import com.github.horitaku1124.ml_advisor.entities.SearchForm
import com.github.horitaku1124.ml_advisor.entities.TrainForm
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Controller
class ProjectController(var trainDataDao: TrainDataDao,
                        var trainLabelDao: TrainLabelDao) {
  companion object {
    var allWords = hashMapOf<Int, List<String>>()
    var allFrequent = hashMapOf<Int, WordFrequent>()
    var allCentroidByBrowser = hashMapOf<Int, HashMap<Int, List<Double>>>()
  }

  @GetMapping("/project/{projectId}")
  fun index(@PathVariable("projectId") projectId: Int,
            model: MutableMap<String, Any>) : String {
    return "index"
  }

  @PostMapping("/train")
  fun train(@Validated trainEntity: TrainForm,
            model: MutableMap<String, Any>) : String {
    val projectId = trainEntity.project!!
    val trainData = trainDataDao.findAllById(projectId)
    trainDo(projectId, trainData)
    model["result_text"] = "訓練完了"
    model["projectId"] = projectId

    return "index"
  }

  @PostMapping("/search")
  fun search(@Validated searchEntity: SearchForm,
             model: MutableMap<String, Any>) : String {
    val projectId = searchEntity.project!!
    val searchWord = searchEntity.query!!
    var modUas = MorphologicalAnalysis.parse(searchWord)
    var allLabel = trainLabelDao.findAll(projectId)

    println("searchWord=" + searchWord)
    val wf = allFrequent[projectId]!!
    val words = allWords[projectId]!!
    val vecByBrowser = allCentroidByBrowser[projectId]!!
    var vec = wf.testScore(modUas, words)
    println(vec)
    var resultBuf = StringBuffer()
    vecByBrowser.forEach { resultId, centroid ->
      var label = allLabel[resultId]!!
      var score = VectorUtil.cosSim(centroid, vec)
      resultBuf
        .append(label.first)
        .append(" - ")
        .append(label.second)
        .append(" - ")
        .append(score)
        .append("\n")
    }
    model["result_text"] = resultBuf.toString()
    model["projectId"] = projectId
    model["query"] = searchWord

    return "index"
  }

  fun trainDo(projectId: Int, trainData: List<Pair<Int, String>>) {
    var uniqueWords = arrayListOf<String>()
    var allDocsByLines = arrayListOf<List<String>>()
    var browserByLine = arrayListOf<Int>()
    trainData.forEach{row ->
      var modUas = MorphologicalAnalysis.parse(row.second)
      uniqueWords.addAll(modUas)

      allDocsByLines.add(modUas)
      browserByLine.add(row.first)
    }

    var vecsByLine = arrayListOf<List<Double>>()
    var vectorsByBrowser = HashMap<Int, ArrayList<List<Double>>>()
    var uniqueWords2 = uniqueWords.distinct()
    var wf = WordFrequent(allDocsByLines)
    for (i in 0 until allDocsByLines.size) {
      var vec = wf.toScoreVec(i, uniqueWords2)
      var browser = browserByLine[i]
      println(i.toString()+ " - " + browser +  " " + vec)
      if (vectorsByBrowser.containsKey(browser)) {
        vectorsByBrowser[browser]!!.add(vec)
      } else {
        vectorsByBrowser[browser] = arrayListOf(vec)
      }
      vecsByLine.add(vec)
    }

    var centroidByBrowser = HashMap<Int, List<Double>>()
    vectorsByBrowser.forEach {browser, vectors ->
      centroidByBrowser[browser] = VectorUtil.vecAverage(vectors)
    }
    println(centroidByBrowser)

    ProjectController.allWords[projectId] = uniqueWords2
    ProjectController.allFrequent[projectId] = wf
    ProjectController.allCentroidByBrowser[projectId] = centroidByBrowser
  }
}