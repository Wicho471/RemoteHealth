package org.axolotlj.remotehealth.core.analysis;

import java.util.function.Consumer;

/**
 * Dispara SOLO una vez por episodio de anomalía.
 * - persistenceMs: tiempo mínimo anómalo para disparar.
 * - recoveryMs: tiempo mínimo en normalidad para “liberar” un nuevo episodio.
 * - cooldownMs: (opcional) tiempo de bloqueo global después de un disparo.
 */
public class AnomalyTrigger {

    public enum State { NORMAL, PENDING, ALERTED }

    private final long persistenceMs;
    private final long recoveryMs;
    private final long cooldownMs;
    private final Consumer<String> onAnomaly;

    private State state = State.NORMAL;

    private long anomalyStart = 0L;     // inicio de la anomalía (para persistencia)
    private long recoveryStart = 0L;    // inicio de la normalidad (para recuperación)
    private long lastAlertTime = Long.MIN_VALUE; // último disparo (para cooldown)

    public AnomalyTrigger(long persistenceMs, long cooldownMs, Consumer<String> onAnomaly) {
        this(persistenceMs, /*recoveryMs=*/persistenceMs, cooldownMs, onAnomaly);
    }

    public AnomalyTrigger(long persistenceMs, long recoveryMs, long cooldownMs, Consumer<String> onAnomaly) {
        this.persistenceMs = persistenceMs;
        this.recoveryMs = recoveryMs;
        this.cooldownMs = cooldownMs;
        this.onAnomaly = onAnomaly;
    }

    /**
     * Evalúa la condición a tiempo-REAL del stream (usar el mismo reloj que las muestras).
     * @param isAnomaly condición anómala actual
     * @param nowMs     timestamp del stream (no System.currentTimeMillis si puedes)
     * @param message   mensaje a enviar al disparar
     */
    public synchronized void evaluate(boolean isAnomaly, long nowMs, String message) {
        // Bloqueo global por cooldown (si está configurado)
        boolean inGlobalCooldown = (cooldownMs > 0) && (nowMs - lastAlertTime < cooldownMs);

        switch (state) {

            case NORMAL:
                recoveryStart = 0L; // fuera de recuperación
                if (isAnomaly && !inGlobalCooldown) {
                    state = State.PENDING;
                    anomalyStart = nowMs;
                }
                break;

            case PENDING:
                if (!isAnomaly) {
                    // Cancelamos episodio: volvió a normal antes de cumplir persistencia
                    state = State.NORMAL;
                } else {
                    if (nowMs - anomalyStart >= persistenceMs && !inGlobalCooldown) {
                        // Se confirmó el episodio -> disparar una única vez
                        onAnomaly.accept(message);
                        lastAlertTime = nowMs;
                        state = State.ALERTED;
                    }
                }
                break;

            case ALERTED:
                // Ya se disparó; no volverá a disparar hasta que se recupere
                if (!isAnomaly) {
                    if (recoveryStart == 0L) {
                        recoveryStart = nowMs;
                    } else if (nowMs - recoveryStart >= recoveryMs) {
                        // Recuperación confirmada: listo para futuros episodios (respetando cooldown)
                        state = State.NORMAL;
                        recoveryStart = 0L;
                    }
                } else {
                    // Sigue anómalo, seguimos “latched” en ALERTED
                    recoveryStart = 0L;
                }
                break;
        }
    }

    public synchronized void reset() {
        state = State.NORMAL;
        anomalyStart = 0L;
        recoveryStart = 0L;
        // lastAlertTime NO se toca para respetar cooldown global, si existe
    }
}
