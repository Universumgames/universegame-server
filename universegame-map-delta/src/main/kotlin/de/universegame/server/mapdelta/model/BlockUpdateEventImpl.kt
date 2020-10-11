package de.universegame.server.mapdelta.model

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("block_update")
class BlockUpdateEventImpl(
    override val new_block: String,
    override val x: Int,
    override val y: Int,
    override var id: Long?
) : BlockUpdateEvent