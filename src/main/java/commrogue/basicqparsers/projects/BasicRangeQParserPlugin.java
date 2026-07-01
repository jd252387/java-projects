package commrogue.basicqparsers.projects;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

public class BasicRangeQParserPlugin extends QParserPlugin {
    public static final String NAME = "basic_range";

    @Override
    public QParser createParser(
            String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        return new BasicRangeQParser(qstr, localParams, params, req);
    }
}
