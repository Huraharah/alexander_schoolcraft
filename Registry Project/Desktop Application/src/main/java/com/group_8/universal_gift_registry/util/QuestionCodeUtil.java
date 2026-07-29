package com.group_8.universal_gift_registry.util;
/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/21/2024
 * @version: 3.0
 */

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class QuestionCodeUtil {
    private static final Map<String, String> QUESTION_CODES;

    static {
        QUESTION_CODES = new HashMap<>();
        QUESTION_CODES.put("What was your childhood nickname?", "SECQ1");
        QUESTION_CODES.put("In what city did your parents meet?", "SECQ2");
        QUESTION_CODES.put("What is the name of your favorite childhood friend?", "SECQ3");
        QUESTION_CODES.put("What is your favorite team?", "SECQ4");
        QUESTION_CODES.put("What was your dream job as a child?", "SECQ5");
        QUESTION_CODES.put("What is your favorite movie?", "SECQ6");
        QUESTION_CODES.put("In what town was your first job?", "SECQ7");
        QUESTION_CODES.put("What was the make and model of your first car?", "SECQ8");
        QUESTION_CODES.put("Where did you vacation last year?", "SECQ9");
        QUESTION_CODES.put("What is the name of your pet?", "SECQ0 ");
        QUESTION_CODES.put("Other", "OTHR");
    }

    public static String getCodeFromQuestion(String question) {
        return QUESTION_CODES.getOrDefault(question, "");
    }

    public static String getQuestionFromCode(String code) {
        for (Map.Entry<String, String> entry : QUESTION_CODES.entrySet()) {
            if (entry.getValue().equals(code)) {
                return entry.getKey();
            }
        }
        return "Other";
    }
}


