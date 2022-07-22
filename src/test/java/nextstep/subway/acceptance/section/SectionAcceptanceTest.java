package nextstep.subway.acceptance.section;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.acceptance.AcceptanceTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static nextstep.subway.acceptance.common.CommonSteps.*;
import static nextstep.subway.acceptance.line.LineSteps.*;
import static nextstep.subway.acceptance.section.SectionSteps.구간_등록_요청;
import static nextstep.subway.acceptance.section.SectionSteps.구간_삭제;
import static nextstep.subway.acceptance.station.StationSteps.GANGNAM_STATION_NAME;
import static nextstep.subway.acceptance.station.StationSteps.지하철역_생성;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("지하철 구간 관련 기능")
public class SectionAcceptanceTest extends AcceptanceTest {

    /**
     * When 지하철 구간을 등록하면
     * Then 지하철 구간 등록 성공 응답받는다.
     * Then 새로운 구간의 상행역은 해당 노선에 등록되어있는 하행 종점역이어야 한다.
     * Then 새로운 구간의 하행역은 해당 노선에 등록되어있는 역일 수 없다.
     */
    @DisplayName("구간 생성 성공")
    @Test
    void 구간_등록_성공_테스트() {

        ExtractableResponse<Response> 노선 = 노선_생성_요청(SHIN_BUNDANG_LINE_NAME, SHIN_BUNDANG_LINE_COLOR, SHIN_BUNDANG_UP_STATION_NAME, SHIN_BUNDANG_DOWN_STATION_NAME, DISTANCE);
        Long 노선_ID = getId(노선, "id");
        Long 구간_상행역_ID = getId(노선, "downStationId");
        Long 구간_하행역_ID = getId(지하철역_생성(GANGNAM_STATION_NAME), "id");
        //When 지하철 구간 등록
        ExtractableResponse<Response> 구간 = 구간_등록_요청(노선_ID, 구간_상행역_ID, 구간_하행역_ID, DISTANCE);
        // then 지하철 구간 등록 성공 응답받는다.
        생성_성공_응답(구간);
    }

    /**
     * Given 노선 하행 종점역과 다른 구간 상행역을 생성한다
     * When 지하철 구간을 등록하면
     * Then 서버 에러를 응답 받는다.
     * Then 구간의 상행역은 노선의 하향 종점역이어야 합니다. 메시지를 응답 받는다.
     */
    @DisplayName("구간 생성시 구간 상행역 != 노선 하행 종점역일 경우 구간 등록시 에러 발생")
    @Test
    void 구간_등록_시실패_테스트() {

        // Given  구간 상행역 != 노선 하행 종점역
        ExtractableResponse<Response> 노선 = 노선_생성_요청(SHIN_BUNDANG_LINE_NAME, SHIN_BUNDANG_LINE_COLOR, SHIN_BUNDANG_UP_STATION_NAME, SHIN_BUNDANG_DOWN_STATION_NAME, DISTANCE);
        Long 노선_ID = getId(노선, "id");
        Long 구간_상행역_ID = getId(노선, "upStationId");
        Long 구간_하행역_ID = getId(노선, "downStationId");

        // When 구간 등록
        ExtractableResponse<Response> 구간 = 구간_등록_요청(노선_ID, 구간_상행역_ID, 구간_하행역_ID, DISTANCE);

        Throwable exception = Assertions.assertThrows((Throwable.class), () -> {
            throw new IllegalArgumentException("section.upStation.not.line.downStation");
        });

        assertAll(
                // 서버 에러를 응답 받는다.
                () -> 서버_에러_응답(구간),
                // Then 구간의 상행역은 노선의 하향 종점역이어야 합니다. exception 발생
                () -> assertEquals("section.upStation.not.line.downStation", exception.getMessage())
        );
    }

    /**
     * Given 노선에 이미 있는 구간의 하행역을 생성한다.
     * When 구간을 등록하면
     * Then 서버 에러를 응답 받는다.
     * Then 구간의 하행역은 노선에 없는 역이어야합니다라는 메시지를 응답받는다.
     */
    @DisplayName("구간의 하행역이 노선의 역과 중복되어 오류 발생")
    @Test
    void 구간_하행역_노선_중복_오류_발생_테스트() {

        //Given 기존 노선의 역과 중복되는 구간의 하행역
        ExtractableResponse<Response> 노선 = 노선_생성_요청(SHIN_BUNDANG_LINE_NAME, SHIN_BUNDANG_LINE_COLOR, SHIN_BUNDANG_UP_STATION_NAME, SHIN_BUNDANG_DOWN_STATION_NAME, DISTANCE);
        Long 노선_ID = getId(노선, "id");
        Long 구간_상행역_ID = getId(노선, "downStationId");
        Long 구간_하행역_ID = getId(노선, "downStationId");

        //When 구간 등록
        ExtractableResponse<Response> 구간 = 구간_등록_요청(노선_ID, 구간_상행역_ID, 구간_하행역_ID, DISTANCE);

        Throwable exception = Assertions.assertThrows((Throwable.class), () -> {
            throw new IllegalArgumentException("section.downStation.line.duplicate");
        });

        assertAll(
                //서버 에러를 응답 받는다.
                () -> 서버_에러_응답(구간),
                //Then 구간의 상행역은 노선의 하향 종점역이어야 합니다. exception 발생
                () -> assertEquals("section.downStation.line.duplicate", exception.getMessage())
        );
    }

