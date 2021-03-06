package com.github.rnewson.couchdb.lucene;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import net.sf.json.JSONObject;

import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;

import com.github.rnewson.couchdb.lucene.util.Constants;

public class DocumentConverterTest {

    private Context context;

    @Before
    public void setup() {
        context = Context.enter();
    }

    @After
    public void teardown() {
        Context.exit();
    }

    @Test
    public void testSingleDocumentReturn() throws Exception {
        final DocumentConverter converter = new DocumentConverter(context, "single", "function(doc) {return new Document();}");
        final Document[] result = converter.convert(doc("{_id:\"hello\"}"), new JSONObject(), null);
        assertThat(result.length, is(1));
        assertThat(result[0].get("_id"), is("hello"));
    }

    @Test
    public void testMultipleDocumentReturn() throws Exception {
        final DocumentConverter converter = new DocumentConverter(context, "multi",
                "function(doc) {var ret = new Array(); ret.push(new Document()); ret.push(new Document()); return ret;}");
        final Document[] result = converter.convert(doc("{_id:\"hello\"}"), new JSONObject(), null);
        assertThat(result.length, is(2));
        assertThat(result[0].get("_id"), is("hello"));
    }

    @Test
    public void testAdd() throws Exception {
        final DocumentConverter converter = new DocumentConverter(context, "single",
                "function(doc) {var ret=new Document(); ret.add(doc.key); return ret;}");
        final Document[] result = converter.convert(doc("{_id:\"hello\", key:\"value\"}"), new JSONObject(), null);
        assertThat(result.length, is(1));
        assertThat(result[0].get(Constants.DEFAULT_FIELD), is("value"));
    }

    @Test
    public void testForLoopOverObject() throws Exception {
        final DocumentConverter converter = new DocumentConverter(context, "multi",
                "function(doc) {var ret=new Document(); for (var key in doc) { ret.add(doc[key]); } return ret; }");
        final Document[] result = converter.convert(doc("{_id:\"hello\", key:\"value\"}"), new JSONObject(), null);
        assertThat(result.length, is(1));
        assertThat(result[0].get("_id"), is("hello"));
        assertThat(result[0].getValues(Constants.DEFAULT_FIELD)[0], is("hello"));
        assertThat(result[0].getValues(Constants.DEFAULT_FIELD)[1], is("value"));
    }

    @Test
    public void testForLoopOverArray() throws Exception {
        final DocumentConverter converter = new DocumentConverter(context, "multi",
                "function(doc) {var ret=new Document(); for (var key in doc.arr) {ret.add(doc.arr[key]); } return ret; }");
        final Document[] result = converter.convert(doc("{_id:\"hello\", arr:[0,1,2,3]}"), new JSONObject(), null);
        assertThat(result.length, is(1));
        assertThat(result[0].get("_id"), is("hello"));
        assertThat(result[0].getValues(Constants.DEFAULT_FIELD)[0], is("0"));
        assertThat(result[0].getValues(Constants.DEFAULT_FIELD)[1], is("1"));
        assertThat(result[0].getValues(Constants.DEFAULT_FIELD)[2], is("2"));
        assertThat(result[0].getValues(Constants.DEFAULT_FIELD)[3], is("3"));
    }

    @Test
    public void testForEverything() throws Exception {
        final DocumentConverter converter = new DocumentConverter(
                context,
                "multi",
                "function(doc) {var ret=new Document(); function idx(obj) {for (var key in obj) {switch (typeof obj[key]) {case 'object':idx(obj[key]); break; case 'function': break; default: ret.add(obj[key]); break;} } }; idx(doc); return ret; }");

        final Document[] result = converter.convert(doc("{_id:\"hello\", l1: { l2: {l3:[\"v3\", \"v4\"]}}}"), new JSONObject(),
                null);
        assertThat(result[0].getValues(Constants.DEFAULT_FIELD)[0], is("hello"));
        assertThat(result[0].getValues(Constants.DEFAULT_FIELD)[1], is("v3"));
        assertThat(result[0].getValues(Constants.DEFAULT_FIELD)[2], is("v4"));
    }

    @Test
    public void testNullReturn() throws Exception {
        final DocumentConverter converter = new DocumentConverter(context, "null", "function(doc) {return null;}");
        final Document[] result = converter.convert(doc("{_id:\"hello\"}"), new JSONObject(), null);
        assertThat(result.length, is(0));
    }

    @Test
    public void testUndefinedReturn() throws Exception {
        final DocumentConverter converter = new DocumentConverter(context, "null", "function(doc) {return doc.nope;}");
        final Document[] result = converter.convert(doc("{_id:\"hello\"}"), new JSONObject(), null);
        assertThat(result.length, is(0));
    }

    @Test
    public void testRuntimeException() throws Exception {
        final DocumentConverter converter = new DocumentConverter(context, "null", "function(doc) {throw {bad : \"stuff\"}}");
        final Document[] result = converter.convert(doc("{_id:\"hello\"}"), new JSONObject(), null);
        assertThat(result.length, is(0));
    }

    @Test
    public void testNullAddsAreIgnored() throws Exception {
        final DocumentConverter converter = new DocumentConverter(context, "null",
                "function(doc) {var ret=new Document(); ret.add(doc.nope); return ret;}");
        final Document[] result = converter.convert(doc("{_id:\"hello\"}"), new JSONObject(), null);
        assertThat(result.length, is(1));
    }

    @Test
    public void testQuoteRemoval() throws Exception {
        final DocumentConverter converter = new DocumentConverter(context, "single", "\"function(doc) {return new Document();}\"");
        final Document[] result = converter.convert(doc("{_id:\"hello\"}"), new JSONObject(), null);
        assertThat(result.length, is(1));
        assertThat(result[0].get("_id"), is("hello"));
    }

    private JSONObject doc(final String json) {
        return JSONObject.fromObject(json);
    }

}
