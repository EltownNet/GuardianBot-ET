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

public class D_UnbanCommand extends Command {

    private final Bot bot;

    public D_UnbanCommand(final Bot bot) {
        super("unban", new HashSet<>());
        this.bot = bot;
    }

    @Override
    public void execute(ExecuteData data, String[] args) {
        final User sender = data.getAuthor().asUser().get();
        final Server server = data.getMessage().getServer().get();
        if (server.hasPermission(sender, PermissionType.ADMINISTRATOR)) {
            if (args.length >= 1) {
                final String userId = args[0];
                CompletableFuture.runAsync(() -> this.bot.getGuardianAPI().isPunished(userId, w -> {
                    if (w) {
                        final StringBuilder reasonB = new StringBuilder();
                        for (int i = 1; i < args.length; ++i) reasonB.append(args[i]).append(" ");
                        final String reason = reasonB.substring(0, reasonB.length() - 1);
                        this.bot.getGuardianAPI().unBan(userId, sender, server, reason);
                        final EmbedBuilder embed = new EmbedBuilder()
                                .setDescription("Die Bestrafung von **" + userId + "** wurde aufgehoben.")
                                .setColor(Color.RED);
                        data.getChannel().sendMessage(embed).thenAccept(message -> {
                            final TimerService service = new TimerService(this.bot.getExecutorService());
                            service.run(message::delete, 20000);
                        });
                    } else {
                        final EmbedBuilder embed = new EmbedBuilder()
                                .setDescription("Der Nutzer ist nicht gebannt oder wurde nicht gefunden.")
                                .setColor(Color.RED);
                        data.getChannel().sendMessage(embed).thenAccept(message -> {
                            final TimerService service = new TimerService(this.bot.getExecutorService());
                            service.run(message::delete, 20000);
                        });
                    }
                }));
            }
        }
    }

}
