import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

target_start = "    val totalCattle = remember(cattleUnits) {"
target_end = """    val urgentCount = 3 + pendingRequests.size"""

replacement = """    val totalCattle = remember(cattleUnits) {
        cattleUnits.sumOf { it.headCount }
    }
    val totalBirds = remember(poultryUnits) {
        poultryUnits.sumOf { it.headCount }
    }
    val totalPoultryFlocks = remember(poultryUnits) {
        poultryUnits.size
    }

    // Milk Production Calculations
    val todayFormatted1 = remember { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date()) }
    val todayFormatted2 = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
    val todayFormatted3 = remember { java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date()) }

    val todayMilkLitres = remember(milkLogs) {
        val todayLogs = milkLogs.filter { log ->
            log.date.contains(todayFormatted1, ignoreCase = true) ||
            log.date.contains(todayFormatted2, ignoreCase = true) ||
            log.date.contains(todayFormatted3, ignoreCase = true) ||
            log.date.contains("Today", ignoreCase = true) ||
            log.loggedAt.contains(todayFormatted1, ignoreCase = true) ||
            log.loggedAt.contains(todayFormatted3, ignoreCase = true) ||
            log.loggedAt.contains("Today", ignoreCase = true)
        }
        todayLogs.sumOf { it.litres }
    }
    val weeklyMilkLitres = remember(milkLogs) {
        milkLogs.sumOf { it.litres }
    }

    // Egg Production Calculations
    val todayEggsCount = remember(eggLogs) {
        val todayLogs = eggLogs.filter { log ->
            log.loggedAt.contains(todayFormatted1, ignoreCase = true) ||
            log.loggedAt.contains(todayFormatted2, ignoreCase = true) ||
            log.loggedAt.contains(todayFormatted3, ignoreCase = true) ||
            log.loggedAt.contains("Today", ignoreCase = true)
        }
        todayLogs.sumOf { it.totalEggs }
    }
    val weeklyEggsCount = remember(eggLogs) {
        eggLogs.sumOf { it.totalEggs }
    }

    // Cattle Stage Breakdown Counts
    val cattleStages = remember(cattleUnits, milkLogs) {
        val stageCounts = mutableMapOf(
            "Milking" to 0,
            "In-calf" to 0,
            "Heifers" to 0,
            "Calves" to 0,
            "Bulls" to 0,
            "Dry" to 0,
            "Inseminated" to 0,
            "Disposed" to 0
        )
        val evaluated = cattleUnits.map { unit ->
            val mockDetail = com.example.ui.screens.AnimalDetailData(
                id = "unit_${unit.id}",
                name = unit.name,
                tagNumber = "#${unit.id}",
                breed = unit.breed,
                category = "CATTLE",
                status = unit.healthStatus,
                age = unit.dob,
                weight = unit.currentWeight,
                lastMilk = "",
                breedingStatus = "ACTIVE",
                dateOfBirth = unit.dob,
                weightAtBirth = unit.weightAtBirth,
                sire = unit.sire,
                dam = unit.dam
            )
            CattleLifecycleEngine.evaluateCattleStage(mockDetail, emptyList(), milkLogs)
        }
        stageCounts["Milking"] = evaluated.count { it.stage == CattleStage.MILKING }
        stageCounts["In-calf"] = evaluated.count { it.stage == CattleStage.INCALF || it.stage == CattleStage.INCALF_MILKING }
        stageCounts["Heifers"] = evaluated.count { it.stage == CattleStage.HEIFER }
        stageCounts["Calves"] = evaluated.count { it.stage == CattleStage.CALF }
        stageCounts["Bulls"] = evaluated.count { it.stage == CattleStage.BULL }
        stageCounts["Dry"] = evaluated.count { it.stage == CattleStage.DRY }
        stageCounts["Inseminated"] = evaluated.count { it.stage == CattleStage.INSEMINATED }
        stageCounts["Disposed"] = evaluated.count { it.stage == CattleStage.DISPOSED }
        stageCounts
    }

    // Finance Calculations
    val totalIncome = remember(financeRecords) {
        financeRecords.filter { it.type == FinanceType.INCOME }.sumOf { it.amount }
    }
    val milkIncome = remember(financeRecords) {
        financeRecords.filter { it.type == FinanceType.INCOME && it.category.contains("Milk", ignoreCase = true) }.sumOf { it.amount }
    }
    val eggIncome = remember(financeRecords) {
        financeRecords.filter { it.type == FinanceType.INCOME && it.category.contains("Egg", ignoreCase = true) }.sumOf { it.amount }
    }
    val feedExpense = remember(financeRecords) {
        financeRecords.filter { it.type == FinanceType.EXPENSE && it.category.contains("Feed", ignoreCase = true) }.sumOf { it.amount }
    }
    val vetExpense = remember(financeRecords) {
        financeRecords.filter { it.type == FinanceType.EXPENSE && (it.category.contains("Vet", ignoreCase = true) || it.category.contains("Vaccine", ignoreCase = true)) }.sumOf { it.amount }
    }
    val netRevenue = totalIncome - (feedExpense + vetExpense) // Wait, totalIncome includes both milk and egg.

    // Attention / Urgent items
    val pendingRequests = remember(employeeRequests) {
        employeeRequests.filter { it.status == RequestStatus.PENDING }
    }
    val urgentCount = pendingRequests.size"""

idx1 = content.find(target_start)
idx2 = content.find(target_end) + len(target_end)

if idx1 != -1 and content.find(target_end) != -1:
    new_content = content[:idx1] + replacement + content[idx2:]
    with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
        f.write(new_content)
    print("Successfully replaced.")
else:
    print("Could not find start or end index.")
