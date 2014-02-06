package k.core.util.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.LinkedList;

class TelnetClient extends SimpleChannelInboundHandler<String> implements
        PacketSender {
    private LinkedList<Packet> send = new LinkedList<Packet>();
    NetHandlerClient client = null;
    private ChannelHandlerContext mostRecent = null;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        mostRecent = ctx;
        System.err.println("accepted connection on client!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request)
            throws Exception {
        mostRecent = ctx;
        client.addPacketToRecvQueue(Packet.fromData(request));
        client.processQueue(100);
        writePackets();
    }

    public void writePackets() {
        while (!send.isEmpty()) {
            mostRecent.writeAndFlush(Packet.toData(send.poll()) + "\r\n")
                    .syncUninterruptibly();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        mostRecent = ctx;
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        mostRecent = ctx;
        System.err.println("EXCEPTION: ");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public boolean sendPacket(Packet p) {
        send.addLast(p);
        writePackets();
        return true;
    }
}