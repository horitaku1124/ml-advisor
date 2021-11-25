package com.github.horitaku1124.ml_advisor.entities

import javax.validation.constraints.NotNull
class Test1Form {
  @NotNull
  var query: String? = null
  var result: String? = null
}
