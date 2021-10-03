package com.github.horitaku1124.blog_manager.util


import kotlin.math.ln

class WordFrequent(var docs: List<List<String>>) {
  private val docSize = docs.size.toDouble()
  private val docsIndexes: List<Map<String, Int>>
  init {
    docsIndexes = arrayListOf()
    for (doc in docs) {
      val docIndex = hashMapOf<String, Int>()
      for (word in doc) {
        if (docIndex.containsKey(word)) {
          val count = docIndex[word]!! + 1
          docIndex[word] = count
        } else {
          docIndex[word] = 1
        }
      }
      docsIndexes.add(docIndex)
    }
  }

  /**
   * 単語の出現頻度
   * 単語が文章の中の割合
   * @return 出現回数 / 総単語数
   */
  fun getTf(docIndex:Int, word: String): Double {
    val wordList = docs[docIndex]
    val wordCount = docsIndexes[docIndex].getOrDefault(word, 0).toDouble()
    return wordCount / wordList.size
  }

  /**
   * 逆文書頻度
   * 単語を含む文書の割合の逆数の自然対数+1
   */
  fun getIdf(word: String): Double {
    val count = docsIndexes.stream().filter { it.containsKey(word) }.count().toDouble()
    val div = ln(docSize / count)
    return div + 1
  }

  fun getTfIdf(docIndex:Int, word: String): Double {
    val tfidf = getTf(docIndex, word) * getIdf(word)
    return if (tfidf.isNaN()) 0.0 else tfidf
  }

  fun toScoreVec2(docIndex:Int, allUniqueWords: List<String>): DoubleArray {
    val vecs = DoubleArray(allUniqueWords.size) {0.0}
    for (i in allUniqueWords.indices) {
      vecs[i] = getTfIdf(docIndex, allUniqueWords[i])
    }
    return vecs
  }

  fun testScore2(testDoc: List<String>, allUniqueWords: List<String>): DoubleArray {
    val vecs = DoubleArray(allUniqueWords.size) {0.0}

    for (i in allUniqueWords.indices) {
      val word = allUniqueWords[i]
      val wordCount1 = testDoc.stream().filter{ it == word}.count().toDouble()

      val tf = wordCount1 / testDoc.size
      val idf = getIdf(word)
      val tfidf = tf * idf
      val score = if (tfidf.isNaN()) 0.0 else tfidf

      vecs[i] = score
    }
    return vecs
  }
}