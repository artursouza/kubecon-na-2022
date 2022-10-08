package binding;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import io.dapr.v1.InputBindingGrpc.InputBindingImplBase;
import reactor.core.publisher.Mono;

public class DiscordBindingImpl extends InputBindingImplBase {

  private final Logger log = LoggerFactory.getLogger(DiscordBindingImpl.class);

  private AtomicReference<DiscordClient> client = new AtomicReference<>();

  @Override
  public void init(io.dapr.v1.Bindings.InputBindingInitRequest request,
      io.grpc.stub.StreamObserver<io.dapr.v1.Bindings.InputBindingInitResponse> responseObserver) {
    log.info("Called init");
    final String token = request.getMetadata().getPropertiesMap().getOrDefault("token", "");
    client.set(DiscordClient.create(token));
    responseObserver.onNext(io.dapr.v1.Bindings.InputBindingInitResponse.newBuilder().build());
    responseObserver.onCompleted();
  }

  @Override
  public void ping(io.dapr.v1.ComponentProtos.PingRequest request,
      io.grpc.stub.StreamObserver<io.dapr.v1.ComponentProtos.PingResponse> responseObserver) {
    responseObserver.onNext(io.dapr.v1.ComponentProtos.PingResponse.newBuilder().build());
    responseObserver.onCompleted();
  }

  @Override
  public io.grpc.stub.StreamObserver<io.dapr.v1.Bindings.ReadRequest> read(
      io.grpc.stub.StreamObserver<io.dapr.v1.Bindings.ReadResponse> responseObserver) {
    log.info("Called inputbinding read");
    this.client.get().withGateway(gateway -> gateway.on(MessageCreateEvent.class, event -> {
      Message message = event.getMessage();

      responseObserver.onNext(io.dapr.v1.Bindings.ReadResponse.newBuilder()
          .setContentType("text/plain")
          .setData(ByteString.copyFromUtf8(message.getContent())).build());

      return Mono.empty();
    })).block();

    return new io.grpc.stub.StreamObserver<io.dapr.v1.Bindings.ReadRequest>() {
      @Override
      public void onNext(io.dapr.v1.Bindings.ReadRequest value) {
        log.info("Called inputbinding onnext");
      }

      @Override
      public void onCompleted() {
        log.info("Called inputbinding completed");
        responseObserver.onCompleted();
      }

      @Override
      public void onError(Throwable t) {
        log.error("Error on read stream", t);
      }
    };
  }

}