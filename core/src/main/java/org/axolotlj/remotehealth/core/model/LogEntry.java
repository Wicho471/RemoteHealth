package org.axolotlj.remotehealth.core.model;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogEntry {
    private final String name;
    private final File file;
    private final String size;
    private final long lineCount;

    public LogEntry(String name, File file, String size, long lineCount) {
        this.name = name;
        this.file = file;
        this.size = size;
        this.lineCount = lineCount;
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

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public String getSize() {
        return size;
    }

    public long getLineCount() {
        return lineCount;
    }
}
