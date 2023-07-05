package subway;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql("/station-setup.sql")
public class LineAcceptanceTest {

    //    When 지하철 노선을 생성하면
    //    Then 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다
    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() {
        // when
        지하철_노선을_생성한다("신분당선", "bg-red-600", 1L, 2L, 10L);

        // then
        List<String> lineNames = 지하철_노선_목록을_조회한다();
        생성된_노선이_노선_목록에_포함된다(lineNames, "신분당선");
    }

    //    Given 2개의 지하철 노선을 생성하고
    //    When 지하철 노선 목록을 조회하면
    //    Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void showLines() {
        // given
        지하철_노선을_생성한다("신분당선", "bg-red-600", 1L, 2L, 10L);
        지하철_노선을_생성한다("분당선", "bg-green-600", 1L, 3L, 15L);

        // when
        List<String> lineNames = 지하철_노선_목록을_조회한다();

        // then
        생성한_갯수의_지하철_노선_목록을_응답한다(lineNames, 2);
    }

    //    Given 지하철 노선을 생성하고
    //    When 생성한 지하철 노선을 조회하면
    //    Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void showLine() {
        // given
        ExtractableResponse<Response> createdResponse = 지하철_노선을_생성한다("신분당선", "bg-red-600", 1L, 2L, 10L);

        // when
        ExtractableResponse<Response> selectedResponse = 지하철_노선을_조회한다(createdResponse.header("Location"));

        // then
        생성한_노선의_정보를_응답한다(createdResponse.as(LineResponse.class), selectedResponse.as(LineResponse.class));
    }

    //    Given 지하철 노선을 생성하고
    //    When 생성한 지하철 노선을 수정하면
    //    Then 해당 지하철 노선 정보는 수정된다
    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        // given
        ExtractableResponse<Response> createdResponse = 지하철_노선을_생성한다("신분당선", "bg-red-600", 1L, 2L, 10L);

        // when
        UpdateLineRequest request = new UpdateLineRequest("다른분당선", "bg-red-600");
        RestAssured.given().log().all()
                .body(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().put(createdResponse.header("Location"))
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        // then
        ExtractableResponse<Response> selectedResponse = 지하철_노선을_조회한다(createdResponse.header("Location"));
        LineResponse lineResponse = selectedResponse.as(LineResponse.class);
        assertThat(request.getName()).isEqualTo(lineResponse.getName());
        assertThat(request.getColor()).isEqualTo(lineResponse.getColor());
    }

    private static void 생성한_노선의_정보를_응답한다(LineResponse createdLine, LineResponse selectedLine) {
        assertThat(createdLine).isEqualTo(selectedLine);
    }

    private ExtractableResponse<Response> 지하철_노선을_조회한다(String createdResourceUrl) {
        return RestAssured.given().log().all()
                .when().get(createdResourceUrl)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    private static void 생성한_갯수의_지하철_노선_목록을_응답한다(List<String> lineNames, int createdCount) {
        assertThat(lineNames.size()).isEqualTo(createdCount);
    }

    private static void 생성된_노선이_노선_목록에_포함된다(List<String> lineNames, String createdLineName) {
        assertThat(lineNames).containsAnyOf(createdLineName);
    }

    private static List<String> 지하철_노선_목록을_조회한다() {
        return RestAssured.given().log().all()
                .when().get("/lines")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList("name", String.class);
    }

    private static ExtractableResponse<Response> 지하철_노선을_생성한다(String name, String color, Long upStationId, Long downStationId, Long distance) {
        CreateLineRequest request = new CreateLineRequest(
                name,
                color,
                upStationId,
                downStationId,
                distance
        );

        return RestAssured.given().log().all()
                .body(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract();
    }

    //    Given 지하철 노선을 생성하고
    //    When 생성한 지하철 노선을 삭제하면
    //    Then 해당 지하철 노선 정보는 삭제된다
    //    TODO: 지하철노선 삭제 인수 테스트 작성
}
