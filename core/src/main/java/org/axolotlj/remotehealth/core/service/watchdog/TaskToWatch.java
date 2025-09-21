package org.axolotlj.remotehealth.core.service.watchdog;

/**
 * Una tarea que el watchdog supervisa.
 * 
 * El watchdog pasa un Runnable (`task`) que debes ejecutar cuando confirmes
 * que la tarea está viva. Ej: si estás vigilando el hilo UI, usas
 * Platform.runLater(task).
 */
public interface TaskToWatch {
    void check(Runnable task);
}
