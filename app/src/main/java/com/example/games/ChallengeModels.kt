package com.example.games

import com.example.data.model.ChallengeType
import com.example.data.model.Difficulty
import kotlin.random.Random

object ChallengeGenerator {

  fun resolveChallengeType(configuredType: ChallengeType): ChallengeType {
    if (configuredType != ChallengeType.RANDOM) return configuredType
    val playable = listOf(
      ChallengeType.MATH,
      ChallengeType.MEMORY_SEQUENCE,
      ChallengeType.PATTERN,
      ChallengeType.REACTION,
      ChallengeType.QUICK_TAP,
      ChallengeType.MEMORY_CARDS,
      ChallengeType.WORD_SCRAMBLE
    )
    return playable.random()
  }

  // --- Math Challenge Model ---
  data class MathProblem(val question: String, val answer: Int)

  fun generateMathProblem(difficulty: Difficulty): MathProblem {
    return when (difficulty) {
      Difficulty.EASY -> {
        val a = Random.nextInt(12, 60)
        val b = Random.nextInt(8, 45)
        if (Random.nextBoolean()) {
          MathProblem("$a + $b = ?", a + b)
        } else {
          val max = maxOf(a, b)
          val min = minOf(a, b)
          MathProblem("$max - $min = ?", max - min)
        }
      }
      Difficulty.MEDIUM -> {
        if (Random.nextBoolean()) {
          val a = Random.nextInt(6, 16)
          val b = Random.nextInt(4, 12)
          MathProblem("$a × $b = ?", a * b)
        } else {
          val divisor = Random.nextInt(4, 12)
          val quotient = Random.nextInt(5, 15)
          val dividend = divisor * quotient
          MathProblem("$dividend ÷ $divisor = ?", quotient)
        }
      }
      Difficulty.HARD -> {
        val a = Random.nextInt(7, 18)
        val b = Random.nextInt(4, 9)
        val c = Random.nextInt(10, 40)
        if (Random.nextBoolean()) {
          MathProblem("($a × $b) + $c = ?", (a * b) + c)
        } else {
          val product = a * b
          val sub = Random.nextInt(10, product)
          MathProblem("($a × $b) - $sub = ?", product - sub)
        }
      }
    }
  }

  // --- Memory Sequence Model ---
  fun generateSequence(difficulty: Difficulty): List<Int> {
    val length = when (difficulty) {
      Difficulty.EASY -> 4
      Difficulty.MEDIUM -> 5
      Difficulty.HARD -> 7
    }
    return List(length) { Random.nextInt(1, 10) }
  }

  // --- Pattern Recognition Model ---
  data class PatternProblem(
    val sequence: List<String>,
    val correctAnswer: String,
    val options: List<String>
  )

  fun generatePattern(difficulty: Difficulty): PatternProblem {
    val symbols = listOf("▲", "●", "■", "◆", "★", "✦")
    val pIndex = Random.nextInt(0, 3)

    return when (pIndex) {
      0 -> {
        // ABABAB pattern: e.g. ▲ ● ▲ ● ▲ ?
        val s1 = symbols.random()
        var s2 = symbols.random()
        while (s2 == s1) s2 = symbols.random()
        val seq = listOf(s1, s2, s1, s2, s1)
        val ans = s2
        val options = (symbols.shuffled().take(3) + ans).distinct().take(4).shuffled()
        PatternProblem(seq, ans, options)
      }
      1 -> {
        // Math sequence: e.g. 3, 6, 12, 24, ?
        val mult = if (difficulty == Difficulty.HARD) 3 else 2
        val start = Random.nextInt(2, 6)
        val n1 = start
        val n2 = n1 * mult
        val n3 = n2 * mult
        val n4 = n3 * mult
        val ans = (n4 * mult).toString()
        val seq = listOf(n1.toString(), n2.toString(), n3.toString(), n4.toString())
        val options = listOf(
          ans,
          ((n4 * mult) - 2).toString(),
          ((n4 * mult) + mult).toString(),
          ((n4 * mult) + 4).toString()
        ).shuffled()
        PatternProblem(seq, ans, options)
      }
      else -> {
        // ABCABC pattern: e.g. ★ ◆ ■ ★ ◆ ?
        val s1 = symbols[0]
        val s2 = symbols[1]
        val s3 = symbols[2]
        val seq = listOf(s1, s2, s3, s1, s2)
        val ans = s3
        val options = (symbols.shuffled().take(3) + ans).distinct().take(4).shuffled()
        PatternProblem(seq, ans, options)
      }
    }
  }

  // --- Word Scramble Model ---
  data class WordProblem(val scrambled: String, val correctWord: String)

  private val WORD_BANK_EASY = listOf(
    "AWAKE", "SUNNY", "FOCUS", "ALARM", "QUEST", "POWER", "BRAIN", "SHINE", "ALERT"
  )
  private val WORD_BANK_MEDIUM = listOf(
    "ENERGY", "ACTIVE", "WAKING", "COFFEE", "THRIVE", "STRONG", "MOMENT", "MORNING"
  )
  private val WORD_BANK_HARD = listOf(
    "SUNRISE", "CONQUER", "TRIUMPH", "CHAMPION", "BRILLIANT", "RESOLUTE", "DISCIPLINE"
  )

  fun generateWordProblem(difficulty: Difficulty): WordProblem {
    val word = when (difficulty) {
      Difficulty.EASY -> WORD_BANK_EASY.random()
      Difficulty.MEDIUM -> WORD_BANK_MEDIUM.random()
      Difficulty.HARD -> WORD_BANK_HARD.random()
    }
    var scrambledList = word.toList().shuffled()
    while (scrambledList.joinToString("") == word && word.length > 2) {
      scrambledList = word.toList().shuffled()
    }
    return WordProblem(scrambledList.joinToString(" "), word)
  }
}
