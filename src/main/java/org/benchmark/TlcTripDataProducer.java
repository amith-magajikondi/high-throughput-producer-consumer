package org.benchmark;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MappingIterator;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;
import tools.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.util.concurrent.BlockingQueue;

@RequiredArgsConstructor
@Slf4j
public class TlcTripDataProducer {
    private final BlockingQueue<TlcTripData> dataQueue;

    private final String FILE_PATH = "C:\\Users\\Lenovo\\Downloads\\archive\\yellow_tripdata_2015-01.csv";

    public void runProducer(BlockingQueue<TlcTripData> dataQueue) throws InterruptedException{
        log.info("Producer running.");
        CsvMapper mapper = CsvMapper.builder().addModule(new JavaTimeModule()).disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES).build();

        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        MappingIterator<TlcTripData> it = mapper.readerFor(TlcTripData.class)
                .with(schema)
                .readValues(new File(FILE_PATH));

        while (it.hasNext()) {
            TlcTripData dataRow = it.next();
            dataQueue.put(dataRow);
        }

        for(int i = 0; i < 6; i++){
            dataQueue.put(new TlcTripData.TlcTripDataBuilder().dummyRow(1).build());
        }
    }
}
