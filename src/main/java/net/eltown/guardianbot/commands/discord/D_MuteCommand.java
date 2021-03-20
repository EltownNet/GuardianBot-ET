package net.eltown.guardianbot.commands.discord;

import net.eltown.guardianbot.Bot;
import net.eltown.guardianbot.components.data.Command;
import net.eltown.guardianbot.components.services.TimerService;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

public class D_MuteCommand extends Command {

    private final Bot bot;

    public D_MuteCommand(final Bot bot) {
        super("mute", new HashSet<>());
        this.bot = bot;
    }

    @Override
    public void execute(ExecuteData data, String[] args) {
        final User sender = data.getAuthor().asUser().get();
        final Server server = data.getMessage().getServer().get();
        if (server.hasPermission(sender, PermissionType.BAN_MEMBERS)) {
            if (args.length >= 2) {
                try {
                    final String userId = args[0];
                    final User user = this.bot.getDiscordApi().getUserById(userId).get();
                    CompletableFuture.runAsync(() -> this.bot.getGuardianAPI().isPunished(user.getIdAsString(), w -> {
                        if (!w) {

                            final int durationRaw = Integer.parseInt(args[1]);
                            final long duration = durationRaw * 3600000L;

                            final StringBuilder reasonB = new StringBuilder();
                            for (int i = 2; i < args.length; ++i) reasonB.append(args[i]).append(" ");
                            final String reason = reasonB.substring(0, reasonB.length() - 1);

                            this.bot.getGuardianAPI().setMuted(user, sender, server, reason, duration);

                            final EmbedBuilder embed = new EmbedBuilder()
                                    .setDescription("Der Nutzer **" + user.getIdAsString() + "** wurde gemutet. Ein Mute-Report wurde erstellt.")
                                    .setColor(Color.RED);
                            data.getChannel().sendMessage(embed).thenAccept(message -> {
                                final TimerService service = new TimerService(this.bot.getExecutorService());
                                service.run(message::delete, 20000);
                            });
                        } else {
                            final EmbedBuilder embed = new EmbedBuilder()
                                    .setDescription("Der Nutzer **" + user.getIdAsString() + "** ist bereits gemutet oder wurde nicht in der Datenbank gefunden.")
                                    .setColor(Color.RED);
                            data.getChannel().sendMessage(embed).thenAccept(message -> {
                                final TimerService service = new TimerService(this.bot.getExecutorService());
                                service.run(message::delete, 20000);
                            });
                        }
                    }));
                } catch (final Exception e) {
                    e.printStackTrace();
                    final EmbedBuilder embed = new EmbedBuilder()
                            .setDescription("Fehlerhafte Eingabe. [>mute <user> <duration> <reason>]")
                            .setColor(Color.RED);
                    data.getChannel().sendMessage(embed).thenAccept(message -> {
                        final TimerService service = new TimerService(this.bot.getExecutorService());
                        service.run(message::delete, 20000);
                    });
                }
            }
        }
    }
}
