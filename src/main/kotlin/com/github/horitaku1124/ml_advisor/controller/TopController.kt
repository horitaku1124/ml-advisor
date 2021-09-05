package com.github.horitaku1124.ml_advisor.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class TopController {
  @GetMapping("/")
  fun index() : String {
    return "index"
  }
}