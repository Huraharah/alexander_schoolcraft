package com.group_8.universal_gift_registry.util;
/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/21/2024
 * @version: 3.0
 */

import java.util.HashMap;
import java.util.Map;

/**OccasionUtil is a hashmap utility class for the Occasion list field, to limit the amount of
 * space that is taken up on the database.
 */
public class OccasionUtil {

    private static final Map<String, String> OCCASION_CODES;

    static {
        OCCASION_CODES = new HashMap<>();
        OCCASION_CODES.put("Birthday", "BDAY");
        OCCASION_CODES.put("Anniversary", "ANNI");
        OCCASION_CODES.put("Wedding", "WEDG");
        OCCASION_CODES.put("Baby Shower", "BABY");
        OCCASION_CODES.put("Graduation", "GRAD");
        OCCASION_CODES.put("Holiday", "HDAY");
        OCCASION_CODES.put("Retirement", "RETR");
        OCCASION_CODES.put("Housewarming", "HOUS");
        OCCASION_CODES.put("Engagement", "ENGT");
        OCCASION_CODES.put("None", "NONE ");
        OCCASION_CODES.put("Other", "OTHR");
    }

    public static String getCodeForOccasion(String occasion) {
        return OCCASION_CODES.getOrDefault(occasion, "OTHER");
    }

    public static String getOccasionForCode(String code) {
        for (Map.Entry<String, String> entry : OCCASION_CODES.entrySet()) {
            if (entry.getValue().equals(code)) {
                return entry.getKey();
            }
        }
        return "Other";
    }
}