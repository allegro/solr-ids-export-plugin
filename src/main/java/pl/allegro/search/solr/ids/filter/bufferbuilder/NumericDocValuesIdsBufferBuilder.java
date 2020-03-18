package pl.allegro.search.solr.ids.filter.bufferbuilder;

import com.carrotsearch.hppc.LongArrayList;
import pl.allegro.search.solr.ids.buffer.IdsBuffer;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import pl.allegro.search.solr.ids.buffer.LongArrayListIdsBuffer;

import java.io.IOException;

public class NumericDocValuesIdsBufferBuilder implements IdsBufferBuilder {
    private final String field;
    private final LongArrayList buffer;

    private NumericDocValues docValues;

    public NumericDocValuesIdsBufferBuilder(String field, int bufferInitialSize) {
        this.field = field;
        this.buffer = new LongArrayList(bufferInitialSize);
    }

    @Override
    public void doSetNextReader(LeafReaderContext context) throws IOException {
        docValues = context.reader().getNumericDocValues(field);
    }

    @Override
    public void collect(int doc) throws IOException {
        if (docValues != null && docValues.advanceExact(doc)) {
            buffer.add(docValues.longValue());
        }
    }

    @Override
    public IdsBuffer build() {
        return new LongArrayListIdsBuffer(buffer);
    }


}
