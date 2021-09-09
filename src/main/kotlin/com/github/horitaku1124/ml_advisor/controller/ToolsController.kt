package com.github.horitaku1124.ml_advisor.controller

import com.github.horitaku1124.blog_manager.util.MorphologicalAnalysis
import com.github.horitaku1124.blog_manager.util.VectorUtil
import com.github.horitaku1124.blog_manager.util.WordFrequent
import com.github.horitaku1124.ml_advisor.dao.TrainDataDao
import com.github.horitaku1124.ml_advisor.dao.TrainLabelDao
import com.github.horitaku1124.ml_advisor.entities.SearchForm
import com.github.horitaku1124.ml_advisor.entities.SegmentationForm
import com.github.horitaku1124.ml_advisor.entities.TrainForm
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Controller
class ToolsController(var trainDataDao: TrainDataDao,
                      var trainLabelDao: TrainLabelDao) {
  var logger: Logger = LoggerFactory.getLogger(ToolsController::class.java)
  companion object {
    var allWords = hashMapOf<Int, List<String>>()
    var allFrequent = hashMapOf<Int, WordFrequent>()
    var allCentroidByBrowser = hashMapOf<Int, HashMap<Int, List<Double>>>()
  }

  @GetMapping("/segmentation")
  fun segmentation(model: MutableMap<String, Any>) : String {
    return "segmentation"
  }

  @PostMapping("/segmentation")
  fun segmentationExe(@Validated segmentationForm: SegmentationForm,
            model: MutableMap<String, Any>) : String {
    val query = segmentationForm.query ?: ""
    model["query"] = query
    val modUas = MorphologicalAnalysis.parse(query)

    val result = StringBuffer()
    modUas.forEach {
      result.append(it).append(" ")
    }

    model["result_text"] = result.toString()

    return "segmentation"
  }
}