package api.storage;

import api.Bot;
import api.file.JsonCustom;
import api.permission.PermissionGroup;
import api.permission.PermissionManager;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import jolyjdia.bot.Loader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class ProfileList extends JsonCustom implements
        JsonDeserializer<Map<Integer, Map<Integer, User>>>,
        JsonSerializer<Map<Integer, Map<Integer, User>>> {
    private Map<Integer, Map<Integer, User>> map = new HashMap<>();

    public ProfileList(File file) {
        super(file);
        this.setGson(new GsonBuilder()
                .registerTypeAdapter(Map.class, this)
                .setPrettyPrinting()
                .setExclusionStrategies(new MyExclusionStrategy())
                .create());
        this.load();
    }
    @Override
    public void load() {
        try (FileInputStream fileInputStream = new FileInputStream(getFile());
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8)) {
            this.map = this.getGson().fromJson(inputStreamReader, new MapTypeToken().getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Contract(pure = true)
    public @NotNull Set<Integer> getChats() {
        return map.keySet();
    }

    private boolean hasUser(@NotNull User user) {
        return map.containsKey(user.getPeerId()) && map.get(user.getPeerId()).containsKey(user.getUserId());
    }
    private boolean hasUser(int peerId, int userId) {
        return map.containsKey(peerId) && map.get(peerId).containsKey(userId);
    }
    public @Nullable User getUser(@NotNull User user) {
        if (hasUser(user)) {
            return map.get(user.getPeerId()).get(user.getUserId());
        }
        return null;
    }

    public @Nullable User getUser(int peerId, int userId) {
        if (hasUser(peerId, userId)) {
            return map.get(peerId).get(userId);
        }
        return null;
    }

    public User addIfAbsentAndReturn(int peerId, int userId) {
        Map<Integer, User> users = map.computeIfAbsent(peerId, k -> new HashMap<>());
        User user;
        if (users.containsKey(userId)) {
            user = users.get(userId);
        } else {
            user = new User(peerId, userId);
            //ГАВНО КАКОЕ-ТО
            try {
                Loader.getVkApiClient().messages().getConversationMembers(Bot.getGroupActor(), user.getPeerId()).execute().getItems()
                        .stream().filter(e -> {
                            Boolean isAdmin = e.getIsAdmin();
                            return (e.getMemberId() == userId) && (isAdmin != null && isAdmin);
                        }).findFirst()
                        .ifPresent(e -> user.setGroup(PermissionManager.getPermGroup("admin")));
            } catch (ApiException | ClientException e) {
                e.printStackTrace();
            }
            users.put(userId, user);
            this.save(map, new MapTypeToken().getType());
        }
        return user;
    }
    public void addIfAbsentAndConsumer(@NotNull User entity, @NotNull Consumer<? super User> consumer) {
        Map<Integer, User> users = map.computeIfAbsent(entity.getPeerId(), k -> new HashMap<>());
        int userId = entity.getUserId();
        if(users.containsKey(userId)) {
            consumer.accept(users.get(userId));
        } else {
            consumer.accept(entity);
            users.put(userId, entity);
        }
        this.save(map, new MapTypeToken().getType());
    }
    public void setRank(int peerId, int userId, PermissionGroup rank) {
        addIfAbsentAndConsumer(new User(peerId, userId), user -> user.setGroup(rank));
    }
    public void setPrefix(int peerId, int userId, String prefix) {
        addIfAbsentAndConsumer(new User(peerId, userId), user -> user.setPrefix(prefix));
    }
    public void setSuffix(int peerId, int userId, String suffix) {
        addIfAbsentAndConsumer(new User(peerId, userId), user -> user.setSuffix(suffix));
    }
    public void setRank(User user, PermissionGroup rank) {
        if(user == null) {
            return;
        }
        addIfAbsentAndConsumer(user, userId -> userId.setGroup(rank));
    }
    public void setPrefix(User user, String prefix) {
        if(user == null) {
            return;
        }
        addIfAbsentAndConsumer(user, userId -> userId.setPrefix(prefix));
    }

    public void setSuffix(User user, String suffix) {
        if(user == null) {
            return;
        }
        addIfAbsentAndConsumer(user, userId -> userId.setSuffix(suffix));
    }

    public void remove(@NotNull User user) {
        if (!map.containsKey(user.getPeerId())) {
            return;
        }
        Map<Integer, User> users = map.get(user.getPeerId());
        if(!users.containsKey(user.getUserId())) {
            return;
        }
        users.remove(user.getUserId());
        this.save(map, new MapTypeToken().getType());
    }

    public void remove(int peerId, int userId) {
        if (!map.containsKey(peerId)) {
            return;
        }
        Map<Integer, User> users = map.get(peerId);
        if(!users.containsKey(userId)) {
            return;
        }
        users.remove(userId);
        this.save(map, new MapTypeToken().getType());
    }

    /**
     * @param jsonElement
     * @param type
     * @param context
     * @return
     * @throws JsonParseException
     */
    @Override
    public Map<Integer, Map<Integer, User>> deserialize(@NotNull JsonElement jsonElement, Type type, JsonDeserializationContext context) {
        JsonObject obj = jsonElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> keyEntry : obj.entrySet()) {
            int chat = Integer.parseInt(keyEntry.getKey());
            Map<Integer, User> users = new HashMap<>();
            JsonObject object = keyEntry.getValue().getAsJsonObject();

            for (Map.Entry<String, JsonElement> valueEntry : object.entrySet()) {
                JsonObject element = valueEntry.getValue().getAsJsonObject();
                int id = Integer.parseInt(valueEntry.getKey());
                PermissionGroup group = PermissionManager.getPermGroup(element.get("group").getAsString());
                String prefix = element.get("prefix").getAsString();
                String suffix = element.get("suffix").getAsString();
                users.put(id, new User(chat, id, group, prefix, suffix));
            }
            map.put(chat, users);
        }
        return map;
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull Map<Integer, Map<Integer, User>> data, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        for(Map.Entry<Integer, Map<Integer, User>> chat : data.entrySet()) {
            JsonObject peer = new JsonObject();
            for(Map.Entry<Integer, User> users : chat.getValue().entrySet()) {
                JsonObject user = new JsonObject();
                User account = users.getValue();
                user.addProperty("group", account.getGroup().getName());
                user.addProperty("prefix", account.getPrefix());
                user.addProperty("suffix", account.getSuffix());
                peer.add(String.valueOf(users.getKey()), user);
            }
            object.add(String.valueOf(chat.getKey()), peer);
        }
        return object;
    }

    private static class MapTypeToken extends TypeToken<Map<Integer, Map<Integer, User>>> {}
}
