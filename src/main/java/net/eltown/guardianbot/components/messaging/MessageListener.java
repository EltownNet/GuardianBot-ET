package net.eltown.guardianbot.components.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import lombok.SneakyThrows;
import net.eltown.guardianbot.Bot;
import net.eltown.guardianbot.components.data.CallData;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.nio.charset.StandardCharsets;

public class MessageListener {

    private final Bot guardian;
    private final Connection connection;
    private final Channel channel;

    @SneakyThrows
    public MessageListener(final Bot guardian) {
        this.guardian = guardian;

        final ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        this.connection = connectionFactory.newConnection("Guardian/Discord/Listener");

        this.channel = this.connection.createChannel();
        this.channel.queueDeclare("guardian_discord", false, false, false, null);

        this.startListening();
    }

    @SneakyThrows
    public void startListening() {
        final DeliverCallback deliverCallback = (tag, delivery) -> {
            final String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            final String[] rawCallData = message.split("//");
            final CallData callData = CallData.valueOf(rawCallData[0].toUpperCase());

            switch (callData) {
                case INITIATE_BAN:
                    final EmbedBuilder e = new EmbedBuilder()
                            .setTitle("**Ban-Report** " + rawCallData[1])
                            .setDescription("Der Spieler **" + rawCallData[2] + "** wurde mit dem Grund **" + rawCallData[3] + "** von **" + rawCallData[4] + "** gebannt.")
                            .setColor(Color.RED)
                            .setFooter("Verbleibend: " + this.guardian.getGuardianAPI().getRemainingTime(Long.parseLong(rawCallData[5])))
                            .setTimestampToNow();
                    this.guardian.getServerLogChannel().sendMessage(e);
                    break;
                case INITIATE_MUTE:
                    final EmbedBuilder f = new EmbedBuilder()
                            .setTitle("**Mute-Report** " + rawCallData[1])
                            .setDescription("Der Spieler **" + rawCallData[2] + "** wurde mit dem Grund **" + rawCallData[3] + "** von **" + rawCallData[4] + "** gemutet.")
                            .setColor(Color.RED)
                            .setFooter("Verbleibend: " + this.guardian.getGuardianAPI().getRemainingTime(Long.parseLong(rawCallData[5])))
                            .setTimestampToNow();
                    this.guardian.getServerLogChannel().sendMessage(f);
                    break;
                case CANCEL_BAN:
                    final EmbedBuilder u = new EmbedBuilder()
                            .setTitle("**Unban-Report** " + rawCallData[1])
                            .setDescription("Die Bestrafung von **" + rawCallData[3] + "** wurde mit dem Grund **" + rawCallData[4] + "** von **" + rawCallData[5] + "** aufgehoben.")
                            .setColor(Color.RED)
                            .setFooter(rawCallData[2])
                            .setTimestampToNow();
                    this.guardian.getServerLogChannel().sendMessage(u);
                    break;
                case CANCEL_MUTE:
                    final EmbedBuilder g = new EmbedBuilder()
                            .setTitle("**Unmute-Report** " + rawCallData[1])
                            .setDescription("Die Bestrafung von **" + rawCallData[3] + "** wurde mit dem Grund **" + rawCallData[4] + "** von **" + rawCallData[5] + "** aufgehoben.")
                            .setColor(Color.RED)
                            .setFooter(rawCallData[2])
                            .setTimestampToNow();
                    this.guardian.getServerLogChannel().sendMessage(g);
                    break;
            }
        };

        this.channel.basicConsume("guardian_discord", true, deliverCallback, consumerTag -> {
        });
    }

}
