package pl.allegro.search.solr.ids.buffer;

import com.google.common.collect.Iterators;
import org.apache.lucene.util.BytesRef;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class ListOfBytesRefIdsBuffer implements IdsBuffer {
    private final List<BytesRef> storage;

    public ListOfBytesRefIdsBuffer(List<BytesRef> storage) {
        this.storage = storage;
    }

    @Override
    @NotNull
    public Iterator<String> iterator() {
        return Iterators.transform(storage.iterator(), BytesRef::utf8ToString);
    }
}
