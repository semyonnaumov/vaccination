package com.naumov.identityservice.util;

import com.naumov.identityservice.model.IdentifiableEntity;

import java.time.LocalDate;

public final class JsonUtil {
    public static String translateEscapes(String string) {
        return string != null
                ? string.translateEscapes()
                : null;
    }

    public static String convertLocalDate(LocalDate localDate) {
        return localDate != null
                ? localDate.toString()
                : null;
    }

    public static Long extractId(IdentifiableEntity entity) {
        return entity != null
                ? entity.getId()
                : null;
    }
}
