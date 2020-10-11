package de.universegame.server.mapdelta

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.universegame.server.mapdelta.model.MapEvent
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import redis.clients.jedis.Jedis
import java.net.InetSocketAddress
import kotlin.math.absoluteValue

// Event by id by chunk id
val chunkMap: MutableMap<Long, MutableList<MapEvent>> = mutableMapOf()
val outputs: MutableList<ByteWriteChannel> = mutableListOf()

fun main(args: Array<String>) {

    chunkMap[0] = mutableListOf()

    val jedis = Jedis("localhost")

    runBlocking {
        val server = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().bind(InetSocketAddress("127.0.0.1", 2323))
        println("Started echo telnet server at ${server.localAddress}")

        while (true) {
            val socket = server.accept()
            launch {
                println("Socket accepted: ${socket.remoteAddress}")

                val input = socket.openReadChannel()
                val output = socket.openWriteChannel(autoFlush = true)
                outputs.add(output)
                    while (true) {
                        try {

                            var lastId: Long = if (chunkMap[0]?.isNotEmpty()!!) chunkMap[0]?.last()?.id!!
                            else 0

                            // Blocking???
                            val line = input.readUTF8Line()

                            // Debug print
                            println(line)

                            if (line != null) {
                                // update
                                if (line.startsWith("!")) {
                                    val lastSentEvent = line.substring(1).toLong().absoluteValue
                                    if (lastSentEvent != lastId) {
                                        val missedEventCount : Long = lastId - lastSentEvent
                                        println("user missed $missedEventCount events")
                                        for (i in missedEventCount downTo 1) {
                                            val list = chunkMap[0] ?: throw RuntimeException("Nothign there")
                                            output.writeStringUtf8(jacksonObjectMapper().writeValueAsString(list[(list.size - i).toInt()]) + "\r\n")
                                        }
                                    }
                                } else {
                                    val incomingEvent: MapEvent = jacksonObjectMapper().readValue(line)
                                    if (chunkMap[0] == null) chunkMap[0] = mutableListOf()
                                    incomingEvent.id = lastId + 1
                                    chunkMap[0]?.add(incomingEvent)
                                    writeEvent(incomingEvent)
                                }
                            }
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
            }
        }
    }
}

suspend fun writeEvent(event: MapEvent) {
    outputs.forEach { output ->
        // TODO working with n?
        output.writeStringUtf8(jacksonObjectMapper().writeValueAsString(event) + "\r\n")
    }
}