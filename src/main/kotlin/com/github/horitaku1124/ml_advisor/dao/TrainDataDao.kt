package com.github.horitaku1124.ml_advisor.dao

import com.github.horitaku1124.ml_advisor.entities.DataEntity
import com.github.horitaku1124.ml_advisor.entities.DataForm
import com.github.horitaku1124.ml_advisor.entities.LabelForm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import java.util.*

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

  fun findByProjectId(projectId: Int): List<DataEntity> {
    val sql = "select * from train_data where project_id = :project_id"
    val param = hashMapOf<String, Any>().also {
      it["project_id"] = projectId
    }

    val rs = jdbcTemplate.queryForRowSet(sql, param)
    val result = arrayListOf<DataEntity>()
    while (rs.next()) {
      result.add(
        DataEntity(
          id = rs.getInt("id"),
          projectId = rs.getInt("project_id"),
          resultId = rs.getInt("result_id"),
          data = rs.getString("data") ?: ""
        )
      )
    }
    return result
  }

  fun findById(dataId: Int): Optional<DataEntity> {
    val sql = "select * from train_data where id = :id"
    val param = hashMapOf<String, Any>().also {
      it["id"] = dataId
    }

    val rs = jdbcTemplate.queryForRowSet(sql, param)
    if (rs.next()) {
      return Optional.of(
        DataEntity(
          id = rs.getInt("id"),
          projectId = rs.getInt("project_id"),
          resultId = rs.getInt("result_id"),
          data = rs.getString("data") ?: ""
        )
      )
    }
    return Optional.empty()
  }

  fun update(dataId: Int, dataForm: DataForm) {
    val param = hashMapOf<String, Any>().also {
      it["id"] = dataId
      it["result_id"] = dataForm.result_id!!
      it["data"] = dataForm.data!!
    }

    val sql = "update train_data set result_id = :result_id, data = :data, updated_at = now() where id = :id"

    jdbcTemplate.update(sql, param)
  }

  fun create(projectId: Int, dataForm: DataForm): Int {
    val param = hashMapOf<String, Any>().also {
      it["project_id"] = projectId
      it["result_id"] = dataForm.result_id!!
      it["data"] = dataForm.data!!
    }

    val sql = "insert into train_data(project_id, result_id, data) values (:project_id, :result_id, :data)"
    jdbcTemplate.update(sql, param)

    val rs = jdbcTemplate.queryForRowSet("select last_insert_id()", mapOf<String, Any>())
    if (rs.next()) {
      return rs.getInt(1)
    }

    throw RuntimeException("insert error")
  }

  fun delete(dataId: Int) {
    val param = hashMapOf<String, Any>().also {
      it["id"] = dataId
    }
    val sql = "delete from train_data where id = :id"
    jdbcTemplate.update(sql, param)
  }
}