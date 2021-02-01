package me.drepic.dynamicproxy;

import com.google.common.io.ByteStreams;
import me.drepic.proton.common.ProtonManager;
import me.drepic.proton.common.ProtonProvider;
import me.drepic.proton.common.message.MessageAttributes;
import me.drepic.proton.common.message.MessageHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.net.InetSocketAddress;

public class DynamicProxyBungee extends Plugin implements Listener {

    ProtonManager manager;

    private String secret = "secret";
    private final String NAMESPACE = "DynamicProxy";
    private final String REQUEST_SUBJECT = "addServer";
    private final String ACKED_SUBJECT = "acknowledgeServer";

    @Override
    public void onEnable() {
        this.manager = ProtonProvider.get();
        this.saveDefaultConfig();

        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "secret.yml"));
            this.secret = config.getString("secret");
        } catch (IOException e) {
            getLogger().severe("Unable to load config!");
            return;
        }

        this.manager.registerMessageHandlers(this);
    }

    @MessageHandler(namespace = NAMESPACE, subject = REQUEST_SUBJECT)
    public void onServerAddRequest(ServerAddRequest request, MessageAttributes attributes) {
        getLogger().info("Server has requested adoption: " + request.toString());
        if (!request.getSecret().equals(secret)) {
            this.manager.send(
                    NAMESPACE,
                    ACKED_SUBJECT,
                    "Secret is incorrect",
                    attributes.getSenderName());
            return;
        }

        if (this.getProxy().getServers().containsKey(request.getID())) {
            this.manager.send(
                    NAMESPACE,
                    ACKED_SUBJECT,
                    String.format("Server of name `%s` already registered with proxy.", request.getID()),
                    attributes.getSenderName());
            return;
        }

        ServerInfo info = this.getProxy().constructServerInfo(
                request.getID(),
                InetSocketAddress.createUnresolved(request.getHost(), request.getPort()),
                request.getMotd(),
                request.isRestricted());

        this.getProxy().getServers().put(info.getName(), info);
        getLogger().info(String.format("%s has been added to the proxy.", request.getID()));
        this.manager.send(NAMESPACE, ACKED_SUBJECT, "Added to proxy.", attributes.getSenderName());
    }

    private void saveDefaultConfig() {
        File configFile = new File(getDataFolder(), "secret.yml");
        if (!configFile.exists()) { //Simply save default config into datafolder
            getDataFolder().mkdir();
            try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("secret.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to save default configuration file.");
            }
        }
    }


}
