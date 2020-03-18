package pl.allegro.search.solr.ids.buffer;

import org.apache.commons.collections.IteratorUtils;
import org.apache.lucene.util.BytesRef;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static pl.allegro.search.solr.ids.buffer.IteratorAssertions.assertThat;

public class ListOfBytesRefIdsBufferTest {
    @Test
    public void shouldIterateOverAllStorageElements() {
        // given
        List<BytesRef> elements = Arrays.asList(new BytesRef("dog"), new BytesRef("cat"), new BytesRef("horse"), new BytesRef("cow"), new BytesRef("fish"));
        List<String> elementsAsString = Arrays.asList("dog", "cat", "horse", "cow", "fish");
        ListOfBytesRefIdsBuffer buffer = new ListOfBytesRefIdsBuffer(elements);

        // when
        Iterator<String> iterator = buffer.iterator();

        // then
        assertThat(iterator).iteratesOver(elementsAsString);
    }
}
