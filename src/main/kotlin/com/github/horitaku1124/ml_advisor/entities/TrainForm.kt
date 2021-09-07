package com.github.horitaku1124.ml_advisor.entities

import javax.validation.constraints.NotNull
class TrainForm {
  @NotNull
  var project: Int? = null
}
