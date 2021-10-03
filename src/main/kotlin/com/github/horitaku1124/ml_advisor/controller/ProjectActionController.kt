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
import com.github.horitaku1124.ml_advisor.models.KNearestNeighbor
import com.github.horitaku1124.ml_advisor.service.JanomeCommunicator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

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
    var allCentroidByLabel = hashMapOf<Int, KNearestNeighbor.KnnResult<Int>>()
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
    val searchWord = searchEntity.query!!
    val allLabel = trainLabelDao.findAll(projectId)

    val resultBuf = StringBuffer()

    val scoreAndId = searchDo(projectId, searchWord)
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

  @PostMapping("/search.json",
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun searchJson(@RequestBody @Validated searchEntity: SearchForm) : ResponseEntity<List<Map<String, Any>>> {
    val projectId = searchEntity.project!!
    val searchWord = searchEntity.query!!
    val allLabel = trainLabelDao.findAll(projectId)

    val scoreAndId = searchDo(projectId, searchWord)

    val result = scoreAndId.map {
      val (score, resultId) = it

      hashMapOf(
        Pair("id", resultId),
        Pair("score", score),
        Pair("label", allLabel[resultId]!!.first),
      )
    }

    logger.info("finish")

    return ResponseEntity.ok(result)
  }

  fun searchDo(projectId: Int, searchWord: String): List<Pair<Double, Int>> {
    val project = projectDao.findById(projectId).get()

    val wf = allFrequent[projectId]!!
    val words = allWords[projectId]!!
    val centroidByLabel = allCentroidByLabel[projectId]!!

    val modUas = when (project.type) {
      1 -> MorphologicalAnalysis.parse(searchWord)
      2 -> janomeCommunicator.parseRequest(searchWord)
      else -> throw RuntimeException("")
    }
    val vec = wf.testScore2(modUas, words)

    val model = KNearestNeighbor<Int>()
    return model.scoring(centroidByLabel, vec)
  }

  fun trainDo(project: ProjectEntity, trainData: List<Pair<Int, String>>) {
    val projectId = project.id
    val preUniqueWords = arrayListOf<String>()
    val allDocsByLines = arrayListOf<List<String>>()
    val labelByLine = arrayListOf<Int>()
    trainData.forEach{row ->
      logger.info("extract " + (labelByLine.size + 1)) // TODO should be debug
      val modUas = when (project.type) {
        1 -> MorphologicalAnalysis.parse(row.second)
        2 -> janomeCommunicator.parseRequest(row.second)
        else -> throw RuntimeException("")
      }

      preUniqueWords.addAll(modUas)

      allDocsByLines.add(modUas)
      labelByLine.add(row.first)
    }

    val vectorsByLabel = HashMap<Int, ArrayList<DoubleArray>>()
    val uniqueWords = preUniqueWords.distinct()
    logger.info("start classification")
    val wf = WordFrequent(allDocsByLines)
    for (i in 0 until allDocsByLines.size) {
      val vec = wf.toScoreVec2(i, uniqueWords)
      val label = labelByLine[i]
//      println("$i - $browser $vec")
      if (vectorsByLabel.containsKey(label)) {
        vectorsByLabel[label]!!.add(vec)
      } else {
        vectorsByLabel[label] = arrayListOf(vec)
      }
    }
    logger.info("finish classification")

    val model = KNearestNeighbor<Int>()
    val trainedData = model.train(vectorsByLabel)
    println(trainedData)

    // TODO make these persistent other than static vars
    ProjectActionController.allWords[projectId] = uniqueWords
    ProjectActionController.allFrequent[projectId] = wf
    ProjectActionController.allCentroidByLabel[projectId] = trainedData
  }
}