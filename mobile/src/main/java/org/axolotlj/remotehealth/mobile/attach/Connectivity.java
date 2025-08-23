package org.axolotlj.remotehealth.mobile.attach;

import java.util.Optional;

import com.gluonhq.attach.connectivity.ConnectivityService;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;

/**
 * Clase de utilidad que facilita el uso del {@link ConnectivityService}.
 * Permite verificar si hay conectividad y registrar listeners de forma sencilla.
 */
public class Connectivity {

    private final ConnectivityService service;

    private Connectivity(ConnectivityService service) {
        this.service = service;
    }

    /**
     * Crea una instancia de {@link NetworkMonitor} si el servicio está disponible.
     * @return Optional de {@link NetworkMonitor}.
     */
    public static Optional<Connectivity> create() {
        Optional<ConnectivityService> optionalService = ConnectivityService.create();
        return optionalService.map(Connectivity::new);
    }

    /**
     * Devuelve true si hay conectividad de red disponible, false en caso contrario.
     * @return estado de conectividad.
     */
    public boolean isConnected() {
        return service.isConnected();
    }

    /**
     * Devuelve la propiedad de solo lectura que indica conectividad de red.
     * @return {@link ReadOnlyBooleanProperty} de conectividad.
     */
    public ReadOnlyBooleanProperty connectedProperty() {
        return service.connectedProperty();
    }

    /**
     * Registra un listener que se ejecutará cada vez que cambie el estado de conectividad.
     * @param listener el listener que recibe el nuevo valor de conectividad.
     */
    public void onConnectivityChange(ChangeListener<? super Boolean> listener) {
        service.connectedProperty().addListener(listener);
    }

    /**
     * Registra un listener que se ejecuta una sola vez cuando cambia la conectividad.
     * @param listener el listener que se ejecuta al detectar un cambio.
     */
    public void onConnectivityChangeOnce(ChangeListener<? super Boolean> listener) {
        service.connectedProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(javafx.beans.value.ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) {
                listener.changed(obs, oldVal, newVal);
                obs.removeListener(this);
            }
        });
    }
    
    private void example() {
    	// TODO Auto-generated method stub
        Connectivity.create().ifPresentOrElse(monitor -> {
            System.out.println("Conectado inicialmente: " + monitor.isConnected());

            monitor.onConnectivityChange((obs, oldVal, newVal) ->
                    System.out.println("Cambio de conectividad: " + newVal));
        }, () -> System.out.println("No se pudo inicializar el monitor de red."));
	}
}
