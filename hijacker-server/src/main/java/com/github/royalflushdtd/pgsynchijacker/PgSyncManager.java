package com.github.royalflushdtd.pgsynchijacker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class PgSyncManager {

    private static final Map<String, PgIncrementSyncServer>      /**/ SERVERS        /**/ = new ConcurrentHashMap<>();

    private PgSyncManager() {
    }

    public static PgIncrementSyncServer findServer(String serverId) {
        return SERVERS.get(serverId);
    }

    public static void putServer(PgIncrementSyncServer server) {
        SERVERS.put(server.getServerId(), server);
    }

    public static void remove(String serverId) {
        SERVERS.remove(serverId);
    }

    public static void close() {
        SERVERS.values()
                .forEach(PgIncrementSyncServer::shutdown);
        SERVERS.clear();
    }

}
