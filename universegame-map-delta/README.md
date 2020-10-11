# UniverseGame Server Map-Delta

This server provides an api to receive and send map events via TCP.

### Map Events

Maps are defined fully by map events. All new events are passed to the connected clients.
Events can be sent and received in json format.

## Update client

To get past events the client can use the `!` command together with the last known event id
to get all events wich happend after the specified one.