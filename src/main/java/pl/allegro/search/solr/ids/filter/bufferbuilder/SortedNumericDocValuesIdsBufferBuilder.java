package pl.allegro.search.solr.ids.filter.bufferbuilder;

import com.carrotsearch.hppc.LongArrayList;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedNumericDocValues;
import pl.allegro.search.solr.ids.buffer.IdsBuffer;
import pl.allegro.search.solr.ids.buffer.LongArrayListIdsBuffer;

import java.io.IOException;

public class SortedNumericDocValuesIdsBufferBuilder implements IdsBufferBuilder {
    private final String field;
    private final LongArrayList buffer;

    private SortedNumericDocValues docValues;

    public SortedNumericDocValuesIdsBufferBuilder(String field, int bufferInitialSize) {
        this.field = field;
        this.buffer = new LongArrayList(bufferInitialSize);
    }

    @Override
    public void doSetNextReader(LeafReaderContext context) throws IOException {
        docValues = context.reader().getSortedNumericDocValues(field);
    }

    @Override
    public void collect(int doc) throws IOException {
        if (docValues != null && docValues.advanceExact(doc)) {
            for (int i = 0; i < docValues.docValueCount(); i++) {
                buffer.add(docValues.nextValue());
            }
        }
    }

    @Override
    public IdsBuffer build() {
        return new LongArrayListIdsBuffer(buffer);
    }


}