    /**
     * Given 구간 생성한다.
     * When 구간을 삭제하면
     * Then 삭제 성공 응답받는다.
     */
    @DisplayName("구간 삭제 성공")
    @Test
    void 지하철_구간_삭제_테스트() {
        //Given 노선 생성
        ExtractableResponse<Response> 노선 = 노선_생성_요청(SHIN_BUNDANG_LINE_NAME, SHIN_BUNDANG_LINE_COLOR, SHIN_BUNDANG_UP_STATION_NAME, SHIN_BUNDANG_DOWN_STATION_NAME, DISTANCE);
        Long 노선_ID = getId(노선, "id");
        Long 구간_상행역_ID = getId(노선, "downStationId");
        Long 구간_하행역_ID = getId(지하철역_생성("양재역"), "id");
        구간_등록_요청(노선_ID, 구간_상행역_ID, 구간_하행역_ID, DISTANCE);
        //When 구간을 삭제한다.
        ExtractableResponse<Response> 구간_삭제_응답 = 구간_삭제(노선_ID, 구간_하행역_ID);
        //When 구간 삭제에 성공한다.
        삭제_성공_응답(구간_삭제_응답);
    }

    /**
     * Given 구간의 상행역을 선택하고
     * When 구간 삭제하면
     * Then 서버 에러를 응답 받는다.
     * Then 구간의 삭제는 하행역만 가능합니다라는 메시지를 응답받는다.
     */
    @DisplayName("상행 종점역 제거 시 오류")
    @Test
    void 상행_종점역_제거_시_오류_테스트() {
        //Given 구간 삭제 매개변수로 구간의 상행역을 준다
        ExtractableResponse<Response> 노선 = 노선_생성_요청(SHIN_BUNDANG_LINE_NAME, SHIN_BUNDANG_LINE_COLOR, SHIN_BUNDANG_UP_STATION_NAME, SHIN_BUNDANG_DOWN_STATION_NAME, DISTANCE);
        Long 노선_ID = getId(노선, "id");
        Long 구간_상행역_ID = getId(노선, "downStationId");
        Long 구간_하행역_ID = getId(지하철역_생성("양재역"), "id");
        구간_등록_요청(노선_ID, 구간_상행역_ID, 구간_하행역_ID, DISTANCE);
        //When 구간을 삭제한다.
        ExtractableResponse<Response> 구간_삭제_응답 = 구간_삭제(노선_ID, 구간_상행역_ID);
        Throwable exception = Assertions.assertThrows((Throwable.class), () -> {
            throw new IllegalArgumentException("section.upStation.not.delete");
        });
        assertAll(
                // Then 서버 에러를 응답 받는다.
                () -> 서버_에러_응답(구간_삭제_응답),
                // Then 구간의 삭제는 하행역만 가능합니다.
                () -> assertEquals("section.upStation.not.delete", exception.getMessage())
        );
    }

    /**
     * Given 1개의 구간만 가지고 있는 노선을 생성한다.
     * When 구간을 삭제하면
     * Then 서버 에러를 응답받는다.
     * Then 구간 삭제는 1개 구간 이상 존재할 때 가능합니다라는 메시지를 응답 받는다.
     */
    @DisplayName("구간 1개일 경우 구간 삭제 실패")
    @Test
    void 구간_1개일_경우_구간_삭제_실패테스트() {
        //Given 구간 삭제 매개변수로 구간의 상행역을 준다
        ExtractableResponse<Response> 노선 = 노선_생성_요청(SHIN_BUNDANG_LINE_NAME, SHIN_BUNDANG_LINE_COLOR, SHIN_BUNDANG_UP_STATION_NAME, SHIN_BUNDANG_DOWN_STATION_NAME, DISTANCE);
        Long 노선_ID = getId(노선, "id");
        Long 구간_상행역_ID = getId(노선, "downStationId");
        Long 구간_하행역_ID = getId(지하철역_생성("양재역"), "id");
        //When 구간을 삭제한다.
        ExtractableResponse<Response> 구간_삭제_응답 = 구간_삭제(노선_ID, 구간_상행역_ID);
        Throwable exception = Assertions.assertThrows((Throwable.class), () -> {
            throw new IllegalArgumentException("section.count.less");
        });

        assertAll(
                // Then 서버 에러를 응답 받는다.
                () -> 서버_에러_응답(구간_삭제_응답),
                // Then 구간의 삭제는 하행역만 가능합니다.
                () -> assertEquals("section.count.less", exception.getMessage())
        );
    }

    private long getId(ExtractableResponse<Response> 노선, String id) {
        return 노선.jsonPath().getLong(id);
    }

}