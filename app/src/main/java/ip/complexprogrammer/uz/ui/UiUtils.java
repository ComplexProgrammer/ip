package ip.complexprogrammer.uz.ui;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;

import ip.complexprogrammer.uz.R;

public final class UiUtils {
    private UiUtils() {
    }

    public static void copy(Context context, String label, String text, boolean announce) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null) return;
        cm.setPrimaryClip(ClipData.newPlainText(label, text));
        // Android 13+ shows its own confirmation.
        if (announce && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, context.getString(R.string.copied, shorten(text)), Toast.LENGTH_SHORT).show();
        }
    }

    public static String pasteText(Context context) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null || !cm.hasPrimaryClip() || cm.getPrimaryClip() == null
                || cm.getPrimaryClip().getItemCount() == 0) {
            return null;
        }
        CharSequence text = cm.getPrimaryClip().getItemAt(0).coerceToText(context);
        return text == null ? null : text.toString().trim();
    }

    public static void openUrl(Context context, String url) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.no_browser, Toast.LENGTH_LONG).show();
        }
    }

    /** Adds a "label: value" row that copies the value on long press. Skips null values. */
    public static void addRow(ViewGroup container, @StringRes int label, String value) {
        addRow(container, container.getContext().getString(label), value);
    }

    public static void addRow(ViewGroup container, String label, String value) {
        if (value == null || value.isEmpty()) return;
        View row = LayoutInflater.from(container.getContext()).inflate(R.layout.item_info_row, container, false);
        ((TextView) row.findViewById(R.id.label)).setText(label);
        ((TextView) row.findViewById(R.id.value)).setText(value);
        row.setOnLongClickListener(v -> {
            copy(v.getContext(), label, value, true);
            return true;
        });
        container.addView(row);
    }

    private static String shorten(String text) {
        return text.length() > 40 ? text.substring(0, 40) + "…" : text;
    }
}
