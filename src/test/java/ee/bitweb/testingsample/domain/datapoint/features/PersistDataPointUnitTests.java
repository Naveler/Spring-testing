package ee.bitweb.testingsample.domain.datapoint.features;

import ee.bitweb.testingsample.common.exception.persistence.ConflictException;
import ee.bitweb.testingsample.domain.datapoint.DataPointHelper;
import ee.bitweb.testingsample.domain.datapoint.common.DataPoint;
import ee.bitweb.testingsample.domain.datapoint.common.DataPointRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;


@ExtendWith(MockitoExtension.class)
public class PersistDataPointUnitTests {

    @InjectMocks
    private PersistDataPointFeature persistDataPointFeature;

    @Mock
    private DataPointRepository repository;

    @Captor
    private ArgumentCaptor<DataPoint> dataPointArgumentCaptor;


    @Test
    void onValidDataPointShouldSaveAndReturnEntity() throws Exception {
        DataPoint point = DataPointHelper.create(1L);
        doReturn(point).when(repository).save(point);
        persistDataPointFeature.save(point);

        assertEquals("external-id-1", point.getExternalId());
        assertEquals("some-value-1", point.getValue());
    }

    @Test
    void onInvalidDataPointShouldThrowError() throws Exception{

        DataPoint point = DataPointHelper.create(1L);

        persistDataPointFeature.save(point);
        persistDataPointFeature.save(point);

        doThrow(DataIntegrityViolationException.class).when(repository).save(any());

        assertThrows(ConflictException.class, () -> {
           persistDataPointFeature.save(point);
        });

    }


}