package com.github.horitaku1124.ml_advisor.controller

import com.github.horitaku1124.blog_manager.util.MorphologicalAnalysis
import com.github.horitaku1124.blog_manager.util.VectorUtil
import com.github.horitaku1124.blog_manager.util.WordFrequent
import com.github.horitaku1124.ml_advisor.dao.ProjectDao
import com.github.horitaku1124.ml_advisor.dao.TrainDataDao
import com.github.horitaku1124.ml_advisor.dao.TrainLabelDao
import com.github.horitaku1124.ml_advisor.entities.ProjectEntity
import com.github.horitaku1124.ml_advisor.entities.SearchForm
import com.github.horitaku1124.ml_advisor.entities.TrainForm
import com.github.horitaku1124.ml_advisor.service.JanomeCommunicator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping

@Controller
class ProjectActionController(var projectDao: ProjectDao,
                              var trainDataDao: TrainDataDao,
                              var trainLabelDao: TrainLabelDao,
                              var janomeCommunicator: JanomeCommunicator
) {

  var logger: Logger = LoggerFactory.getLogger(ProjectActionController::class.java)
  companion object {
    var allWords = hashMapOf<Int, List<String>>()
    var allFrequent = hashMapOf<Int, WordFrequent>()
    var allCentroidByBrowser = hashMapOf<Int, HashMap<Int, List<Double>>>()
  }

  @PostMapping("/train")
  fun train(@Validated trainEntity: TrainForm,
            model: MutableMap<String, Any>) : String {
    val projectId = trainEntity.project!!
    val project = projectDao.findById(projectId).get()
    val trainData = trainDataDao.findAllById(projectId)
    logger.info("train start projectId=$projectId")
    trainDo(project, trainData)
    logger.info("train finish projectId=$projectId")
    model["result_text"] = "訓練完了"
    model["projectId"] = projectId

    return "project"
  }

  @PostMapping("/search")
  fun search(@Validated searchEntity: SearchForm,
             model: MutableMap<String, Any>) : String {
    val projectId = searchEntity.project!!
    val project = projectDao.findById(projectId).get()
    val searchWord = searchEntity.query!!
    val allLabel = trainLabelDao.findAll(projectId)

//    logger.info("searchWord=$searchWord")
    val wf = allFrequent[projectId]!!
    val words = allWords[projectId]!!
    val vecByBrowser = allCentroidByBrowser[projectId]!!

    val modUas = when (project.type) {
      1 -> MorphologicalAnalysis.parse(searchWord)
      2 -> janomeCommunicator.parseRequest(searchWord)
      else -> throw RuntimeException("")
    }
//    println(modUas)
    val vec = wf.testScore(modUas, words)
//    println(vec)

    val scoreAndId = arrayListOf<Pair<Double, Int>>()
    vecByBrowser.forEach { (resultId, centroid) ->
      val score = VectorUtil.cosSim(centroid, vec)
      scoreAndId.add(Pair(score, resultId))
    }

    scoreAndId.sortByDescending { it.first }
    val resultBuf = StringBuffer()
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

  fun trainDo(project: ProjectEntity, trainData: List<Pair<Int, String>>) {
    val projectId = project.id
    val preUniqueWords = arrayListOf<String>()
    val allDocsByLines = arrayListOf<List<String>>()
    val browserByLine = arrayListOf<Int>()
    trainData.forEach{row ->
      val modUas = when (project.type) {
        1 -> MorphologicalAnalysis.parse(row.second)
        2 -> janomeCommunicator.parseRequest(row.second)
        else -> throw RuntimeException("")
      }

      preUniqueWords.addAll(modUas)

      allDocsByLines.add(modUas)
      browserByLine.add(row.first)
    }

    val vecsByLine = arrayListOf<List<Double>>()
    val vectorsByBrowser = HashMap<Int, ArrayList<List<Double>>>()
    val uniqueWords = preUniqueWords.distinct()
    val wf = WordFrequent(allDocsByLines)
    for (i in 0 until allDocsByLines.size) {
      val vec = wf.toScoreVec(i, uniqueWords)
      val browser = browserByLine[i]
//      println("$i - $browser $vec")
      if (vectorsByBrowser.containsKey(browser)) {
        vectorsByBrowser[browser]!!.add(vec)
      } else {
        vectorsByBrowser[browser] = arrayListOf(vec)
      }
      vecsByLine.add(vec)
    }

    val centroidByBrowser = HashMap<Int, List<Double>>()
    vectorsByBrowser.forEach { (browser, vectors) ->
      centroidByBrowser[browser] = VectorUtil.vecAverage(vectors)
    }
//    println(centroidByBrowser)

    // TODO make these persistent other than static vars
    ProjectActionController.allWords[projectId] = uniqueWords
    ProjectActionController.allFrequent[projectId] = wf
    ProjectActionController.allCentroidByBrowser[projectId] = centroidByBrowser
  }
}