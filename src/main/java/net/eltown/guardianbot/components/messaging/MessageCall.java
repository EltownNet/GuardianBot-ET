package net.eltown.guardianbot.components.messaging;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.SneakyThrows;
import net.eltown.guardianbot.Bot;
import net.eltown.guardianbot.components.data.CallData;
import net.eltown.guardianbot.components.data.CallbackData;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;

public class MessageCall {

    private final Bot guardian;
    private final Connection connection;
    private final Channel channel;

    @SneakyThrows
    public MessageCall(final Bot guardian) {
        this.guardian = guardian;

        final ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        this.connection = connectionFactory.newConnection("Guardian/Discord/Call");

        this.channel = this.connection.createChannel();
    }

    @SneakyThrows
    public void createCall(final BiConsumer<CallbackData, String[]> callBack, final CallData callData, final String... args) {
        final String corrId = UUID.randomUUID().toString();
        final StringBuilder callBuilder = new StringBuilder(callData.name().toLowerCase() + "//");
        for (String arg : args) {
            callBuilder.append(arg).append("//");
        }
        final String call = callBuilder.substring(0, callBuilder.length() - 2);

        final String replyQueueName = this.channel.queueDeclare().getQueue();
        final AMQP.BasicProperties properties = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        this.channel.basicPublish("", "guardian", properties, call.getBytes(StandardCharsets.UTF_8));

        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

        final String tag = this.channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.offer(new String(delivery.getBody(), StandardCharsets.UTF_8));
            }
        }, consumerTag -> {
        });

        final String result = response.take();
        this.channel.basicCancel(tag);
        callBack.accept(CallbackData.valueOf(result.toUpperCase().split("//")[0]), result.split("//"));
    }

}