package me.drepic.dynamicproxy;

import me.drepic.proton.common.ProtonManager;
import me.drepic.proton.common.ProtonProvider;
import me.drepic.proton.common.message.MessageHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class DynamicProxyBukkit extends JavaPlugin {

    ProtonManager manager;

    private String secret;
    private final String NAMESPACE = "DynamicProxy";
    private final String REQUEST_SUBJECT = "addServer";
    private final String ACKED_SUBJECT = "acknowledgeServer";

    private CompletableFuture<String> acknowledged;

    @Override
    public void onEnable() {
        this.manager = ProtonProvider.get();
        this.manager.registerMessageHandlers(this);

        getConfig().options().copyDefaults(true);
        saveConfig();

        if (getServer().getOnlineMode()) {
            getLogger().severe("Cannot request bungee adoption: Server is in online-mode!");
            return;
        }

        ServerAddRequest request = new ServerAddRequest(
                getConfig().getString("secret"),
                getConfig().getString("id"),
                getConfig().getString("host"),
                getConfig().getInt("port"),
                getConfig().getString("motd"),
                getConfig().getBoolean("restricted"));

        this.acknowledged = new CompletableFuture<>();

        int task = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            getLogger().info(String.format("Requesting bungee adoption as `%s`", request.getID()));
            this.manager.send(NAMESPACE, REQUEST_SUBJECT, request, getConfig().getString("bungeeClientId"));
        }, 0, 20 * getConfig().getLong("requestTimeout", 10)).getTaskId();

        acknowledged.thenAccept(message -> {
            getServer().getScheduler().cancelTask(task);
            getLogger().info("Network adoption acknowledged: " + message);
        });
    }

    @MessageHandler(namespace = NAMESPACE, subject = ACKED_SUBJECT)
    public void acknowledgement(String message) {
        this.acknowledged.complete(message);
    }
}
