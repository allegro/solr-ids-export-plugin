package pl.allegro.search.solr.ids.filter.bufferbuilder;

import pl.allegro.search.solr.ids.buffer.IdsBuffer;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.util.BytesRef;
import pl.allegro.search.solr.ids.buffer.ListOfBytesRefIdsBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.lucene.util.BytesRef.deepCopyOf;

public class BinaryDocValuesIdsBufferBuilder implements IdsBufferBuilder {
    private final String field;
    private final List<BytesRef> buffer;

    private BinaryDocValues docValues;

    public BinaryDocValuesIdsBufferBuilder(String field, int bufferInitialSize) {
        this.field = field;
        this.buffer = new ArrayList<>(bufferInitialSize);
    }

    @Override
    public void doSetNextReader(LeafReaderContext context) throws IOException {
        docValues = context.reader().getBinaryDocValues(field);
    }

    @Override
    public void collect(int doc) throws IOException {
        if (docValues != null && docValues.advanceExact(doc)) {
            buffer.add(deepCopyOf(docValues.binaryValue()));
        }
    }

    @Override
    public IdsBuffer build() {
        return new ListOfBytesRefIdsBuffer(buffer);
    }


}
