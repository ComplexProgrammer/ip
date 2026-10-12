package ip.complexprogrammer.uz.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ip.complexprogrammer.uz.MainActivity;
import ip.complexprogrammer.uz.R;
import ip.complexprogrammer.uz.databinding.FragmentDeviceBinding;
import ip.complexprogrammer.uz.services.NetworkUtil;

/** Local network information that the public-IP lookup can't show. */
public class DeviceFragment extends Fragment {
    private FragmentDeviceBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDeviceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.refresh.setOnClickListener(v -> refresh());
        binding.checkPublic.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).openLookup("");
        });
        refresh();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && binding != null) refresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void refresh() {
        binding.rows.removeAllViews();
        NetworkUtil.Transport transport = NetworkUtil.getTransport(requireContext());
        UiUtils.addRow(binding.rows, R.string.device_connection, getString(transportLabel(transport)));
        if (transport == NetworkUtil.Transport.NONE) return;

        UiUtils.addRow(binding.rows, R.string.device_vpn,
                getString(NetworkUtil.isVpnActive(requireContext()) ? R.string.on : R.string.off));
        NetworkUtil.LocalInfo info = NetworkUtil.getLocalInfo(requireContext());
        UiUtils.addRow(binding.rows, R.string.device_interface, info.interfaceName);
        UiUtils.addRow(binding.rows, R.string.device_local_ip,
                info.addresses.isEmpty() ? getString(R.string.device_unknown) : TextUtils.join("\n", info.addresses));
        UiUtils.addRow(binding.rows, R.string.device_gateway, info.gateways.isEmpty() ? null : TextUtils.join("\n", info.gateways));
        UiUtils.addRow(binding.rows, R.string.device_dns, info.dnsServers.isEmpty() ? null : TextUtils.join("\n", info.dnsServers));
        UiUtils.addRow(binding.rows, R.string.device_private_dns, info.privateDnsServer);
    }

    private static int transportLabel(NetworkUtil.Transport t) {
        switch (t) {
            case WIFI:
                return R.string.transport_wifi;
            case CELLULAR:
                return R.string.transport_cellular;
            case ETHERNET:
                return R.string.transport_ethernet;
            case VPN:
                return R.string.transport_vpn;
            case NONE:
                return R.string.transport_none;
            default:
                return R.string.transport_other;
        }
    }
}
