package api.storage;

import api.permission.PermissionGroup;

import java.util.Set;

public interface UserBackend {
    User addIfAbsentAndReturn(int peerId, int userId);
    Set<Integer> getChats();
    void setRank(int peerId, int userId, PermissionGroup rank);
    void setRank(User user, PermissionGroup rank);
    User getUser(int peerId, int userId);
    void deleteUser(int peerId, int userId);
}
