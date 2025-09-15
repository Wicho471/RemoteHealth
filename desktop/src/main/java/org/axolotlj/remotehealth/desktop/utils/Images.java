package org.axolotlj.remotehealth.desktop.utils;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.path.SharedPaths;

import javafx.scene.image.Image;

/**
 * Carga en memoria (en tiempo de clase) todos los recursos gráficos del
 * proyecto. Cada constante corresponde a la ruta declarada en {@link SharedPaths}.
 *
 * Ejemplo de uso: someButton.setGraphic(new
 * ImageView(Images.IMG_BUTTONS_CONFIGURACIONES));
 */
public class Images {
	private static DataLogger logger = Log.get();

	/**
	 * Carga una imagen desde el recurso especificado.
	 *
	 * @param resourcePath Ruta relativa al archivo de imagen (por ejemplo: "/img/icon.png").
	 * @return Instancia de {@link Image}, o {@code null} si no se pudo cargar.
	 */
	private static Image load(String resourcePath) {
	    if (resourcePath == null) {
	        logger.logFatal("La ruta del recurso de imagen es null.");
	        throw new IllegalArgumentException("La ruta del recurso no puede ser null.");
	    }

	    try (var stream = Images.class.getResourceAsStream(resourcePath)) {
	        if (stream == null) {
	            logger.logWarn("No se encontró el recurso de imagen: " + resourcePath);
	            return null;
	        }

	        Image image = new Image(stream);
	        return image;

	    } catch (Exception e) {
	        logger.logException("Excepción al cargar la imagen '" + resourcePath + "'", e);
	        return null;
	    }
	}

	// ========================= IMG/buttons =========================
	public static final Image IMG_BUTTONS_ABRIR = load(SharedPaths.IMG_BUTTONS_ABRIR_PNG);
	public static final Image IMG_BUTTONS_ACTUALIZAR = load(SharedPaths.IMG_BUTTONS_ACTUALIZAR_PNG);
	public static final Image IMG_BUTTONS_AGREGAR_ARCHIVO = load(SharedPaths.IMG_BUTTONS_AGREGAR_ARCHIVO_PNG);
	public static final Image IMG_BUTTONS_ANADIR = load(SharedPaths.IMG_BUTTONS_ANADIR_PNG);
	public static final Image IMG_BUTTONS_BACK = load(SharedPaths.IMG_BUTTONS_BACK_PNG);
	public static final Image IMG_BUTTONS_BUSQUEDA = load(SharedPaths.IMG_BUTTONS_BUSQUEDA_PNG);
	public static final Image IMG_BUTTONS_CAMARA_FOTOGRAFICA = load(SharedPaths.IMG_BUTTONS_CAMARA_FOTOGRAFICA_PNG);
	public static final Image IMG_BUTTONS_CARGADOR = load(SharedPaths.IMG_BUTTONS_CARGADOR_PNG);
	public static final Image IMG_BUTTONS_CERRAR_SESION = load(SharedPaths.IMG_BUTTONS_CERRAR_SESION_PNG);
	public static final Image IMG_BUTTONS_CHEQUE = load(SharedPaths.IMG_BUTTONS_CHEQUE_PNG);
	public static final Image IMG_BUTTONS_CLOSE = load(SharedPaths.IMG_BUTTONS_CLOSE_PNG);
	public static final Image IMG_BUTTONS_CODIGO_QR = load(SharedPaths.IMG_BUTTONS_CODIGO_QR_PNG);
	public static final Image IMG_BUTTONS_CONECTAR = load(SharedPaths.IMG_BUTTONS_CONECTAR_PNG);
	public static final Image IMG_BUTTONS_CONFIGURACION = load(SharedPaths.IMG_BUTTONS_CONFIGURACION_PNG);
	public static final Image IMG_BUTTONS_CONFIGURACIONES = load(SharedPaths.IMG_BUTTONS_CONFIGURACIONES_PNG);
	public static final Image IMG_BUTTONS_DESCARGAR = load(SharedPaths.IMG_BUTTONS_DESCARGAR_PNG);
	public static final Image IMG_BUTTONS_ELIMINAR = load(SharedPaths.IMG_BUTTONS_ELIMINAR_PNG);
	public static final Image IMG_BUTTONS_ENGRANAJE = load(SharedPaths.IMG_BUTTONS_ENGRANAJE_PNG);
	public static final Image IMG_BUTTONS_FAST_FORWARD = load(SharedPaths.IMG_BUTTONS_FAST_FORWARD_PNG);
	public static final Image IMG_BUTTONS_GESTION_DEL_TIEMPO = load(SharedPaths.IMG_BUTTONS_GESTION_DEL_TIEMPO_PNG);
	public static final Image IMG_BUTTONS_GRABACION = load(SharedPaths.IMG_BUTTONS_GRABACION_PNG);
	public static final Image IMG_BUTTONS_LIMPIEZA_DE_DATOS = load(SharedPaths.IMG_BUTTONS_LIMPIEZA_DE_DATOS_PNG);
	public static final Image IMG_BUTTONS_LISTA_DE_VERIFICACION = load(SharedPaths.IMG_BUTTONS_LISTA_DE_VERIFICACION_PNG);
	public static final Image IMG_BUTTONS_LOADING = load(SharedPaths.IMG_BUTTONS_LOADING_PNG);
	public static final Image IMG_BUTTONS_NEXT = load(SharedPaths.IMG_BUTTONS_NEXT_PNG);
	public static final Image IMG_BUTTONS_NO_WIFI = load(SharedPaths.IMG_BUTTONS_NO_WIFI_PNG);
	public static final Image IMG_BUTTONS_RETURN = load(SharedPaths.IMG_BUTTONS_RETURN_PNG);
	public static final Image IMG_BUTTONS_REWIND = load(SharedPaths.IMG_BUTTONS_REWIND_PNG);
	public static final Image IMG_BUTTONS_SUBIR = load(SharedPaths.IMG_BUTTONS_SUBIR_PNG);
	public static final Image IMG_BUTTONS_FLECHA_DERECHA = load(SharedPaths.IMG_BUTTONS_FLECHA_DERECHA_PNG);

