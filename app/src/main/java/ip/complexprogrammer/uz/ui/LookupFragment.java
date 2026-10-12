package ip.complexprogrammer.uz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.gson.Gson;

import java.util.Locale;
import java.util.concurrent.Future;

import ip.complexprogrammer.uz.MapsActivity;
import ip.complexprogrammer.uz.R;
import ip.complexprogrammer.uz.databinding.FragmentLookupBinding;
import ip.complexprogrammer.uz.models.IpInfo;
import ip.complexprogrammer.uz.services.AdsManager;
import ip.complexprogrammer.uz.services.AppPrefs;
import ip.complexprogrammer.uz.services.HistoryStore;
import ip.complexprogrammer.uz.services.IpRepository;
import ip.complexprogrammer.uz.services.IpUtils;
import ip.complexprogrammer.uz.services.NetworkUtil;

public class LookupFragment extends Fragment {
    private static final String STATE_INFO = "info";
    private static final String STATE_SELF = "self";
    private static final String ARG_QUERY = "query";

    private FragmentLookupBinding binding;
    private IpRepository repository;
    private HistoryStore history;
    private Future<?> pending;
    private IpInfo current;
    private boolean currentIsSelf;
    private boolean detailsExpanded;
    private boolean nativeAdLoaded;
    private Runnable retryAction;

    public static LookupFragment newInstance(@Nullable String query) {
        LookupFragment f = new LookupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLookupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repository = new IpRepository(requireContext());
        history = new HistoryStore(requireContext());

        setupInput();
        binding.lookupButton.setOnClickListener(v -> submit());
        binding.myIpButton.setOnClickListener(v -> {
            binding.input.setText("");
            binding.inputLayout.setError(null);
            load("", true);
        });
        binding.error.errorRetry.setOnClickListener(v -> {
            if (retryAction != null) retryAction.run();
        });
        binding.detailsHeader.setOnClickListener(v -> setDetailsExpanded(!detailsExpanded));
        binding.headerIp.setOnLongClickListener(v -> {
            if (current != null) UiUtils.copy(requireContext(), getString(R.string.label_ip), current.ip, true);
            return true;
        });
        binding.copyButton.setOnClickListener(v -> {
            if (current == null) return;
            UiUtils.copy(requireContext(), getString(R.string.app_name), new IpInfoPresenter(requireContext(), current).summary(), false);
            Toast.makeText(requireContext(), R.string.copied_all, Toast.LENGTH_SHORT).show();
        });
        binding.shareButton.setOnClickListener(v -> share());
        binding.favoriteButton.setOnClickListener(v -> toggleFavorite());
        binding.mapButton.setOnClickListener(v -> openMap());

        IpInfo restored = null;
        if (savedInstanceState != null && savedInstanceState.getString(STATE_INFO) != null) {
            restored = new Gson().fromJson(savedInstanceState.getString(STATE_INFO), IpInfo.class);
            currentIsSelf = savedInstanceState.getBoolean(STATE_SELF);
        }
        if (restored != null) {
            showResult(restored, currentIsSelf);
        } else {
            String query = getArguments() == null ? null : getArguments().getString(ARG_QUERY);
            if (query != null && !query.isEmpty()) {
                binding.input.setText(query);
                load(query, false);
            } else {
                load("", false);
            }
        }
        loadNativeAd();
        AdsManager.preloadInterstitial(requireContext());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (current != null) {
            outState.putString(STATE_INFO, new Gson().toJson(current));
            outState.putBoolean(STATE_SELF, currentIsSelf);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pending != null) pending.cancel(true);
        binding = null;
    }

    /** Called by other tabs (history, device) to look something up here. */
    public void lookup(String query) {
        if (binding == null) return;
        binding.input.setText(query);
        binding.inputLayout.setError(null);
        load(query, false);
    }

