package commrogue.basicqparsers.projects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.request.SolrQueryRequestBase;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.IndexSchemaFactory;
import org.apache.solr.schema.NumberType;
import org.apache.solr.schema.TextField;
import org.apache.solr.search.QParserPlugin;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

class SampleConfigsetTest {
    private static final Path CONFIGSET_CONF_DIR =
            Path.of("config", "configsets", "sample-qparser-plugins");

    @Test
    void solrconfigRegistersEveryCustomQParserPlugin() throws Exception {
        var expectedPlugins = discoverCustomQParserPlugins();
        var configuredPlugins = readQueryParsers(CONFIGSET_CONF_DIR.resolve("solrconfig.xml"));

        assertFalse(expectedPlugins.isEmpty());
        assertEquals(expectedPlugins, configuredPlugins);
    }

    @Test
    void sampleSchemaLoadsAndSupportsConfiguredParserFieldTypes() throws Exception {
        var solrConfig = new SolrConfig(CONFIGSET_CONF_DIR, "solrconfig.xml");
        var schema = IndexSchemaFactory.buildIndexSchema("schema.xml", solrConfig);

        assertTrue(schema.getField("sku").getType().isUtf8Field());
        assertFalse(schema.getField("sku").getType().isTokenized());
        assertInstanceOf(TextField.class, schema.getField("title").getType());
        assertTrue(schema.getField("title").getType().isTokenized());
        assertEquals(NumberType.INTEGER, schema.getField("price").getType().getNumberType());
        assertEquals(NumberType.DOUBLE, schema.getField("rating").getType().getNumberType());
        assertEquals(NumberType.LONG, schema.getField("timestamp").getType().getNumberType());
        assertEquals(NumberType.DATE, schema.getField("published_at").getType().getNumberType());
    }

    @Test
    void sampleSchemaCanInstantiateEachCustomParser() throws Exception {
        var solrConfig = new SolrConfig(CONFIGSET_CONF_DIR, "solrconfig.xml");
        var schema = IndexSchemaFactory.buildIndexSchema("schema.xml", solrConfig);

        try (var req = new TestSolrQueryRequest(schema, new ModifiableSolrParams())) {
            assertNotNull(new BasicExactQParserPlugin()
                    .createParser(
                            null,
                            localParams("field", "sku", "value", "ABC-123"),
                            new ModifiableSolrParams(),
                            req)
                    .parse());
            assertNotNull(new BasicRangeQParserPlugin()
                    .createParser(
                            null,
                            localParams("field", "price", "gte", "10", "lt", "20"),
                            new ModifiableSolrParams(),
                            req)
                    .parse());
            assertNotNull(new BasicTextQParserPlugin()
                    .createParser(
                            "(hello \"two words\")",
                            localParams("field", "title"),
                            new ModifiableSolrParams(),
                            req)
                    .parse());
        }
    }

    private static Map<String, String> discoverCustomQParserPlugins() throws Exception {
        var plugins = new LinkedHashMap<String, String>();
        try (var paths = Files.walk(Path.of("src", "main", "java"))) {
            var pluginSources =
                    paths.filter(path -> path.getFileName().toString().endsWith("QParserPlugin.java"))
                            .sorted()
                            .toList();
            for (var pluginSource : pluginSources) {
                var className = toClassName(pluginSource);
                var pluginClass = Class.forName(className);
                if (!QParserPlugin.class.isAssignableFrom(pluginClass)) {
                    continue;
                }
                var name = (String) pluginClass.getField("NAME").get(null);
                plugins.put(name, className);
            }
        }
        return plugins;
    }

    private static String toClassName(Path pluginSource) throws IOException {
        var source = Files.readString(pluginSource);
        var packageName =
                source.lines()
                        .filter(line -> line.startsWith("package "))
                        .findFirst()
                        .map(line -> line.substring("package ".length(), line.length() - 1))
                        .orElseThrow();
        var simpleClassName = pluginSource.getFileName().toString().replace(".java", "");
        return packageName + "." + simpleClassName;
    }

    private static Map<String, String> readQueryParsers(Path solrConfigPath) throws Exception {
        var documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(false);
        var document =
                documentBuilderFactory.newDocumentBuilder().parse(solrConfigPath.toFile());
        var nodes = document.getElementsByTagName("queryParser");
        var queryParsers = new LinkedHashMap<String, String>();
        for (var i = 0; i < nodes.getLength(); i++) {
            var element = (Element) nodes.item(i);
            queryParsers.put(element.getAttribute("name"), element.getAttribute("class"));
        }
        return queryParsers;
    }

    private static ModifiableSolrParams localParams(String... pairs) {
        var params = new ModifiableSolrParams();
        for (var i = 0; i < pairs.length; i += 2) {
            params.set(pairs[i], pairs[i + 1]);
        }
        return params;
    }

    private static final class TestSolrQueryRequest extends SolrQueryRequestBase {
        private TestSolrQueryRequest(IndexSchema schema, ModifiableSolrParams params) {
            super(null, params);
            this.schema = schema;
        }
    }
}
