package org.axolotlj.RemoteHealth.model;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogEntry {
    private final String name;
    private final File file;

    public LogEntry(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public String getDisplayName() {
        // Remueve la extensión .log o .gz
        if (name.endsWith(".log")) return name.replace(".log", "");
        if (name.endsWith(".gz")) return name.replace(".gz", "");
        return name;
    }

    public String getFormattedDate() {
        long modified = file.lastModified();
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy, HH:mm", new Locale("es", "ES"));
        return sdf.format(new Date(modified));
    }

    public File getFile() {
        return file;
    }
}
