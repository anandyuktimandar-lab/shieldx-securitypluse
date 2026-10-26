package com.example.ui.screens.learn

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LearningLesson
import com.example.ui.components.CyberCard
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LearningCenterScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }

  val lessons by viewModel.curriculum.collectAsState()
  val completedCount = lessons.count { it.isCompleted }
  val progress = if (lessons.isNotEmpty()) completedCount.toFloat() / lessons.size.toFloat() else 0f

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberBackground)
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp)
      .padding(bottom = 80.dp)
  ) {
    Spacer(modifier = Modifier.height(12.dp))

    // Top Bar
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.testTag("learning_center_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = TextPrimary
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(
          text = "Security Learning Center",
          color = TextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Defensive Cyber Hygiene & Practical Checklists",
          color = TextSecondary,
          fontSize = 12.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Curriculum Progress Card
    CyberCard(borderColor = NeonEmerald.copy(alpha = 0.4f), backgroundColor = Color(0xFF091F26)) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Curriculum Completion",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          Text(
            text = "$completedCount of ${lessons.size} Completed",
            color = NeonCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
          progress = { progress },
          color = NeonEmerald,
          trackColor = CyberBackground,
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Lessons List
    lessons.forEach { lesson ->
      LessonCard(
        lesson = lesson,
        onToggleComplete = { viewModel.toggleLesson(lesson.id) }
      )
      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@Composable
fun LessonCard(
  lesson: LearningLesson,
  onToggleComplete: () -> Unit
) {
  CyberCard(
    borderColor = if (lesson.isCompleted) AlertGreen.copy(alpha = 0.5f) else CyberBorder
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = lesson.category.uppercase(),
            color = NeonEmerald,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = lesson.title,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = lesson.readTime,
            color = TextMuted,
            fontSize = 11.sp
          )
        }

        IconButton(onClick = onToggleComplete) {
          Icon(
            imageVector = if (lesson.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (lesson.isCompleted) "Completed" else "Mark Complete",
            tint = if (lesson.isCompleted) AlertGreen else TextMuted,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = lesson.overview,
        color = TextSecondary,
        fontSize = 12.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Defensive Action Checklist:",
        color = NeonCyan,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(4.dp))

      lesson.checklist.forEach { item ->
        Row(
          modifier = Modifier.padding(vertical = 2.dp),
          verticalAlignment = Alignment.Top
        ) {
          Text(text = "🛡️", fontSize = 11.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = item,
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
        }
      }
    }
  }
}
