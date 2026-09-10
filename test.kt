import java.util.*
import java.text.*

fun main() {
    val formats = arrayOf(
        "dd MMM yyyy, hh:mm a",
        "dd MMM yyyy, HH:mm",
        "dd MMM yyyy",
        "d MMM yyyy",
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "MM/dd/yyyy",
        "dd-MM-yyyy",
        "dd MMM, hh:mm a",
        "d MMM"
    )
    val clean = "08 Sep 2026"
    for (format in formats) {
        try {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            sdf.isLenient = false
            val date = sdf.parse(clean)
            if (date != null) {
                println("Parsed with $format: $date")
            }
        } catch (e: Exception) {}
    }
}
