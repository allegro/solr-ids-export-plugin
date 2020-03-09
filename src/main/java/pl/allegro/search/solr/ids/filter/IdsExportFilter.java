package pl.allegro.search.solr.ids.filter;

import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.solr.search.DelegatingCollector;
import org.apache.solr.search.ExtendedQueryBase;
import org.apache.solr.search.PostFilter;
import pl.allegro.search.solr.ids.filter.bufferbuilder.IdsBufferBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static pl.allegro.search.solr.ids.IdsExportConfiguration.REQ_CONTEXT_KEY_FOR_IDS_BUFFER;
import static pl.allegro.search.solr.ids.filter.bufferbuilder.IdsBufferBuilder.newIdsBufferBuilder;

public class IdsExportFilter extends ExtendedQueryBase implements PostFilter {
    private final Map<Object, Object> reqContext;
    private final int bufferInitialSize;
    private final String field;

    public IdsExportFilter(Map<Object, Object> context, int bufferInitialSize, String field) {
        this.reqContext = context;
        this.bufferInitialSize = bufferInitialSize;
        this.field = field;
    }

    @Override
    public DelegatingCollector getFilterCollector(IndexSearcher searcher) {

        return new DelegatingCollector() {
            IdsBufferBuilder idsBufferBuilder;

            @Override
            protected void doSetNextReader(LeafReaderContext context) throws IOException {
                super.doSetNextReader(context);
                if (idsBufferBuilder == null) {
                    DocValuesType docValuesType = context.reader().getFieldInfos().fieldInfo(field).getDocValuesType();
                    idsBufferBuilder = newIdsBufferBuilder(docValuesType, field, bufferInitialSize);
                }
                idsBufferBuilder.doSetNextReader(context);
            }

            @Override
            public void collect(int doc) throws IOException {
                super.collect(doc);
                idsBufferBuilder.collect(doc);
            }

            @Override
            public void finish() throws IOException {
                super.finish();
                reqContext.put(REQ_CONTEXT_KEY_FOR_IDS_BUFFER, idsBufferBuilder.build());
            }
        };
    }


    @Override
    public int getCost() {
        return Math.max(super.getCost(), 100);
    }

    @Override
    public boolean getCache() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdsExportFilter that = (IdsExportFilter) o;
        return bufferInitialSize == that.bufferInitialSize &&
                Objects.equals(reqContext, that.reqContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reqContext, bufferInitialSize);
    }
}
