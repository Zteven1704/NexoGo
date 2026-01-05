package com.example.nexogo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexogo.model.Appointment
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentCalendar(
    appointments: List<Appointment>,
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    
    // Update current month when selected date changes
    LaunchedEffect(selectedDate) {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        currentMonth = calendar
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Calendar header with month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        currentMonth.add(Calendar.MONTH, -1)
                    }
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Mes anterior")
                }
                
                Text(
                    text = getMonthYearString(currentMonth),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = {
                        currentMonth.add(Calendar.MONTH, 1)
                    }
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Mes siguiente")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Days of week header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar grid
            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                appointments = appointments,
                onDateSelected = onDateSelected
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: Calendar,
    selectedDate: Date,
    appointments: List<Appointment>,
    onDateSelected: (Date) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.time = currentMonth.time
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // Calculate days to show (including previous month's trailing days)
    val totalCells = 42 // 6 weeks * 7 days
    val startOffset = firstDayOfWeek - 1
    
    val days = mutableListOf<CalendarDay>()
    
    // Add previous month's trailing days
    val prevMonth = Calendar.getInstance()
    prevMonth.time = currentMonth.time
    prevMonth.add(Calendar.MONTH, -1)
    val daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    for (i in startOffset - 1 downTo 0) {
        val day = daysInPrevMonth - i
        val date = Calendar.getInstance().apply {
            time = prevMonth.time
            set(Calendar.DAY_OF_MONTH, day)
        }.time
        
        days.add(CalendarDay(day, date, false, false))
    }
    
    // Add current month's days
    for (day in 1..daysInMonth) {
        val date = Calendar.getInstance().apply {
            time = currentMonth.time
            set(Calendar.DAY_OF_MONTH, day)
        }.time
        
        val isSelected = isSameDay(date, selectedDate)
        val hasAppointments = hasAppointmentsOnDate(date, appointments)
        
        days.add(CalendarDay(day, date, isSelected, hasAppointments))
    }
    
    // Add next month's leading days
    val remainingDays = totalCells - days.size
    val nextMonth = Calendar.getInstance()
    nextMonth.time = currentMonth.time
    nextMonth.add(Calendar.MONTH, 1)
    
    for (day in 1..remainingDays) {
        val date = Calendar.getInstance().apply {
            time = nextMonth.time
            set(Calendar.DAY_OF_MONTH, day)
        }.time
        
        days.add(CalendarDay(day, date, false, false))
    }
    
    // Create 6 rows of 7 days each
    Column(
        modifier = Modifier.height(300.dp)
    ) {
        // Ensure we have exactly 42 days (6 weeks * 7 days)
        val paddedDays = if (days.size < 42) {
            days + List(42 - days.size) { 
                CalendarDay(0, Date(), false, false) 
            }
        } else {
            days.take(42)
        }
        
        paddedDays.chunked(7).forEach { weekDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekDays.forEach { day ->
                    if (day.dayNumber > 0) {
                        CalendarDayItem(
                            day = day,
                            onClick = { onDateSelected(day.date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayItem(
    day: CalendarDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentMonth = isCurrentMonth(day.date)
    val isToday = isToday(day.date)
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    day.isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (day.hasAppointments) 2.dp else 0.dp,
                color = if (day.hasAppointments) MaterialTheme.colorScheme.secondary else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.dayNumber.toString(),
                fontSize = 14.sp,
                fontWeight = if (day.isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    day.isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            if (day.hasAppointments) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }
        }
    }
}

private fun getMonthYearString(calendar: Calendar): String {
    val months = arrayOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    val month = months[calendar.get(Calendar.MONTH)]
    val year = calendar.get(Calendar.YEAR)
    return "$month $year"
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
           cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}

private fun isToday(date: Date): Boolean {
    val today = Calendar.getInstance()
    val dateCal = Calendar.getInstance().apply { time = date }
    return isSameDay(today.time, dateCal.time)
}

private fun isCurrentMonth(date: Date): Boolean {
    val today = Calendar.getInstance()
    val dateCal = Calendar.getInstance().apply { time = date }
    return today.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
           today.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH)
}

private fun hasAppointmentsOnDate(date: Date, appointments: List<Appointment>): Boolean {
    return try {
        appointments.any { appointment ->
            try {
                val appointmentDate = appointment.dateTime.toDate()
                isSameDay(appointmentDate, date)
            } catch (e: Exception) {
                false
            }
        }
    } catch (e: Exception) {
        false
    }
}

private data class CalendarDay(
    val dayNumber: Int,
    val date: Date,
    val isSelected: Boolean,
    val hasAppointments: Boolean
)
