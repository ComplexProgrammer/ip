package ip.complexprogrammer.uz.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.widget.RemoteViews;

import java.util.Date;

import ip.complexprogrammer.uz.R;
import ip.complexprogrammer.uz.models.IpInfo;
import ip.complexprogrammer.uz.services.IpRepository;
import ip.complexprogrammer.uz.ui.IpInfoPresenter;

/** Home-screen widget with the device's public IP. Tap to refresh. */
public class IpWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_REFRESH = "ip.complexprogrammer.uz.widget.REFRESH";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        refresh(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction())) refresh(context);
    }

    private void refresh(Context context) {
        Context app = context.getApplicationContext();
        PendingResult pending = goAsync();
        push(app, base(app, app.getString(R.string.loading), null, null));
        new IpRepository(app).lookup("", new IpRepository.Callback() {
            @Override
            public void onSuccess(IpInfo info) {
                String updated = app.getString(R.string.widget_updated, DateFormat.getTimeFormat(app).format(new Date()));
                push(app, base(app, info.ip, new IpInfoPresenter(app, info).place(), updated));
                pending.finish();
            }

            @Override
            public void onError(IpRepository.ErrorType type, String detail) {
                push(app, base(app, app.getString(R.string.widget_error), null,
                        app.getString(R.string.widget_tap_to_refresh)));
                pending.finish();
            }
        });
    }

    private static RemoteViews base(Context context, String ip, String place, String updated) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_ip);
        views.setTextViewText(R.id.widget_ip, ip);
        views.setTextViewText(R.id.widget_place, place == null ? "" : place);
        views.setTextViewText(R.id.widget_updated, updated == null ? "" : updated);
        Intent refresh = new Intent(context, IpWidgetProvider.class).setAction(ACTION_REFRESH);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        views.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getBroadcast(context, 0, refresh, flags));
        return views;
    }

    private static void push(Context context, RemoteViews views) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(new ComponentName(context, IpWidgetProvider.class), views);
    }
}
