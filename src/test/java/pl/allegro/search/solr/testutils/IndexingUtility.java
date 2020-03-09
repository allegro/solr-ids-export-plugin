package pl.allegro.search.solr.testutils;

import org.apache.solr.util.TestHarness;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;

public class IndexingUtility {

    private final TestHarness testHarness;

    public IndexingUtility(TestHarness testHarness) {
        this.testHarness = testHarness;
    }

    private void indexAndCommit(Document document) {
        indexDocument(document);
        commit();
    }

    public void indexEachInSeparateSegments(Document... documents) {
        stream(documents).forEach(this::indexAndCommit);
    }

    public void indexEachInSingleSegments(Document... documents) {
        stream(documents).forEach(this::indexDocument);
        commit();
    }

    private <T> String prepareIndexXmlForMultiValuedField(String name, List<T> values) {
        return values.stream()
                .map(value -> "<field name=\"" + name + "\">" + value.toString() + "</field>\n")
                .collect(Collectors.joining("\n"));
    }

    private <T> String ifExists(Optional<T> data, Function<T, String> toString) {
        return data.map(toString).orElse("");
    }

    private void indexDocument(Document document) {
        testHarness.update("<add>\n" +
                "  <doc>\n" +
                "    <field name=\"doc_id\">" + document.id + "</field>\n" +
                ifExists(document.size, size -> "    <field name=\"size\">" + size + "</field>\n") +
                ifExists(document.desc, desc -> "    <field name=\"desc\">" + desc + "</field>\n") +
                ifExists(document.name, name -> "    <field name=\"name\">" + name + "</field>\n") +
                ifExists(document.tags, tags -> prepareIndexXmlForMultiValuedField("tags", tags)) +
                ifExists(document.prices, prices -> prepareIndexXmlForMultiValuedField("prices", prices)) +
                "  </doc>\n" +
                "</add>\n" +
                "");
    }

    private void commit() {
        testHarness.update("<add><commit/></add>");
    }

    public static Document docId(long docId) {
        return new Document(docId);
    }

    public static class Document {
        long id = 0L;
        Optional<Long> size = empty();
        Optional<String> name = empty();
        Optional<String> desc = empty();
        Optional<List<String>> tags = empty();
        Optional<List<Long>> prices = empty();

        Document(long id) {
            this.id = id;
        }

        public Document size(long size) {
            this.size = Optional.of(size);
            return this;
        }

        public Document name(String name) {
            this.name = Optional.of(name);
            return this;
        }

        public Document desc(String desc) {
            this.desc = Optional.of(desc);
            return this;
        }

        public Document tags(String... tags) {
            this.tags = Optional.of(asList(tags));
            return this;
        }

        public Document prices(Long... prices) {
            this.prices = Optional.of(asList(prices));
            return this;
        }
    }
}
