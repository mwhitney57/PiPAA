package dev.mwhitney.gui.binds;

import static dev.mwhitney.gui.binds.ShortcutMask.*;
import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON2_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON3_DOWN_MASK;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Objects;

/**
 * An enumerator of shortcuts available for use within PiPAA. Each shortcut is
 * constructed with any default keyboard and mouse bindings used to activate it.
 * Some shortcuts do not have any specified defaults. This class should not be
 * used as a reference for the <b>current</b> user shortcut configuration, as
 * that may ultimately change and differ from the values found here.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public enum Shortcut {
    // Binds and Defaults
    DEBUG_INFO(
        new KeyBind(KeyEvent.VK_I),
        new KeyBind(KeyEvent.VK_I, CTRL_DOWN_MASK)
    ),
    KEYBOARD_SHORTCUTS(
        new KeyBind(KeyEvent.VK_K)
    ),
    
    // Regular Binds
    ADD_MEDIA_ARTWORK(
        new KeyBind(KeyEvent.VK_A)
    ),
    ADD_WINDOW(
        new KeyBind(KeyEvent.VK_A, SHIFT_DOWN_MASK),
        new MouseBind(BindOptions.build().onHit(2), MouseEvent.BUTTON2)
    ),
    /** Adapts: For closing media within a window, or the window itself if it has no media. */
    CLOSE_FLEX(
        // Use lower default delay to minimize accidental closings from consecutive RMB drag (move) operations.
        new MouseBind(BindOptions.build().useDelay(225).onHit(3).onRelease(), MouseEvent.BUTTON3)
    ),
    CLOSE_MEDIA(
        new KeyBind(KeyEvent.VK_C, CTRL_DOWN_MASK)
    ),
    CLOSE_WINDOW(
        new KeyBind(KeyEvent.VK_ESCAPE)
    ),
    CLOSE_WINDOWS(
        new KeyBind(KeyEvent.VK_ESCAPE, SHIFT_DOWN_MASK)
    ),
    COPY_MEDIA(
        new KeyBind(KeyEvent.VK_C)
    ),
    COPY_MEDIA_SRC(
        new KeyBind(KeyEvent.VK_C, ALT_DOWN_MASK)
    ),
    COPY_ALL_MEDIA(
        new KeyBind(KeyEvent.VK_C, SHIFT_DOWN_MASK)
    ),
    COPY_ALL_MEDIA_SRC(
        new KeyBind(KeyEvent.VK_C, ALT_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    CYCLE_AUDIO_TRACKS(
        new KeyBind(KeyEvent.VK_T)
    ),
    CYCLE_SUBTITLE_TRACKS(
        new KeyBind(KeyEvent.VK_S)
    ),
    DELETE_MEDIA(
        new KeyBind(KeyEvent.VK_D, CTRL_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    DISABLE_SUBTITLES(
        new KeyBind(KeyEvent.VK_S, ALT_DOWN_MASK)
    ),
    DUPLICATE_WINDOW(
        new KeyBind(KeyEvent.VK_D, SHIFT_DOWN_MASK)
    ),
    FLASH_BORDERS(
        new KeyBind(KeyEvent.VK_B)
    ),
    FLASH_BORDERS_ALL(
        new KeyBind(KeyEvent.VK_B, SHIFT_DOWN_MASK)
    ),
    FLIP_HORIZONTAL(
        new KeyBind(KeyEvent.VK_F3)
    ),
    FLIP_VERTICAL(
        new KeyBind(KeyEvent.VK_F4)
    ),
    FULLSCREEN(
        new KeyBind(KeyEvent.VK_F),
        new MouseBind(BindOptions.build().onHit(2).useDelay(250), MouseEvent.BUTTON1)
    ),
    GLOBAL_MUTE(
        new KeyBind(KeyEvent.VK_M, CTRL_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    HIDE_WINDOW(
        new KeyBind(KeyEvent.VK_H, CTRL_DOWN_MASK)
    ),
    HIDE_WINDOWS(
        new KeyBind(KeyEvent.VK_H, CTRL_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    LOCK_WINDOW_MENU(
        new KeyBind(KeyEvent.VK_L, CTRL_DOWN_MASK)
    ),
    LOCK_WINDOW_SIZEPOS(
        new KeyBind(KeyEvent.VK_L, CTRL_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    LOCK_WINDOW_ALLOFF(
        new KeyBind(KeyEvent.VK_L, CTRL_DOWN_MASK | ALT_DOWN_MASK)
    ),
    MINIMIZE_WINDOW(
        new KeyBind(KeyEvent.VK_DOWN, ALT_DOWN_MASK)
    ),
    MINIMIZE_WINDOWS(
        new KeyBind(KeyEvent.VK_DOWN, ALT_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    MOVE_WINDOW_W(
        new KeyBind(KeyEvent.VK_LEFT, CUSTOM_MASK_1.mask())
    ),
    MOVE_WINDOW_W_LESS(
        new KeyBind(KeyEvent.VK_LEFT, CUSTOM_MASK_1.mask() | ALT_DOWN_MASK)
    ),
    MOVE_WINDOW_W_MORE(
        new KeyBind(KeyEvent.VK_LEFT, CUSTOM_MASK_1.mask() | CTRL_DOWN_MASK)
    ),
    MOVE_WINDOWS_W(
        new KeyBind(KeyEvent.VK_LEFT, CUSTOM_MASK_1.mask() | SHIFT_DOWN_MASK)
    ),
    MOVE_WINDOWS_W_LESS(
        new KeyBind(KeyEvent.VK_LEFT, CUSTOM_MASK_1.mask() | SHIFT_DOWN_MASK | ALT_DOWN_MASK)
    ),
    MOVE_WINDOWS_W_MORE(
        new KeyBind(KeyEvent.VK_LEFT, CUSTOM_MASK_1.mask() | SHIFT_DOWN_MASK | CTRL_DOWN_MASK)
    ),
    MOVE_WINDOW_E(
        new KeyBind(KeyEvent.VK_RIGHT, CUSTOM_MASK_1.mask())
    ),
    MOVE_WINDOW_E_LESS(
        new KeyBind(KeyEvent.VK_RIGHT, CUSTOM_MASK_1.mask() | ALT_DOWN_MASK)
    ),
    MOVE_WINDOW_E_MORE(
        new KeyBind(KeyEvent.VK_RIGHT, CUSTOM_MASK_1.mask() | CTRL_DOWN_MASK)
    ),
    MOVE_WINDOWS_E(
        new KeyBind(KeyEvent.VK_RIGHT, CUSTOM_MASK_1.mask() | SHIFT_DOWN_MASK)
    ),
    MOVE_WINDOWS_E_LESS(
        new KeyBind(KeyEvent.VK_RIGHT, CUSTOM_MASK_1.mask() | SHIFT_DOWN_MASK | ALT_DOWN_MASK)
    ),
    MOVE_WINDOWS_E_MORE(
        new KeyBind(KeyEvent.VK_RIGHT, CUSTOM_MASK_1.mask() | SHIFT_DOWN_MASK | CTRL_DOWN_MASK)
    ),
    MOVE_WINDOW_S(
        new KeyBind(KeyEvent.VK_DOWN, CUSTOM_MASK_1.mask())
    ),
    MOVE_WINDOW_S_LESS(
        new KeyBind(KeyEvent.VK_DOWN, CUSTOM_MASK_1.mask() | ALT_DOWN_MASK)
    ),
    MOVE_WINDOW_S_MORE(
        new KeyBind(KeyEvent.VK_DOWN, CUSTOM_MASK_1.mask() | CTRL_DOWN_MASK)
    ),
    MOVE_WINDOWS_S(
        new KeyBind(KeyEvent.VK_DOWN, CUSTOM_MASK_1.mask() | SHIFT_DOWN_MASK)
    ),
    MOVE_WINDOWS_S_LESS(
        new KeyBind(KeyEvent.VK_DOWN, CUSTOM_MASK_1.mask() | SHIFT_DOWN_MASK | ALT_DOWN_MASK)
    ),
    MOVE_WINDOWS_S_MORE(
        new KeyBind(KeyEvent.VK_DOWN, CUSTOM_MASK_1.mask() | SHIFT_DOWN_MASK | CTRL_DOWN_MASK)
    ),
    MOVE_WINDOW_N(
        new KeyBind(KeyEvent.VK_UP, CUSTOM_MASK_1.mask())
    ),
    MOVE_WINDOW_N_LESS(
        new KeyBind(KeyEvent.VK_UP, CUSTOM_MASK_1.mask() | ALT_DOWN_MASK)
    ),
    MOVE_WINDOW_N_MORE(
        new KeyBind(KeyEvent.VK_UP, CUSTOM_MASK_1.mask() | CTRL_DOWN_MASK)
    ),
    MOVE_WINDOWS_N(
        new KeyBind(KeyEvent.VK_UP, CUSTOM_MASK_1.mask() | SHIFT_DOWN_MASK)
    ),
    MOVE_WINDOWS_N_LESS(
        new KeyBind(KeyEvent.VK_UP, CUSTOM_MASK_1.mask() | SHIFT_DOWN_MASK | ALT_DOWN_MASK)
    ),
    MOVE_WINDOWS_N_MORE(
        new KeyBind(KeyEvent.VK_UP, CUSTOM_MASK_1.mask() | SHIFT_DOWN_MASK | CTRL_DOWN_MASK)
    ),
    OPACITY_MAX_ALL(
        new KeyBind(KeyEvent.VK_UP, CUSTOM_MASK_2.mask() | SHIFT_DOWN_MASK | CTRL_DOWN_MASK)
    ),
    OPACITY_INCREASE_ALL(
        new KeyBind(KeyEvent.VK_UP, CUSTOM_MASK_2.mask() | SHIFT_DOWN_MASK)
    ),
    OPACITY_DECREASE_ALL(
        new KeyBind(KeyEvent.VK_DOWN, CUSTOM_MASK_2.mask() | SHIFT_DOWN_MASK)
    ),
    OPACITY_MIN_ALL(
        new KeyBind(KeyEvent.VK_DOWN, CUSTOM_MASK_2.mask() | SHIFT_DOWN_MASK | CTRL_DOWN_MASK)
    ),
    OPACITY_MAX(
        new KeyBind(KeyEvent.VK_UP, CUSTOM_MASK_2.mask() | CTRL_DOWN_MASK)
    ),
    OPACITY_INCREASE(
        new KeyBind(KeyEvent.VK_UP, CUSTOM_MASK_2.mask())
    ),
    OPACITY_DECREASE(
        new KeyBind(KeyEvent.VK_DOWN, CUSTOM_MASK_2.mask())
    ),
    OPACITY_MIN(
        new KeyBind(KeyEvent.VK_DOWN, CUSTOM_MASK_2.mask() | CTRL_DOWN_MASK)
    ),
    OPEN_MEDIA_DIRECTORY(
        new KeyBind(KeyEvent.VK_O, CTRL_DOWN_MASK)
    ),
    /** Allows & prefers showing cached versions of the media, like trimmed or converted sources. */
    OPEN_CACHE_DIRECTORY(
        new KeyBind(KeyEvent.VK_O, CTRL_DOWN_MASK | ALT_DOWN_MASK)
    ),
    PASTE_MEDIA(
        new KeyBind(KeyEvent.VK_V, CTRL_DOWN_MASK)
    ),
    PAUSE(
    ),
    PAUSE_ALL(
        new KeyBind(KeyEvent.VK_P, ALT_DOWN_MASK)
    ),
    PLAY(
    ),
    PLAY_ALL(
        new KeyBind(KeyEvent.VK_P, SHIFT_DOWN_MASK)
    ),
    PLAY_PAUSE(
        new KeyBind(KeyEvent.VK_SPACE),
        new MouseBind(BindOptions.build().useDelay(250), MouseEvent.BUTTON1)
    ),
    PLAY_PAUSE_ALL(
        new KeyBind(KeyEvent.VK_SPACE, SHIFT_DOWN_MASK),
        new MouseBind(MouseEvent.BUTTON1, SHIFT_DOWN_MASK)
    ),
    PLAYBACK_RATE_0(
        new KeyBind(KeyEvent.VK_0, ALT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD0, ALT_DOWN_MASK)
    ),
    PLAYBACK_RATE_1(
        new KeyBind(KeyEvent.VK_1, ALT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD1, ALT_DOWN_MASK)
    ),
    PLAYBACK_RATE_2(
        new KeyBind(KeyEvent.VK_2, ALT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD2, ALT_DOWN_MASK)
    ),
    PLAYBACK_RATE_3(
        new KeyBind(KeyEvent.VK_3, ALT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD3, ALT_DOWN_MASK)
    ),
    PLAYBACK_RATE_4(
        new KeyBind(KeyEvent.VK_4, ALT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD4, ALT_DOWN_MASK)
    ),
    PLAYBACK_RATE_5(
        new KeyBind(KeyEvent.VK_5, ALT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD5, ALT_DOWN_MASK)
    ),
    PLAYBACK_RATE_6(
        new KeyBind(KeyEvent.VK_6, ALT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD6, ALT_DOWN_MASK)
    ),
    PLAYBACK_RATE_7(
        new KeyBind(KeyEvent.VK_7, ALT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD7, ALT_DOWN_MASK)
    ),
    PLAYBACK_RATE_8(
        new KeyBind(KeyEvent.VK_8, ALT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD8, ALT_DOWN_MASK)
    ),
    PLAYBACK_RATE_9(
        new KeyBind(KeyEvent.VK_9, ALT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD9, ALT_DOWN_MASK)
    ),
    PLAYBACK_RATE_UP_LESS(
        new KeyBind(KeyEvent.VK_EQUALS, SHIFT_DOWN_MASK),
        new MouseBind(ShortcutCode.CODE_SCROLL_UP, BUTTON3_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    PLAYBACK_RATE_UP(
        new KeyBind(KeyEvent.VK_EQUALS),
        new MouseBind(ShortcutCode.CODE_SCROLL_UP, BUTTON3_DOWN_MASK)
    ),
    PLAYBACK_RATE_UP_MORE(
        new KeyBind(KeyEvent.VK_EQUALS, CTRL_DOWN_MASK),
        new MouseBind(ShortcutCode.CODE_SCROLL_UP, BUTTON3_DOWN_MASK | CTRL_DOWN_MASK)
    ),
    PLAYBACK_RATE_UP_MAX(
        new KeyBind(KeyEvent.VK_EQUALS, CTRL_DOWN_MASK | SHIFT_DOWN_MASK),
        new MouseBind(MouseEvent.BUTTON2, BUTTON3_DOWN_MASK),   // Quick and easy reset to 1x rate.
        new MouseBind(ShortcutCode.CODE_SCROLL_UP, BUTTON3_DOWN_MASK | CTRL_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    PLAYBACK_RATE_DOWN_LESS(
        new KeyBind(KeyEvent.VK_MINUS, SHIFT_DOWN_MASK),
        new MouseBind(ShortcutCode.CODE_SCROLL_DOWN, BUTTON3_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    PLAYBACK_RATE_DOWN(
        new KeyBind(KeyEvent.VK_MINUS),
        new MouseBind(ShortcutCode.CODE_SCROLL_DOWN, BUTTON3_DOWN_MASK)
    ),
    PLAYBACK_RATE_DOWN_MORE(
        new KeyBind(KeyEvent.VK_MINUS, CTRL_DOWN_MASK),
        new MouseBind(ShortcutCode.CODE_SCROLL_DOWN, BUTTON3_DOWN_MASK | CTRL_DOWN_MASK)
    ),
    PLAYBACK_RATE_DOWN_MAX(
        new KeyBind(KeyEvent.VK_MINUS, CTRL_DOWN_MASK | SHIFT_DOWN_MASK),
        new MouseBind(ShortcutCode.CODE_SCROLL_DOWN, BUTTON3_DOWN_MASK | CTRL_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    RELOAD(
        new KeyBind(KeyEvent.VK_R, CTRL_DOWN_MASK)
    ),
    RELOAD_QUICK(
        new KeyBind(KeyEvent.VK_R, CTRL_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    RELOCATE_WINDOW(
        new KeyBind(KeyEvent.VK_L)
    ),
    RELOCATE_WINDOWS(
        new KeyBind(KeyEvent.VK_L, SHIFT_DOWN_MASK)
    ),
    RESET_SIZE(
        new KeyBind(BindOptions.build().onHit(2), KeyEvent.VK_Z)
    ),
    RESET_SIZE_ALL(
        new KeyBind(BindOptions.build().onHit(2), KeyEvent.VK_Z, SHIFT_DOWN_MASK)
    ),
    RESET_ZOOM(
        new KeyBind(KeyEvent.VK_OPEN_BRACKET, CTRL_DOWN_MASK | SHIFT_DOWN_MASK),
        new MouseBind(MouseEvent.BUTTON2, CTRL_DOWN_MASK)
    ),
    RESIZE_WINDOW(
        new KeyBind(BindOptions.build().onHit(2), KeyEvent.VK_W)
    ),
    RESIZE_WINDOWS(
        new KeyBind(BindOptions.build().onHit(2), KeyEvent.VK_W, SHIFT_DOWN_MASK)
    ),
    RESTORE_WINDOW(
        new KeyBind(KeyEvent.VK_UP, ALT_DOWN_MASK)
    ),
    RESTORE_WINDOWS(
        new KeyBind(KeyEvent.VK_UP, ALT_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    SAVE_MEDIA(
        new KeyBind(KeyEvent.VK_S, CTRL_DOWN_MASK)
    ),
    SAVE_MEDIA_ALT(
        new KeyBind(KeyEvent.VK_S, CTRL_DOWN_MASK | ALT_DOWN_MASK)
    ),
    SEEK(
    ),
    SEEK_ALL(
    ),
    SEEK_0(
        new KeyBind(KeyEvent.VK_0),
        new KeyBind(KeyEvent.VK_NUMPAD0)
    ),
    SEEK_1(
        new KeyBind(KeyEvent.VK_1),
        new KeyBind(KeyEvent.VK_NUMPAD1)
    ),
    SEEK_2(
        new KeyBind(KeyEvent.VK_2),
        new KeyBind(KeyEvent.VK_NUMPAD2)
    ),
    SEEK_3(
        new KeyBind(KeyEvent.VK_3),
        new KeyBind(KeyEvent.VK_NUMPAD3)
    ),
    SEEK_4(
        new KeyBind(KeyEvent.VK_4),
        new KeyBind(KeyEvent.VK_NUMPAD4)
    ),
    SEEK_5(
        new KeyBind(KeyEvent.VK_5),
        new KeyBind(KeyEvent.VK_NUMPAD5)
    ),
    SEEK_6(
        new KeyBind(KeyEvent.VK_6),
        new KeyBind(KeyEvent.VK_NUMPAD6)
    ),
    SEEK_7(
        new KeyBind(KeyEvent.VK_7),
        new KeyBind(KeyEvent.VK_NUMPAD7)
    ),
    SEEK_8(
        new KeyBind(KeyEvent.VK_8),
        new KeyBind(KeyEvent.VK_NUMPAD8)
    ),
    SEEK_9(
        new KeyBind(KeyEvent.VK_9),
        new KeyBind(KeyEvent.VK_NUMPAD9)
    ),
    SEEK_0_ALL(
        // CTRL + SHIFT + 0 is absorbed by Windows OS, so cannot use it. Was originally desired.
        new KeyBind(KeyEvent.VK_0, SHIFT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD0, SHIFT_DOWN_MASK)
    ),
    SEEK_1_ALL(
        new KeyBind(KeyEvent.VK_1, SHIFT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD1, SHIFT_DOWN_MASK)
    ),
    SEEK_2_ALL(
        new KeyBind(KeyEvent.VK_2, SHIFT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD2, SHIFT_DOWN_MASK)
    ),
    SEEK_3_ALL(
        new KeyBind(KeyEvent.VK_3, SHIFT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD3, SHIFT_DOWN_MASK)
    ),
    SEEK_4_ALL(
        new KeyBind(KeyEvent.VK_4, SHIFT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD4, SHIFT_DOWN_MASK)
    ),
    SEEK_5_ALL(
        new KeyBind(KeyEvent.VK_5, SHIFT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD5, SHIFT_DOWN_MASK)
    ),
    SEEK_6_ALL(
        new KeyBind(KeyEvent.VK_6, SHIFT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD6, SHIFT_DOWN_MASK)
    ),
    SEEK_7_ALL(
        new KeyBind(KeyEvent.VK_7, SHIFT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD7, SHIFT_DOWN_MASK)
    ),
    SEEK_8_ALL(
        new KeyBind(KeyEvent.VK_8, SHIFT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD8, SHIFT_DOWN_MASK)
    ),
    SEEK_9_ALL(
        new KeyBind(KeyEvent.VK_9, SHIFT_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD9, SHIFT_DOWN_MASK)
    ),
    SEEK_BACKWARD_LESS(
        new KeyBind(KeyEvent.VK_LEFT, SHIFT_DOWN_MASK),
        new MouseBind(MouseEvent.BUTTON1, BUTTON2_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    SEEK_BACKWARD(
        new KeyBind(KeyEvent.VK_LEFT),
        new MouseBind(MouseEvent.BUTTON1, BUTTON2_DOWN_MASK)
    ),
    SEEK_BACKWARD_MORE(
        new KeyBind(KeyEvent.VK_LEFT, CTRL_DOWN_MASK),
        new MouseBind(MouseEvent.BUTTON1, BUTTON2_DOWN_MASK | CTRL_DOWN_MASK)
    ),
    SEEK_BACKWARD_MAX(
        new KeyBind(KeyEvent.VK_LEFT, CTRL_DOWN_MASK | SHIFT_DOWN_MASK),
        new MouseBind(MouseEvent.BUTTON1, BUTTON2_DOWN_MASK | CTRL_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    SEEK_FORWARD_LESS(
        new KeyBind(KeyEvent.VK_RIGHT, SHIFT_DOWN_MASK),
        new MouseBind(MouseEvent.BUTTON3, BUTTON2_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    SEEK_FORWARD(
        new KeyBind(KeyEvent.VK_RIGHT),
        new MouseBind(MouseEvent.BUTTON3, BUTTON2_DOWN_MASK)
    ),
    SEEK_FORWARD_MORE(
        new KeyBind(KeyEvent.VK_RIGHT, CTRL_DOWN_MASK),
        new MouseBind(MouseEvent.BUTTON3, BUTTON2_DOWN_MASK | CTRL_DOWN_MASK)
    ),
    SEEK_FORWARD_MAX(
        new KeyBind(KeyEvent.VK_RIGHT, CTRL_DOWN_MASK | SHIFT_DOWN_MASK),
        new MouseBind(MouseEvent.BUTTON3, BUTTON2_DOWN_MASK | CTRL_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    SEEK_FRAME(
        new KeyBind(KeyEvent.VK_PERIOD),
        new KeyBind(KeyEvent.VK_RIGHT, ALT_DOWN_MASK)
    ),
    SEND_TO_BACK(
        new KeyBind(KeyEvent.VK_BACK_SPACE)
    ),
    SEND_TO_FRONT(
        new KeyBind(KeyEvent.VK_BACK_SPACE, ALT_DOWN_MASK)
    ),
    SHOW_WINDOW(
        new KeyBind(KeyEvent.VK_H, ALT_DOWN_MASK)
    ),
    SHOW_WINDOWS(
        new KeyBind(KeyEvent.VK_H, ALT_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    VOLUME_0(
        new KeyBind(KeyEvent.VK_0, CTRL_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD0, CTRL_DOWN_MASK)
    ),
    VOLUME_1(
        new KeyBind(KeyEvent.VK_1, CTRL_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD1, CTRL_DOWN_MASK)
    ),
    VOLUME_2(
        new KeyBind(KeyEvent.VK_2, CTRL_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD2, CTRL_DOWN_MASK)
    ),
    VOLUME_3(
        new KeyBind(KeyEvent.VK_3, CTRL_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD3, CTRL_DOWN_MASK)
    ),
    VOLUME_4(
        new KeyBind(KeyEvent.VK_4, CTRL_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD4, CTRL_DOWN_MASK)
    ),
    VOLUME_5(
        new KeyBind(KeyEvent.VK_5, CTRL_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD5, CTRL_DOWN_MASK)
    ),
    VOLUME_6(
        new KeyBind(KeyEvent.VK_6, CTRL_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD6, CTRL_DOWN_MASK)
    ),
    VOLUME_7(
        new KeyBind(KeyEvent.VK_7, CTRL_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD7, CTRL_DOWN_MASK)
    ),
    VOLUME_8(
        new KeyBind(KeyEvent.VK_8, CTRL_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD8, CTRL_DOWN_MASK)
    ),
    VOLUME_9(
        new KeyBind(KeyEvent.VK_9, CTRL_DOWN_MASK),
        new KeyBind(KeyEvent.VK_NUMPAD9, CTRL_DOWN_MASK)
    ),
    VOLUME_UP_LESS(
        new KeyBind(KeyEvent.VK_UP, SHIFT_DOWN_MASK),
        new MouseBind(ShortcutCode.CODE_SCROLL_UP, SHIFT_DOWN_MASK)
    ),
    VOLUME_UP(
        new KeyBind(KeyEvent.VK_UP),
        new MouseBind(ShortcutCode.CODE_SCROLL_UP)
	),
    VOLUME_UP_MORE(
        new KeyBind(KeyEvent.VK_UP, CTRL_DOWN_MASK),
        new MouseBind(ShortcutCode.CODE_SCROLL_UP, CTRL_DOWN_MASK)
    ),
    VOLUME_UP_MAX(
        new KeyBind(KeyEvent.VK_UP, CTRL_DOWN_MASK | SHIFT_DOWN_MASK),
        new MouseBind(ShortcutCode.CODE_SCROLL_UP, CTRL_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    VOLUME_DOWN_LESS(
        new KeyBind(KeyEvent.VK_DOWN, SHIFT_DOWN_MASK),
        new MouseBind(ShortcutCode.CODE_SCROLL_DOWN, SHIFT_DOWN_MASK)
    ),
    VOLUME_DOWN(
        new KeyBind(KeyEvent.VK_DOWN),
        new MouseBind(ShortcutCode.CODE_SCROLL_DOWN)
	),
    VOLUME_DOWN_MORE(
        new KeyBind(KeyEvent.VK_DOWN, CTRL_DOWN_MASK),
        new MouseBind(ShortcutCode.CODE_SCROLL_DOWN, CTRL_DOWN_MASK)
    ),
    VOLUME_DOWN_MAX(
        new KeyBind(KeyEvent.VK_DOWN, CTRL_DOWN_MASK | SHIFT_DOWN_MASK),
        new MouseBind(ShortcutCode.CODE_SCROLL_DOWN, CTRL_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    VOLUME_MUTE_UNMUTE(
        new KeyBind(KeyEvent.VK_M)
	),
    VOLUME_MUTE(
        new KeyBind(KeyEvent.VK_M, CUSTOM_MASK_1.mask() | CTRL_DOWN_MASK)
    ),
    VOLUME_UNMUTE(
        new KeyBind(KeyEvent.VK_M, CUSTOM_MASK_1.mask() | ALT_DOWN_MASK)
    ),
    // Step increase/decrease size of window.
    WINDOW_SIZE_INCREASE_LESS(
        new KeyBind(KeyEvent.VK_QUOTE, SHIFT_DOWN_MASK)
    ),
    WINDOW_SIZE_INCREASE(
        new KeyBind(KeyEvent.VK_QUOTE)
    ),
    WINDOW_SIZE_INCREASE_MORE(
        new KeyBind(KeyEvent.VK_QUOTE, CTRL_DOWN_MASK)
    ),
    WINDOW_SIZE_DECREASE_LESS(
        new KeyBind(KeyEvent.VK_SEMICOLON, SHIFT_DOWN_MASK)
    ),
    WINDOW_SIZE_DECREASE(
        new KeyBind(KeyEvent.VK_SEMICOLON)
    ),
    WINDOW_SIZE_DECREASE_MORE(
        new KeyBind(KeyEvent.VK_SEMICOLON, CTRL_DOWN_MASK)
    ),
    // Step increase/decrease size of ALL windows.
    WINDOWS_SIZE_INCREASE_LESS(
        new KeyBind(KeyEvent.VK_QUOTE, ALT_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    WINDOWS_SIZE_INCREASE(
        new KeyBind(KeyEvent.VK_QUOTE, ALT_DOWN_MASK)
    ),
    WINDOWS_SIZE_INCREASE_MORE(
        new KeyBind(KeyEvent.VK_QUOTE, ALT_DOWN_MASK | CTRL_DOWN_MASK)
    ),
    WINDOWS_SIZE_DECREASE_LESS(
        new KeyBind(KeyEvent.VK_SEMICOLON, ALT_DOWN_MASK | SHIFT_DOWN_MASK)
    ),
    WINDOWS_SIZE_DECREASE(
        new KeyBind(KeyEvent.VK_SEMICOLON, ALT_DOWN_MASK)
    ),
    WINDOWS_SIZE_DECREASE_MORE(
        new KeyBind(KeyEvent.VK_SEMICOLON, ALT_DOWN_MASK | CTRL_DOWN_MASK)
    ),
    ZOOM_IN_LESS(
        new KeyBind(KeyEvent.VK_CLOSE_BRACKET, SHIFT_DOWN_MASK)
    ),
    ZOOM_IN(
        new KeyBind(KeyEvent.VK_CLOSE_BRACKET)
    ),
    ZOOM_IN_MORE(
        new KeyBind(KeyEvent.VK_CLOSE_BRACKET, CTRL_DOWN_MASK)
    ),
    ZOOM_OUT_LESS(
        new KeyBind(KeyEvent.VK_OPEN_BRACKET, SHIFT_DOWN_MASK)
    ),
    ZOOM_OUT(
        new KeyBind(KeyEvent.VK_OPEN_BRACKET)
    ),
    ZOOM_OUT_MORE(
        new KeyBind(KeyEvent.VK_OPEN_BRACKET, CTRL_DOWN_MASK)
    );
    
    /**
     * An array of zero or more binds associated with each shortcut. While the
     * specified type is {@link Bind}, each value will take the form of
     * {@link KeyBind} or {@link MouseBind}, depending on the type of input. The
     * values stored here only indicate the default bindings for each
     * {@link Shortcut}, and may not represent the current configuration at runtime.
     */
    private final Bind<?>[] binds;
    
    /**
     * Creates the shortcut with the passed default bindings.
     * <p>
     * <b>Never pass <code>null</code> as an argument to this constructor.</b> If
     * you wish to not specify default bindings, simply omit the arguments from the
     * constructor call. Callers of the {@link #binds()} method should only expect an
     * empty array at worst.
     * 
     * 
     * @param binds - zero or more bindings, typically represented by
     *              {@link KeyBind} or {@link MouseBind}.
     */
    private Shortcut(Bind<?>... binds) {
        this.binds = binds;
        Objects.requireNonNull(this.binds, "<!> Error constructing Shortcut: Default bindings array cannot be null; remove null value from constructor.");
    }
    
    /**
     * Retrieves the default bindings for the shortcut. If there are no bindings by
     * default, this array may be empty, but should never be <code>null</code>.
     * 
     * @return an array zero or more default binds.
     */
    public Bind<?>[] binds() {
        return this.binds;
    }
    
    /**
     * Retrieves all of the {@link KeyBind} instances within the default bindings
     * for this shortcut. Any {@link MouseBind} instances will be ignored and not
     * included in the returned list.
     * 
     * @return an {@link ArrayList} of default key binds.
     */
    public ArrayList<KeyBind> keyBinds() {
        final ArrayList<KeyBind> keyBinds = new ArrayList<>(this.binds.length);
        for (final Bind<?> b : this.binds) {
            if (b instanceof KeyBind kb) {
                keyBinds.add(kb);
            }
        }
        return keyBinds;
    }

    /**
     * Retrieves all of the {@link MouseBind} instances within the default bindings
     * for this shortcut. Any {@link KeyBind} instances will be ignored and not
     * included in the returned list.
     * 
     * @return an {@link ArrayList} of default mouse binds.
     */
    public ArrayList<MouseBind> mouseBinds() {
        final ArrayList<MouseBind> mouseBinds = new ArrayList<>(this.binds.length);
        for (final Bind<?> b : this.binds) {
            if (b instanceof MouseBind mb) {
                mouseBinds.add(mb);
            }
        }
        return mouseBinds;
    }
}
