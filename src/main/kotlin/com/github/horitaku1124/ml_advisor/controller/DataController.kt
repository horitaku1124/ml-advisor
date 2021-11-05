package com.github.horitaku1124.ml_advisor.controller

import com.github.horitaku1124.ml_advisor.dao.TrainDataDao
import com.github.horitaku1124.ml_advisor.dao.TrainLabelDao
import com.github.horitaku1124.ml_advisor.entities.DataForm
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Controller
class DataController(var trainDataDao: TrainDataDao,
                     var labelDao: TrainLabelDao) {

  private fun allLabelMap(projectId: Int): Map<Int, String> {
    val allLabels = labelDao.findByProjectId(projectId)
    val map = hashMapOf<Int, String>()
    allLabels.forEach {
      map[it.id] = it.slug
    }
    return map
  }

  @GetMapping("/project/{projectId}/data")
  fun index(@PathVariable("projectId") projectId: Int,
            model: MutableMap<String, Any>) : String {
    model["projectId"] = projectId
    model["data"] = trainDataDao.findByProjectId(projectId)
    model["allLabelMap"] = allLabelMap(projectId)

    return "data/index"
  }

  @GetMapping("/project/{projectId}/data/new")
  fun new(@PathVariable("projectId") projectId: Int,
          model: MutableMap<String, Any>) : String {
    model["projectId"] = projectId
    model["allLabelMap"] = allLabelMap(projectId).toList()
    return "data/new"
  }

  @PostMapping("/project/{projectId}/data/created")
  fun created(@PathVariable("projectId") projectId: Int,
              @Validated dataForm: DataForm,
              model: MutableMap<String, Any>) : String {
    val id = trainDataDao.create(projectId, dataForm)
    model["projectId"] = projectId
    model["dataId"] = id
    return "data/created"
  }

  @GetMapping("/project/{projectId}/data/{dataId}/edit")
  fun edit(@PathVariable("projectId") projectId: Int,
           @PathVariable("dataId") dataId: Int,
           model: MutableMap<String, Any>) : String {
    model["projectId"] = projectId
    model["dataId"] = dataId
    model["row"] = trainDataDao.findById(dataId).get()
    model["allLabelMap"] = allLabelMap(projectId).toList()
    return "data/edit"
  }

  @PostMapping("/project/{projectId}/data/{dataId}/updated")
  fun update(@PathVariable("projectId") projectId: Int,
             @PathVariable("dataId") dataId: Int,
             @Validated dataForm: DataForm,
             model: MutableMap<String, Any>) : String {
    model["projectId"] = projectId
    model["dataId"] = dataId
    trainDataDao.update(dataId, dataForm)
    return "data/updated"
  }

  @GetMapping("/project/{projectId}/data/{dataId}/delete")
  fun delete(@PathVariable("projectId") projectId: Int,
           @PathVariable("dataId") dataId: Int,
           model: MutableMap<String, Any>) : String {
    model["projectId"] = projectId
    model["dataId"] = dataId
    model["row"] = trainDataDao.findById(dataId).get()
    return "data/delete"
  }

  @PostMapping("/project/{projectId}/data/{dataId}/deleted")
  fun deleteSubmit(@PathVariable("projectId") projectId: Int,
             @PathVariable("dataId") dataId: Int,
             model: MutableMap<String, Any>) : String {
    trainDataDao.delete(dataId)
    model["projectId"] = projectId
    return "data/deleted"
  }
}