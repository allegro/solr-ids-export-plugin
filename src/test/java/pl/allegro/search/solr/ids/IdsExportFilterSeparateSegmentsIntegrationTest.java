package pl.allegro.search.solr.ids;

import pl.allegro.search.solr.testutils.IndexingUtility;
import pl.allegro.search.solr.testutils.IndexingUtility.Document;

public class IdsExportFilterSeparateSegmentsIntegrationTest extends IdsExportFilterIntegrationTest {

    /*
     * SolrTestCaseJ4 requires it's own TestRunner, so I can't use JUnit's parametrized test
     *
     * This class is a workaround - it will run all tests from IdsExportFilterIntegrationTest,
     * but with different setup - documents will be placed in separate segments instead of one big segment.
     */

    @Override
    protected void indexAll(IndexingUtility index, Document... documents) {
        index.indexEachInSeparateSegments(documents);
    }
}
