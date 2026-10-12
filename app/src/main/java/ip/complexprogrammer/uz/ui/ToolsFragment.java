package ip.complexprogrammer.uz.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ip.complexprogrammer.uz.R;
import ip.complexprogrammer.uz.databinding.FragmentToolsBinding;
import ip.complexprogrammer.uz.services.IpUtils;
import ip.complexprogrammer.uz.services.NetTools;

public class ToolsFragment extends Fragment {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private FragmentToolsBinding binding;
    private Future<?> running;
    /** Incremented per run so a stopped run can't write into the next one. */
    private volatile int runId;
    private final StringBuilder output = new StringBuilder();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToolsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.toolGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) updateDescription();
        });
        updateDescription();
        binding.run.setOnClickListener(v -> {
            if (running != null && !running.isDone()) stop();
            else start();
        });
        binding.host.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                start();
                return true;
            }
            return false;
        });
        binding.copyOutput.setOnClickListener(v ->
                UiUtils.copy(requireContext(), getString(R.string.nav_tools), output.toString(), true));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stop();
        binding = null;
    }

    /** Pre-fills the host field, e.g. from the lookup result. */
    public void setHost(String host) {
        if (binding != null) binding.host.setText(host);
    }

    private void updateDescription() {
        int id = binding.toolGroup.getCheckedButtonId();
        if (id == R.id.tool_dns) binding.toolDesc.setText(R.string.tool_dns_desc);
        else if (id == R.id.tool_whois) binding.toolDesc.setText(R.string.tool_whois_desc);
        else if (id == R.id.tool_ports) binding.toolDesc.setText(getString(R.string.tool_ports_desc, NetTools.COMMON_PORTS.length));
        else binding.toolDesc.setText(R.string.tool_ping_desc);
    }

    private void start() {
        String host = binding.host.getText() == null ? "" : IpUtils.normalize(binding.host.getText().toString());
        IpUtils.InputType type = IpUtils.classify(host);
        if (type == IpUtils.InputType.EMPTY || type == IpUtils.InputType.INVALID) {
            binding.hostLayout.setError(getString(R.string.tool_host_required));
            return;
        }
        binding.hostLayout.setError(null);
        stop();
        final int id = ++runId;
        output.setLength(0);
        binding.output.setText("");
        binding.copyOutput.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);
        binding.run.setText(R.string.tool_stop);

        int tool = binding.toolGroup.getCheckedButtonId();
        String openLabel = getString(R.string.port_open);
        String summary = getString(R.string.ports_summary);
        NetTools.Output out = new NetTools.Output() {
            @Override
            public void line(String text) {
                main.post(() -> {
                    if (id == runId) append(text);
                });
            }

            @Override
            public boolean isCancelled() {
                return id != runId;
            }
        };
        running = EXECUTOR.submit(() -> {
            try {
                if (tool == R.id.tool_dns) NetTools.dns(host, out);
                else if (tool == R.id.tool_whois) NetTools.whois(host, out);
                else if (tool == R.id.tool_ports) NetTools.ports(host, out, openLabel, summary);
                else NetTools.ping(host, out);
            } catch (Exception e) {
                String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                main.post(() -> {
                    if (id == runId && binding != null) append(getString(R.string.tool_error, message));
                });
            } finally {
                main.post(() -> {
                    if (id == runId) finished();
                });
            }
        });
    }

    private void stop() {
        if (running != null && !running.isDone()) {
            runId++;
            running.cancel(true);
            if (binding != null) {
                append(getString(R.string.tool_stopped));
                finished();
            }
        }
    }

    private void append(String line) {
        if (binding == null) return;
        output.append(line).append('\n');
        binding.output.setText(output);
    }

    private void finished() {
        if (binding == null) return;
        binding.progress.setVisibility(View.INVISIBLE);
        binding.run.setText(R.string.tool_run);
        binding.copyOutput.setVisibility(output.length() > 0 ? View.VISIBLE : View.GONE);
        if (output.length() == 0) binding.output.setText(R.string.tool_output_empty);
    }
}
