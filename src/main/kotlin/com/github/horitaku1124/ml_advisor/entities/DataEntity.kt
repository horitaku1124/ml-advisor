package com.github.horitaku1124.ml_advisor.entities

data class DataEntity(
  val id: Int,
  val projectId: Int,
  val resultId: Int,
  var data: String,
) {
  fun getShrinkData(): String {
    return data.substring(0, if (data.length > 30) 30 else data.length)
  }
}
