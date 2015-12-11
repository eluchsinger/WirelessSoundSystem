package ch.wirelesssoundsystem.server.controllers.networking.streaming.music;

import ch.wirelesssoundsystem.server.controllers.networking.NetworkStream;
import ch.wirelesssoundsystem.server.models.songs.Song;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.rtsp.RtspEncoder;
import io.netty.handler.codec.rtsp.RtspMethods;

import java.io.IOException;
import java.net.*;

/**
 * Created by Esteban Luchsinger on 03.12.2015.
 */
public class Mp3NetworkStream implements NetworkStream<Song> {
    private final static int CLIENT_SOCKET_PORT = 23546;
    private ServerSocket streamSocket;

    public Mp3NetworkStream() throws IOException {
        this.streamSocket = new ServerSocket(Mp3NetworkStream.CLIENT_SOCKET_PORT);
    }

    @Override
    public void startStream(Song song) {
    }

    @Override
    public void stopStream() {

    }
}
