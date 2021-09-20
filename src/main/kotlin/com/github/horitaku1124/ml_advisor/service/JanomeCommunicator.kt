package com.github.horitaku1124.ml_advisor.service

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class JanomeCommunicator {
  @Value("\${docker.janome-url}")
  private var janomeUrl: String? = null

  val client = HttpClient.newHttpClient()!!

  fun parseRequest(query: String): List<String> {
    val postData = JSONObject().also {
      it["word"] = query
        .replace("\r\n", " ")
        .replace("\n", " ")
    }

    val request = HttpRequest.newBuilder()
      .uri(URI.create(janomeUrl!!))
      .setHeader("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(postData.toString()))
      .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    val jsonParser = JSONParser()
    val obj = jsonParser.parse(response.body()) as JSONObject
    val tokens = obj["tokens"] as JSONArray
    return tokens.map { it.toString() }
  }
}