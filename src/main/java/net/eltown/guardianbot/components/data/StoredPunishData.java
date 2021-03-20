package net.eltown.guardianbot.components.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StoredPunishData {

    private final String id;
    private final String user;
    private final String reason;
    private final int type;
    private final long duration;
    private final String executor;
    private final String date;

}
