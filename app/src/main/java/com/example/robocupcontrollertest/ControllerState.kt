package com.example.robocupcontrollertest

data class ControllerState(
    val movement: MovementCommand,
    val aPressed: Boolean,
    val bPressed: Boolean
)