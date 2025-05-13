package com.example.habittrackerapplication

data class Habit(
    var name: String,
    var description: String,
    var isCompleted: Boolean,
    val id: String,
    var currentValue: Int = 0,
    var targetValue: Int = 1
)
