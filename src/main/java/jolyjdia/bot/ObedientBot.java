package jolyjdia.bot;

import api.RoflanBot;
import api.Watchdog;
import api.command.RegisterCommandList;
import api.event.RegisterListEvent;
import api.file.ProfileList;
import api.scheduler.BotScheduler;
import api.utils.MathUtils;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Keyboard;
import com.vk.api.sdk.queries.messages.MessagesSendQuery;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ObedientBot implements RoflanBot {
    private final BotScheduler scheduler = new BotScheduler();
    private final ProfileList profileList = new ProfileList(new File(
            "D:\\IdeaProjects\\VkBot\\src\\main\\resources\\users.json"));
    private final RegisterCommandList registerCommandList = new RegisterCommandList();
    private final RegisterListEvent registerListEvent = new RegisterListEvent();
    private final Properties properties = new Properties();
    public final String accessToken;
    public final int groupId = 178836630;

    public ObedientBot() {
        try (InputStream inputStream = Loader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if(inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //this.groupId = groupId;
        this.accessToken = properties.getProperty("accessToken");
        Watchdog.doStart();
    }

    @Contract(pure = true)
    @Override
    public int getGroupId() {
        return groupId;
    }

    @Contract(pure = true)
    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Contract(pure = true)
    @Override
    public Properties getConfig() {
        return properties;
    }

    @Contract(pure = true)
    @Override
    public ProfileList getProfileList() {
        return profileList;
    }

    @Contract(pure = true)
    @Override
    public RegisterCommandList getRegisterCommandList() {
        return registerCommandList;
    }

    @Contract(pure = true)
    @Override
    public RegisterListEvent getRegisterListEvent() {
        return registerListEvent;
    }

    @Contract(pure = true)
    @Override
    public BotScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public void sendMessage(String msg, int peerId) {
        scheduler.runTask(() -> {
            try {
                send().peerId(peerId).message(msg).execute();
            } catch (ApiException | ClientException ignored) {}
        });
    }

    @Override
    public void sendKeyboard(String msg, int peerId, Keyboard keyboard) {
        scheduler.runTask(() -> {
            try {
                send().peerId(peerId).keyboard(keyboard).message(msg).execute();
            } catch (ApiException | ClientException ignored) {}
        });
    }

    @Override
    public void editChat(String title, int peerId) {
        scheduler.runTask(() -> {
            try {
                Loader.getVkApiClient().messages().editChat(Loader.getGroupActor(), peerId - 2000000000, title).execute();
            } catch (ApiException | ClientException ignored) { }
        });
    }
    private MessagesSendQuery send() {
        return Loader.getVkApiClient().messages()
                .send(Loader.getGroupActor())
                .randomId(MathUtils.RANDOM.nextInt(10000))
                .groupId(groupId);
    }
}