    private void setupInput() {
        binding.inputLayout.setEndIconOnClickListener(v -> {
            Editable text = binding.input.getText();
            if (text != null && text.length() > 0) {
                binding.input.setText("");
            } else {
                String clip = UiUtils.pasteText(requireContext());
                if (clip != null) {
                    String normalized = IpUtils.normalize(clip);
                    binding.input.setText(normalized);
                    binding.input.setSelection(normalized.length());
                }
            }
        });
        binding.input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.inputLayout.setError(null);
                boolean empty = s.length() == 0;
                binding.inputLayout.setEndIconDrawable(empty ? R.drawable.ic_paste : R.drawable.ic_close);
                binding.inputLayout.setEndIconContentDescription(empty ? R.string.paste : R.string.clear);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        binding.input.setOnEditorActionListener((v, actionId, event) -> {
            boolean enter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_SEARCH || enter) {
                submit();
                return true;
            }
            return false;
        });
    }

    private void submit() {
        String text = binding.input.getText() == null ? "" : binding.input.getText().toString();
        IpUtils.InputType type = IpUtils.classify(text);
        if (type == IpUtils.InputType.EMPTY) {
            load("", true);
            return;
        }
        if (type == IpUtils.InputType.INVALID) {
            binding.inputLayout.setError(getString(R.string.input_invalid));
            return;
        }
        hideKeyboard();
        load(text, true);
    }

    private void load(String query, boolean manual) {
        if (pending != null) pending.cancel(true);
        boolean self = IpUtils.normalize(query).isEmpty();
        showLoading();
        pending = repository.lookup(query, new IpRepository.Callback() {
            @Override
            public void onSuccess(IpInfo info) {
                if (binding == null) return;
                history.add(info);
                showResult(info, self);
                if (manual && getActivity() != null) AdsManager.onSearchCompleted(getActivity());
            }

            @Override
            public void onError(IpRepository.ErrorType type, String detail) {
                if (binding == null) return;
                showError(type, detail, () -> load(query, manual));
            }
        });
    }

    // States

    private void showLoading() {
        binding.result.setVisibility(View.GONE);
        binding.error.getRoot().setVisibility(View.GONE);
        binding.skeleton.getRoot().setVisibility(View.VISIBLE);
        AlphaAnimation pulse = new AlphaAnimation(1f, 0.4f);
        pulse.setDuration(700);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        binding.skeleton.getRoot().startAnimation(pulse);
    }

    private void hideLoading() {
        binding.skeleton.getRoot().clearAnimation();
        binding.skeleton.getRoot().setVisibility(View.GONE);
    }

    private void showError(IpRepository.ErrorType type, String detail, Runnable retry) {
        hideLoading();
        binding.result.setVisibility(View.GONE);
        binding.error.getRoot().setVisibility(View.VISIBLE);
        retryAction = retry;
        int icon = R.drawable.ic_cloud_off;
        String title;
        String message;
        boolean canRetry = true;
        switch (type) {
            case NO_INTERNET:
                title = getString(R.string.error_no_internet_title);
                message = getString(R.string.error_no_internet_msg);
                break;
            case RESERVED:
                icon = R.drawable.ic_info;
                title = getString(R.string.error_reserved_title);
                message = getString(R.string.error_reserved_msg, detail == null ? "" : detail);
                canRetry = false;
                break;
            case UNKNOWN_HOST:
                title = getString(R.string.error_unknown_host_title);
                message = getString(R.string.error_unknown_host_msg, detail == null ? "" : detail);
                break;
            case RATE_LIMITED:
                title = getString(R.string.error_server_title);
                message = getString(R.string.error_rate_limited_msg);
                break;
            default:
                title = getString(R.string.error_server_title);
                message = getString(R.string.error_server_msg);
        }
        binding.error.errorIcon.setImageResource(icon);
        binding.error.errorTitle.setText(title);
        binding.error.errorMessage.setText(message);
        binding.error.errorRetry.setVisibility(canRetry ? View.VISIBLE : View.GONE);
    }

    private void showResult(IpInfo info, boolean self) {
        current = info;
        currentIsSelf = self;
        hideLoading();
        binding.error.getRoot().setVisibility(View.GONE);
        binding.result.setVisibility(View.VISIBLE);
        IpInfoPresenter p = new IpInfoPresenter(requireContext(), info);

        // Header
        binding.headerIp.setText(info.ip);
        String place = p.place();
        binding.headerPlace.setText(place);
        binding.headerPlace.setVisibility(place.isEmpty() ? View.GONE : View.VISIBLE);
        if (info.countryCode != null) {
            binding.flag.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load("https://flagcdn.com/w80/" + info.countryCode.toLowerCase(Locale.ROOT) + ".png")
                    .into(binding.flag);
        } else {
            binding.flag.setVisibility(View.GONE);
        }
        if (info.query != null) {
            binding.resolved.setVisibility(View.VISIBLE);
            binding.resolved.setText(getString(R.string.resolved_from, info.query, info.ip));
        } else {
            binding.resolved.setVisibility(View.GONE);
        }
        bindSelfStatus(info, self);
        updateFavoriteButton(history.isFavorite(info.ip));
        binding.mapButton.setVisibility(info.hasLocation() ? View.VISIBLE : View.GONE);

        // Sections
        binding.networkRows.removeAllViews();
        UiUtils.addRow(binding.networkRows, R.string.label_ip, info.ip);
        UiUtils.addRow(binding.networkRows, R.string.label_ip_version, info.version);
        UiUtils.addRow(binding.networkRows, R.string.label_org, info.org);
        UiUtils.addRow(binding.networkRows, R.string.label_asn, info.asn);

        binding.locationRows.removeAllViews();
        UiUtils.addRow(binding.locationRows, R.string.label_city, info.city);
        UiUtils.addRow(binding.locationRows, R.string.label_region, info.region);
        UiUtils.addRow(binding.locationRows, R.string.label_postal, info.postal);
        UiUtils.addRow(binding.locationRows, R.string.label_coordinates, p.coordinates());
        UiUtils.addRow(binding.locationRows, R.string.label_timezone, p.timezone());
        UiUtils.addRow(binding.locationRows, R.string.label_local_time, p.localTime());
        boolean hasLocation = binding.locationRows.getChildCount() > 0;
        binding.locationCard.setVisibility(hasLocation ? View.VISIBLE : View.GONE);
        binding.approxNote.setVisibility(info.hasLocation() ? View.VISIBLE : View.GONE);

        binding.countryRows.removeAllViews();
        UiUtils.addRow(binding.countryRows, R.string.label_country, p.countryWithFlag());
        UiUtils.addRow(binding.countryRows, R.string.label_capital, info.countryCapital);
        UiUtils.addRow(binding.countryRows, R.string.label_calling_code, info.callingCode);
        UiUtils.addRow(binding.countryRows, R.string.label_currency, p.currency());
        UiUtils.addRow(binding.countryRows, R.string.label_languages, p.languages());
        UiUtils.addRow(binding.countryRows, R.string.label_in_eu, p.yesNo(info.inEu));
        binding.countryCard.setVisibility(binding.countryRows.getChildCount() > 0 ? View.VISIBLE : View.GONE);

        binding.detailsRows.removeAllViews();
        UiUtils.addRow(binding.detailsRows, R.string.label_continent, p.continent());
        UiUtils.addRow(binding.detailsRows, R.string.label_region_code, info.regionCode);
        UiUtils.addRow(binding.detailsRows, R.string.label_iso3, info.countryCodeIso3);
        UiUtils.addRow(binding.detailsRows, R.string.label_tld, info.countryTld);
        UiUtils.addRow(binding.detailsRows, R.string.label_area, p.area());
        UiUtils.addRow(binding.detailsRows, R.string.label_population, p.population());
        binding.detailsCard.setVisibility(binding.detailsRows.getChildCount() > 0 ? View.VISIBLE : View.GONE);
        setDetailsExpanded(detailsExpanded);

        binding.footer.setText(getString(R.string.data_source, info.source) + " · " + getString(R.string.long_press_to_copy));
    }

    private void bindSelfStatus(IpInfo info, boolean self) {
        if (!self) {
            binding.vpnChip.setVisibility(View.GONE);
            binding.ipChanged.setVisibility(View.GONE);
            return;
        }
        boolean vpn = NetworkUtil.isVpnActive(requireContext());
        binding.vpnChip.setVisibility(View.VISIBLE);
        binding.vpnChip.setText(vpn ? R.string.vpn_active : R.string.vpn_inactive);
        binding.vpnChip.setChipIconResource(vpn ? R.drawable.ic_lock : R.drawable.ic_lock_open);
        binding.vpnChip.setChipIconTint(ContextCompat.getColorStateList(requireContext(),
                vpn ? R.color.status_ok : R.color.status_warn));

        String previous = AppPrefs.getLastOwnIp(requireContext());
        if (previous != null && !previous.equals(info.ip)) {
            binding.ipChanged.setVisibility(View.VISIBLE);
            binding.ipChanged.setText(getString(R.string.ip_changed, previous, info.ip));
        } else {
            binding.ipChanged.setVisibility(View.GONE);
        }
        AppPrefs.setLastOwnIp(requireContext(), info.ip);
    }

    private void setDetailsExpanded(boolean expanded) {
        detailsExpanded = expanded;
        binding.detailsRows.setVisibility(expanded ? View.VISIBLE : View.GONE);
        binding.detailsArrow.setImageResource(expanded ? R.drawable.ic_expand_less : R.drawable.ic_expand_more);
        binding.detailsArrow.setContentDescription(getString(expanded ? R.string.show_less : R.string.show_more));
    }

    private void updateFavoriteButton(boolean favorite) {
        binding.favoriteButton.setIconResource(favorite ? R.drawable.ic_star : R.drawable.ic_star_border);
        binding.favoriteButton.setContentDescription(getString(
                favorite ? R.string.action_favorite_remove : R.string.action_favorite_add));
    }

    private void toggleFavorite() {
        if (current == null) return;
        if (!history.contains(current.ip)) history.add(current);
        boolean favorite = history.toggleFavorite(current.ip);
        updateFavoriteButton(favorite);
        Toast.makeText(requireContext(), favorite ? R.string.favorite_added : R.string.favorite_removed,
                Toast.LENGTH_SHORT).show();
    }

    private void share() {
        if (current == null) return;
        Intent send = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, new IpInfoPresenter(requireContext(), current).summary());
        startActivity(Intent.createChooser(send, getString(R.string.share_title)));
    }

    private void openMap() {
        if (current == null || !current.hasLocation()) return;
        Intent intent = new Intent(requireContext(), MapsActivity.class)
                .putExtra(MapsActivity.EXTRA_LAT, current.latitude)
                .putExtra(MapsActivity.EXTRA_LON, current.longitude)
                .putExtra(MapsActivity.EXTRA_TITLE, new IpInfoPresenter(requireContext(), current).place())
                .putExtra(MapsActivity.EXTRA_IP, current.ip);
        startActivity(intent);
    }

    private void loadNativeAd() {
        if (nativeAdLoaded || AppPrefs.isAdFree(requireContext())) return;
        nativeAdLoaded = true;
        AdLoader loader = new AdLoader.Builder(requireContext(), AdsManager.NATIVE_ID)
                .forNativeAd(nativeAd -> {
                    if (binding == null || AppPrefs.isAdFree(requireContext())) {
                        nativeAd.destroy();
                        return;
                    }
                    binding.nativeAd.setNativeAd(nativeAd);
                    binding.nativeAd.setVisibility(View.VISIBLE);
                })
                .build();
        loader.loadAd(new AdRequest.Builder().build());
    }

    /** Hides ads immediately after the user earns an ad-free period. */
    public void onAdFreeChanged() {
        if (binding != null && AppPrefs.isAdFree(requireContext())) {
            binding.nativeAd.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = ContextCompat.getSystemService(requireContext(), InputMethodManager.class);
        if (imm != null) imm.hideSoftInputFromWindow(binding.input.getWindowToken(), 0);
        binding.input.clearFocus();
    }
}
