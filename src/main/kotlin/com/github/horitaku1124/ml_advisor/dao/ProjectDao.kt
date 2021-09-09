package com.github.horitaku1124.ml_advisor.dao

import com.github.horitaku1124.ml_advisor.entities.ProjectEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

@Service
class ProjectDao {
  @Autowired
  private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

  fun findAll(): List<ProjectEntity> {
    val sql = "select * from projects"
    val rs = jdbcTemplate.queryForRowSet(sql, mapOf<String, Any>())
    val list = arrayListOf<ProjectEntity>()
    while(rs.next()) {
      list.add(
        ProjectEntity(
          rs.getInt("id"),
          rs.getString("name")!!,
        )
      )
    }
    return list
  }
}