package net.eltown.guardianbot.components.api;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.eltown.guardianbot.Bot;
import lombok.Getter;
import net.eltown.guardianbot.components.data.StoredPunishData;
import net.eltown.guardianbot.components.services.TimerService;
import org.bson.Document;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Getter
public class GuardianAPI {

    private final MongoCollection<Document> punishDataCollection;
    private final Bot bot;

    public final HashMap<String, StoredPunishData> cachedPunishments = new HashMap<>();

    public GuardianAPI(final MongoDatabase mongoDatabase, final Bot bot) {
        this.punishDataCollection = mongoDatabase.getCollection("punish_data");
        this.bot = bot;

        for (final Document document : this.punishDataCollection.find()) {
            if (document.getInteger("_type") == 1) {
                if (document.getString("_id").startsWith("B") || document.getString("_id").startsWith("M")) {
                    final StoredPunishData data = new StoredPunishData(
                            document.getString("_id"),
                            document.getString("_user"),
                            document.getString("_reason"),
                            document.getInteger("_type"),
                            document.getLong("_duration"),
                            document.getString("_executor"),
                            document.getString("_date")
                    );
                    this.cachedPunishments.put(document.getString("_user"), data);
                }
            }
        }
        this.checkForCancel();
    }

    public void checkForCancel() {
        this.cachedPunishments.values().forEach(data -> {
            if (data.getDuration() < System.currentTimeMillis()) {
                if (data.getId().startsWith("B")) this.unBan(data.getUser(), this.bot.getLogChannel().getServer());
                else if (data.getId().startsWith("M")) this.unMute(data.getUser(), this.bot.getLogChannel().getServer());
            }
        });
        final TimerService service = new TimerService(this.bot.getExecutorService());
        service.run(this::checkForCancel, 1800000);
    }

    public void setBanned(final User user, final User executor, final Server server, final String reason, final int delete, long duration) {
        duration = duration + System.currentTimeMillis();
        final String id = this.getRandomId(10, "B");
        final Document document = new Document("_id", id)
                .append("_user", user.getIdAsString())
                .append("_reason", reason)
                .append("_type", 1)
                .append("_date", this.getDate())
                .append("_duration", duration)
                .append("_executor", executor.getIdAsString());
        this.punishDataCollection.insertOne(document);
        this.cachedPunishments.put(user.getIdAsString(), new StoredPunishData(
                id,
                user.getIdAsString(),
                reason,
                1,
                duration,
                executor.getIdAsString(),
                this.getDate()
        ));

        final EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("**Ban-Report** " + id)
                .setDescription("Der Nutzer <@" + user.getId() + "> wurde mit dem Grund **" + reason + "** von <@" + executor.getId() + "> gebannt.")
                .setAuthor(executor)
                .setColor(Color.RED)
                .setFooter("Verbleibend: " + this.getRemainingTime(duration))
                .setTimestampToNow();
        this.bot.getLogChannel().sendMessage(embedBuilder);
        server.banUser(user, delete, reason);
    }

    public void unBan(final String user, final User executor, final Server server, final String reason) {
        server.unbanUser(user, reason);
        final Document document = this.punishDataCollection.find(new Document("_user", user).append("_type", 1)).first();
        if (document == null) return;
        this.punishDataCollection.updateMany(Objects.requireNonNull(this.punishDataCollection.find(new Document("_user", user).append("_type", 1)).first()), new Document("$set", new Document("_type", 0)));
        this.cachedPunishments.remove(user);
        final EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("**Aufhebung** " + document.getString("_id"))
                .setDescription("Die Bestrafung von <@" + user + "> wurde mit dem Grund **" + reason + "** von <@" + executor.getId() + "> aufgehoben.")
                .setAuthor(executor)
                .setColor(Color.RED)
                .setTimestampToNow();
        this.bot.getLogChannel().sendMessage(embedBuilder);
    }

    private void unBan(final String user, final Server server) {
        server.unbanUser(user, "Automatischer Zeitablauf der Bestrafung");
        final Document document = this.punishDataCollection.find(new Document("_user", user).append("_type", 1)).first();
        if (document == null) return;
        this.punishDataCollection.updateMany(Objects.requireNonNull(this.punishDataCollection.find(new Document("_user", user).append("_type", 1)).first()), new Document("$set", new Document("_type", 0)));
        this.cachedPunishments.remove(user);
    }

    public void isPunished(final String user, final Consumer<Boolean> w) {
        final Document document = this.punishDataCollection.find(new Document("_user", user).append("_type", 1)).first();
        w.accept(document != null);
    }

    public void isConspicuous(final String user, final Consumer<Boolean> w) {
        final Document document = this.punishDataCollection.find(new Document("_user", user)).first();
        w.accept(document != null);
    }

