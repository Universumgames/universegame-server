package de.universegame.server.mapdelta.model

interface BlockUpdateEvent : MapEvent {
    val new_block: String
    val x: Int
    val y: Int
}