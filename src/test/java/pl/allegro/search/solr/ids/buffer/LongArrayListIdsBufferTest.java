package pl.allegro.search.solr.ids.buffer;

import com.carrotsearch.hppc.LongArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static pl.allegro.search.solr.ids.buffer.IteratorAssertions.assertThat;


public class LongArrayListIdsBufferTest {
    @Test
    public void shouldIterateOverAllStorageElements() {
        // given
        List<Long> elements = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L);
        List<String> elementsAsString = Arrays.asList("1", "2", "3", "4", "5", "6");

        LongArrayList longArrayList = new LongArrayList();
        longArrayList.add(ArrayUtils.toPrimitive(elements.toArray(new Long[0])));

        LongArrayListIdsBuffer buffer = new LongArrayListIdsBuffer(longArrayList);

        // when
        Iterator<String> iterator = buffer.iterator();

        // then
        assertThat(iterator).iteratesOver(elementsAsString);
    }

}
