package com.example.games.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Difficulty
import com.example.games.ChallengeGenerator
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EnergyAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// ----------------------------------------------------
// 1. MATH CHALLENGE COMPOSABLE
// ----------------------------------------------------
@Composable
fun MathChallengeComposable(
  difficulty: Difficulty,
  onCompleted: () -> Unit,
  modifier: Modifier = Modifier
) {
  var problem by remember { mutableStateOf(ChallengeGenerator.generateMathProblem(difficulty)) }
  var userInput by remember { mutableStateOf("") }
  var isError by remember { mutableStateOf(false) }

  fun checkAnswer() {
    val intVal = userInput.toIntOrNull()
    if (intVal != null && intVal == problem.answer) {
      onCompleted()
    } else {
      isError = true
      userInput = ""
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Solve the equation:",
      style = MaterialTheme.typography.titleMedium,
      color = TextSecondary
    )
    Spacer(modifier = Modifier.height(12.dp))

    // Equation Card
    Card(
      colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
      shape = RoundedCornerShape(20.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(if (isError) DangerRed else NeonCyan)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = problem.question.replace("?", ""),
          style = MaterialTheme.typography.displayMedium,
          fontWeight = FontWeight.Black,
          color = TextPrimary,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = if (userInput.isEmpty()) "Enter Answer" else "= $userInput",
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Bold,
          color = if (userInput.isEmpty()) TextSecondary else NeonCyan
        )
        if (isError) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Incorrect! Try again",
            color = DangerRed,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Keypad (1..9, Clear, 0, Enter)
    KeypadGrid(
      onNumberClick = { digit ->
        isError = false
        if (userInput.length < 5) userInput += digit.toString()
      },
      onClearClick = {
        userInput = ""
        isError = false
      },
      onSubmitClick = { checkAnswer() }
    )
  }
}

// ----------------------------------------------------
// 2. MEMORY SEQUENCE COMPOSABLE
// ----------------------------------------------------
@Composable
fun MemorySequenceComposable(
  difficulty: Difficulty,
  onCompleted: () -> Unit,
  modifier: Modifier = Modifier
) {
  var sequence by remember { mutableStateOf(ChallengeGenerator.generateSequence(difficulty)) }
  var isShowingSequence by remember { mutableStateOf(true) }
  var countdownSeconds by remember { mutableIntStateOf(3) }
  var userEntered by remember { mutableStateOf("") }
  var isError by remember { mutableStateOf(false) }

  // Countdown timer to memorize
  LaunchedEffect(sequence) {
    isShowingSequence = true
    userEntered = ""
    isError = false
    countdownSeconds = when (difficulty) {
      Difficulty.EASY -> 3
      Difficulty.MEDIUM -> 4
      Difficulty.HARD -> 5
    }
    while (countdownSeconds > 0) {
      delay(1000)
      countdownSeconds--
    }
    isShowingSequence = false
  }

  fun checkInput(digit: Int) {
    isError = false
    val nextInput = userEntered + digit
    userEntered = nextInput
    val target = sequence.joinToString("")

    if (nextInput == target) {
      onCompleted()
    } else if (!target.startsWith(nextInput)) {
      // Mistake! Regenerate sequence
      isError = true
      sequence = ChallengeGenerator.generateSequence(difficulty)
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (isShowingSequence) {
      Text(
        text = "MEMORIZE IN $countdownSeconds SECONDS",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = EnergyAmber
      )
      Spacer(modifier = Modifier.height(16.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(12.dp)
      ) {
        sequence.forEach { num ->
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(ElectricViolet)
              .border(2.dp, NeonCyan, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = num.toString(),
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Black,
              color = TextPrimary
            )
          }
        }
      }
    } else {
      Text(
        text = "Enter the memorized sequence:",
        style = MaterialTheme.typography.titleMedium,
        color = TextSecondary
      )
      Spacer(modifier = Modifier.height(12.dp))

      // Dots/Display row
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 16.dp)
      ) {
        repeat(sequence.size) { index ->
          val filled = index < userEntered.length
          val char = if (filled) userEntered[index].toString() else "•"
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(if (filled) DarkSurfaceElevated else DarkSurface)
              .border(
                1.5.dp,
                if (isError) DangerRed else if (filled) NeonCyan else DarkSurfaceBorder,
                RoundedCornerShape(10.dp)
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = char,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = if (filled) NeonCyan else TextSecondary
            )
          }
        }
      }

      if (isError) {
        Text(
          text = "Wrong digit! New sequence generated.",
          color = DangerRed,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
      }

      KeypadGrid(
        onNumberClick = { checkInput(it) },
        onClearClick = { userEntered = "" },
        onSubmitClick = { }
      )
    }
  }
}

