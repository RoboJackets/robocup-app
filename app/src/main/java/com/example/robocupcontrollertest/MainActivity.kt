
package com.example.robocupcontrollertest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.KeyEvent
import android.util.Log
import android.view.MotionEvent
import android.view.InputDevice
import android.os.Handler
import android.os.Looper
import android.hardware.input.InputManager
import android.content.Context
import java.util.concurrent.Executors
import androidx.compose.ui.platform.ComposeView
import androidx.compose.material3.MaterialTheme


class MainActivity : AppCompatActivity(), InputManager.InputDeviceListener {

    private val executor = Executors.newSingleThreadExecutor()
    private val udpSender = UDPSender()
    private val handler = Handler(Looper.getMainLooper())
    private var controllerDeviceId: Int? = null
    private lateinit var inputManager: InputManager
    private val controlLoop = object : Runnable {
        override fun run() {
            val movement = controllerState.movement

            executor.execute {
                try {
                    udpSender.sendMovement(movement, "Put your ip address", 5005)
                } catch (e: Exception) {
                    Log.e("xbox", "UDP send failed", e)
                }
            }

            handler.postDelayed(this, 50L)
        }
    }

    private val controllerInput = ControllerInput()
    private var controllerState = ControllerState(
        movement = MovementCommand(0f, 0f, 0f),
        aPressed = false,
        bPressed = false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 37 && checkSelfPermission("android.permission.ACCESS_LOCAL_NETWORK") != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf("android.permission.ACCESS_LOCAL_NETWORK"),
                100
            )
        }

        val composeView = ComposeView(this)

        composeView.setContent {
            MaterialTheme {
                DriverStationScreen()
            }
        }

        setContentView(composeView)

        inputManager = getSystemService(Context.INPUT_SERVICE) as InputManager
        Log.d("xbox", "Main: ${Thread.currentThread().name}")

        executor.execute {
            Log.d("xbox", "Background: ${Thread.currentThread().name}")
        }
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_A) {
            controllerDeviceId = event.deviceId
            controllerState = controllerState.copy(aPressed = true)
            Log.d("xbox", "A button pressed!")
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_A) {
            controllerDeviceId = event.deviceId
            controllerState = controllerState.copy(aPressed = false)
            Log.d("xbox", "A button released")
            return true
        }

        return super.onKeyUp(keyCode, event)

    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.isFromSource(InputDevice.SOURCE_JOYSTICK) && event.action == MotionEvent.ACTION_MOVE) {
            val x = event.getAxisValue(MotionEvent.AXIS_X)
            val y = event.getAxisValue(MotionEvent.AXIS_Y)
            val o = event.getAxisValue(MotionEvent.AXIS_RX)

            val movement = controllerInput.getMovement(x, y, o)

            controllerState = controllerState.copy(
                movement = movement
            )

            controllerDeviceId = event.deviceId

            Log.d("xbox", controllerState.movement.toString())
            return true
        }

        return super.onGenericMotionEvent(event)
    }

    override fun onResume() {
        super.onResume()
        handler.post(controlLoop)
        inputManager.registerInputDeviceListener(this, handler)
    }

    override fun onPause() {
        handler.removeCallbacks(controlLoop)

        resetControllerState()
        inputManager.unregisterInputDeviceListener(this)
        super.onPause()
    }

    override fun onInputDeviceAdded(deviceId: Int) {
        Log.d("xbox", "Device connected: $deviceId")
    }

    override fun onInputDeviceRemoved(deviceId: Int) {
        if (deviceId == controllerDeviceId) {
            resetControllerState()
            controllerDeviceId = null
            Log.d("xbox", "Controller disconnected — movement reset")
        }
    }

    override fun onInputDeviceChanged(deviceId: Int) {
        Log.d("xbox", "Device changed: $deviceId")
    }

    private fun resetControllerState() {
        controllerState = ControllerState(
            movement = MovementCommand(0f, 0f, 0f),
            aPressed = false,
            bPressed = false
        )
    }
}
