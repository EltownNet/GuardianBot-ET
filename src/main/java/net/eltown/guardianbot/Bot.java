package net.eltown.guardianbot;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import lombok.SneakyThrows;
import net.eltown.guardianbot.commands.discord.*;
import net.eltown.guardianbot.components.api.GuardianAPI;
import net.eltown.guardianbot.components.messaging.GuardianListener;
import net.eltown.guardianbot.components.messaging.LogListener;
import net.eltown.guardianbot.components.services.CommandService;
import net.eltown.guardianbot.components.tinyrabbit.TinyRabbit;
import net.eltown.guardianbot.listeners.CommandListener;
import lombok.Getter;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.UserStatus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class Bot {

    /*
     * Bot
     */
    private final DiscordApi discordApi;
    private final CommandService commandService;
    private final ExecutorService executorService;

    /*
     * API
     */
    private final GuardianAPI guardianAPI;

    /*
     * Database
     */
    private MongoClient databaseClient;
    private MongoDatabase database;

    /*
     * Messaging
     */
    private final TinyRabbit rabbit;
    private final GuardianListener guardianListener;
    private final LogListener logListener;

    private final ServerTextChannel logChannel;
    private final ServerTextChannel serverLogChannel;

    @SneakyThrows
    public Bot(final String token, final CommandService commandService) {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);

        commandService.register(
                new D_BanCommand(this),
                new D_UnbanCommand(this),
                new D_MuteCommand(this),
                new D_UnmuteCommand(this),
                new D_HistoryCommand(this)
        );
        this.discordApi = new DiscordApiBuilder()
                .setToken(token)
                .addListener(new CommandListener(this))
                .login().join();
        System.out.println("[bot] Bot status: Online");
        this.logChannel = this.getDiscordApi().getChannelById("794988533227716644").get().asServerTextChannel().get();
        this.serverLogChannel = this.getDiscordApi().getChannelById("794987406754447380").get().asServerTextChannel().get();
        this.discordApi.updateStatus(UserStatus.DO_NOT_DISTURB);
        this.discordApi.updateActivity(ActivityType.WATCHING, "Ich beobachte dich");
        this.connectDatabase();
        this.guardianAPI = new GuardianAPI(this.database, this);
        this.commandService = commandService;
        this.rabbit = new TinyRabbit("localhost", "Discord/Guardian/System[Main]");

        this.guardianListener = new GuardianListener(this);
        this.guardianListener.startListening();

        this.logListener = new LogListener(this);
        this.logListener.startListening();

        System.out.println("[bot] All API Components successfully initialized.");
    }

    private void connectDatabase() {
        try {
            final MongoClientURI clientURI = new MongoClientURI("mongodb://root:Qco7TDqoYq3RXq4pA3y7ETQTK6AgqzmTtRGLsgbN@45.138.50.23:27017/admin?authSource=admin&readPreference=primary&appname=MongoDB%20Compass&ssl=false");
            this.databaseClient = new MongoClient(clientURI);
            this.database = databaseClient.getDatabase("eltown_bot");
            final Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
            mongoLogger.setLevel(Level.OFF);
            System.out.println("[bot] Connected to database!");
        } catch (final Exception e) {
            e.printStackTrace();
            System.out.println("[bot] Failed to connect to database!");
        }
    }

}
