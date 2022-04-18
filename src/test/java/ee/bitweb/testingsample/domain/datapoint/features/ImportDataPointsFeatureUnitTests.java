package ee.bitweb.testingsample.domain.datapoint.features;

import ee.bitweb.testingsample.domain.datapoint.common.DataPoint;
import ee.bitweb.testingsample.domain.datapoint.external.ExternalService;
import ee.bitweb.testingsample.domain.datapoint.external.ExternalServiceApi;
import ee.bitweb.testingsample.domain.datapoint.features.create.CreateDataPointFeature;
import ee.bitweb.testingsample.domain.datapoint.features.update.UpdateDataPointFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ImportDataPointsFeatureUnitTests {

    @InjectMocks
    private UpdateDataPointFeature updateFeature;

    @Mock
    private ImportDataPointsFeature importDataPointsFeature;

    @Mock
    private CreateDataPointFeature createFeature;

    @Mock
    private GetDataPointByIdFeature getDataPointByExternalIdFeature;

    @Mock
    private ExternalService externalService;

    @Test
    void findAll() {
        ExternalServiceApi.DataPointResponse dataPointResponse = new ExternalServiceApi.DataPointResponse();
        dataPointResponse.setComment("some comment");
        dataPointResponse.setSignificance(1);
        dataPointResponse.setExternalId("some id");
        dataPointResponse.setValue("some value");

        List<DataPoint> dataPoint =  new ArrayList<>();


    }
}