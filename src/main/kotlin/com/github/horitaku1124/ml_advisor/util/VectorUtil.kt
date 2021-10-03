package com.github.horitaku1124.blog_manager.util

import kotlin.math.sqrt
import kotlin.random.Random

class VectorUtil {
  companion object {
    fun cosSim(list1: List<Double> , list2: List<Double> ): Double {
      var top = 0.0
      var bottom1 = 0.0
      var bottom2 = 0.0
      for (i in list1.indices) {
        val l = list1[i]
        val r = list2[i]

        top += l * r

        bottom1 += l * l
        bottom2 += r * r
      }
      return top / (sqrt(bottom1) * sqrt(bottom2))
    }

    fun cosSim2(list1: DoubleArray , list2: DoubleArray): Double {
      var top = 0.0
      var bottom1 = 0.0
      var bottom2 = 0.0
      for (i in list1.indices) {
        val l = list1[i]
        val r = list2[i]

        top += l * r

        bottom1 += l * l
        bottom2 += r * r
      }
      return top / (sqrt(bottom1) * sqrt(bottom2))
    }

    fun kmeans(num: Int, vectors: List<List<Double>>):
        Pair<Map<Int, List<Int>>, List<List<Double>>> {
      val width = vectors[0].size
      val centroids = arrayListOf<List<Double>>()
      var max = 1.0

      vectors.forEach { vecs ->
        vecs.forEach { value ->
          if (max < value) {
            max = value
          }
        }
      }
      println("max=${max}")
      
      for (i in 0 until num) {
        val centroid = arrayListOf<Double>()
        for (j in 0 until width) {
          centroid.add(Random.nextDouble() * max)
        }
        centroids.add(centroid)
      }

      val classified = HashMap<Int, ArrayList<Int>>()
      var lastClassified = centroids.toString()
      println("centroids = ${centroids}")
      for (i in 0 until 10) {
        for (j in 0 until num) {
          classified[j] = arrayListOf()
        }

        for (k in vectors.indices) {
          val vecs = vectors[k]
          var nearstCenter = 0
          var distance = cosSim(vecs, centroids[0])
          for (l in 1 until num) {
            val newDist = cosSim(vecs, centroids[l])
            if (distance < newDist) {
              nearstCenter = l
              distance = newDist
            }
          }
//          var distance = calcDistance(vecs, centroids[0])
//          for (l in 1 until num) {
//            val newDist = calcDistance(vecs, centroids[l])
//            if (distance > newDist) {
//              nearstCenter = l
//              distance = newDist
//            }
//          }
          classified[nearstCenter]!!.add(k)
        }

        for (j in centroids.indices) {
          val classifiedVec = arrayListOf<List<Double>>()
          classified[j]!!.forEach {
            classifiedVec.add(vectors[it])
          }
          if (classifiedVec.isNotEmpty()) {
            centroids[j] = vecAverage(classifiedVec)
          }
        }
        println("classified = ${classified}")
        println("centroids = ${centroids}")
        if (lastClassified == centroids.toString()) {
          println("classification fixed")
          return Pair(classified, centroids)
        }
        lastClassified = centroids.toString()
      }
      return Pair(classified, centroids)
    }

    fun calcDistance(vec1: List<Double>, vec2: List<Double>): Double {
      var distance = 0.0
      for (i in vec1.indices) {
        val d = vec1[i] - vec2[i]
        distance += (d * d)
      }
      return Math.sqrt(distance)
    }

    fun vecAverage(vec1: List<List<Double>>): List<Double> {
      val width = vec1[0].size
      val averages = arrayListOf<Double>()
      for (i in 0 until width) {
        averages.add(0.0)
      }
      for (j in vec1.indices) {
        for (l in vec1[j].indices) {
          averages[l] += vec1[j][l]
        }
      }
      for (l in 0 until width) {
        averages[l] = averages[l] / vec1.size
      }
      return averages
    }

    fun vecAverage2(vec1: List<DoubleArray>): DoubleArray {
      val width = vec1[0].size
      val averages = DoubleArray(width) { 0.0 }

      for (j in vec1.indices) {
        for (l in vec1[j].indices) {
          averages[l] += vec1[j][l]
        }
      }
      for (l in 0 until width) {
        averages[l] = averages[l] / vec1.size
      }
      return averages
    }
  }
}