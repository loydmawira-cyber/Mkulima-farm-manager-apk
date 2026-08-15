import re

with open("app/src/main/java/com/example/ui/screens/MilkLogScreen.kt", "r") as f:
    content = f.read()

target_start = "                        // Dynamic Totals and Line Chart Calculation based on overallTimeframe and real milkLogs"
target_end = """                                    else -> {
                                        val points = milkLogs.take(7).map { it.litres.toFloat() }
                                        val labels = milkLogs.take(7).map { it.date }
                                        val totalL = milkLogs.sumOf { it.litres }
                                        Triple(
                                            MilkTotalsSummary("%.1f L".format(totalL), "%.1f L/day".format(totalL / 7), "Herd", "Overview"),
                                            points,
                                            labels
                                        )
                                    }
                                }
                            }
                        }"""

replacement = """                        // Dynamic Totals and Line Chart Calculation based on overallTimeframe, selectedHerdMonth, and selectedHerdYear
                        val (summary, chartData, xLabels) = remember(milkLogs, overallTimeframe, selectedHerdMonth, selectedHerdYear) {
                            if (milkLogs.isEmpty()) {
                                Triple(
                                    MilkTotalsSummary("0.0 L", "0.0 L/day", "No records", "0 logs recorded"),
                                    emptyList<Float>(),
                                    listOf("No Data")
                                )
                            } else {
                                val nowCal = java.util.Calendar.getInstance()
                                val cMonth = nowCal.get(java.util.Calendar.MONTH)
                                val cYear = nowCal.get(java.util.Calendar.YEAR)
                                val cDayOfYear = nowCal.get(java.util.Calendar.DAY_OF_YEAR)
                                
                                val targetMonthIdx = monthsList.indexOfFirst { it.equals(selectedHerdMonth, ignoreCase = true) }
                                val targetYearInt = selectedHerdYear.toIntOrNull() ?: cYear
                                val shortMonthLabel = if (targetMonthIdx >= 0) {
                                    val cal = java.util.Calendar.getInstance().apply { set(java.util.Calendar.MONTH, targetMonthIdx) }
                                    java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(cal.time)
                                } else selectedHerdMonth.take(3)

                                when (overallTimeframe) {
                                    "TODAY" -> {
                                        val todayLogs = milkLogs.filter { log ->
                                            val c = parseMilkLogCalendar(log.date)
                                            (c != null && c.get(java.util.Calendar.YEAR) == cYear && c.get(java.util.Calendar.DAY_OF_YEAR) == cDayOfYear) ||
                                                    log.date.equals(todayDateStr, ignoreCase = true) ||
                                                    log.date.contains("Today", ignoreCase = true)
                                        }
                                        val targetLogs = if (todayLogs.isNotEmpty()) todayLogs else {
                                            val latestDate = milkLogs.firstOrNull()?.date ?: todayDateStr
                                            milkLogs.filter { it.date == latestDate }
                                        }
                                        val morningLogs = targetLogs.filter { it.session.contains("Morning", ignoreCase = true) || it.session.contains("AM", ignoreCase = true) }
                                        val afternoonLogs = targetLogs.filter { it.session.contains("Afternoon", ignoreCase = true) || it.session.contains("Midday", ignoreCase = true) || it.session.contains("Noon", ignoreCase = true) }
                                        val eveningLogs = targetLogs.filter { it.session.contains("Evening", ignoreCase = true) || it.session.contains("Night", ignoreCase = true) || (it.session.contains("PM", ignoreCase = true) && !it.session.contains("Afternoon", ignoreCase = true)) }
                                        
                                        val morningL = morningLogs.sumOf { it.litres }.toFloat()
                                        val afternoonL = afternoonLogs.sumOf { it.litres }.toFloat()
                                        val eveningL = eveningLogs.sumOf { it.litres }.toFloat()
                                        
                                        val totalL = targetLogs.sumOf { it.litres }
                                        val topCowName = targetLogs.groupBy { it.cowName }.maxByOrNull { entry -> entry.value.sumOf { it.litres } }?.let { "${it.key} (${"%.1f".format(it.value.sumOf { it.litres })}L)" } ?: "Herd"
                                        
                                        Triple(
                                            MilkTotalsSummary(
                                                totalLitres = "%.1f L".format(totalL),
                                                avgPerDay = "%.1f L/day".format(totalL),
                                                topCow = topCowName,
                                                trendStr = "AM: %.1fL • Mid: %.1fL • PM: %.1fL".format(morningL, afternoonL, eveningL)
                                            ),
                                            listOf(morningL, afternoonL, eveningL),
                                            listOf("Morning (AM)", "Midday (Noon)", "Evening (PM)")
                                        )
                                    }
                                    "MONTH" -> {
                                        val monthLogs = milkLogs.filter { log ->
                                            val c = parseMilkLogCalendar(log.date)
                                            if (c != null) {
                                                c.get(java.util.Calendar.MONTH) == targetMonthIdx && c.get(java.util.Calendar.YEAR) == targetYearInt
                                            } else {
                                                (log.date.contains(selectedHerdMonth, ignoreCase = true) || log.date.contains(shortMonthLabel, ignoreCase = true)) &&
                                                        (log.date.contains(targetYearInt.toString()) || log.loggedAt.contains(targetYearInt.toString()))
                                            }
                                        }
                                        val w1Logs = monthLogs.filter { (parseMilkLogCalendar(it.date)?.get(java.util.Calendar.DAY_OF_MONTH) ?: 1) in 1..7 }
                                        val w2Logs = monthLogs.filter { (parseMilkLogCalendar(it.date)?.get(java.util.Calendar.DAY_OF_MONTH) ?: 8) in 8..14 }
                                        val w3Logs = monthLogs.filter { (parseMilkLogCalendar(it.date)?.get(java.util.Calendar.DAY_OF_MONTH) ?: 15) in 15..21 }
                                        val w4Logs = monthLogs.filter { (parseMilkLogCalendar(it.date)?.get(java.util.Calendar.DAY_OF_MONTH) ?: 22) >= 22 }
                                        
                                        val w1 = w1Logs.sumOf { it.litres }.toFloat()
                                        val w2 = w2Logs.sumOf { it.litres }.toFloat()
                                        val w3 = w3Logs.sumOf { it.litres }.toFloat()
                                        val w4 = w4Logs.sumOf { it.litres }.toFloat()
                                        
                                        val totalL = monthLogs.sumOf { it.litres }
                                        val distinctDays = monthLogs.map { it.date }.distinct().size.coerceAtLeast(1)
                                        val dailyAvg = if (monthLogs.isNotEmpty()) totalL / distinctDays.toDouble() else 0.0
                                        val topCowName = monthLogs.groupBy { it.cowName }.maxByOrNull { it.value.sumOf { it.litres } }?.let { "${it.key} (${"%.1f".format(it.value.sumOf { it.litres })}L)" } ?: "Herd"
                                        
                                        Triple(
                                            MilkTotalsSummary(
                                                totalLitres = "%.1f L".format(totalL),
                                                avgPerDay = "%.1f L/day".format(dailyAvg),
                                                topCow = topCowName,
                                                trendStr = "$selectedHerdMonth $targetYearInt: ${monthLogs.size} logs across $distinctDays days"
                                            ),
                                            listOf(w1, w2, w3, w4),
                                            listOf("1-7 $shortMonthLabel", "8-14 $shortMonthLabel", "15-21 $shortMonthLabel", "22+ $shortMonthLabel")
                                        )
                                    }
                                    "YEAR" -> {
                                        val yearLogs = milkLogs.filter { log ->
                                            val c = parseMilkLogCalendar(log.date)
                                            if (c != null) {
                                                c.get(java.util.Calendar.YEAR) == targetYearInt
                                            } else {
                                                log.date.contains(targetYearInt.toString()) || log.loggedAt.contains(targetYearInt.toString())
                                            }
                                        }
                                        val allMonthsData = (0..11).map { mIdx ->
                                            val cal = java.util.Calendar.getInstance().apply {
                                                set(java.util.Calendar.YEAR, targetYearInt)
                                                set(java.util.Calendar.MONTH, mIdx)
                                            }
                                            val monthLabel = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(cal.time)
                                            val logsForMonth = yearLogs.filter { log ->
                                                val c = parseMilkLogCalendar(log.date)
                                                if (c != null) {
                                                    c.get(java.util.Calendar.MONTH) == mIdx && c.get(java.util.Calendar.YEAR) == targetYearInt
                                                } else {
                                                    log.date.contains(monthLabel, ignoreCase = true)
                                                }
                                            }
                                            val sumL = logsForMonth.sumOf { it.litres }.toFloat()
                                            monthLabel to sumL
                                        }
                                        val labels = allMonthsData.map { it.first }
                                        val points = allMonthsData.map { it.second }
                                        val totalL = yearLogs.sumOf { it.litres }
                                        val distinctDays = yearLogs.map { it.date }.distinct().size.coerceAtLeast(1)
                                        val dailyAvg = if (yearLogs.isNotEmpty()) totalL / distinctDays.toDouble() else 0.0
                                        val topCowName = yearLogs.groupBy { it.cowName }.maxByOrNull { it.value.sumOf { it.litres } }?.let { "${it.key} (${"%.1f".format(it.value.sumOf { it.litres })}L)" } ?: "Herd"
                                        
                                        Triple(
                                            MilkTotalsSummary(
                                                totalLitres = "%.1f L".format(totalL),
                                                avgPerDay = "%.1f L/day".format(dailyAvg),
                                                topCow = topCowName,
                                                trendStr = "Annual Total $targetYearInt: ${yearLogs.size} logs across $distinctDays days"
                                            ),
                                            points,
                                            labels
                                        )
                                    }
                                    else -> {
                                        val points = milkLogs.take(7).map { it.litres.toFloat() }
                                        val labels = milkLogs.take(7).map { it.date }
                                        val totalL = milkLogs.sumOf { it.litres }
                                        Triple(
                                            MilkTotalsSummary("%.1f L".format(totalL), "%.1f L/day".format(totalL / 7), "Herd", "Overview"),
                                            points,
                                            labels
                                        )
                                    }
                                }
                            }
                        }"""

idx1 = content.find(target_start)
idx2 = content.find(target_end) + len(target_end)

if idx1 != -1 and content.find(target_end) != -1:
    new_content = content[:idx1] + replacement + content[idx2:]
    with open("app/src/main/java/com/example/ui/screens/MilkLogScreen.kt", "w") as f:
        f.write(new_content)
    print("Successfully replaced.")
else:
    print("Could not find start or end index.")
