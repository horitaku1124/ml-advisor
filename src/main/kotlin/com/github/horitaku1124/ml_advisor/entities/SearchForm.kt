package com.github.horitaku1124.ml_advisor.entities

import javax.validation.constraints.NotNull
class SearchForm {
  @NotNull
  var query: String? = null
  @NotNull
  var project: Int? = null
}
