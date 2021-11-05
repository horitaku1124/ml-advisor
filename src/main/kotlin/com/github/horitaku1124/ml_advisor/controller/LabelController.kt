package com.github.horitaku1124.ml_advisor.controller

import com.github.horitaku1124.ml_advisor.dao.TrainLabelDao
import com.github.horitaku1124.ml_advisor.entities.LabelForm
import com.github.horitaku1124.ml_advisor.entities.ProjectForm
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Controller
class LabelController(var labelDao: TrainLabelDao) {

  @GetMapping("/project/{projectId}/label")
  fun index(@PathVariable("projectId") projectId: Int,
            model: MutableMap<String, Any>) : String {
    model["projectId"] = projectId
    model["labels"] = labelDao.findByProjectId(projectId)

    return "label/index"
  }

  @GetMapping("/project/{projectId}/label/new")
  fun new(@PathVariable("projectId") projectId: Int,
          model: MutableMap<String, Any>) : String {
    model["projectId"] = projectId
    return "label/new"
  }

  @PostMapping("/project/{projectId}/label/created")
  fun created(@PathVariable("projectId") projectId: Int,
              @Validated label: LabelForm,
              model: MutableMap<String, Any>) : String {
    val id = labelDao.create(projectId, label)
    model["projectId"] = projectId
    model["labelId"] = id
    return "label/created"
  }

  @GetMapping("/project/{projectId}/label/{labelId}/edit")
  fun edit(@PathVariable("projectId") projectId: Int,
           @PathVariable("labelId") labelId: Int,
           model: MutableMap<String, Any>) : String {
    model["projectId"] = projectId
    model["labelId"] = labelId
    model["row"] = labelDao.findById(labelId).get()
    return "label/edit"
  }

  @PostMapping("/project/{projectId}/label/{labelId}/updated")
  fun update(@PathVariable("projectId") projectId: Int,
             @PathVariable("labelId") labelId: Int,
             @Validated labelForm: LabelForm,
             model: MutableMap<String, Any>) : String {
    model["projectId"] = projectId
    model["labelId"] = labelId
    labelDao.update(labelId, labelForm)
    return "label/updated"
  }

  @GetMapping("/project/{projectId}/label/{labelId}/delete")
  fun delete(@PathVariable("projectId") projectId: Int,
           @PathVariable("labelId") labelId: Int,
           model: MutableMap<String, Any>) : String {
    model["projectId"] = projectId
    model["labelId"] = labelId
    model["row"] = labelDao.findById(labelId).get()
    return "label/delete"
  }

  @PostMapping("/project/{projectId}/label/{labelId}/deleted")
  fun deleteSubmit(@PathVariable("projectId") projectId: Int,
             @PathVariable("labelId") labelId: Int,
             model: MutableMap<String, Any>) : String {
    labelDao.delete(labelId)
    model["projectId"] = projectId
    return "label/deleted"
  }
}