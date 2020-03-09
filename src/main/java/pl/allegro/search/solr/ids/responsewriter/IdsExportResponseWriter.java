package pl.allegro.search.solr.ids.responsewriter;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import pl.allegro.search.solr.ids.buffer.IdsBuffer;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import static pl.allegro.search.solr.ids.IdsExportConfiguration.REQ_CONTEXT_KEY_FOR_IDS_BUFFER;

public class IdsExportResponseWriter implements QueryResponseWriter {
    private static final String DEFAULT_SEPARATOR = ",";

    private String responseKey = REQ_CONTEXT_KEY_FOR_IDS_BUFFER;
    private String separator = DEFAULT_SEPARATOR;

    @Override
    public void init(NamedList args) {
        if (args.get("responseKey") != null) {
            responseKey = (String) args.get("responseKey");
        }
        if (args.get("separator") != null) {
            separator = (String) args.get("separator");
        }
    }

    @Override
    public void write(Writer writer, SolrQueryRequest request, SolrQueryResponse response) throws IOException {
        IdsBuffer idsBuffer = (IdsBuffer) response.getValues().get(responseKey);
        if (idsBuffer != null) {
            Iterator<String> iterator = idsBuffer.iterator();
            while (iterator.hasNext()) {
                String item = iterator.next();
                writer.write(item);
                if (iterator.hasNext()) {
                    writer.write(separator);
                }
            }
        }
    }

    @Override
    public String getContentType(SolrQueryRequest request, SolrQueryResponse response) {
        return CONTENT_TYPE_TEXT_UTF8;
    }

}
