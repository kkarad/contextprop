package org.kkarad.contextprop;

import static java.lang.String.format;

interface TrueFalse {
    static boolean parse(String value) {
        if ("true".equals(value)) {
            return true;
        } else if ("false".equals(value)) {
            return false;
        } else {
            throw new ParseException(format("value '%s' cannot be parsed to a boolean; valid values are 'true' and 'false'", value));
        }
    }
}
