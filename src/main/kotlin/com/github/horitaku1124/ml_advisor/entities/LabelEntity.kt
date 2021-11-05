package com.github.horitaku1124.ml_advisor.entities

data class LabelEntity(
  val id: Int,
  val projectId: Int,
  var slug: String,
  var result: String,
)
