package ip.complexprogrammer.uz.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class ChangeLanguage {
    private static SharedPreferences preferences;

    public static void setLocal(Context context, String langCode){
        Locale locale;
        Configuration config;
        locale = new Locale(langCode);
        Locale.setDefault(locale);
        config = new Configuration();
        config.locale = locale;
        context.createConfigurationContext(config);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        preferences=context.getSharedPreferences("C0mplexLanguage", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString("langCode",langCode);
        editor.apply();
    }

    public static String getLangCode(Context context){
        preferences=context.getSharedPreferences("C0mplexLanguage", Context.MODE_PRIVATE);
        return preferences.getString("langCode","en");
    }
}
