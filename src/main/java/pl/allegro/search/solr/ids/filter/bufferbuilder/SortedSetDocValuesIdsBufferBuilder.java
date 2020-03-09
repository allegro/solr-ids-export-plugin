package pl.allegro.search.solr.ids.filter.bufferbuilder;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.util.BytesRef;
import pl.allegro.search.solr.ids.buffer.IdsBuffer;
import pl.allegro.search.solr.ids.buffer.ListOfBytesRefIdsBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.lucene.util.BytesRef.deepCopyOf;

public class SortedSetDocValuesIdsBufferBuilder implements IdsBufferBuilder {
    private final String field;
    private final List<BytesRef> buffer;

    private SortedSetDocValues docValues;

    public SortedSetDocValuesIdsBufferBuilder(String field, int bufferInitialSize) {
        this.field = field;
        this.buffer = new ArrayList<>(bufferInitialSize);
    }

    @Override
    public void doSetNextReader(LeafReaderContext context) throws IOException {
        docValues = context.reader().getSortedSetDocValues(field);
    }

    @Override
    public void collect(int doc) throws IOException {
        if (docValues != null && docValues.advanceExact(doc)) {
            long ord;
            while ((ord = docValues.nextOrd()) != SortedSetDocValues.NO_MORE_ORDS) {
                buffer.add(deepCopyOf(docValues.lookupOrd(ord)));
            }
        }
    }

    @Override
    public IdsBuffer build() {
        return new ListOfBytesRefIdsBuffer(buffer);
    }


}
