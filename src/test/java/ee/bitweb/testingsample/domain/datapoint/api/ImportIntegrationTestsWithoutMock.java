package ee.bitweb.testingsample.domain.datapoint.api;

import ee.bitweb.testingsample.common.trace.TraceIdCustomizerImpl;
import ee.bitweb.testingsample.domain.datapoint.DataPointHelper;
import ee.bitweb.testingsample.domain.datapoint.MockServerHelper;
import ee.bitweb.testingsample.domain.datapoint.common.DataPoint;
import ee.bitweb.testingsample.domain.datapoint.common.DataPointRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockserver.model.HttpRequest.request;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"data-points.external.baseUrl=http://localhost:12347/"}
)


public class ImportIntegrationTestsWithoutMock {

    private static final String URI = "/data-points/import";
    private static final String REQUEST_ID = "ThisIsARequestId";
    private static ClientAndServer externalService;


    @BeforeAll
    static void setup() {
        externalService = ClientAndServer.startClientAndServer(12347);
    }

    @BeforeEach
    public void beforeEach() {
        externalService.reset();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataPointRepository repository;

    @Test
    @Transactional
    void onRequestShouldRequestDataPointsFromExternalServiceAndPersist() throws Exception {
        repository.save(DataPointHelper.create(1L));

        MockServerHelper.setupGetMockRouteWithString(
                externalService,
                "/data-points",
                200,
                1,
                createExternalServiceResponse(
                        List.of(
                                createExternalServiceResponse(1L),
                                createExternalServiceResponse(2L),
                                createExternalServiceResponse(3L)
                        )
                ).toString()


        );

        mockMvc.perform(createDefaultRequest())
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", aMapWithSize(5)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].externalId", is("external-id-1")))
                .andExpect(jsonPath("$[0].value", is("value-1")))
                .andExpect(jsonPath("$[0].comment", is("comment-1")))
                .andExpect(jsonPath("$[0].significance", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].externalId", is("external-id-2")))
                .andExpect(jsonPath("$[1].value", is("value-2")))
                .andExpect(jsonPath("$[1].comment", is("comment-2")))
                .andExpect(jsonPath("$[1].significance", is(0)))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].externalId", is("external-id-3")))
                .andExpect(jsonPath("$[2].value", is("value-3")))
                .andExpect(jsonPath("$[2].comment", is("comment-3")))
                .andExpect(jsonPath("$[2].significance", is(1)));


        List<DataPoint> dataPoints = repository.findAll();

        assertAll(
                () -> Assertions.assertEquals("external-id-1", dataPoints.get(0).getExternalId()),
                () -> Assertions.assertEquals("value-1", dataPoints.get(0).getValue()),
                () -> Assertions.assertEquals("comment-1", dataPoints.get(0).getComment()),
                () -> Assertions.assertEquals(1, dataPoints.get(0).getSignificance()),

                () -> Assertions.assertEquals("external-id-2", dataPoints.get(1).getExternalId()),
                () -> Assertions.assertEquals("value-2", dataPoints.get(1).getValue()),
                () -> Assertions.assertEquals("comment-2", dataPoints.get(1).getComment()),
                () -> Assertions.assertEquals(0, dataPoints.get(1).getSignificance()),

                () -> Assertions.assertEquals("external-id-3", dataPoints.get(2).getExternalId()),
                () -> Assertions.assertEquals("value-3", dataPoints.get(2).getValue()),
                () -> Assertions.assertEquals("comment-3", dataPoints.get(2).getComment()),
                () -> Assertions.assertEquals(1, dataPoints.get(2).getSignificance())

        );

        externalService.verify(request().withMethod("GET").withPath("/data-points"));
    }

    private DataPoint createDatapoint(long id) {
        DataPoint p = new DataPoint();

        return p;
    }

    JSONArray createExternalServiceResponse(Collection<JSONObject> objects) {
        JSONArray array = new JSONArray();
        objects.forEach((array::put));

        return array;
    }

    JSONObject createExternalServiceResponse(Long id) {
        JSONObject element = new JSONObject();

        element.put("externalId", "external-id-" + id);
        element.put("comment", "comment-" + id);
        element.put("value", "value-" + id);
        element.put("significance", (id % 2));

        return element;
    }


    private MockHttpServletRequestBuilder createDefaultRequest() {
        return post(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(TraceIdCustomizerImpl.DEFAULT_HEADER_NAME, REQUEST_ID);
    }


}