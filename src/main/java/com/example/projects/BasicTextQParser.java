package com.example.projects;

import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.QueryBuilder;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TextField;
import org.apache.solr.search.SyntaxError;

public class BasicTextQParser extends BasicQParser {
    public BasicTextQParser(
            String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        super(qstr, localParams, params, req);
    }

    @Override
    protected Query parseImpl(String field) throws SyntaxError {
        var schemaField = getTextSchemaField(field);
        var queryBuilder = new FieldQueryBuilder(schemaField.getType().getQueryAnalyzer());
        var clauses = new ArrayList<Query>();
        for (var part : parseQueryParts()) {
            var clause = queryBuilder.createFieldQuery(field, part.text(), part.quoted());
            if (clause != null) {
                clauses.add(clause);
            }
        }

        if (clauses.isEmpty()) {
            return new BooleanQuery.Builder().build();
        }
        if (clauses.size() == 1) {
            return clauses.get(0);
        }

        var builder = new BooleanQuery.Builder();
        for (var clause : clauses) {
            builder.add(clause, Occur.SHOULD);
        }
        return builder.build();
    }

    private SchemaField getTextSchemaField(String field) throws SyntaxError {
        final SchemaField schemaField;
        try {
            schemaField = req.getSchema().getField(field);
        } catch (SolrException e) {
            throw new SyntaxError("Unknown field: " + field);
        }

        var fieldType = schemaField.getType();
        if (!(fieldType instanceof TextField)
                || !fieldType.isTokenized()
                || fieldType.getQueryAnalyzer() == null) {
            throw new SyntaxError("Field '" + field + "' must use a tokenized text field type");
        }
        return schemaField;
    }

    private List<QueryPart> parseQueryParts() throws SyntaxError {
        var rawQuery = getString();
        var query = rawQuery == null ? null : rawQuery.strip();
        if (query == null || query.length() < 2 || query.charAt(0) != '('
                || query.charAt(query.length() - 1) != ')') {
            throw new SyntaxError("Query must start with '(' and end with ')'");
        }

        var body = query.substring(1, query.length() - 1);
        var parts = new ArrayList<QueryPart>();
        var index = 0;
        while (index < body.length()) {
            while (index < body.length() && Character.isWhitespace(body.charAt(index))) {
                index++;
            }
            if (index >= body.length()) {
                break;
            }

            if (body.charAt(index) == '"') {
                index = parseQuotedPart(body, index, parts);
                continue;
            }

            index = parseUnquotedPart(body, index, parts);
        }

        if (parts.isEmpty()) {
            throw new SyntaxError("Empty query body is not allowed");
        }
        return parts;
    }

    private int parseQuotedPart(String body, int start, List<QueryPart> parts) throws SyntaxError {
        var closingQuote = body.indexOf('"', start + 1);
        if (closingQuote < 0) {
            throw new SyntaxError("Unterminated quoted phrase");
        }

        var phrase = body.substring(start + 1, closingQuote);
        if (phrase.isEmpty()) {
            throw new SyntaxError("Quoted phrases must not be empty");
        }

        if (closingQuote + 1 < body.length()
                && !Character.isWhitespace(body.charAt(closingQuote + 1))) {
            throw new SyntaxError("Expected whitespace after quoted phrase");
        }

        parts.add(new QueryPart(phrase, true));
        return closingQuote + 1;
    }

    private int parseUnquotedPart(String body, int start, List<QueryPart> parts) throws SyntaxError {
        var end = start;
        while (end < body.length() && !Character.isWhitespace(body.charAt(end))) {
            if (body.charAt(end) == '"') {
                throw new SyntaxError("Unexpected quote in unquoted term");
            }
            end++;
        }

        parts.add(new QueryPart(body.substring(start, end), false));
        return end;
    }

    private record QueryPart(String text, boolean quoted) {}

    private static final class FieldQueryBuilder extends QueryBuilder {
        private FieldQueryBuilder(Analyzer analyzer) {
            super(analyzer);
        }

        private Query createFieldQuery(String field, String queryText, boolean quoted) {
            return createFieldQuery(
                    analyzer,
                    quoted ? Occur.MUST : Occur.SHOULD,
                    field,
                    queryText,
                    quoted,
                    0);
        }
    }
}
