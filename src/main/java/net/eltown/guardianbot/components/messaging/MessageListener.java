package net.eltown.guardianbot.components.messaging;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.eltown.guardianbot.Bot;
import net.eltown.guardianbot.components.data.CallData;
import net.eltown.guardianbot.components.tinyrabbit.TinyRabbitListener;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;

@RequiredArgsConstructor
public class MessageListener {

    private final Bot guardian;

    @SneakyThrows
    public void startListening() {
        final TinyRabbitListener listener = new TinyRabbitListener("localhost");
        listener.receive((request -> {
            switch (CallData.valueOf(request.getKey().toUpperCase())) {
                case REQUEST_INITIATE_BAN:
                    final EmbedBuilder e = new EmbedBuilder()
                            .setTitle("**Ban-Report** " + request.getData()[1])
                            .setDescription("Der Spieler **" + request.getData()[2] + "** wurde mit dem Grund **" + request.getData()[3] + "** von **" + request.getData()[4] + "** gebannt.")
                            .setColor(Color.RED)
                            .setFooter("Verbleibend: " + this.guardian.getGuardianAPI().getRemainingTime(Long.parseLong(request.getData()[5])))
                            .setTimestampToNow();
                    this.guardian.getServerLogChannel().sendMessage(e);
                    break;
                case REQUEST_INITIATE_MUTE:
                    final EmbedBuilder f = new EmbedBuilder()
                            .setTitle("**Mute-Report** " + request.getData()[1])
                            .setDescription("Der Spieler **" + request.getData()[2] + "** wurde mit dem Grund **" + request.getData()[3] + "** von **" + request.getData()[4] + "** gemutet.")
                            .setColor(Color.RED)
                            .setFooter("Verbleibend: " + this.guardian.getGuardianAPI().getRemainingTime(Long.parseLong(request.getData()[5])))
                            .setTimestampToNow();
                    this.guardian.getServerLogChannel().sendMessage(f);
                    break;
                case REQUEST_CANCEL_BAN:
                    final EmbedBuilder u = new EmbedBuilder()
                            .setTitle("**Unban-Report** " + request.getData()[1])
                            .setDescription("Die Bestrafung von **" + request.getData()[3] + "** wurde mit dem Grund **" + request.getData()[4] + "** von **" + request.getData()[5] + "** aufgehoben.")
                            .setColor(Color.RED)
                            .setFooter(request.getData()[2])
                            .setTimestampToNow();
                    this.guardian.getServerLogChannel().sendMessage(u);
                    break;
                case REQUEST_CANCEL_MUTE:
                    final EmbedBuilder g = new EmbedBuilder()
                            .setTitle("**Unmute-Report** " + request.getData()[1])
                            .setDescription("Die Bestrafung von **" + request.getData()[3] + "** wurde mit dem Grund **" + request.getData()[4] + "** von **" + request.getData()[5] + "** aufgehoben.")
                            .setColor(Color.RED)
                            .setFooter(request.getData()[2])
                            .setTimestampToNow();
                    this.guardian.getServerLogChannel().sendMessage(g);
                    break;
            }
        }), "Guardian/Discord/Listener", "guardian_discord");
    }
}
