package net.eltown.guardianbot.components.messaging;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.eltown.guardianbot.Bot;
import net.eltown.guardianbot.components.data.CallData;
import net.eltown.guardianbot.components.tinyrabbit.TinyRabbitListener;

@RequiredArgsConstructor
public class LogListener {

    private final Bot guardian;

    @SneakyThrows
    public void startListening() {
        final TinyRabbitListener listener = new TinyRabbitListener("localhost");
        listener.receive((request -> {
            switch (CallData.valueOf(request.getKey().toUpperCase())) {
                case REQUEST_INFO_LOG:

                    break;
                case REQUEST_WARNING_LOG:

                    break;
                case REQUEST_CRITICAL_LOG:

                    break;
            }
        }), "Discord/Guardian[Log]", "discord.guardian.log");
    }
}