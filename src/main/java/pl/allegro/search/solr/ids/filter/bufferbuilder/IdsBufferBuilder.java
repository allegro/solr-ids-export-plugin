package pl.allegro.search.solr.ids.filter.bufferbuilder;

import pl.allegro.search.solr.ids.buffer.IdsBuffer;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.LeafReaderContext;

import java.io.IOException;

public interface IdsBufferBuilder {
    static IdsBufferBuilder newIdsBufferBuilder(DocValuesType docValuesType, String field, int bufferInitialSize) {
        switch (docValuesType) {
            case NUMERIC:
                return new NumericDocValuesIdsBufferBuilder(field, bufferInitialSize);
            case SORTED_NUMERIC:
                return new SortedNumericDocValuesIdsBufferBuilder(field, bufferInitialSize);
            case BINARY:
                return new BinaryDocValuesIdsBufferBuilder(field, bufferInitialSize);
            case SORTED:
                return new SortedDocValuesIdsBufferBuilder(field, bufferInitialSize);
            case SORTED_SET:
                return new SortedSetDocValuesIdsBufferBuilder(field, bufferInitialSize);
            default:
                throw new RuntimeException("Unsupported DocValues type " + docValuesType + " for field " + field + ". IdsExportFilter cannot use this type of DocValues.");
        }
    }

    void doSetNextReader(LeafReaderContext context) throws IOException;

    void collect(int doc) throws IOException;

    IdsBuffer build();

}
