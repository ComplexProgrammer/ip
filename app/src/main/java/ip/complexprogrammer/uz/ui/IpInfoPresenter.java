package ip.complexprogrammer.uz.ui;

import android.content.Context;

import java.util.Date;
import java.util.Locale;

import ip.complexprogrammer.uz.R;
import ip.complexprogrammer.uz.models.IpInfo;
import ip.complexprogrammer.uz.services.IpUtils;
import ip.complexprogrammer.uz.services.ValueFormatter;

/** Human-readable strings for an {@link IpInfo}, shared by the result screen, share text and widget. */
public final class IpInfoPresenter {
    private final Context context;
    private final IpInfo info;
    private final Locale locale;

    public IpInfoPresenter(Context context, IpInfo info) {
        this.context = context;
        this.info = info;
        this.locale = context.getResources().getConfiguration().locale;
    }

    public String place() {
        StringBuilder sb = new StringBuilder();
        append(sb, info.city);
        if (info.region != null && !info.region.equals(info.city)) append(sb, info.region);
        append(sb, countryName());
        return sb.toString();
    }

    public String countryName() {
        if (info.countryCode != null) {
            String localized = new Locale("", info.countryCode).getDisplayCountry(locale);
            if (!localized.isEmpty() && !localized.equalsIgnoreCase(info.countryCode)) return localized;
        }
        return info.countryName;
    }

    public String countryWithFlag() {
        String name = countryName();
        if (name == null) return null;
        String flag = IpUtils.flagEmoji(info.countryCode);
        return flag.isEmpty() ? name : flag + "  " + name + " (" + info.countryCode + ")";
    }

    public String yesNo(Boolean value) {
        if (value == null) return null;
        return context.getString(value ? R.string.yes : R.string.no);
    }

    public String languages() {
        return ValueFormatter.languages(info.languages, locale);
    }

    public String currency() {
        return ValueFormatter.currency(info.currency, info.currencyName, locale);
    }

    public String timezone() {
        if (info.timezone == null) return null;
        String offset = ValueFormatter.utcOffset(info.utcOffset);
        return offset == null ? info.timezone : info.timezone + " (" + offset + ")";
    }

    public String localTime() {
        return ValueFormatter.currentTime(info.timezone, new Date(), locale);
    }

    public String coordinates() {
        return ValueFormatter.coordinates(info.latitude, info.longitude);
    }

    public String population() {
        return ValueFormatter.number(info.countryPopulation, locale);
    }

    public String area() {
        return ValueFormatter.area(info.countryArea, locale);
    }

    public String continent() {
        if (info.continentCode == null) return null;
        switch (info.continentCode) {
            case "AF": return localizedOr("Africa", info.continentCode);
            case "AN": return localizedOr("Antarctica", info.continentCode);
            case "AS": return localizedOr("Asia", info.continentCode);
            case "EU": return localizedOr("Europe", info.continentCode);
            case "NA": return localizedOr("North America", info.continentCode);
            case "OC": return localizedOr("Oceania", info.continentCode);
            case "SA": return localizedOr("South America", info.continentCode);
            default: return info.continentCode;
        }
    }

    private String localizedOr(String english, String code) {
        String lang = locale.getLanguage();
        String[][] names = {
                {"Africa", "Afrika", "Африка"},
                {"Antarctica", "Antarktida", "Антарктида"},
                {"Asia", "Osiyo", "Азия"},
                {"Europe", "Yevropa", "Европа"},
                {"North America", "Shimoliy Amerika", "Северная Америка"},
                {"Oceania", "Okeaniya", "Океания"},
                {"South America", "Janubiy Amerika", "Южная Америка"},
        };
        for (String[] n : names) {
            if (n[0].equals(english)) {
                String name = lang.equals("uz") ? n[1] : lang.equals("ru") ? n[2] : n[0];
                return name + " (" + code + ")";
            }
        }
        return english;
    }

    /** Plain-text summary for copying and sharing. */
    public String summary() {
        StringBuilder sb = new StringBuilder();
        line(sb, R.string.label_ip, info.ip);
        if (info.query != null) line(sb, R.string.hint_ip_or_domain, info.query);
        line(sb, R.string.label_org, info.org);
        line(sb, R.string.label_asn, info.asn);
        line(sb, R.string.label_city, info.city);
        line(sb, R.string.label_region, info.region);
        line(sb, R.string.label_country, countryName());
        line(sb, R.string.label_coordinates, coordinates());
        line(sb, R.string.label_timezone, timezone());
        line(sb, R.string.label_currency, currency());
        sb.append("\n— ").append(context.getString(R.string.app_name));
        return sb.toString();
    }

    private void line(StringBuilder sb, int label, String value) {
        if (value == null) return;
        sb.append(context.getString(label)).append(": ").append(value).append('\n');
    }

    private static void append(StringBuilder sb, String part) {
        if (part == null || part.isEmpty()) return;
        if (sb.length() > 0) sb.append(", ");
        sb.append(part);
    }
}
