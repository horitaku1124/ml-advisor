package com.github.horitaku1124.ml_advisor.dao

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

@Service
class TrainDataDao {
  @Autowired
  private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

  fun findAllById(projectId: Int): List<Pair<Int, String>> {
    val sql = "select result_id, data from train_data where  project_id = :project_id"
    val param = hashMapOf<String, Any>()
    param["project_id"] = projectId
    val rs = jdbcTemplate.queryForRowSet(sql, param)
    val trainData = arrayListOf<Pair<Int, String>>()
    while(rs.next()) {
      trainData.add(Pair(
        rs.getInt("result_id"),
        rs.getString("data")!!
      ))
    }
    return trainData
  }
}