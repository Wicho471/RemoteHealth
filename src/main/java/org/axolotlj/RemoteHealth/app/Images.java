package org.axolotlj.RemoteHealth.app;

import java.net.URL;

import org.axolotlj.RemoteHealth.util.Paths;

import javafx.scene.image.Image;

/**
 * Carga en memoria (en tiempo de clase) todos los recursos gráficos del
 * proyecto. Cada constante corresponde a la ruta declarada en {@link Paths}.
 *
 * Ejemplo de uso: someButton.setGraphic(new
 * ImageView(Images.IMG_BUTTONS_CONFIGURACIONES));
 */
public class Images {

	/** Método auxiliar que centraliza la creación de la instancia {@link Image}. */
	private static Image load(String resourcePath) {
		if(resourcePath == null) return null; 
		
		URL resourceUrl = Images.class.getResource(resourcePath);

		if (resourceUrl == null) {
		    throw new IllegalArgumentException("No se encontró el recurso: " + resourcePath);
		}

		return new Image(resourceUrl.toExternalForm());

	}

	// ========================= IMG/buttons =========================
	public static final Image IMG_BUTTONS_ABRIR = load(Paths.IMG_BUTTONS_ABRIR_PNG);
	public static final Image IMG_BUTTONS_ACTUALIZAR = load(Paths.IMG_BUTTONS_ACTUALIZAR_PNG);
	public static final Image IMG_BUTTONS_AGREGAR_ARCHIVO = load(Paths.IMG_BUTTONS_AGREGAR_ARCHIVO_PNG);
	public static final Image IMG_BUTTONS_ANADIR = load(Paths.IMG_BUTTONS_ANADIR_PNG);
	public static final Image IMG_BUTTONS_BACK = load(Paths.IMG_BUTTONS_BACK_PNG);
	public static final Image IMG_BUTTONS_BUSQUEDA = load(Paths.IMG_BUTTONS_BUSQUEDA_PNG);
	public static final Image IMG_BUTTONS_CAMARA_FOTOGRAFICA = load(Paths.IMG_BUTTONS_CAMARA_FOTOGRAFICA_PNG);
	public static final Image IMG_BUTTONS_CARGADOR = load(Paths.IMG_BUTTONS_CARGADOR_PNG);
	public static final Image IMG_BUTTONS_CERRAR_SESION = load(Paths.IMG_BUTTONS_CERRAR_SESION_PNG);
	public static final Image IMG_BUTTONS_CHEQUE = load(Paths.IMG_BUTTONS_CHEQUE_PNG);
	public static final Image IMG_BUTTONS_CLOSE = load(Paths.IMG_BUTTONS_CLOSE_PNG);
	public static final Image IMG_BUTTONS_CODIGO_QR = load(Paths.IMG_BUTTONS_CODIGO_QR_PNG);
	public static final Image IMG_BUTTONS_CONECTAR = load(Paths.IMG_BUTTONS_CONECTAR_PNG);
	public static final Image IMG_BUTTONS_CONFIGURACION = load(Paths.IMG_BUTTONS_CONFIGURACION_PNG);
	public static final Image IMG_BUTTONS_CONFIGURACIONES = load(Paths.IMG_BUTTONS_CONFIGURACIONES_PNG);
	public static final Image IMG_BUTTONS_DESCARGAR = load(Paths.IMG_BUTTONS_DESCARGAR_PNG);
	public static final Image IMG_BUTTONS_ELIMINAR = load(Paths.IMG_BUTTONS_ELIMINAR_PNG);
	public static final Image IMG_BUTTONS_ENGRANAJE = load(Paths.IMG_BUTTONS_ENGRANAJE_PNG);
	public static final Image IMG_BUTTONS_FAST_FORWARD = load(Paths.IMG_BUTTONS_FAST_FORWARD_PNG);
	public static final Image IMG_BUTTONS_GESTION_DEL_TIEMPO = load(Paths.IMG_BUTTONS_GESTION_DEL_TIEMPO_PNG);
	public static final Image IMG_BUTTONS_GRABACION = load(Paths.IMG_BUTTONS_GRABACION_PNG);
	public static final Image IMG_BUTTONS_LIMPIEZA_DE_DATOS = load(Paths.IMG_BUTTONS_LIMPIEZA_DE_DATOS_PNG);
	public static final Image IMG_BUTTONS_LISTA_DE_VERIFICACION = load(Paths.IMG_BUTTONS_LISTA_DE_VERIFICACION_PNG);
	public static final Image IMG_BUTTONS_LOADING = load(Paths.IMG_BUTTONS_LOADING_PNG);
	public static final Image IMG_BUTTONS_NEXT = load(Paths.IMG_BUTTONS_NEXT_PNG);
	public static final Image IMG_BUTTONS_NO_WIFI = load(Paths.IMG_BUTTONS_NO_WIFI_PNG);
	public static final Image IMG_BUTTONS_RETURN = load(Paths.IMG_BUTTONS_RETURN_PNG);
	public static final Image IMG_BUTTONS_REWIND = load(Paths.IMG_BUTTONS_REWIND_PNG);
	public static final Image IMG_BUTTONS_SUBIR = load(Paths.IMG_BUTTONS_SUBIR_PNG);