// ----------------------------------------------------
// 3. PATTERN RECOGNITION COMPOSABLE
// ----------------------------------------------------
@Composable
fun PatternChallengeComposable(
  difficulty: Difficulty,
  onCompleted: () -> Unit,
  modifier: Modifier = Modifier
) {
  var problem by remember { mutableStateOf(ChallengeGenerator.generatePattern(difficulty)) }
  var isError by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Find the missing element in sequence:",
      style = MaterialTheme.typography.titleMedium,
      color = TextSecondary
    )
    Spacer(modifier = Modifier.height(20.dp))

    // Sequence display row
    Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      problem.sequence.forEach { item ->
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .border(1.5.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = item,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
          )
        }
      }
      // Target placeholder box
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(ElectricViolet.copy(alpha = 0.3f))
          .border(2.dp, ElectricViolet, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "?",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Black,
          color = EnergyAmber
        )
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    if (isError) {
      Text(
        text = "Incorrect pattern! Try again.",
        color = DangerRed,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(12.dp))
    }

    Text(
      text = "Select your answer:",
      style = MaterialTheme.typography.bodyLarge,
      color = TextPrimary
    )
    Spacer(modifier = Modifier.height(16.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      problem.options.forEach { opt ->
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(2.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
            .clickable {
              if (opt == problem.correctAnswer) {
                onCompleted()
              } else {
                isError = true
                problem = ChallengeGenerator.generatePattern(difficulty)
              }
            }
            .testTag("pattern_option_$opt"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = opt,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = TextPrimary
          )
        }
      }
    }
  }
}

// ----------------------------------------------------
// 4. REACTION CHALLENGE COMPOSABLE
// ----------------------------------------------------
@Composable
fun ReactionChallengeComposable(
  difficulty: Difficulty,
  onCompleted: () -> Unit,
  modifier: Modifier = Modifier
) {
  var state by remember { mutableStateOf("WAIT") } // "WAIT", "READY", "EARLY", "SUCCESS"
  var message by remember { mutableStateOf("WAIT FOR GREEN...") }
  var reactionStartTime by remember { mutableLongStateOf(0L) }
  var measuredMs by remember { mutableLongStateOf(0L) }

  val maxAllowedMs = when (difficulty) {
    Difficulty.EASY -> 850L
    Difficulty.MEDIUM -> 650L
    Difficulty.HARD -> 480L
  }

  LaunchedEffect(state) {
    if (state == "WAIT" || state == "EARLY") {
      message = "WAIT FOR SIGNAL..."
      val waitTime = Random.nextLong(1800, 4200)
      delay(waitTime)
      reactionStartTime = System.currentTimeMillis()
      state = "READY"
      message = "TAP NOW!"
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Test your reflex speed",
      style = MaterialTheme.typography.titleMedium,
      color = TextSecondary
    )
    Spacer(modifier = Modifier.height(20.dp))

    val bgColor = when (state) {
      "READY" -> SuccessGreen
      "EARLY" -> DangerRed
      else -> DarkSurfaceElevated
    }

    val borderColor = when (state) {
      "READY" -> NeonCyan
      "EARLY" -> DangerRed
      else -> ElectricViolet
    }

    Box(
      modifier = Modifier
        .size(240.dp)
        .clip(CircleShape)
        .background(bgColor)
        .border(4.dp, borderColor, CircleShape)
        .clickable {
          if (state == "WAIT") {
            // Tapped too early
            state = "EARLY"
            message = "TOO EARLY! RESTARTING..."
          } else if (state == "READY") {
            val delta = System.currentTimeMillis() - reactionStartTime
            measuredMs = delta
            if (delta <= maxAllowedMs) {
              state = "SUCCESS"
              message = "CONQUERED! ${delta}ms"
              onCompleted()
            } else {
              state = "WAIT"
              message = "Too slow (${delta}ms > ${maxAllowedMs}ms). Try again!"
            }
          }
        }
        .testTag("reaction_tap_circle"),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = if (state == "READY") "TAP!" else if (state == "EARLY") "WAIT!" else "WAIT...",
          style = MaterialTheme.typography.displaySmall,
          fontWeight = FontWeight.Black,
          color = if (state == "READY") DeepNavy else TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = message,
          style = MaterialTheme.typography.bodyMedium,
          color = if (state == "READY") DeepNavy else TextSecondary,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
      }
    }
  }
}

