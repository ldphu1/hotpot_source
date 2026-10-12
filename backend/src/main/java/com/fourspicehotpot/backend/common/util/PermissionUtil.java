package com.fourspicehotpot.backend.common.util;

import java.util.List;

public class PermissionUtil {
    public static boolean hasPermission(List<String> permissions, String permission) {
        return permissions.contains(permission);
    }
}
