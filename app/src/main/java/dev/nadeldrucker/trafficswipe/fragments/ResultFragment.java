package dev.nadeldrucker.trafficswipe.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.toolbox.Volley;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import dev.nadeldrucker.trafficswipe.R;
import dev.nadeldrucker.trafficswipe.dao.transport.apis.generic.Entrypoint;
import dev.nadeldrucker.trafficswipe.dao.transport.apis.generic.TransportApiFactory;
import dev.nadeldrucker.trafficswipe.dao.transport.model.data.DepartureTime;
import dev.nadeldrucker.trafficswipe.dao.transport.model.data.Station;
import dev.nadeldrucker.trafficswipe.dao.transport.model.data.vehicle.Vehicle;
import dev.nadeldrucker.trafficswipe.ui.RecyclerResultAdapter;
import dev.nadeldrucker.trafficswipe.ui.UiUtil;
import org.threeten.bp.Duration;
import org.threeten.bp.temporal.ChronoUnit;

public class ResultFragment extends Fragment {

    private static final String TAG = ResultFragment.class.getName();

    private RecyclerResultAdapter recyclerAdapter;
    private TextView tvResult;
    private TextView tvLastFetch;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final Handler handler = new Handler();

    private Runnable secondLoop;
    private long lastUpdateTime;

    private boolean isViewDestroyed = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        secondLoop = () -> {
            if (isViewDestroyed) return;

            // calculate time passed since last update
            if (lastUpdateTime != 0) {
                long updateDelta = System.currentTimeMillis() - lastUpdateTime;

                recyclerAdapter.getViewHolders().forEach(viewHolder -> {
                    viewHolder.subtractTimeFromOriginalDeparture(Duration.of(updateDelta, ChronoUnit.MILLIS));
                });

                updateLastFetchTime(updateDelta);
            }

            handler.postDelayed(this.secondLoop, 1000);
        };
        handler.post(secondLoop);

        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerResult);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAdapter = new RecyclerResultAdapter();
        recyclerView.setAdapter(recyclerAdapter);

        tvResult = view.findViewById(R.id.tvResult);
        TextView tvTitle = view.findViewById(R.id.tvType);
        String query = Objects.requireNonNull(getArguments()).getString(StartFragment.BUNDLE_QUERY);
        tvResult.setText(query);
        tvTitle.setText(R.string.activity_title_departures);

        tvLastFetch = view.findViewById(R.id.tvLastFetch);

        swipeRefreshLayout = view.findViewById(R.id.refreshLayoutResult);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            queryData(query);
        });

        queryData(query);
    }

    @Override
    public void onDestroyView() {
        isViewDestroyed = true;
        super.onDestroyView();
    }

    private void onStationNameChanged(String name) {
        tvResult.setText(name);
    }

    /**
     * Queries data from api
     *
     * @param query query string
     */
    private void queryData(String query) {
        Entrypoint api = TransportApiFactory.createTransportApiDao(TransportApiFactory.ApiProvider.VVO,
                Volley.newRequestQueue(Objects.requireNonNull(getContext())));

        Objects.requireNonNull(api);

        api.getStops(query).thenCompose(stations -> {
            if (stations.size() > 0) {
                Station station = stations.get(0);
                onStationNameChanged(station.getName());
                return station.getDepartures();
            } else {
                throw new RuntimeException("No stations found!");
            }
        }).exceptionally(throwable -> {
            Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
            return Collections.emptyMap();
        }).thenAccept(this::onDeparturesChanged);
    }

    /**
     * Called when departures have been queried, and are ready to be displayed
     *
     * @param vehicleDepartureTimeMap departure table map
     */
    private void onDeparturesChanged(Map<Vehicle, DepartureTime> vehicleDepartureTimeMap) {
        recyclerAdapter.setDepartureItems(vehicleDepartureTimeMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(entry -> new RecyclerResultAdapter.DepartureItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList()));

        lastUpdateTime = System.currentTimeMillis();
        updateLastFetchTime(0);

        recyclerAdapter.notifyDataSetChanged();

        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Updated textView with last updated and formatted time
     *
     * @param updateDeltaMillis time since last update in millis
     */
    private void updateLastFetchTime(long updateDeltaMillis) {
        Duration duration = Duration.of(updateDeltaMillis, ChronoUnit.MILLIS);
        String formatted = UiUtil.formatDuration(duration);
        tvLastFetch.setText(String.format("Time since last refresh: %s", formatted));
    }
}
