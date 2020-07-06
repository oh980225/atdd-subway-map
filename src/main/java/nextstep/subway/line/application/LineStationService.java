package nextstep.subway.line.application;

import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.LineStation;
import nextstep.subway.line.domain.LineStations;
import nextstep.subway.line.dto.LineStationRequest;
import nextstep.subway.line.dto.LineStationResponse;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.domain.StationRepository;
import nextstep.subway.station.dto.StationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LineStationService {

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;

    public LineStationService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    public List<LineStationResponse> findAll(Long lineId) {
        Line line = lineRepository.findById(lineId)
                .orElseThrow(() -> new IllegalStateException("not found line : " + lineId));

        LineStations lineStations = line.getLineStations();
        return LineStationResponse.from(lineStations);
    }

    @Transactional
    public LineStationResponse addLineStation(Long lineId, LineStationRequest createLineStationRequest) {

        Line line = lineRepository.findById(lineId)
                .orElseThrow(() -> new IllegalStateException("not found line : " + lineId));

        LineStation lineStation = createNewLineStation(createLineStationRequest);

        line.registerLineStation(lineStation);

        return toLineStationsResponse(lineStation);
    }

    private LineStation createNewLineStation(LineStationRequest createLineStationRequest) {
        Long stationId = createLineStationRequest.getStationId();
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new IllegalStateException("not found station : " + stationId));

        Station preStation = null;
        Long preStationId = createLineStationRequest.getPreStationId();
        if (preStationId != null) {
            preStation = stationRepository.findById(preStationId)
                    .orElseThrow(() -> new IllegalStateException("not found pre-station : " + preStationId));
        }

        return LineStation.createLineStation(station, preStation, createLineStationRequest.getDistance(), createLineStationRequest.getDuration());
    }

    private LineStationResponse toLineStationsResponse(LineStation lineStation) {
        StationResponse stationResponse = StationResponse.of(lineStation.getStation());
        return LineStationResponse.of(lineStation, stationResponse);
    }
}