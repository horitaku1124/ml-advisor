package com.github.horitaku1124.ml_advisor.controller

import com.github.horitaku1124.ml_advisor.dao.ProjectDao
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class TopController(var projectDao: ProjectDao) {
  @GetMapping("/")
  fun index() : String {
    return "index"
  }

  @GetMapping("/top")
  fun top(model: MutableMap<String, Any>) : String {
    model["projects"] = projectDao.findAll()
    return "top"
  }
}