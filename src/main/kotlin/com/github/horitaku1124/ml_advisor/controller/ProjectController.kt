package com.github.horitaku1124.ml_advisor.controller

import com.github.horitaku1124.blog_manager.util.MorphologicalAnalysis
import com.github.horitaku1124.blog_manager.util.VectorUtil
import com.github.horitaku1124.blog_manager.util.WordFrequent
import com.github.horitaku1124.ml_advisor.dao.ProjectDao
import com.github.horitaku1124.ml_advisor.dao.TrainDataDao
import com.github.horitaku1124.ml_advisor.dao.TrainLabelDao
import com.github.horitaku1124.ml_advisor.entities.ProjectForm
import com.github.horitaku1124.ml_advisor.entities.SearchForm
import com.github.horitaku1124.ml_advisor.entities.TrainForm
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Controller
class ProjectController(var trainDataDao: TrainDataDao,
                        var trainLabelDao: TrainLabelDao,
                        var projectDao: ProjectDao
) {
  var logger: Logger = LoggerFactory.getLogger(ProjectController::class.java)
  companion object {
    var allWords = hashMapOf<Int, List<String>>()
    var allFrequent = hashMapOf<Int, WordFrequent>()
    var allCentroidByBrowser = hashMapOf<Int, HashMap<Int, List<Double>>>()
  }

  @GetMapping("/project/new")
  fun new(model: MutableMap<String, Any>) : String {
    return "project_new"
  }

  @PostMapping("/project/created")
  fun created(@Validated project: ProjectForm,
            model: MutableMap<String, Any>) : String {
    var id = projectDao.create(project)
    model["projectId"] = id
    return "project_created"
  }

  @GetMapping("/project/{projectId}")
  fun index(@PathVariable("projectId") projectId: Int,
            model: MutableMap<String, Any>) : String {
    model["projectId"] = projectId
    return "project"
  }

  @PostMapping("/train")
  fun train(@Validated trainEntity: TrainForm,
            model: MutableMap<String, Any>) : String {
    val projectId = trainEntity.project!!
    val trainData = trainDataDao.findAllById(projectId)
    logger.info("train start projectId=$projectId")
    trainDo(projectId, trainData)
    logger.info("train finish projectId=$projectId")
    model["result_text"] = "訓練完了"
    model["projectId"] = projectId

    return "project"
  }

  @PostMapping("/search")
  fun search(@Validated searchEntity: SearchForm,
             model: MutableMap<String, Any>) : String {
    val projectId = searchEntity.project!!
    val searchWord = searchEntity.query!!
    var modUas = MorphologicalAnalysis.parse(searchWord)
    var allLabel = trainLabelDao.findAll(projectId)

    logger.info("searchWord=$searchWord")
    val wf = allFrequent[projectId]!!
    val words = allWords[projectId]!!
    val vecByBrowser = allCentroidByBrowser[projectId]!!
    var vec = wf.testScore(modUas, words)
    println(vec)

    var scoreAndId = arrayListOf<Pair<Double, Int>>()
    vecByBrowser.forEach { (resultId, centroid) ->
      val score = VectorUtil.cosSim(centroid, vec)
      scoreAndId.add(Pair(score, resultId))
    }

    scoreAndId.sortByDescending { it.first }
    var resultBuf = StringBuffer()
    scoreAndId.forEach {
      val (score, resultId) = it
      val label = allLabel[resultId]!!
      resultBuf
        .append(String.format("%3.2f", score))
        .append(" - ")
        .append(label.first)
        .append(" - ")
        .append(label.second)
        .append("\n")
    }

    model["result_text"] = resultBuf.toString()
    model["projectId"] = projectId
    model["query"] = searchWord

    logger.info("finish")

    return "project"
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