	// ========================= IMG/favicons =========================
	public static final Image IMG_FAVICONS_ACERCA_DE = load(SharedPaths.IMG_FAVICONS_ACERCA_DE_PNG);
	public static final Image IMG_FAVICONS_ANALYSIS = load(SharedPaths.IMG_FAVICONS_ANALYSIS_PNG);
	public static final Image IMG_FAVICONS_APP_ICON = load(SharedPaths.IMG_FAVICONS_APP_ICON_PNG);
	public static final Image IMG_FAVICONS_DASHBOARD = load(SharedPaths.IMG_FAVICONS_DASHBOARD_PNG);
	public static final Image IMG_FAVICONS_HEALTH_CHECKUP = load(SharedPaths.IMG_FAVICONS_HEALTH_CHECKUP_PNG);
	public static final Image IMG_FAVICONS_INTERACTIVO = load(SharedPaths.IMG_FAVICONS_INTERACTIVO_PNG);
	public static final Image IMG_FAVICONS_MANUAL = load(SharedPaths.IMG_FAVICONS_MANUAL_PNG);
	public static final Image IMG_FAVICONS_MICROCONTROLER = load(SharedPaths.IMG_FAVICONS_MICROCONTROLER_PNG);
	public static final Image IMG_FAVICONS_MONITOREO_CARDIACO = load(SharedPaths.IMG_FAVICONS_MONITOREO_CARDIACO_PNG);
	public static final Image IMG_FAVICONS_OPTIONS = load(SharedPaths.IMG_FAVICONS_OPTIONS_PNG);
	public static final Image IMG_FAVICONS_QR = load(SharedPaths.IMG_FAVICONS_QR_PNG);
	public static final Image IMG_FAVICONS_RENDIMIENTO = load(SharedPaths.IMG_FAVICONS_RENDIMIENTO_PNG);
	public static final Image IMG_FAVICONS_SETTINGS = load(SharedPaths.IMG_FAVICONS_SETTINGS_PNG);
	public static final Image IMG_FAVICONS_SIMULACION = load(SharedPaths.IMG_FAVICONS_SIMULACION_PNG);
	public static final Image IMG_FAVICONS_UPLOAD = load(SharedPaths.IMG_FAVICONS_UPLOAD_PNG);
	public static final Image IMG_FAVICONS_CSV = load(SharedPaths.IMG_FAVICONS_CSV_PNG);
	public static final Image IMG_FAVICONS_LOG = load(SharedPaths.IMG_FAVICONS_LOG_PNG);

