package com.github.horitaku1124.ml_advisor.controller

import com.github.horitaku1124.blog_manager.util.MorphologicalAnalysis
import com.github.horitaku1124.ml_advisor.entities.SegmentationForm
import com.github.horitaku1124.ml_advisor.entities.Test1Form
import com.github.horitaku1124.ml_advisor.service.JanomeCommunicator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
class ToolsController(
  var janomeCommunicator: JanomeCommunicator
) {
  var logger: Logger = LoggerFactory.getLogger(ToolsController::class.java)

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

  @GetMapping("/segmentation_ja")
  fun segmentationJa(model: MutableMap<String, Any>) : String {
    return "segmentation_ja"
  }

  @PostMapping("/segmentation_ja")
  fun segmentationJaExe(@Validated segmentationForm: SegmentationForm,
            model: MutableMap<String, Any>) : String {
    val query = segmentationForm.query ?: ""

    model["query"] = query
    model["result_text"] = janomeCommunicator.parseRequest(query)
    return "segmentation_ja"
  }

  @GetMapping("/reduce_dimension")
  fun reduceDimension(model: MutableMap<String, Any>) : String {
    return "reduce_dimension"
  }

  @GetMapping("/td_idf")
  fun tdIdf(model: MutableMap<String, Any>) : String {
    model["form"] = Test1Form()
    return "td_idf"
  }

  @PostMapping("/td_idf_2")
  fun tdIdf2(@Validated test1Form: Test1Form,
             model: MutableMap<String, Any>) : String {
    if (test1Form.query != null) {
      val query = test1Form.query!!
      val modUas = janomeCommunicator.parseRequest(query).distinct()
      val vec = ProjectActionController.jaCopus.getTfIdfArray(modUas)
      val pairs = arrayListOf<Pair<Int, Double>>()

      for (i in vec.indices) {
        pairs.add(Pair(i, vec[i]))
      }

      val pairs2 = pairs.sortedByDescending { it.second }

      var result = ""
      for (i in pairs2) {
        result += modUas[i.first] + " = " + i.second + "\n"
      }
      test1Form.result = result
    }
    model["form"] = test1Form
    return "td_idf"
  }
}