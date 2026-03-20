package com.example.projects;

import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.SyntaxError;

public class BasicRangeQParser extends BasicQParser {
    public BasicRangeQParser(
            String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        super(qstr, localParams, params, req);
    }

    @Override
    protected Query parseImpl(String field) throws SyntaxError {
        var gt = localParams == null ? null : localParams.get("gt");
        var gte = localParams == null ? null : localParams.get("gte");
        var lt = localParams == null ? null : localParams.get("lt");
        var lte = localParams == null ? null : localParams.get("lte");

        if (gt == null && gte == null && lt == null && lte == null) {
            throw new SyntaxError("At least one of gt, gte, lt, or lte must be specified");
        }
        if (gt != null && gte != null) {
            throw new SyntaxError("Only one of gt or gte may be specified");
        }
        if (lt != null && lte != null) {
            throw new SyntaxError("Only one of lt or lte may be specified");
        }

        var schemaField = getSchemaField(field);
        var numberType = schemaField.getType().getNumberType();
        if (numberType == null) {
            throw new SyntaxError(
                    "Field '" + field + "' must use a numeric or date field type");
        }

        var lower = gte != null ? gte : gt;
        var upper = lte != null ? lte : lt;
        var lowerInclusive = gte != null;
        var upperInclusive = lte != null;
        return schemaField
                .getType()
                .getRangeQuery(this, schemaField, lower, upper, lowerInclusive, upperInclusive);
    }

    private SchemaField getSchemaField(String field) throws SyntaxError {
        try {
            return req.getSchema().getField(field);
        } catch (SolrException e) {
            throw new SyntaxError("Unknown field: " + field);
        }
    }
}
