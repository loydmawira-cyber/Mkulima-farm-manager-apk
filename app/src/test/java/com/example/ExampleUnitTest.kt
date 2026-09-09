package com.example

import com.example.data.FarmUnit
import com.example.util.FarmReminderEngine
import com.example.util.ReminderType
import com.example.utils.PoultryAgeAndVaccinationUtils
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExampleUnitTest {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testStarterFeedTransitionAlertInWeek8() {
        // Day 20 (Week 3) -> No transition alert
        val week3Stage = PoultryAgeAndVaccinationUtils.getFlockFeedStage(20)
        assertEquals("Starter Feed (Week 1 - 8)", week3Stage.stageName)
        assertFalse(week3Stage.hasTransitionAlert)
        assertNull(week3Stage.transitionAlertMessage)

        // Day 52 (Week 8) -> Has transition alert to introduce growers feed gradually
        val week8Stage = PoultryAgeAndVaccinationUtils.getFlockFeedStage(52)
        assertEquals("Starter Feed (Week 1 - 8)", week8Stage.stageName)
        assertTrue(week8Stage.hasTransitionAlert)
        assertNotNull(week8Stage.transitionAlertMessage)
        assertTrue(week8Stage.transitionAlertMessage!!.contains("introducing growers feed gradually", ignoreCase = true))
    }

    @Test
    fun testGrowerFeedTransitionAlertInWeek18() {
        // Day 80 (Week 12) -> No transition alert
        val week12Stage = PoultryAgeAndVaccinationUtils.getFlockFeedStage(80)
        assertEquals("Grower Feed (Week 9 - 18)", week12Stage.stageName)
        assertFalse(week12Stage.hasTransitionAlert)
        assertNull(week12Stage.transitionAlertMessage)

        // Day 122 (Week 18) -> Has transition alert to introduce layers feed gradually
        val week18Stage = PoultryAgeAndVaccinationUtils.getFlockFeedStage(122)
        assertEquals("Grower Feed (Week 9 - 18)", week18Stage.stageName)
        assertTrue(week18Stage.hasTransitionAlert)
        assertNotNull(week18Stage.transitionAlertMessage)
        assertTrue(week18Stage.transitionAlertMessage!!.contains("introducing layers feed gradually", ignoreCase = true))
    }

    @Test
    fun testFarmReminderEngineGeneratesFeedTransitionRemindersForFlocks() {
        // Create flock in Week 8 (53 days old)
        val cal8 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -53) }
        val dateAdded8 = dateFormat.format(cal8.time)
        val flockWeek8 = FarmUnit(
            id = 101L,
            name = "Broiler Batch A",
            type = "Poultry",
            headCount = 500,
            healthStatus = "Healthy",
            location = "Coop 1",
            lastUpdated = "Today",
            breed = "Broiler",
            dateAdded = dateAdded8,
            dob = dateAdded8
        )

        // Create flock in Week 18 (123 days old)
        val cal18 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -123) }
        val dateAdded18 = dateFormat.format(cal18.time)
        val flockWeek18 = FarmUnit(
            id = 102L,
            name = "Layer Flock 2",
            type = "Poultry",
            headCount = 300,
            healthStatus = "Healthy",
            location = "Coop 2",
            lastUpdated = "Today",
            breed = "Isa Brown",
            dateAdded = dateAdded18,
            dob = dateAdded18
        )

        val reminders = FarmReminderEngine.computeAllReminders(
            units = listOf(flockWeek8, flockWeek18),
            tasks = emptyList(),
            completedRuleKeys = emptyMap(),
            inventoryItems = emptyList()
        )

        val feedReminders = reminders.filter { it.type == ReminderType.FEED_TRANSITION }
        assertEquals(2, feedReminders.size)

        val growerReminder = feedReminders.find { it.unitId == 101L }
        assertNotNull(growerReminder)
        assertTrue(growerReminder!!.title.contains("Growers", ignoreCase = true))
        assertTrue(growerReminder.details.contains("introducing growers feed gradually", ignoreCase = true))

        val layerReminder = feedReminders.find { it.unitId == 102L }
        assertNotNull(layerReminder)
        assertTrue(layerReminder!!.title.contains("Layers", ignoreCase = true))
        assertTrue(layerReminder.details.contains("introducing layers feed gradually", ignoreCase = true))
    }
}
