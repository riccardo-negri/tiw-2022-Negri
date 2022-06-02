package it.polimi.tiw.utils;

public class ParameterValidator {
    public static boolean validate(String param) {
        return (param!=null && !param.isEmpty());
    }
}