    public void setMuted(final User user, final User executor, final Server server, final String reason, long duration) {
        duration = duration + System.currentTimeMillis();
        final String id = this.getRandomId(10, "M");
        final Document document = new Document("_id", id)
                .append("_user", user.getIdAsString())
                .append("_reason", reason)
                .append("_type", 1)
                .append("_date", this.getDate())
                .append("_duration", duration)
                .append("_executor", executor.getIdAsString());
        this.punishDataCollection.insertOne(document);
        this.cachedPunishments.put(user.getIdAsString(), new StoredPunishData(
                id,
                user.getIdAsString(),
                reason,
                1,
                duration,
                executor.getIdAsString(),
                this.getDate()
        ));

        final EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("**Mute-Report** " + id)
                .setDescription("Der Nutzer <@" + user.getId() + "> wurde mit dem Grund **" + reason + "** von <@" + executor.getId() + "> gemutet.")
                .setAuthor(executor)
                .setColor(Color.RED)
                .setFooter("Verbleibend: " + this.getRemainingTime(duration))
                .setTimestampToNow();
        this.bot.getLogChannel().sendMessage(embedBuilder);
        user.removeRole(server.getRoleById("MEMBER ROLE").get());
    }

    public void unMute(final User user, final User executor, final Server server, final String reason) {
        user.addRole(server.getRoleById("MEMBER ROLE").get());
        final Document document = this.punishDataCollection.find(new Document("_user", user.getIdAsString()).append("_type", 1)).first();
        if (document == null) return;
        this.punishDataCollection.updateMany(Objects.requireNonNull(this.punishDataCollection.find(new Document("_user", user.getIdAsString()).append("_type", 1)).first()), new Document("$set", new Document("_type", 0)));
        this.cachedPunishments.remove(user.getIdAsString());
        final EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("**Aufhebung** " + document.getString("_id"))
                .setDescription("Die Bestrafung von <@" + user.getIdAsString() + "> wurde mit dem Grund **" + reason + "** von <@" + executor.getId() + "> aufgehoben.")
                .setAuthor(executor)
                .setColor(Color.RED)
                .setTimestampToNow();
        this.bot.getLogChannel().sendMessage(embedBuilder);
    }

    private void unMute(final String userId, final Server server) {
        try {
            final User user = this.bot.getDiscordApi().getUserById(userId).get();
            if (user != null) user.addRole(server.getRoleById("MEMBER ROLE").get());
            final Document document = this.punishDataCollection.find(new Document("_user", userId).append("_type", 1)).first();
            if (document == null) return;
            this.punishDataCollection.updateMany(Objects.requireNonNull(this.punishDataCollection.find(new Document("_user", userId).append("_type", 1)).first()), new Document("$set", new Document("_type", 0)));
            this.cachedPunishments.remove(userId);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void addWarn(final User user, final User executor, final String reason, final long duration) {
        final Document document = new Document("_id", this.getRandomId(10, "W"))
                .append("_user", user.getIdAsString())
                .append("_reason", reason)
                .append("_date", this.getDate())
                .append("_duration", duration)
                .append("_executor", executor.getIdAsString());
        this.punishDataCollection.insertOne(document);
    }

    public void getStoredPunishData(final String userId, final Consumer<Set<StoredPunishData>> set) {
        final Set<StoredPunishData> storedPunishData = new HashSet<>();
        for (final Document document : this.punishDataCollection.find(new Document("_user", userId))) {
            final StoredPunishData data = new StoredPunishData(
                    document.getString("_id"),
                    document.getString("_user"),
                    document.getString("_reason"),
                    document.getInteger("_type"),
                    document.getLong("_duration"),
                    document.getString("_executor"),
                    document.getString("_date")
            );
            storedPunishData.add(data);
        }
        set.accept(storedPunishData);
    }

    private String getRandomId(final int i) {
        final String chars = "1234567890";
        final StringBuilder stringBuilder = new StringBuilder();
        final Random rnd = new Random();
        while (stringBuilder.length() < i) {
            int index = (int) (rnd.nextFloat() * chars.length());
            stringBuilder.append(chars.charAt(index));
        }
        return stringBuilder.toString();
    }

    private String getRandomId(final int i, final String prefix) {
        final String chars = "1234567890";
        final StringBuilder stringBuilder = new StringBuilder(prefix + "-");
        final Random rnd = new Random();
        while (stringBuilder.length() < i) {
            int index = (int) (rnd.nextFloat() * chars.length());
            stringBuilder.append(chars.charAt(index));
        }
        return stringBuilder.toString();
    }

    private String getDate() {
        final Date now = new Date();
        final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
        return dateFormat.format(now);
    }

    public String getRemainingTime(final long duration) {
        if (duration == -1L) {
            return "Permanent";
        } else {
            SimpleDateFormat today = new SimpleDateFormat("dd.MM.yyyy");
            today.format(System.currentTimeMillis());
            SimpleDateFormat future = new SimpleDateFormat("dd.MM.yyyy");
            future.format(duration);
            long time = future.getCalendar().getTimeInMillis() - today.getCalendar().getTimeInMillis();
            int days = (int) (time / 86400000L);
            int hours = (int) (time / 3600000L % 24L);
            int minutes = (int) (time / 60000L % 60L);
            String day = "Tage";
            if (days == 1) {
                day = "Tag";
            }

            String hour = "Stunden";
            if (hours == 1) {
                hour = "Stunde";
            }

            String minute = "Minuten";
            if (minutes == 1) {
                minute = "Minute";
            }

            if (minutes < 1 && days == 0 && hours == 0) {
                return "Wenige Sekunden";
            } else if (hours == 0 && days == 0) {
                return minutes + " " + minute;
            } else {
                return days == 0 ? hours + " " + hour + " " + minutes + " " + minute : days + " " + day + " " + hours + " " + hour + " " + minutes + " " + minute;
            }
        }
    }

}
