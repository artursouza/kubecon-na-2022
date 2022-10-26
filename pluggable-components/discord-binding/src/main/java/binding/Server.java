package binding;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;

public class Server {

    private static final String SOCKET_PATH = "/tmp/dapr-components-sockets/discord.sock";

    public static void main(String[] args) throws Exception {
        final Logger log = LoggerFactory.getLogger(Server.class);

        log.info("Starting server listening on " + SOCKET_PATH + " ...");
        // If file exists, remove it.
        final File unixSocketFile = new File(SOCKET_PATH);
        if (unixSocketFile.exists()) {
            log.warn("Unix Socket Descriptor in [" + SOCKET_PATH + "] already exists. "
                    + "Removing it to recreate it.");
            Files.deleteIfExists(unixSocketFile.toPath());
        }
        unixSocketFile.deleteOnExit();

        final DomainSocketAddress unixSocket = new DomainSocketAddress(SOCKET_PATH);
        final EventLoopGroup eventLoopGroup;
        final Class<? extends ServerChannel> serverChannelClass;
        if (KQueue.isAvailable()) {
            log.info("Using KQueue");
            eventLoopGroup = new KQueueEventLoopGroup();
            serverChannelClass = KQueueServerDomainSocketChannel.class;
        } else {
            log.info("Using Epoll");
            eventLoopGroup = new EpollEventLoopGroup();
            serverChannelClass = EpollServerDomainSocketChannel.class;
        }

        io.grpc.Server server = NettyServerBuilder.forAddress(unixSocket)
                .channelType(serverChannelClass)
                .workerEventLoopGroup(eventLoopGroup)
                .bossEventLoopGroup(eventLoopGroup)
                .addService(new DiscordBindingImpl())
                .addService(ProtoReflectionService.newInstance())
                .keepAliveTime(1, TimeUnit.MINUTES)
                .build();

        server.start();
        log.info("Started server listening on " + SOCKET_PATH);
        updateSocketPermissionInBackground(log);
        server.awaitTermination();
    }

    private static void updateSocketPermissionInBackground(Logger log) {
        new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    final File unixSocketFile = new File(SOCKET_PATH);
                    if (unixSocketFile.exists()) {
                        boolean success = true;
                        if (!unixSocketFile.setReadable(true, false)) {
                            log.error("Failed to set socket readable");
                            success = false;
                        }
                        if (!unixSocketFile.setWritable(true, false)) {
                            log.error("Failed to set socket writable");
                            success = false;
                        }
                        if (success) {
                            log.info("Successfully set socket permissions on " + SOCKET_PATH);
                            return;
                        }
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Interrupted", e);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }).start();
    }
}
