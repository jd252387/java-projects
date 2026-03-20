This sample configset registers every custom `QParserPlugin` under `src/main/java/com/example/projects`.

It expects the built plugin JAR to be available on Solr's classpath before the configset is used.

Example queries:

```text
q={!basic_exact field=sku value=ABC-123}
q={!basic_exact field=title value=solr}&f.title.qf=title_en title_fr
q={!basic_range field=price gte=10 lt=20}
q={!basic_range field=published_at gt=2024-01-01T00:00:00Z}
q={!basic_text field=title}(hello "two words")
q={!basic_text field=title}(bonjour)&f.title.qf=title_en title_fr
```

The sample schema includes fields that match the parser requirements:

- `sku` for exact string matching
- `title`, `title_en`, and `title_fr` for tokenized text queries and aliases
- `price`, `rating`, and `timestamp` for numeric range queries
- `published_at` for date exact and range queries
