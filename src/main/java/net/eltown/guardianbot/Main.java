package net.eltown.guardianbot;

import net.eltown.guardianbot.components.services.CommandService;
import lombok.Getter;

public class Main {

    @Getter
    private static Bot bot;

    public static void main(final String[] args) {
        final CommandService commandService = new CommandService();
        bot = new Bot("ODE4NTQ2Njc1ODE1Mjg0NzU2.YEZo_w.d7g6jre7Oy547ZzM6ls-_avRuys", commandService);
    }

}
