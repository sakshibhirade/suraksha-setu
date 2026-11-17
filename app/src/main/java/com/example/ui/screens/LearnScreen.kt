package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AwarenessContentEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.repository.QuizAnswerResult
import com.example.ui.SurakshaViewModel
import com.example.ui.theme.LightTextBlack
import com.example.ui.theme.SurakshaTheme

@Composable
fun LearnScreen(
    viewModel: SurakshaViewModel,
    modifier: Modifier = Modifier
) {
    val colors = SurakshaTheme.colors
    val awarenessList by viewModel.awarenessList.collectAsState()
    val allQuestions by viewModel.allQuizQuestions.collectAsState()
    val quizState by viewModel.quizState.collectAsState()

    var isQuizModeActive by remember { mutableStateOf(false) }

    val activeQuestions = if (quizState.activeTopicFilter == null) {
        allQuestions
    } else {
        allQuestions.filter { it.topic.equals(quizState.activeTopicFilter, ignoreCase = true) }
    }.ifEmpty { allQuestions }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (!isQuizModeActive) {
            // Awareness & Defense Screen Header
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Awareness & Defense",
                    color = colors.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    maxLines = 1
                )
                Text(
                    text = "Learn safe digital habits and common trap patterns.",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Structured Quiz Hero Frame / Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.2.dp, colors.accentLime.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.softLime),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = "Quiz Icon",
                                tint = LightTextBlack,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Cyber Threat Quiz",
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                            Text(
                                text = "Test threat detection reflexes across real scam cases",
                                color = colors.textSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            viewModel.filterQuizByTopic(null)
                            isQuizModeActive = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accentLime,
                            contentColor = LightTextBlack
                        ),
                        modifier = Modifier.testTag("start_quiz_button")
                    ) {
                        Text(
                            text = "Take Quiz",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            // Awareness Lessons List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(awarenessList, key = { it.id }) { item ->
                    AwarenessCard(
                        item = item,
                        onTakeTopicQuiz = {
                            viewModel.filterQuizByTopic(item.topic)
                            isQuizModeActive = true
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        } else {
            // Interactive Quiz Mode Active
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Cyber Threat Quiz",
                        color = colors.textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        maxLines = 1
                    )
                    Text(
                        text = if (quizState.activeTopicFilter != null) "Topic: ${quizState.activeTopicFilter}" else "Interactive Fraud Scenario Practice",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                IconButton(onClick = { isQuizModeActive = false }) {
                    Icon(Icons.Default.Close, contentDescription = "Exit Quiz", tint = colors.textSecondary)
                }
            }

            if (activeQuestions.isNotEmpty()) {
                QuizInteractiveView(
                    questions = activeQuestions,
                    currentIndex = quizState.currentQuestionIndex,
                    selectedOptionIndex = quizState.selectedOptionIndex,
                    answerResult = quizState.answerResult,
                    score = quizState.score,
                    isCompleted = quizState.isCompleted,
                    onSelectOption = { viewModel.selectQuizOption(it) },
                    onSubmitAnswer = { question -> viewModel.submitQuizAnswer(question) },
                    onNextQuestion = { viewModel.nextQuizQuestion(activeQuestions.size) },
                    onReset = { viewModel.resetQuiz() },
                    onExitQuiz = { isQuizModeActive = false }
                )
            }
        }
    }
}

