package org.axolotlj.RemoteHealth.lang;

import java.util.ArrayList;
import java.util.List;

public class LocaleChangeNotifier {

    private static final List<LocaleChangeListener> listeners = new ArrayList<>();

    public static void addListener(LocaleChangeListener listener) {
        listeners.add(listener);
    }

    public static void notifyLocaleChanged() {
        for (LocaleChangeListener listener : listeners) {
            listener.onLocaleChanged();
        }
    }
    
    public static void removeListener(LocaleChangeListener listener) {
        listeners.remove(listener);
    }
}
