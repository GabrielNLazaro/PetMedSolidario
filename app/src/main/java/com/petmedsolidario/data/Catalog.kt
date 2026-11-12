package com.petmedsolidario.data

data class Point(
    val id: String,
    val name: String,
    val district: String,
    val address: String,
    val hours: String
)

data class Medicine(
    val id: String,
    val name: String,
    val form: String,
    val strength: String,
    val units: Int,
    val point: Point
)

enum class ActivityType { RESERVATION, DONATION }

data class UserActivity(
    val id: String,
    val type: ActivityType,
    val title: String,
    val details: String,
    val medicineId: String? = null
)

val initialPoints = listOf(
    Point("p1", "Clínica Ipê", "Vila Verde", "Rua das Folhas, 18", "Segunda a sexta, 9h às 17h"),
    Point("p2", "Instituto Amora", "Jardim Sul", "Alameda Amora, 42", "Segunda a sábado, 8h às 14h")
)

val initialCatalog = listOf(
    Medicine("demo01", "Amostra Ipê", "Comprimidos", "10 mg", 12, initialPoints.first()),
    Medicine("demo02", "Amostra Amora", "Solução", "2 mg/mL", 4, initialPoints.last()),
    Medicine("demo03", "Amostra Cedro", "Cápsulas", "5 mg", 8, initialPoints.first())
)
