package com.github.horitaku1124.ml_advisor.entities

import javax.validation.constraints.NotEmpty
class ProjectForm {
  @NotEmpty
  var name: String? = null
  @NotEmpty
  var type: String? = null
}
