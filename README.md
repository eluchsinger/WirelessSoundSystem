# WirelessSoundSystem.Server
This is the server software for the Wireless Sound System.

The server is written in Java using the JavaFX Framework.


#ToDo's
- Add Playlist controls (A new Control just for the playlist).
    - Create a playlist that plays sequential songs.
- Add Buttons NEXT and PREV.
- Create Speaker Model.
- Add Speakers to the ListView.
- Optional: Create a "Right Now" Info-View on the right side of the MainWindow.
- Develop "Discovery" Protocol.
- Implement Streaming protocol with NETTY.
- Use L4J for logging.

#To make it run
- When the client receives data, there could be an OutOfMemoryError. Run the JVM with the following parameter: -Xmx300m to get the maximum 300 MB Java Heap Memory.

#Copyright
Copyright © 2015 Esteban Luchsinger
