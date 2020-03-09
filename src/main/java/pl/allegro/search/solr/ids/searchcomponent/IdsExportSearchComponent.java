package pl.allegro.search.solr.ids.searchcomponent;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;

import static pl.allegro.search.solr.ids.IdsExportConfiguration.REQ_CONTEXT_KEY_FOR_IDS_BUFFER;

public class IdsExportSearchComponent extends SearchComponent {

    private String responseKey = REQ_CONTEXT_KEY_FOR_IDS_BUFFER;

    @Override
    public void init(NamedList args) {
        super.init(args);
        if (args.get("responseKey") != null) {
            responseKey = (String) args.get("responseKey");
        }
    }

    @Override
    public void prepare(ResponseBuilder rb) {

    }

    @Override
    public void process(ResponseBuilder rb) {
        if (rb.req.getContext().containsKey(REQ_CONTEXT_KEY_FOR_IDS_BUFFER)) {
            rb.rsp.add(responseKey, rb.req.getContext().get(REQ_CONTEXT_KEY_FOR_IDS_BUFFER));
        }
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName();
    }

}
