package ip.complexprogrammer.uz.ui;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import ip.complexprogrammer.uz.MainActivity;
import ip.complexprogrammer.uz.R;
import ip.complexprogrammer.uz.databinding.FragmentHistoryBinding;
import ip.complexprogrammer.uz.databinding.ItemHistoryBinding;
import ip.complexprogrammer.uz.models.HistoryEntry;
import ip.complexprogrammer.uz.services.HistoryStore;
import ip.complexprogrammer.uz.services.IpUtils;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private HistoryStore store;
    private final Adapter adapter = new Adapter();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        store = new HistoryStore(requireContext());
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);
        binding.clear.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.history_clear)
                .setMessage(R.string.history_clear_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    store.clearRecent();
                    refresh();
                })
                .show());
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
        List<HistoryEntry> entries = store.getSorted();
        List<Object> rows = new ArrayList<>();
        boolean hasFavorites = false;
        boolean recentHeaderAdded = false;
        for (HistoryEntry e : entries) {
            if (e.favorite && !hasFavorites) {
                rows.add(getString(R.string.history_favorites));
                hasFavorites = true;
            }
            if (!e.favorite && !recentHeaderAdded) {
                if (hasFavorites) rows.add(getString(R.string.history_recent));
                recentHeaderAdded = true;
            }
            rows.add(e);
        }
        adapter.submit(rows);
        binding.empty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
        boolean hasRecent = false;
        for (HistoryEntry e : entries) hasRecent |= !e.favorite;
        binding.clear.setVisibility(hasRecent ? View.VISIBLE : View.GONE);
    }

    private void open(HistoryEntry e) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openLookup(e.query != null ? e.query : e.ip);
        }
    }

    private void delete(HistoryEntry e) {
        List<HistoryEntry> backup = store.getAll();
        store.remove(e.ip);
        refresh();
        Snackbar.make(binding.getRoot(), R.string.history_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    store.replaceAll(backup);
                    if (binding != null) refresh();
                })
                .show();
    }

    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        private List<Object> rows = new ArrayList<>();

        void submit(List<Object> rows) {
            this.rows = rows;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return rows.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_HEADER) {
                return new RecyclerView.ViewHolder(inflater.inflate(R.layout.item_history_header, parent, false)) {
                };
            }
            return new ItemHolder(ItemHistoryBinding.inflate(inflater, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Object row = rows.get(position);
            if (row instanceof String) {
                ((TextView) holder.itemView).setText((String) row);
                return;
            }
            HistoryEntry e = (HistoryEntry) row;
            ItemHistoryBinding b = ((ItemHolder) holder).b;
            b.flag.setText(IpUtils.flagEmoji(e.countryCode));
            b.title.setText(e.query != null ? e.query + "  ·  " + e.ip : e.ip);
            StringBuilder sub = new StringBuilder();
            if (e.city != null) sub.append(e.city);
            if (e.countryName != null) sub.append(sub.length() > 0 ? ", " : "").append(e.countryName);
            if (e.org != null) sub.append(sub.length() > 0 ? " · " : "").append(e.org);
            b.subtitle.setText(sub);
            b.subtitle.setVisibility(sub.length() == 0 ? View.GONE : View.VISIBLE);
            b.time.setText(DateUtils.getRelativeTimeSpanString(e.time, System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS));
            b.favorite.setIconResource(e.favorite ? R.drawable.ic_star : R.drawable.ic_star_border);
            b.favorite.setContentDescription(getString(
                    e.favorite ? R.string.action_favorite_remove : R.string.action_favorite_add));
            b.favorite.setOnClickListener(v -> {
                store.toggleFavorite(e.ip);
                refresh();
            });
            b.delete.setOnClickListener(v -> delete(e));
            b.getRoot().setOnClickListener(v -> open(e));
        }
    }

    private static class ItemHolder extends RecyclerView.ViewHolder {
        final ItemHistoryBinding b;

        ItemHolder(ItemHistoryBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
