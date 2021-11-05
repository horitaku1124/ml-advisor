package com.github.horitaku1124.ml_advisor.dao

import com.github.horitaku1124.ml_advisor.entities.LabelEntity
import com.github.horitaku1124.ml_advisor.entities.LabelForm
import org.json.simple.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import java.util.*

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

  fun findById(labelId: Int): Optional<LabelEntity> {
    val sql = "select * from train_labels where id = :id"
    val param = hashMapOf<String, Any>().also {
      it["id"] = labelId
    }

    val rs = jdbcTemplate.queryForRowSet(sql, param)
    if (rs.next()) {
      return Optional.of(
        LabelEntity(
          id = rs.getInt("id"),
          projectId = rs.getInt("project_id"),
          slug = rs.getString("slug")!!,
          result = rs.getString("result") ?: "",
        )
      )
    }
    return Optional.empty()
  }

  fun findByProjectId(projectId: Int): List<LabelEntity> {
    val sql = "select * from train_labels where project_id = :project_id"
    val param = hashMapOf<String, Any>().also {
      it["project_id"] = projectId
    }

    val rs = jdbcTemplate.queryForRowSet(sql, param)
    val result = arrayListOf<LabelEntity>()
    while (rs.next()) {
      result.add(LabelEntity(
        id = rs.getInt("id"),
        projectId = rs.getInt("project_id"),
        slug = rs.getString("slug")!!,
        result = rs.getString("slug") ?: ""
      ))
    }
    return result
  }

  fun create(projectId: Int, label: LabelForm): Int {
    val param = hashMapOf<String, Any>().also {
      it["project_id"] = projectId
      it["slug"] = label.slug!!
      it["result"] = label.result ?: ""
    }

    val sql = "insert into train_labels(project_id, slug, result) values (:project_id, :slug, :result)"
    jdbcTemplate.update(sql, param)

    val rs = jdbcTemplate.queryForRowSet("select last_insert_id()", mapOf<String, Any>())
    if (rs.next()) {
      return rs.getInt(1)
    }
    throw RuntimeException("insert error")
  }
  fun update(labelId: Int, label: LabelForm) {
    val param = hashMapOf<String, Any>().also {
      it["id"] = labelId
      it["slug"] = label.slug!!
      it["result"] = label.result!!
    }

    val sql = "update train_labels set slug = :slug, result = :result, updated_at = now() where id = :id"

    jdbcTemplate.update(sql, param)
  }

  fun delete(labelId: Int) {
    val param = hashMapOf<String, Any>().also {
      it["id"] = labelId
    }
    val sql = "delete from train_labels where id = :id"
    jdbcTemplate.update(sql, param)
  }
}