
package pl.allegro.search.solr.ids.filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.emptyMap;

public class IdsExportFilterCacheAndCostTest {

    private IdsExportFilter filter;

    @Before
    public void setUp() {
        filter = new IdsExportFilter(emptyMap(), 0, "");
    }

    @Test
    public void shouldDisableCache() {
        // expect
        Assert.assertFalse(filter.getCache());
    }

    @Test
    public void shouldUseDefaultCost() {
        // expect
        Assert.assertEquals(100, filter.getCost());
    }

    @Test
    public void shouldNotAllowLowerCostThenDefault() {
        // when
        filter.setCost(50);

        // then
        Assert.assertEquals(100, filter.getCost());
    }

    @Test
    public void shouldAllowHigherCostThenDefault() {
        // when
        filter.setCost(150);

        // then
        Assert.assertEquals(150, filter.getCost());
    }
}
