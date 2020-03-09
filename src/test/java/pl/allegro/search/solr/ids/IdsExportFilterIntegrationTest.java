package pl.allegro.search.solr.ids;

import ch.qos.logback.classic.Level;
import com.google.common.io.Files;
import com.jayway.jsonpath.JsonPath;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.request.SolrQueryRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.allegro.search.solr.testutils.IndexingUtility;
import pl.allegro.search.solr.testutils.IndexingUtility.Document;

import java.util.Collections;
import java.util.List;

import static com.carrotsearch.randomizedtesting.RandomizedTest.getContext;
import static java.util.Arrays.asList;
import static pl.allegro.search.solr.testutils.IndexingUtility.docId;

public class IdsExportFilterIntegrationTest extends SolrTestCaseJ4 {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setup() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.ERROR);
        System.out.println("seed: " + getContext().getRunnerSeedAsString());
    }

    void indexAll(IndexingUtility index, Document... documents) {
        index.indexEachInSingleSegments(documents);
    }

    private void prepareDefaultConfigurationIndex(Document... documents) throws Exception {
        initCore("solrconfig.xml", "schema.xml", Files.createTempDir().getAbsolutePath());
        IndexingUtility index = new IndexingUtility(h);
        indexAll(index, documents);
    }

    private void prepareAlternativeConfigurationIndex(Document... documents) throws Exception {
        initCore("solrconfig-different-data.xml", "schema.xml", Files.createTempDir().getAbsolutePath());
        IndexingUtility index = new IndexingUtility(h);
        indexAll(index, documents);
    }

    @After
    public void close() {
        deleteCore();
    }

    @Test
    public void shouldCollectValuesFromConfigurationDefinedField() throws Exception {
        // given
        prepareDefaultConfigurationIndex(
                docId(1),
                docId(2),
                docId(3)
        );

        SolrQueryRequest req = req(
                "q", "*:*",
                "fq", "{!ids}",
                "wt", "json"
        );

        // when
        String response = h.query(req);

        // then
        List<String> values = JsonPath.read(response, "$.ids");
        assertEquals(asList("1", "2", "3"), values);
    }

    @Test
    public void shouldCollectValuesFromFieldWithNumericDocValues() throws Exception {
        // given
        prepareDefaultConfigurationIndex(
                docId(1).size(1),
                docId(2).size(2),
                docId(3).size(3),
                docId(4)
        );

        SolrQueryRequest req = req(
                "q", "*:*",
                "fq", "{!ids field=size}",
                "wt", "json"
        );

        // when
        String response = h.query(req);

        // then
        List<String> values = JsonPath.read(response, "$.ids");
        assertEquals(asList("1", "2", "3"), values);
    }

    @Test
    public void shouldCollectValuesFromFieldWithSortedDocValues() throws Exception {
        // given
        prepareDefaultConfigurationIndex(
                docId(1).name("dog"),
                docId(2).name("cat"),
                docId(3).name("cow"),
                docId(4)
        );

        SolrQueryRequest req = req(
                "q", "*:*",
                "fq", "{!ids field=name}",
                "wt", "json"
        );

        // when
        String response = h.query(req);

        // then
        List<String> values = JsonPath.read(response, "$.ids");
        assertEquals(asList("dog", "cat", "cow"), values);
    }

    @Test
    public void shouldCollectValuesFromFieldWithSortedSetDocValues() throws Exception {
        // given
        prepareDefaultConfigurationIndex(
                docId(1).tags("mammal", "barks"),
                docId(2).tags("mammal", "meows"),
                docId(3).tags("mammal", "moos"),
                docId(4)
        );

        SolrQueryRequest req = req(
                "q", "*:*",
                "fq", "{!ids field=tags}",
                "wt", "json"
        );

        // when
        String response = h.query(req);

        // then
        List<String> values = JsonPath.read(response, "$.ids");
        assertEquals(asList("barks", "mammal", "mammal", "meows", "mammal", "moos"), values);
    }

    @Test
    public void shouldIncludeValuesFromFieldWithSortedNumericDocValues() throws Exception {
        // given
        prepareDefaultConfigurationIndex(
                docId(1).prices(1L, 2L),
                docId(2).prices(100L, 200L),
                docId(3).prices(20L, 10L),
                docId(4)
        );


        SolrQueryRequest req = req(
                "q", "*:*",
                "fq", "{!ids field=prices}",
                "wt", "json"
        );

        // when
        String response = h.query(req);

        // then
        List<String> values = JsonPath.read(response, "$.ids");
        assertEquals(asList("1", "2", "100", "200", "10", "20"), values);
    }

    @Test
    public void shouldIncludeValuesFromFieldWithBinaryDocValues() {
        // TODO
        // I was unable to create field with DocValues, which would be reported by SOLR as DocValuesType.BINARY
    }

    @Test
    public void shouldThrowExceptionIfFieldDoesntHaveDocValues() throws Exception {
        // given
        prepareDefaultConfigurationIndex(
                docId(1),
                docId(2),
                docId(3)
        );


        SolrQueryRequest req = req(
                "q", "*:*",
                "fq", "{!ids field=desc}",
                "wt", "json"
        );

        // expect
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("IdsExportFilter supports only fields with docValues. Field desc doesn't have them.");

        // when
        h.query(req);
    }


    @Test
    public void shouldConsiderFiltering() throws Exception {
        // given
        prepareDefaultConfigurationIndex(
                docId(1).prices(100L),
                docId(2).prices(200L),
                docId(3).prices(300L)
        );

        SolrQueryRequest req = req(
                "q", "*:*",
                "fq", "prices:100",
                "fq", "{!ids}",
                "wt", "json"
        );

        // when
        String response = h.query(req);

        // then
        List<String> values = JsonPath.read(response, "$.ids");
        assertEquals(Collections.singletonList("1"), values);
    }

    @Test
    public void shouldNotConsiderPaging() throws Exception {
        // given
        prepareDefaultConfigurationIndex(
                docId(1),
                docId(2),
                docId(3)
        );

        SolrQueryRequest req = req(
                "q", "*:*",
                "fq", "{!ids}",
                "wt", "json",
                "rows", "1"
        );

        // when
        String response = h.query(req);

        // then
        List<String> values = JsonPath.read(response, "$.ids");
        assertEquals(asList("1", "2", "3"), values);
    }

    @Test
    public void shouldUseDifferentResponseKey() throws Exception {
        // given
        prepareAlternativeConfigurationIndex(
                docId(1),
                docId(2),
                docId(3)
        );
        String responsePath = "$.responseIds";
        SolrQueryRequest req = req(
                "q", "*:*",
                "fq", "{!ids field=doc_id}",
                "wt", "json"
        );

        // when
        String response = h.query(req);

        // then
        List<String> values = JsonPath.read(response, responsePath);
        assertEquals(asList("1", "2", "3"), values);
    }

    @Test
    public void shouldUseDifferentDefaultIndexField() throws Exception {
        // given
        prepareAlternativeConfigurationIndex(
                docId(1).name("dog"),
                docId(2).name("cat"),
                docId(3).name("cow")
        );
        SolrQueryRequest req = req(
                "q", "*:*",
                "fq", "{!ids}",
                "wt", "json"
        );

        // when
        String response = h.query(req);

        // then
        List<String> values = JsonPath.read(response, "$.responseIds");
        assertEquals(asList("dog", "cat", "cow"), values);
    }
}
