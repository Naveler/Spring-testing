package ee.bitweb.testingsample.domain.datapoint.features;


import ee.bitweb.testingsample.domain.datapoint.DataPointHelper;
import ee.bitweb.testingsample.domain.datapoint.common.DataPoint;
import ee.bitweb.testingsample.domain.datapoint.common.DataPointRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class FindAllDataPointsFeaturesUnitTests {


    @InjectMocks
    private FindAllDataPointsFeature findAllDataPointsFeature;

    @Mock
    private DataPointRepository repository;


    @Test
    void onGetShouldReturnArrayOfDataPoints() {
        DataPoint firstPoint = DataPointHelper.create(1L);
        DataPoint secondPoint = DataPointHelper.create(2L);

        when(repository.findAll()).thenReturn(List.of(firstPoint, secondPoint));
        List<DataPoint> dataPoints = findAllDataPointsFeature.find();

        assertAll(
                () -> assertEquals(2L, dataPoints.size()),
                () -> Assertions.assertEquals("some-value-1", dataPoints.get(0).getValue()),
                () -> Assertions.assertEquals("some-comment-1", dataPoints.get(0).getComment()),
                () -> Assertions.assertEquals(1, dataPoints.get(0).getSignificance()),
                () -> Assertions.assertEquals("some-value-2", dataPoints.get(1).getValue()),
                () -> Assertions.assertEquals("some-comment-2", dataPoints.get(1).getComment()),
                () -> Assertions.assertEquals(0, dataPoints.get(1).getSignificance())

        );

    }
}