	// ========================= IMG/icons =========================
	public static final Image IMG_ICONS_BOTON_X = load(SharedPaths.IMG_ICONS_BOTON_X_PNG);
	public static final Image IMG_ICONS_COMPROBADO = load(SharedPaths.IMG_ICONS_COMPROBADO_PNG);
	public static final Image IMG_ICONS_REC_BUTTON = load(SharedPaths.IMG_ICONS_REC_BUTTON_PNG);
	public static final Image IMG_ICONS_STOP_RECORD = load(SharedPaths.IMG_ICONS_STOP_RECORD_PNG);
	public static final Image IMG_ICONS_TRABAJO_EN_PROGRESO = load(SharedPaths.IMG_ICONS_TRABAJO_EN_PROGRESO_PNG);
	public static final Image IMG_ICONS_DELETE = load(SharedPaths.IMG_ICONS_DELETE_PNG);
	public static final Image IMG_ICONS_GREEN = load(SharedPaths.IMG_ICONS_GREEN_PNG);
	public static final Image IMG_ICONS_RED = load(SharedPaths.IMG_ICONS_RED_PNG);
	
	// ========================= IMG/icons/alerts =========================
	public static final Image IMG_ICONS_WARNING = load(SharedPaths.IMG_ALERT_WARNING);
	public static final Image IMG_ICONS_ERROR = load(SharedPaths.IMG_ALERT_ERROR);
	public static final Image IMG_ICONS_INFO = load(SharedPaths.IMG_ALERT_INFO);
	public static final Image IMG_ICONS_CHOISE = load(SharedPaths.IMG_ALERT_CHOICE);
	
	// ========================= IMG/icons/vitals =========================
	public static final Image IMG_VITALS_ASK = load(SharedPaths.IMG_ASK_PNG);
	public static final Image IMG_VITALS_DYSPNOEA_ALERT = load(SharedPaths.IMG_DYSPNOEA_ALERT_PNG);
	public static final Image IMG_VITALS_HEARTH_ALERT = load(SharedPaths.IMG_HEARTH_ALERT_PNG);
	public static final Image IMG_VITALS_OK = load(SharedPaths.IMG_OK_PNG);
	public static final Image IMG_VITALS_STOP_ALERT = load(SharedPaths.IMG_STOP_ALERT_PNG);
	public static final Image IMG_VITALS_TEMP_ALERT = load(SharedPaths.IMG_TEMP_ALERT_PNG);
	public static final Image IMG_VITALS_HIP_ALERT = load(SharedPaths.IMG_HIP_ALERT_PNG);

	// ========================= IMG/langs =========================
	public static final Image IMG_LANGS_ES = load(SharedPaths.IMG_LANGS_ES_PNG);
	public static final Image IMG_LANGS_US = load(SharedPaths.IMG_LANGS_US_PNG);

	// ========================= IMG/sensors =========================
	public static final Image IMG_SENSORS_AD8232 = load(SharedPaths.IMG_SENSORS_AD8232_PNG);
	public static final Image IMG_SENSORS_BATERIA = load(SharedPaths.IMG_SENSORS_BATERIA_JPG);
	public static final Image IMG_SENSORS_ESP32 = load(SharedPaths.IMG_SENSORS_ESP32_PNG);
	public static final Image IMG_SENSORS_MAX30102 = load(SharedPaths.IMG_SENSORS_MAX30102_PNG);
	public static final Image IMG_SENSORS_MLX90614T = load(SharedPaths.IMG_SENSORS_MLX90614T_PNG);
	public static final Image IMG_SENSORS_MMA8452Q = load(SharedPaths.IMG_SENSORS_MMA8452Q_PNG);
	
	// ========================= IMG/misc =========================
	public static final Image IMG_MISC_EXCEL = load(SharedPaths.IMG_MISC_EXCEL_PNG);

}
