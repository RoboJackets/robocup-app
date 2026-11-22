package com.example.robocupcontrollertest
import kotlin.math.sqrt;

class ControllerInput {
    fun applyDeadzone(x: Float, y: Float): Pair<Float, Float> {
        val magnitude = sqrt(x * x + y * y)

        if (magnitude <= 0.1f) {
            return Pair(0f, 0f)
        }

        val clampedMagnitude = minOf(1f, magnitude)
        val rescaledMagnitude = (clampedMagnitude - 0.1f) / 0.9f

        val scale = rescaledMagnitude / magnitude

        return Pair(x * scale, y * scale)
    }

    fun getMovement(leftX: Float, leftY: Float, rightX: Float): MovementCommand {
        // Step 1: Apply dead zone to left joystick.
        val (xProcessed, yProcessed) = applyDeadzone(leftX, leftY)
        val (omegaProcessed, _) = applyDeadzone(rightX, 0f)

        val vx = -yProcessed
        val vy = xProcessed
        val omega = omegaProcessed

        return MovementCommand(vx, vy, omega)
    }
}