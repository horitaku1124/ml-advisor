package com.github.horitaku1124.ml_advisor.dao

import com.github.horitaku1124.ml_advisor.entities.ProjectEntity
import com.github.horitaku1124.ml_advisor.entities.ProjectForm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import java.lang.RuntimeException

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

  fun create(project: ProjectForm): Int {
    val sql = "insert into projects(name) values (:name)"
    var param = hashMapOf<String, Any>()
    param["name"] = project.name!!
    jdbcTemplate.update(sql, param)

    val rs = jdbcTemplate.queryForRowSet("select last_insert_id()", mapOf<String, Any>())
    if (rs.next()) {
      return rs.getInt(1)
    }
    throw RuntimeException("insert error")
  }
}