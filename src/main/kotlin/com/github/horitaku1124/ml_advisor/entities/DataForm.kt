package com.github.horitaku1124.ml_advisor.entities

import javax.validation.constraints.NotEmpty

class DataForm {
  var result_id: Int? = null
  @NotEmpty
  var data: String? = null
}
