package pl.allegro.search.solr.ids;

import ch.qos.logback.classic.Level;
import com.google.common.io.Files;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.request.SolrQueryRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.allegro.search.solr.testutils.IndexingUtility;

import static com.carrotsearch.randomizedtesting.RandomizedTest.getContext;
import static pl.allegro.search.solr.testutils.IndexingUtility.docId;

public class IdsExportResponseWriterIntegrationTest extends SolrTestCaseJ4 {

    @Before
    public void setup() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.ERROR);
        System.out.println("seed: " + getContext().getRunnerSeedAsString());
    }

    @After
    public void close() {
        deleteCore();
    }

    private void prepareDefaultConfigurationIndex(IndexingUtility.Document... documents) throws Exception {
        initCore("solrconfig.xml", "schema.xml", Files.createTempDir().getAbsolutePath());
        IndexingUtility index = new IndexingUtility(h);
        index.indexEachInSeparateSegments(documents);
    }

    private void prepareAlternativeConfigurationIndex(IndexingUtility.Document... documents) throws Exception {
        initCore("solrconfig-different-data.xml", "schema.xml", Files.createTempDir().getAbsolutePath());
        IndexingUtility index = new IndexingUtility(h);
        index.indexEachInSeparateSegments(documents);
    }

    @Test
    public void shouldSendIdsFilterResponse() throws Exception {
        // given
        prepareDefaultConfigurationIndex(
                docId(1),
                docId(2),
                docId(3)
        );

        SolrQueryRequest req = req(
                "q", "*:*",
                "fq", "{!ids}",
                "wt", "ids"
        );

        // when
        String response = h.query(req);

        // then
        assertEquals("1,2,3", response);
    }

    @Test
    public void shouldSendIdsFilterResponseUsingAlternativeConfiguration() throws Exception {
        // given
        prepareAlternativeConfigurationIndex(
                docId(1),
                docId(2),
                docId(3)
        );

        SolrQueryRequest req = req(
                "q", "*:*",
                "fq", "{!ids field=doc_id}",
                "wt", "ids"
        );

        // when
        String response = h.query(req);

        // then
        assertEquals("1;2;3", response);
    }
}