// ----------------------------------------------------
// 5. QUICK TAP COMPOSABLE
// ----------------------------------------------------
@Composable
fun QuickTapComposable(
  difficulty: Difficulty,
  onCompleted: () -> Unit,
  modifier: Modifier = Modifier
) {
  val targetTotal = when (difficulty) {
    Difficulty.EASY -> 5
    Difficulty.MEDIUM -> 9
    Difficulty.HARD -> 14
  }

  var tapsDone by remember { mutableIntStateOf(0) }
  var orbOffsetX by remember { mutableFloatStateOf(0.5f) }
  var orbOffsetY by remember { mutableFloatStateOf(0.4f) }

  fun moveOrb() {
    orbOffsetX = Random.nextFloat().coerceIn(0.15f, 0.85f)
    orbOffsetY = Random.nextFloat().coerceIn(0.15f, 0.75f)
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Tap the energized orbs to deactivate alarm",
      style = MaterialTheme.typography.titleMedium,
      color = TextSecondary,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "TARGETS CLEARED: $tapsDone / $targetTotal",
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Black,
      color = NeonCyan
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Arena container
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(DarkSurfaceElevated)
        .border(1.5.dp, DarkSurfaceBorder, RoundedCornerShape(24.dp))
    ) {
      val orbSize = 64.dp
      val pxWidth = maxWidth - orbSize
      val pxHeight = maxHeight - orbSize

      Box(
        modifier = Modifier
          .offset(x = pxWidth * orbOffsetX, y = pxHeight * orbOffsetY)
          .size(orbSize)
          .clip(CircleShape)
          .background(ElectricViolet)
          .border(3.dp, NeonCyan, CircleShape)
          .clickable {
            tapsDone++
            if (tapsDone >= targetTotal) {
              onCompleted()
            } else {
              moveOrb()
            }
          }
          .testTag("quick_tap_orb"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Bolt,
          contentDescription = "Target Orb",
          tint = EnergyAmber,
          modifier = Modifier.size(36.dp)
        )
      }
    }
  }
}

// ----------------------------------------------------
// 6. MEMORY CARDS COMPOSABLE
// ----------------------------------------------------
@Composable
fun MemoryCardsComposable(
  difficulty: Difficulty,
  onCompleted: () -> Unit,
  modifier: Modifier = Modifier
) {
  val pairCount = when (difficulty) {
    Difficulty.EASY -> 2 // 4 cards
    Difficulty.MEDIUM -> 3 // 6 cards
    Difficulty.HARD -> 4 // 8 cards
  }

  val allIcons = listOf(
    Icons.Default.WbSunny,
    Icons.Default.Coffee,
    Icons.Default.Bolt,
    Icons.Default.Star,
    Icons.Default.NotificationsActive,
    Icons.Default.Psychology
  )

  data class CardState(val id: Int, val icon: ImageVector, var isFlipped: Boolean, var isMatched: Boolean)

  val cards = remember(difficulty) {
    val selectedIcons = allIcons.shuffled().take(pairCount)
    val cardList = mutableListOf<CardState>()
    selectedIcons.forEachIndexed { i, icon ->
      cardList.add(CardState(id = i * 2, icon = icon, isFlipped = false, isMatched = false))
      cardList.add(CardState(id = i * 2 + 1, icon = icon, isFlipped = false, isMatched = false))
    }
    cardList.shuffle()
    mutableStateListOf(*cardList.toTypedArray())
  }

  var selectedFirstIndex by remember { mutableStateOf<Int?>(null) }
  var isChecking by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Match all icon pairs to silence alarm",
      style = MaterialTheme.typography.titleMedium,
      color = TextSecondary,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))

    LazyVerticalGrid(
      columns = GridCells.Fixed(if (cards.size <= 4) 2 else if (cards.size <= 6) 3 else 4),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      itemsIndexed(cards) { index, card ->
        val isRevealed = card.isFlipped || card.isMatched
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (card.isMatched) SuccessGreen.copy(alpha = 0.2f) else if (isRevealed) ElectricViolet else DarkSurfaceElevated)
            .border(
              2.dp,
              if (card.isMatched) SuccessGreen else if (isRevealed) NeonCyan else DarkSurfaceBorder,
              RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isRevealed && !isChecking) {
              cards[index] = card.copy(isFlipped = true)
              if (selectedFirstIndex == null) {
                selectedFirstIndex = index
              } else {
                val firstIdx = selectedFirstIndex!!
                isChecking = true
                coroutineScope.launch {
                  delay(600)
                  if (cards[firstIdx].icon == cards[index].icon) {
                    cards[firstIdx] = cards[firstIdx].copy(isMatched = true)
                    cards[index] = cards[index].copy(isMatched = true)
                    // Check if all matched
                    if (cards.all { it.isMatched }) {
                      onCompleted()
                    }
                  } else {
                    cards[firstIdx] = cards[firstIdx].copy(isFlipped = false)
                    cards[index] = cards[index].copy(isFlipped = false)
                  }
                  selectedFirstIndex = null
                  isChecking = false
                }
              }
            }
            .testTag("memory_card_$index"),
          contentAlignment = Alignment.Center
        ) {
          if (isRevealed) {
            Icon(
              imageVector = card.icon,
              contentDescription = "Card icon",
              tint = if (card.isMatched) SuccessGreen else TextPrimary,
              modifier = Modifier.size(36.dp)
            )
          } else {
            Text(
              text = "?",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = TextSecondary
            )
          }
        }
      }
    }
  }
}

