package io.kickflip.sdk.utilities;

import android.content.Context;
import android.content.Intent;

public class NavigationUtils {

        public static void jumpToClearingTask(Context a, Class clazz) {
            Intent intent = new Intent(a.getApplicationContext(), clazz);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            a.startActivity(intent);
        }

        public static void jumpTo(Context a, Class clazz) {
            Intent intent = new Intent(a.getApplicationContext(), clazz);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            a.startActivity(intent);
        }
}
