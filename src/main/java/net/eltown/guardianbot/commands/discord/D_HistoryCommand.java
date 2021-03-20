package net.eltown.guardianbot.commands.discord;

import net.eltown.guardianbot.Bot;
import net.eltown.guardianbot.components.data.Command;
import net.eltown.guardianbot.components.data.StoredPunishData;
import net.eltown.guardianbot.components.services.TimerService;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

public class D_HistoryCommand extends Command {

    private final Bot bot;

    public D_HistoryCommand(final Bot bot) {
        super("history", new HashSet<>());
        this.bot = bot;
    }

    @Override
    public void execute(ExecuteData data, String[] args) {
        final User sender = data.getAuthor().asUser().get();
        final Server server = data.getMessage().getServer().get();
        if (server.hasPermission(sender, PermissionType.ADMINISTRATOR)) {
            if (args.length >= 1) {
                CompletableFuture.runAsync(() -> {
                    final String user = args[0];
                    this.bot.getGuardianAPI().isConspicuous(user, w -> {
                        if (w) {
                            this.bot.getGuardianAPI().getStoredPunishData(user, storedPunishData -> {
                                final StringBuilder reasonB = new StringBuilder();
                                for (int i = 1; i < args.length; ++i) reasonB.append(args[i]).append(" ");
                                final String reason = reasonB.substring(0, reasonB.length() - 1);

                                sender.sendMessage("**History Auszug von** " + user + "\n**Datensätze:** " + storedPunishData.size());
                                //sender.sendMessage(new EmbedBuilder().setTitle("History Auszug von " + user).setDescription("Es wurden **" + storedPunishData.size() + " Einträge** gefunden.").setColor(Color.DARK_GRAY));
                                for (final StoredPunishData e : storedPunishData) {
                                    if (e.getType() == 1) {
                                        sender.sendMessage("**Eintrag** " + e.getId() + "\n**Grund:** " + e.getReason() + "; **Ersteller:** " + e.getExecutor() + "; **Datum:** " + e.getDate() + "; **Ausstehende Zeit:** " + this.bot.getGuardianAPI().getRemainingTime(e.getDuration()) + "\n----------");
                                        //sender.sendMessage(new EmbedBuilder().setDescription("Eintrag **" + e.getId() + "**").addField("Grund", e.getReason(), false).addField("Ersteller", e.getExecutor(), false).addField("Datum", "e.getDate()", false).setFooter(this.bot.getGuardianAPI().getRemainingTime(e.getDuration())).setColor(Color.RED));
                                    } else if (e.getType() == 0) {
                                        sender.sendMessage("**Eintrag** " + e.getId() + "\n**Grund:** " + e.getReason() + "; **Ersteller:** " + e.getExecutor() + "; **Datum:** " + e.getDate() + "\n----------");
                                        //sender.sendMessage(new EmbedBuilder().setDescription("Eintrag **" + e.getId() + "**").addField("Grund", e.getReason(), false).addField("Ersteller", e.getExecutor(), false).addField("Datum", "e.getDate()", false).setColor(Color.GRAY));
                                    }
                                }

                                final EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setTitle("**Log Anfrage**")
                                        .setDescription("<@" + sender.getIdAsString() + "> hat die Logs von <@" + user + "> mit dem Grund **" + reason + "** angefragt.")
                                        .setAuthor(data.getAuthor())
                                        .setColor(Color.ORANGE)
                                        .setTimestampToNow();
                                this.bot.getLogChannel().sendMessage(embedBuilder);
                            });
                        } else {
                            final EmbedBuilder embed = new EmbedBuilder()
                                    .setDescription("Es konnten keine Einträge gefunden werden.")
                                    .setColor(Color.RED);
                            data.getChannel().sendMessage(embed).thenAccept(message -> {
                                final TimerService service = new TimerService(this.bot.getExecutorService());
                                service.run(message::delete, 20000);
                            });
                        }
                    });
                });
            }
        }
    }
}
