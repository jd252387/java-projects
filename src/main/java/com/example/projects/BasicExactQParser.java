package com.example.projects;

import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.SyntaxError;

public class BasicExactQParser extends BasicQParser {
    public BasicExactQParser(
            String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        super(qstr, localParams, params, req);
    }

    @Override
    protected Query parseImpl(String field) throws SyntaxError {
        var value = localParams == null ? null : localParams.get("value");
        if (value == null) {
            throw new SyntaxError("Missing required local param: value");
        }

        var schemaField = getSchemaField(field);
        var fieldType = schemaField.getType();
        var isSupportedTextField = fieldType.isUtf8Field() && !fieldType.isTokenized();
        if (fieldType.getNumberType() == null && !isSupportedTextField) {
            throw new SyntaxError(
                    "Field '"
                            + field
                            + "' must use a non-tokenized text, numeric, or date field type");
        }

        return fieldType.getFieldQuery(this, schemaField, value);
    }

    private SchemaField getSchemaField(String field) throws SyntaxError {
        try {
            return req.getSchema().getField(field);
        } catch (SolrException e) {
            throw new SyntaxError("Unknown field: " + field);
        }
    }
}
