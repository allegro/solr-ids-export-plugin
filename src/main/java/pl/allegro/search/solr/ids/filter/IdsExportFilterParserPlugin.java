package pl.allegro.search.solr.ids.filter;

import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

import java.util.Map;

public class IdsExportFilterParserPlugin extends QParserPlugin {
    private static final int DEFAULT_BUFFER_INITIAL_SIZE = 100_000;
    private static final String DEFAULT_DEFAULT_INDEX_FIELD = "doc_id";

    private int bufferInitialSize = DEFAULT_BUFFER_INITIAL_SIZE;
    private String defaultIndexField = DEFAULT_DEFAULT_INDEX_FIELD;

    @Override
    public void init(NamedList args) {
        super.init(args);
        if (args.get("bufferInitialSize") != null) {
            bufferInitialSize = (Integer) args.get("bufferInitialSize");
        }
        if (args.get("defaultIndexField") != null) {
            defaultIndexField = (String) args.get("defaultIndexField");
        }
    }

    @Override
    public QParser createParser(String queryString, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        String indexField = localParams.get("field", defaultIndexField);
        if (!req.getSchema().getField(indexField).hasDocValues()) {
            throw new RuntimeException("IdsExportFilter supports only fields with docValues. Field " + indexField + " doesn't have them.");
        }
        return new QParser(queryString, localParams, params, req) {
            @Override
            public Query parse() {
                Map<Object, Object> context = req.getContext();
                return new IdsExportFilter(context, bufferInitialSize, indexField);
            }
        };
    }
}
