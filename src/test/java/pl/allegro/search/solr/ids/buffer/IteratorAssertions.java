package pl.allegro.search.solr.ids.buffer;

import org.apache.commons.collections.IteratorUtils;
import org.junit.Assert;

import java.util.Iterator;
import java.util.List;

class IteratorAssertions {
    static IteratorAssertions assertThat(Iterator<String> iterator) {
        return new IteratorAssertions(iterator);
    }

    private final Iterator<String> iterator;

    IteratorAssertions(Iterator<String> iterator) {
        this.iterator = iterator;
    }

    void iteratesOver(List<String> elementsAsString) {
        Assert.assertEquals(elementsAsString, IteratorUtils.toList(iterator));
    }
}
