package it.polimi.tiw.utils;

public final class ParameterValidator {
    public static boolean validate(String param) {
        return (param!=null && !param.isEmpty());
    }
}