package fr.cph.chicago.service.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.TrainArrival;
import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.parser.XmlParser;
import fr.cph.chicago.service.TrainService;
import fr.cph.chicago.util.Util;
import rx.exceptions.Exceptions;

import static fr.cph.chicago.connection.CtaRequestType.TRAIN_ARRIVALS;

public class TrainServiceImpl implements TrainService {

    private final XmlParser xmlParser;

    public TrainServiceImpl() {
        this.xmlParser = XmlParser.getInstance();
    }

    @NonNull
    @Override
    public SparseArray<TrainArrival> loadFavoritesTrain(@NonNull final Context context) {
        final MultiValuedMap<String, String> trainParams = Util.getFavoritesTrainParams(context);
        SparseArray<TrainArrival> trainArrivals = new SparseArray<>();
        try {
            final CtaConnect ctaConnect = CtaConnect.getInstance(context);
            for (final Map.Entry<String, Collection<String>> entry : trainParams.asMap().entrySet()) {
                final String key = entry.getKey();
                if ("mapid".equals(key)) {
                    final List<String> list = (List<String>) entry.getValue();
                    if (list.size() < 5) {
                        final InputStream xmlResult = ctaConnect.connect(context, TRAIN_ARRIVALS, trainParams);
                        trainArrivals = xmlParser.parseArrivals(xmlResult, DataHolder.getInstance().getTrainData());
                    } else {
                        final int size = list.size();
                        int start = 0;
                        int end = 4;
                        while (end < size + 1) {
                            final List<String> subList = list.subList(start, end);
                            final MultiValuedMap<String, String> paramsTemp = new ArrayListValuedHashMap<>();
                            for (final String sub : subList) {
                                paramsTemp.put(key, sub);
                            }
                            final InputStream xmlResult = ctaConnect.connect(context, TRAIN_ARRIVALS, paramsTemp);
                            final SparseArray<TrainArrival> temp = xmlParser.parseArrivals(xmlResult, DataHolder.getInstance().getTrainData());
                            for (int j = 0; j < temp.size(); j++) {
                                trainArrivals.put(temp.keyAt(j), temp.valueAt(j));
                            }
                            start = end;
                            if (end + 3 >= size - 1 && end != size) {
                                end = size;
                            } else {
                                end = end + 3;
                            }
                        }
                    }
                }
            }

            // Apply filters
            int index = 0;
            while (index < trainArrivals.size()) {
                final TrainArrival trainArrival = trainArrivals.valueAt(index++);
                final List<Eta> etas = trainArrival.getEtas();
                trainArrival.setEtas(Stream.of(etas)
                    .filter(eta -> {
                        final TrainLine line = eta.getRouteName();
                        final TrainDirection direction = eta.getStop().getDirection();
                        return Preferences.getTrainFilter(context, eta.getStation().getId(), line, direction);
                    })
                    .sorted()
                    .collect(Collectors.toList())
                );
            }
        } catch (final Throwable e) {
            throw Exceptions.propagate(e);
        }
        return trainArrivals;
    }

    @NonNull
    @Override
    public TrainData loadLocalTrainData(@NonNull final Context context) {
        TrainData.getInstance().read(context);
        return TrainData.getInstance();
    }

    @NonNull
    @Override
    public Optional<TrainArrival> loadStationTrainArrival(@NonNull final Context context, int stationId) {
        try {
            final MultiValuedMap<String, String> params = new ArrayListValuedHashMap<>();
            params.put(context.getString(R.string.request_map_id), Integer.toString(stationId));

            final XmlParser xml = XmlParser.getInstance();
            final InputStream xmlResult = CtaConnect.getInstance(context).connect(context, TRAIN_ARRIVALS, params);
            final SparseArray<TrainArrival> arrivals = xml.parseArrivals(xmlResult, TrainData.getInstance());
            if (arrivals.size() == 1) {
                return Optional.of(arrivals.get(stationId));
            } else {
                return Optional.empty();
            }
        } catch (final Throwable e) {
            throw Exceptions.propagate(e);
        }
    }
}
