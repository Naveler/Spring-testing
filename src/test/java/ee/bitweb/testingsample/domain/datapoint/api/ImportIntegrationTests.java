package ee.bitweb.testingsample.domain.datapoint.api;

import java.util.List;

import ee.bitweb.testingsample.common.trace.TraceIdCustomizerImpl;
import ee.bitweb.testingsample.domain.datapoint.DataPointHelper;
import ee.bitweb.testingsample.domain.datapoint.common.DataPoint;
import ee.bitweb.testingsample.domain.datapoint.common.DataPointRepository;
import ee.bitweb.testingsample.domain.datapoint.external.ExternalService;
import ee.bitweb.testingsample.domain.datapoint.external.ExternalServiceApi;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"data-points.external.baseUrl=http://localhost:12347/"}
)
class ImportIntegrationTests {

    private static final String URI = "/data-points/import";
    private static final String REQUEST_ID = "ThisIsARequestId";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataPointRepository repository;

    @MockBean
    private ExternalService externalService;

    @Test
    @Transactional
    void onRequestShouldRequestDataPointsFromExternalServiceAndPersist() throws Exception {
        DataPoint dataPoint = DataPointHelper.create(1L);
        DataPoint newDataPoint = DataPointHelper.create(1L);
        newDataPoint.setValue("Something-new");

        repository.save(dataPoint);

        Mockito.doReturn(List.of(
                toResponse(newDataPoint)
        )).when(externalService).getAll();

        mockMvc.perform(createDefaultRequest())
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]", aMapWithSize(5)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].externalId", is("external-id-1")))
                .andExpect(jsonPath("$[0].value", is("Something-new")))
                .andExpect(jsonPath("$[0].comment", is("some-comment-1")))
                .andExpect(jsonPath("$[0].significance", is(1)));
    }

    private MockHttpServletRequestBuilder createDefaultRequest() {
        return post(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(TraceIdCustomizerImpl.DEFAULT_HEADER_NAME, REQUEST_ID);
    }

    private ExternalServiceApi.DataPointResponse toResponse(DataPoint point) {
        ExternalServiceApi.DataPointResponse response = new ExternalServiceApi.DataPointResponse();

        response.setExternalId(point.getExternalId());
        response.setValue(point.getValue());
        response.setComment(point.getComment());
        response.setSignificance(point.getSignificance());

        return response;
    }
}
