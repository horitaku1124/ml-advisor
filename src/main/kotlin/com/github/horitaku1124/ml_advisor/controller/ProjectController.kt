package com.github.horitaku1124.ml_advisor.controller

import com.github.horitaku1124.ml_advisor.dao.ProjectDao
import com.github.horitaku1124.ml_advisor.entities.ProjectForm
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Controller
class ProjectController(var projectDao: ProjectDao) {

  @GetMapping("/project/new")
  fun new(model: MutableMap<String, Any>) : String {
    return "project_new"
  }

  @PostMapping("/project/created")
  fun created(@Validated project: ProjectForm,
            model: MutableMap<String, Any>) : String {
    val id = projectDao.create(project)
    model["projectId"] = id
    return "project_created"
  }

  @GetMapping("/project/{projectId}")
  fun index(@PathVariable("projectId") projectId: Int,
            model: MutableMap<String, Any>) : String {
    model["projectId"] = projectId
    return "project"
  }

  @GetMapping("/project/{projectId}/edit")
  fun edit(@PathVariable("projectId") projectId: Int,
            model: MutableMap<String, Any>) : String {
    model["projectId"] = projectId
    val project = projectDao.findById(projectId).get()
    model["project"] = project
    return "project_edit"
  }

  @PostMapping("/project/{projectId}/updated")
  fun update(@PathVariable("projectId") projectId: Int,
                  @Validated project: ProjectForm,
                  model: MutableMap<String, Any>) : String {
    model["projectId"] = projectId
    projectDao.update(projectId, project)
    return "project_created"
  }
}