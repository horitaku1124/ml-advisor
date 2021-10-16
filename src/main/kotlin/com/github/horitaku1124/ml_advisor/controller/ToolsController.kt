package com.github.horitaku1124.ml_advisor.controller

import com.github.horitaku1124.blog_manager.util.MorphologicalAnalysis
import com.github.horitaku1124.ml_advisor.entities.SegmentationForm
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest.BodyPublishers.ofString
import java.net.http.HttpRequest.newBuilder
import java.net.http.HttpResponse.BodyHandlers.ofString

@Controller
class ToolsController {
  @Value("\${docker.janome-url}")
  private var janomeUrl: String? = null

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

    val postData = JSONObject().also {
      it["word"] = query
        .replace("\r\n", " ")
        .replace("\n", " ")
    }

    val client = HttpClient.newHttpClient()
    val request = newBuilder()
      .uri(URI.create(janomeUrl!!))
      .setHeader("Content-Type", "application/json")
      .POST(ofString(postData.toString()))
      .build()

    val response = client.send(request, ofString())

    val result = StringBuffer()

    val jsonParser = JSONParser()
    val obj = jsonParser.parse(response.body()) as JSONObject
    val tokens = obj["tokens"] as JSONArray
    result.append(tokens.joinToString(" ") )

    model["query"] = query
    model["result_text"] = result.toString()
    return "segmentation_ja"
  }

  @GetMapping("/reduce_dimension")
  fun reduceDimension(model: MutableMap<String, Any>) : String {
    return "reduce_dimension"
  }
}