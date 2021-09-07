package com.github.horitaku1124.ml_advisor.dao

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

@Service
class TrainLabelDao {
  @Autowired
  private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

  fun findAll(projectId: Int): Map<Int, Pair<String, String>> {
    val sql = "select id, slug, result from train_labels where  project_id = :project_id"
    val param = hashMapOf<String, Any>()
    param["project_id"] = projectId
    val rs = jdbcTemplate.queryForRowSet(sql, param)
    val result = hashMapOf<Int, Pair<String, String>>()
    while(rs.next()) {
      val id = rs.getInt("id")
      result[id] = Pair(
        rs.getString("slug")!!,
        rs.getString("result")!!
      )
    }
    return result
  }

}