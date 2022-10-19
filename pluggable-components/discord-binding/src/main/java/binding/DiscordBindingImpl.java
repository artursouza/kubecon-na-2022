package binding;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;

import binding.Payload.User;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import io.dapr.v1.InputBindingGrpc.InputBindingImplBase;
import reactor.core.publisher.Mono;

public class DiscordBindingImpl extends InputBindingImplBase {

  private static final String DEFAULT_AVATAR_URL = "https://cdn.discordapp.com/embed/avatars/0.png";

  private final Logger log = LoggerFactory.getLogger(DiscordBindingImpl.class);

  private final ObjectMapper mapper = new ObjectMapper();

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
      log.info("Forwarding message: " + message.getContent());
      User author = new User();
      author.setName(message.getAuthor().map(user -> user.getUsername()).orElse("Unknown"));
      author.setPicture(message.getAuthor().map(user -> user.getAvatarUrl()).orElse(DEFAULT_AVATAR_URL));
      author.setScreenName(author.getName());

      Payload payload = new Payload();
      payload.setId(event.getMessage().getId().asString());
      payload.setFullText(message.getContent());
      payload.setText(message.getContent());
      payload.setLanguage("en_US");
      payload.setAuthor(author);

      try {
        responseObserver.onNext(io.dapr.v1.Bindings.ReadResponse.newBuilder()
            .setContentType("application/json")
            .setData(ByteString.copyFrom(mapper.writeValueAsBytes(payload))).build());
      } catch (JsonProcessingException e) {
        log.error("Could not generate payload JSON", e);
      }

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