// ----------------------------------------------------
// 7. WORD SCRAMBLE COMPOSABLE
// ----------------------------------------------------
@Composable
fun WordScrambleComposable(
  difficulty: Difficulty,
  onCompleted: () -> Unit,
  modifier: Modifier = Modifier
) {
  var problem by remember { mutableStateOf(ChallengeGenerator.generateWordProblem(difficulty)) }
  var userWord by remember { mutableStateOf("") }
  var isError by remember { mutableStateOf(false) }

  // Available letter tiles
  val availableTiles = remember(problem) {
    problem.scrambled.filter { !it.isWhitespace() }.toList()
  }
  val usedIndices = remember { mutableStateListOf<Int>() }

  fun handleLetterClick(index: Int, char: Char) {
    if (usedIndices.contains(index)) return
    isError = false
    usedIndices.add(index)
    userWord += char

    if (userWord.length == problem.correctWord.length) {
      if (userWord.equals(problem.correctWord, ignoreCase = true)) {
        onCompleted()
      } else {
        isError = true
      }
    }
  }

  fun handleReset() {
    userWord = ""
    usedIndices.clear()
    isError = false
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Unscramble this awakening word:",
      style = MaterialTheme.typography.titleMedium,
      color = TextSecondary
    )
    Spacer(modifier = Modifier.height(16.dp))

    // Current Answer input
    Card(
      colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
      shape = RoundedCornerShape(18.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(if (isError) DangerRed else NeonCyan)),
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = if (userWord.isEmpty()) "Tap letters below" else userWord,
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Black,
          color = if (userWord.isEmpty()) TextSecondary else NeonCyan,
          letterSpacing = 4.sp
        )
        if (isError) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Incorrect! Tap reset to retry",
            color = DangerRed,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Letter buttons row
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(horizontal = 4.dp)
    ) {
      availableTiles.forEachIndexed { idx, char ->
        val isUsed = usedIndices.contains(idx)
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isUsed) DarkSurfaceBorder else ElectricViolet)
            .border(
              1.5.dp,
              if (isUsed) DarkSurfaceBorder else NeonCyan,
              RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isUsed) {
              handleLetterClick(idx, char)
            }
            .testTag("letter_tile_$idx"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = char.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = if (isUsed) TextDisabled else TextPrimary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      OutlinedButton(
        onClick = { handleReset() },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Reset Letters")
      }
    }
  }
}

// ----------------------------------------------------
// KEYPAD HELPER COMPOSABLE
// ----------------------------------------------------
@Composable
fun KeypadGrid(
  onNumberClick: (Int) -> Unit,
  onClearClick: () -> Unit,
  onSubmitClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val rows = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf("C", "0", "OK")
  )

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    rows.forEach { row ->
      Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        row.forEach { key ->
          Box(
            modifier = Modifier
              .size(width = 82.dp, height = 56.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(if (key == "OK") NeonCyan else if (key == "C") DarkSurfaceBorder else DarkSurfaceElevated)
              .border(
                1.5.dp,
                if (key == "OK") NeonCyan else DarkSurfaceBorder,
                RoundedCornerShape(16.dp)
              )
              .clickable {
                when (key) {
                  "C" -> onClearClick()
                  "OK" -> onSubmitClick()
                  else -> key.toIntOrNull()?.let { onNumberClick(it) }
                }
              }
              .testTag("keypad_$key"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = key,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = if (key == "OK") DeepNavy else TextPrimary
            )
          }
        }
      }
    }
  }
}