	// ========================= IMG/favicons =========================
	public static final Image IMG_FAVICONS_ACERCA_DE = load(Paths.IMG_FAVICONS_ACERCA_DE_PNG);
	public static final Image IMG_FAVICONS_ANALYSIS = load(Paths.IMG_FAVICONS_ANALYSIS_PNG);
	public static final Image IMG_FAVICONS_APP_ICON = load(Paths.IMG_FAVICONS_APP_ICON_PNG);
	public static final Image IMG_FAVICONS_DASHBOARD = load(Paths.IMG_FAVICONS_DASHBOARD_PNG);
	public static final Image IMG_FAVICONS_HEALTH_CHECKUP = load(Paths.IMG_FAVICONS_HEALTH_CHECKUP_PNG);
	public static final Image IMG_FAVICONS_INTERACTIVO = load(Paths.IMG_FAVICONS_INTERACTIVO_PNG);
	public static final Image IMG_FAVICONS_MANUAL = load(Paths.IMG_FAVICONS_MANUAL_PNG);
	public static final Image IMG_FAVICONS_MICROCONTROLER = load(Paths.IMG_FAVICONS_MICROCONTROLER_PNG);
	public static final Image IMG_FAVICONS_MONITOREO_CARDIACO = load(Paths.IMG_FAVICONS_MONITOREO_CARDIACO_PNG);
	public static final Image IMG_FAVICONS_OPTIONS = load(Paths.IMG_FAVICONS_OPTIONS_PNG);
	public static final Image IMG_FAVICONS_QR = load(Paths.IMG_FAVICONS_QR_PNG);
	public static final Image IMG_FAVICONS_RENDIMIENTO = load(Paths.IMG_FAVICONS_RENDIMIENTO_PNG);
	public static final Image IMG_FAVICONS_SETTINGS = load(Paths.IMG_FAVICONS_SETTINGS_PNG);
	public static final Image IMG_FAVICONS_SIMULACION = load(Paths.IMG_FAVICONS_SIMULACION_PNG);
	public static final Image IMG_FAVICONS_UPLOAD = load(Paths.IMG_FAVICONS_UPLOAD_PNG);
	public static final Image IMG_FAVICONS_CSV = load(Paths.IMG_FAVICONS_CSV_PNG);

	// ========================= IMG/icons =========================
	public static final Image IMG_ICONS_BOTON_X = load(Paths.IMG_ICONS_BOTON_X_PNG);
	public static final Image IMG_ICONS_COMPROBADO = load(Paths.IMG_ICONS_COMPROBADO_PNG);
	public static final Image IMG_ICONS_REC_BUTTON = load(Paths.IMG_ICONS_REC_BUTTON_PNG);
	public static final Image IMG_ICONS_STOP_RECORD = load(Paths.IMG_ICONS_STOP_RECORD_PNG);
	public static final Image IMG_ICONS_TRABAJO_EN_PROGRESO = load(Paths.IMG_ICONS_TRABAJO_EN_PROGRESO_PNG);
	public static final Image IMG_ICONS_DELETE = load(Paths.IMG_ICONS_DELETE_PNG);
	public static final Image IMG_ICONS_GREEN = load(Paths.IMG_ICONS_GREEN_PNG);
	public static final Image IMG_ICONS_RED = load(Paths.IMG_ICONS_RED_PNG);
	
	// ========================= IMG/icons/alerts =========================
	public static final Image IMG_ICONS_WARNING = load(Paths.IMG_ALERT_WARNING);
	public static final Image IMG_ICONS_ERROR = load(Paths.IMG_ALERT_ERROR);
	public static final Image IMG_ICONS_INFO = load(Paths.IMG_ALERT_INFO);
	public static final Image IMG_ICONS_CHOISE = load(Paths.IMG_ALERT_CHOICE);
	
	// ========================= IMG/icons/vitals =========================
	public static final Image IMG_VITALS_ASK = load(Paths.IMG_ASK_PNG);
	public static final Image IMG_VITALS_DYSPNOEA_ALERT = load(Paths.IMG_DYSPNOEA_ALERT_PNG);
	public static final Image IMG_VITALS_HEARTH_ALERT = load(Paths.IMG_HEARTH_ALERT_PNG);
	public static final Image IMG_VITALS_OK = load(Paths.IMG_OK_PNG);
	public static final Image IMG_VITALS_STOP_ALERT = load(Paths.IMG_STOP_ALERT_PNG);
	public static final Image IMG_VITALS_TEMP_ALERT = load(Paths.IMG_TEMP_ALERT_PNG);

	// ========================= IMG/langs =========================
	public static final Image IMG_LANGS_ES = load(Paths.IMG_LANGS_ES_PNG);
	public static final Image IMG_LANGS_US = load(Paths.IMG_LANGS_US_PNG);

	// ========================= IMG/sensors =========================
	public static final Image IMG_SENSORS_AD8232 = load(Paths.IMG_SENSORS_AD8232_PNG);
	public static final Image IMG_SENSORS_BATERIA = load(Paths.IMG_SENSORS_BATERIA_JPG);
	public static final Image IMG_SENSORS_ESP32 = load(Paths.IMG_SENSORS_ESP32_PNG);
	public static final Image IMG_SENSORS_MAX30102 = load(Paths.IMG_SENSORS_MAX30102_PNG);
	public static final Image IMG_SENSORS_MLX90614T = load(Paths.IMG_SENSORS_MLX90614T_PNG);
	public static final Image IMG_SENSORS_MMA8452Q = load(Paths.IMG_SENSORS_MMA8452Q_PNG);
	
	// ========================= IMG/misc =========================
	public static final Image IMG_MISC_EXCEL = load(Paths.IMG_MISC_EXCEL_PNG);

}
