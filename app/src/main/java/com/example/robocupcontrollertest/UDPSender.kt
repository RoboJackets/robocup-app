package com.example.robocupcontrollertest
import java.net.DatagramSocket
import java.net.DatagramPacket
import java.net.InetAddress

class UDPSender {
    private val socket = DatagramSocket()
    private var sequenceNumber = 0L

    fun sendMovement(movement: MovementCommand, ip: String, port: Int) {
        val message = "$sequenceNumber,${movement.vx},${movement.vy},${movement.omega}"
        val bytes = message.toByteArray(Charsets.UTF_8)
        val address = InetAddress.getByName(ip)

        val packet = DatagramPacket(bytes, bytes.size, address, port)

        socket.send(packet)
        sequenceNumber++
    }



    fun close() {
        socket.close()
    }
}