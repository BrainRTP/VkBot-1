package api.utils;

import com.vk.api.sdk.objects.messages.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;

import java.util.Collections;

@NonNls public final class KeyboardUtils {
    public static final Keyboard EMPTY_KEYBOARD = new Keyboard().setButtons(Collections.emptyList());
    @Contract(pure = true)
    private KeyboardUtils() {}

    public static KeyboardButton create(String label, KeyboardButtonColor color) {
        return new KeyboardButton().setColor(color).setAction(new KeyboardButtonAction()
                .setType(KeyboardButtonActionType.TEXT)
                .setLabel(label));
    }
    public static KeyboardButton create(KeyboardButtonColor color, KeyboardButtonAction action) {
        return new KeyboardButton().setColor(color).setAction(action);
    }
}
