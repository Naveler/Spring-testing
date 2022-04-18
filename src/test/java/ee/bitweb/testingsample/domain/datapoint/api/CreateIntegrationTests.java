package ee.bitweb.testingsample.domain.datapoint.api;

import ee.bitweb.testingsample.common.trace.TraceIdCustomizerImpl;
import ee.bitweb.testingsample.domain.datapoint.common.DataPoint;
import ee.bitweb.testingsample.domain.datapoint.common.DataPointRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.transaction.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class CreateIntegrationTests {

    private static final String URI = "/data-points";

    private static final String REQUEST_ID = "ThisIsARequestId";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataPointRepository repository;

    @Test
    @Transactional
    void onMinimalValidPayloadShouldCreatePointAndReturnSuccess() throws Exception {

    }


    private MockHttpServletRequestBuilder createDefaultRequest(JSONObject payload) {
        return post(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(TraceIdCustomizerImpl.DEFAULT_HEADER_NAME, REQUEST_ID)
                .content(payload.toString());
    }

    private JSONObject createValidFullPayload(Long id) {
        JSONObject payload = createValidMinimalPayload(id);

        payload.put("comment", "some-comment-" + id);

        return payload;
    }

    private JSONObject createValidMinimalPayload(Long id) {
        JSONObject payload = new JSONObject();

        payload.put("externalId", "external-id-" + id);
        payload.put("value", "some-value-" + id);
        payload.put("significance", (id % 2));

        return payload;
    }

    private DataPoint createDataPoint(Long id) {
        DataPoint p = new DataPoint();
        p.setExternalId("external-id-" + id);
        p.setValue("initial-some-value-" + id);
        p.setComment("initial-some-commend-" + id);
        p.setSignificance(id.intValue());

        return p;
    }
}
