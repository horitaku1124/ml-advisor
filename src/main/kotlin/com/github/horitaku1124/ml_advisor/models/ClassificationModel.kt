package com.github.horitaku1124.ml_advisor.models

interface ClassificationModel<A, B, C, D> {
  fun train(allData: A): B
  fun inference(trainedData: B, newComer: C): D
  fun scoring(trainedData: B, newComer: C): List<Pair<Double, D>>
}