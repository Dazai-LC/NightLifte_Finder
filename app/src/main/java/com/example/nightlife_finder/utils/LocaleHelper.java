package com.example.nightlife_finder.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.LocaleList;

import java.util.Locale;

public class LocaleHelper {

    public static Context applyLocale(Context context) {
        String languageCode = AppSettings.getLanguage(context);

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));

        return context.createConfigurationContext(configuration);
    }
}