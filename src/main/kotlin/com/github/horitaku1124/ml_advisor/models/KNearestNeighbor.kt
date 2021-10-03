@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package com.github.horitaku1124.ml_advisor.models

import com.github.horitaku1124.blog_manager.util.VectorUtil

class KNearestNeighbor<R>: ClassificationModel<
      HashMap<R, ArrayList<DoubleArray>>,
      KNearestNeighbor.KnnResult<R>,
      DoubleArray,
      R
    > {
  override fun train(vectorsByLabel: HashMap<R, ArrayList<DoubleArray>>): KnnResult<R> {
    val centroidByLabel = HashMap<R, DoubleArray>()
    vectorsByLabel.forEach { (label, vectors) ->
      centroidByLabel[label] = VectorUtil.vecAverage2(vectors.toList())
    }
    return KnnResult(centroidByLabel)
  }
  override fun inference(trainedData: KnnResult<R>, newComer:DoubleArray ): R {
    val scoreAndId = scoring(trainedData, newComer)
    scoreAndId.sortByDescending { it.first }
    return scoreAndId.first().second
  }

  override fun scoring(trainedData: KnnResult<R>, newComer:DoubleArray ): ArrayList<Pair<Double, R>> {
    val scoreAndId = arrayListOf<Pair<Double, R>>()
    trainedData.centroidByLabel.forEach { (label, centroid) ->
      val score = VectorUtil.cosSim2(centroid, newComer)
      scoreAndId.add(Pair(score, label))
    }
    return scoreAndId
  }

  data class KnnResult<R> (var centroidByLabel: HashMap<R, DoubleArray>)
}