@Composable
private fun AwarenessCard(
    item: AwarenessContentEntity,
    onTakeTopicQuiz: () -> Unit
) {
    val colors = SurakshaTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.softSurface)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.topic.uppercase(),
                            color = colors.textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.title,
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = colors.textSecondary
                    )
                }
            }

            // Expanded Lesson Body
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = item.body,
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    // Key Defense Tips
                    if (item.keyTips.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (colors.isDark) colors.softSurface else colors.accentLime.copy(alpha = 0.2f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = colors.accentLime,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Key Defense Checklist",
                                    color = colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.keyTips,
                                    color = colors.textPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Topic Practice Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onTakeTopicQuiz,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.softSurface,
                                contentColor = colors.textPrimary
                            )
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test on this topic", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizInteractiveView(
    questions: List<QuizQuestionEntity>,
    currentIndex: Int,
    selectedOptionIndex: Int?,
    answerResult: QuizAnswerResult?,
    score: Int,
    isCompleted: Boolean,
    onSelectOption: (Int) -> Unit,
    onSubmitAnswer: (QuizQuestionEntity) -> Unit,
    onNextQuestion: () -> Unit,
    onReset: () -> Unit,
    onExitQuiz: () -> Unit
) {
    val colors = SurakshaTheme.colors

    if (isCompleted) {
        // Quiz Completion Results Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.5.dp, colors.accentLime, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(colors.softLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = LightTextBlack,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Quiz Completed!",
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "You scored $score out of ${questions.size}",
                    color = colors.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (score >= questions.size * 0.7) {
                        "Outstanding! You have strong cyber threat recognition instincts."
                    } else {
                        "Good effort! Review the awareness lessons to strengthen your defense against novel phishing traps."
                    },
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onReset,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.softSurface,
                            contentColor = colors.textPrimary
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onExitQuiz,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        )
                    ) {
                        Text("Finish", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val currentQuestion = questions.getOrNull(currentIndex) ?: return
    val options = remember(currentQuestion.optionsJson) {
        currentQuestion.optionsJson.split("|").filter { it.isNotBlank() }
    }
    val progress = (currentIndex + 1).toFloat() / questions.size.toFloat()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Progress Bar & Counter
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Question ${currentIndex + 1} of ${questions.size}",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Score: $score",
                    color = colors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = colors.accentLime,
                trackColor = colors.softSurface
            )
        }

        // Question Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Topic Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.softSurface)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = currentQuestion.topic.uppercase(),
                        color = colors.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = currentQuestion.question,
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 21.sp
                )

                // Options List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEachIndexed { index, option ->
                        val isSelected = selectedOptionIndex == index
                        val isCorrectOption = index == currentQuestion.correctIndex

                        val optionBg = when {
                            answerResult != null && isCorrectOption -> colors.severityLowContainer
                            answerResult != null && isSelected && !answerResult.isCorrect -> colors.severityHighContainer
                            isSelected -> (if (colors.isDark) colors.softSurface else colors.accentLime.copy(alpha = 0.25f))
                            else -> colors.softSurface
                        }

                        val optionBorder = when {
                            answerResult != null && isCorrectOption -> colors.severityLow
                            answerResult != null && isSelected && !answerResult.isCorrect -> colors.severityHigh
                            isSelected -> colors.accentLime
                            else -> colors.border.copy(alpha = 0.6f)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(optionBg)
                                .border(1.dp, optionBorder, RoundedCornerShape(12.dp))
                                .clickable(enabled = answerResult == null) { onSelectOption(index) }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) colors.primary else colors.surface)
                                        .border(1.dp, if (isSelected) colors.primary else colors.border, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ('A' + index).toString(),
                                        color = if (isSelected) colors.onPrimary else colors.textSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = option,
                                    color = colors.textPrimary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Explanation Banner (when answered)
                AnimatedVisibility(visible = answerResult != null) {
                    answerResult?.let { result ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (result.isCorrect) colors.severityLowContainer else colors.severityHighContainer)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (result.isCorrect) "✅ Correct!" else "❌ Incorrect",
                                color = if (result.isCorrect) colors.severityLow else colors.severityHigh,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = result.explanation,
                                color = colors.textPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Submit or Next CTA Button
                if (answerResult == null) {
                    Button(
                        onClick = { onSubmitAnswer(currentQuestion) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        enabled = selectedOptionIndex != null,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        )
                    ) {
                        Text("Confirm Answer", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onNextQuestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accentLime,
                            contentColor = LightTextBlack
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (currentIndex + 1 < questions.size) "Next Question" else "View Results",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
