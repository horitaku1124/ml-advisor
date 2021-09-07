package com.github.horitaku1124.blog_manager.util

import java.lang.StringBuilder
import kotlin.streams.toList

class MorphologicalAnalysis {
  companion object {
    private val stopWords = " 　()（）<>[]/|：.,;!★■ ●「」\n\t"
    private val conditionalStopWords = "-"
    fun parse(str: String): List<String> {
      var i = 0
      var target = str

      var result = arrayListOf<String>()
      var isAsciiStr = target[0].toInt() < 256
      var charBuf = StringBuilder()
      while (i < target.length) {
//      println(str[i])
        var c = target[i]
        if (stopWords.contains(c)) {
          if (charBuf.length > 0) {
            result.add(charBuf.toString())
            charBuf.clear()
          }
          isAsciiStr = true
          i++
          continue
        }
        if (conditionalStopWords.contains(c)) {
          if (charBuf.isEmpty()) {
            i++
            continue
          }
        }
        var code = c.toInt()
        if (code < 256) {
          if (isAsciiStr) {
            charBuf.append(c)
          } else {
            if (charBuf.length > 0) {
              result.add(charBuf.toString())
              charBuf.clear()
            }
            charBuf.append(c)
            isAsciiStr = true
          }
        } else {
          if (isAsciiStr) {
            if (charBuf.length > 0) {
              result.add(charBuf.toString())
              charBuf.clear()
            }
            charBuf.append(c)
            isAsciiStr = false
          } else {
            charBuf.append(c)
          }
        }
//      println(c.toInt())
        i++
      }
      if (charBuf.length > 0) {
        result.add(charBuf.toString())
      }
      return result.stream()
        .map { it.trim() }
        .map { it.toLowerCase() }
        .filter{ it.isNotEmpty()}
        .map { minimizeNumber(it) }
        .toList()
    }

    fun minimizeNumber(num: String): String {
      var regx = Regex("^\\d+$")
      if (!regx.matches(num)) {
        return num
      }
      var dist = ""
      for (i in 0 until num.length) {
        if (i == 0) {
          dist += num[i]
        } else {
          dist += '0'
        }
      }
      return dist
    }
  }
}