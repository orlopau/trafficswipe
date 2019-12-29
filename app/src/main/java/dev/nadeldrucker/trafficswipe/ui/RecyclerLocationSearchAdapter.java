package dev.nadeldrucker.trafficswipe.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import dev.nadeldrucker.trafficswipe.Constants;
import dev.nadeldrucker.trafficswipe.R;
import dev.nadeldrucker.trafficswipe.data.db.entities.Station;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RecyclerLocationSearchAdapter extends RecyclerView.Adapter<RecyclerLocationSearchAdapter.ViewHolder>{

    private final List<StationLocationBean> searchResults = new ArrayList<>();
    private Consumer<StationLocationBean> clickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvDistance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }
    }

    public static class StationLocationBean {
        public Station station;
        public double distance;
        public String abbreviation;

        public StationLocationBean(Station station, double distance, String abbreviation) {
            this.station = station;
            this.distance = distance;
            this.abbreviation = abbreviation;
        }

        public double getDistance() {
            return distance;
        }
    }

    public void updateSearchResults(List<StationLocationBean> searchResults) {
        this.searchResults.clear();
        this.searchResults.addAll(searchResults);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_search_location, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final StationLocationBean s = searchResults.get(position);

        holder.tvName.setText(s.station.shortName);
        holder.tvDistance.setText(Constants.formatterMeters.format(s.distance) + "m");

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.accept(s);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public void setClickListener(Consumer<StationLocationBean> clickListener) {
        this.clickListener = clickListener;
    }
}
