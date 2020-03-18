package pl.allegro.search.solr.ids.buffer;

import com.carrotsearch.hppc.LongArrayList;
import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class LongArrayListIdsBuffer implements IdsBuffer {
    private final LongArrayList storage;

    public LongArrayListIdsBuffer(LongArrayList storage) {
        this.storage = storage;
    }

    @Override
    @NotNull
    public Iterator<String> iterator() {
        return Iterators.transform(storage.iterator(), longCursor -> Long.toString(longCursor.value));
    }

}
