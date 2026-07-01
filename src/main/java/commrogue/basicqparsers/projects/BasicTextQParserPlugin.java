package commrogue.basicqparsers.projects;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

public class BasicTextQParserPlugin extends QParserPlugin {
    public static final String NAME = "basic_text";

    @Override
    public QParser createParser(
            String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        return new BasicTextQParser(qstr, localParams, params, req);
    }
}
