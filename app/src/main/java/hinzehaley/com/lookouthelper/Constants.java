package hinzehaley.com.lookouthelper;

/**
 * Created by haleyhinze on 6/22/16.
 */
public class Constants {

    public static String GEOCOMMUNICATOR_DOMAIN = "https://gis.blm.gov/arcgis/rest/services/Cadastral/BLM_Natl_PLSS_CadNSDI/MapServer/exts/CadastralSpecialServices/";
    public static String TOWNSHIP_TO_LAT_LON = "GetLatLon?trs=";

    public static String PRINCIPAL_MERIDIAN_LINK = "https://www.blm.gov/lr2000/codes/CodeMeridian.htm";
    public static String LAT_LON_TO_TOWNSHIP = "GetTRS?";
    public static String UNITS_AND_FORMAT= "&units=DD&f=pjson";


    //Must be greater than 3
    public static int NUM_ELEVATION_REQUESTS = 20;
    //in meters
    public static int MAX_ELEVATOIN_REQUEST_DISTANCE = 80000;
    public static int START_ELEVATION_REQUEST_DISTANCE = 500;
    public static int DISTANCE_BETWEEN_VISIBLE_POINTS = 1000;

    //max number according to google elevation API
    public static int NUM_ELEVATIONS_PER_REQUEST = 512;

    public static double VERTICAL_DEGREE_OF_ERROR = .05;

    public static int NUM_OPTIONS_TO_DISPLAY = 4;


    public static String CROSS_LOOKOUT_HINT = "--Cross Lookout--";

}
