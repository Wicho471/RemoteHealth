package org.axolotlj.remotehealth.mobile.attach;

import java.time.ZonedDateTime;
import java.util.Optional;

import com.gluonhq.attach.localnotifications.LocalNotificationsService;
import com.gluonhq.attach.localnotifications.Notification;

import javafx.collections.ObservableList;

/**
 * Clase de utilidad que facilita el uso de {@link LocalNotificationsService}.
 * Permite programar, eliminar y consultar notificaciones locales de manera sencilla.
 */
public class Notifications {

    private final LocalNotificationsService service;
    private final ObservableList<Notification> notifications;

    private Notifications(LocalNotificationsService service) {
        this.service = service;
        this.notifications = service.getNotifications();
    }

    /**
     * Crea una instancia de {@link Notifications} si el servicio está disponible.
     * @return Optional de {@link Notifications}.
     */
    public static Optional<Notifications> create() {
        Optional<LocalNotificationsService> optionalService = LocalNotificationsService.create();
        return optionalService.map(Notifications::new);
    }

    /**
     * Devuelve la lista observable de notificaciones.
     * @return lista observable de {@link Notification}.
     */
    public ObservableList<Notification> getNotifications() {
        return notifications;
    }

    /**
     * Agrega una notificación al servicio.
     * @param notification notificación a agregar.
     */
    public void addNotification(Notification notification) {
        if (notification != null) {
            notifications.add(notification);
        }
    }

    /**
     * Agrega varias notificaciones al servicio.
     * @param notificationsToAdd notificaciones a agregar.
     */
    public void addNotifications(Notification... notificationsToAdd) {
        if (notificationsToAdd != null && notificationsToAdd.length > 0) {
            for (Notification notification : notificationsToAdd) {
				addNotification(notification);
			}
        }
    }

    /**
     * Elimina una notificación por su ID.
     * @param notificationId ID de la notificación a eliminar.
     */
    public void removeNotificationById(String notificationId) {
        notifications.removeIf(n -> n.getId().equals(notificationId));
    }

    /**
     * Elimina todas las notificaciones.
     */
    public void clearAllNotifications() {
        notifications.clear();
    }

    /**
     * Busca una notificación por su ID.
     * @param notificationId ID de la notificación a buscar.
     * @return la notificación si existe, null si no.
     */
    public Notification findNotificationById(String notificationId) {
        return notifications.stream()
                .filter(n -> n.getId().equals(notificationId))
                .findFirst()
                .orElse(null);
    }
    
    private void example() {
        Notifications.create().ifPresentOrElse(manager -> {
            Notification notification = new Notification(
                    "notif123",
                    "¡Tienes una notificación!",
                    ZonedDateTime.now().plusSeconds(10),
                    () -> System.out.println("Notificación ejecutada!")
            );

            manager.addNotification(notification);

            System.out.println("Notificaciones programadas: " + manager.getNotifications().size());
        }, () -> System.out.println("No se pudo inicializar el gestor de notificaciones."));

	}
}
