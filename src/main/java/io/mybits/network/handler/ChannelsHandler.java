package io.mybits.network.handler;

import io.mybits.network.Network;
import io.mybits.protect.Serials;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChannelsHandler extends ChannelInboundHandlerAdapter implements ChannelHandler {

    private final Network network;

    public ChannelsHandler(Network network) {
        this.network = network;
    }

    @Override
    public void channelActive(final @NotNull ChannelHandlerContext ctx) {
        List<String> message = new ArrayList<>();
        message.add("0");
        message.add(Serials.serial());
        network.getSerials().put(ctx.channel(), new ArrayList<>());
        network.getSerials().get(ctx.channel()).add(message.get(1));
        ctx.channel().writeAndFlush(message);

    }

    @Override
    public void handlerRemoved(final @NotNull ChannelHandlerContext ctx) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, @NotNull Throwable cause) {

    }
}