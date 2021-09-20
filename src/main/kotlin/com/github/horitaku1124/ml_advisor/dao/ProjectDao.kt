package com.github.horitaku1124.ml_advisor.dao

import com.github.horitaku1124.ml_advisor.entities.ProjectEntity
import com.github.horitaku1124.ml_advisor.entities.ProjectForm
import org.apache.logging.log4j.util.Strings
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import java.util.*

@Service
class ProjectDao {
  @Autowired
  private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

  fun findAll(): List<ProjectEntity> {
    val sql = "select * from projects"
    val rs = jdbcTemplate.queryForRowSet(sql, mapOf<String, Any>())
    val list = arrayListOf<ProjectEntity>()
    val jsonParser = JSONParser()
    while(rs.next()) {
      val type = rs.getString("property").let {
        if (Strings.isBlank(it)) {
          1
        } else {
          val property = jsonParser.parse(it)as JSONObject
          (property.getValue("type") as String).toInt()
        }
      }

      list.add(
        ProjectEntity(
          rs.getInt("id"),
          rs.getString("name")!!,
          type
        )
      )
    }
    return list
  }

  fun findById(projectId: Int): Optional<ProjectEntity> {
    val sql = "select * from projects where id = :id"
    var param = hashMapOf<String, Any>()
    param["id"] = projectId

    val rs = jdbcTemplate.queryForRowSet(sql, param)
    if (rs.next()) {
      val jsonParser = JSONParser()


      val type = rs.getString("property").let {
        if (Strings.isBlank(it)) {
          1
        } else {
          val property = jsonParser.parse(it) as JSONObject
          (property.getValue("type") as String).toInt()
        }
      }

      return Optional.of(ProjectEntity(
        rs.getInt("id"),
        rs.getString("name")!!,
        type
      ))
    }
    return Optional.empty()
  }

  fun create(project: ProjectForm): Int {
    val property = JSONObject().also {
      it["type"] = project.type
    }

    val sql = "insert into projects(name, property) values (:name, :property)"
    var param = hashMapOf<String, Any>()
    param["name"] = project.name!!
    param["property"] = property.toString()
    jdbcTemplate.update(sql, param)

    val rs = jdbcTemplate.queryForRowSet("select last_insert_id()", mapOf<String, Any>())
    if (rs.next()) {
      return rs.getInt(1)
    }
    throw RuntimeException("insert error")
  }
  fun update(projectId: Int, project: ProjectForm) {
    val property = JSONObject().also {
      it["type"] = project.type
    }

    val sql = "update projects set name = :name, property = :property where id = :id"
    val param = hashMapOf<String, Any>().also {
      it["id"] = projectId
      it["name"] = project.name!!
      it["property"] = property.toString()
    }
    jdbcTemplate.update(sql, param)
  }
}