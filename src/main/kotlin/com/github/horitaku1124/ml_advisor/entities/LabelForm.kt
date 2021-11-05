package com.github.horitaku1124.ml_advisor.entities

import javax.validation.constraints.NotEmpty

class LabelForm {
  @NotEmpty
  var slug: String? = null
  @NotEmpty
  var result: String? = null
}
