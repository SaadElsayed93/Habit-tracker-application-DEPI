package com.example.habittrackerapplication

data class Habit(
    var name: String,
    var description: String,
    var isCompleted: Boolean,
    val id: String // إضافة id لتحديد العادة بشكل فريد
)
