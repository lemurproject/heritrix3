package org.archive.modules.extractor;

import java.util.Collection;

import org.apache.commons.httpclient.URIException;
import org.archive.modules.CrawlMetadata;
import org.archive.modules.CrawlURI;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;

/**
 * Simple test cases for the class LinkExtractorHTMLTest.
 * 
 * For the time being, this class extends ExtractorHTMLTest, although many
 * tests fail since they are for extracting links from other tags than a or link.
 * 
 * 
 */
public class LinkExtractorHTMLTest extends ExtractorHTMLTest {
    
    
    final public static String[] VALID_TEST_DATA = new String[] {
        "<a href=\"http://www.example0.org\">yellow journalism</a> A",
        "http://www.example0.org/",

        "<a href='http://www.example1.org'>yellow journalism</a> A",
        "http://www.example1.org/",

        "<a href=http://www.example2.org>yellow journalism</a> A",
        "http://www.example2.org/",

        "<a href=\"http://www.example3.org\">yellow journalism A",
        "http://www.example3.org/",

        "<a href='http://www.example4.org'>yellow journalism A",
        "http://www.example4.org/",

        "<a href=http://www.example5.org>yellow journalism A",
        "http://www.example5.org/",

        "<a href=\"http://www.example6.org\"/>yellow journalism A",
        "http://www.example6.org/",

        "<a href='http://www.example7.org'/>yellow journalism A",
        "http://www.example7.org/",

        "<a href=http://www.example8.org/>yellow journalism A",
        "http://www.example8.org/",

        "<link rel='canonical' href='http://globalvoicesonline.org/2012/11/08/yemen-drones-a-day-after-obama-won/' />",
        "http://globalvoicesonline.org/2012/11/08/yemen-drones-a-day-after-obama-won/",
    };
    
        
    @Override
    protected String[] getValidTestData() {
        return VALID_TEST_DATA;
    }
    
    @Override
    protected Extractor makeExtractor() {
        LinkExtractorHTML result = new LinkExtractorHTML();
        UriErrorLoggerModule ulm = new UnitTestUriLoggerModule();
        result.setLoggerModule(ulm);
        CrawlMetadata metadata = new CrawlMetadata();
        metadata.afterPropertiesSet();
        result.setMetadata(metadata);
        result.afterPropertiesSet();
        return result;
    }
    
    
    public void testSimpleLinks() throws URIException {
        UURI uuri = UURIFactory.getInstance("http://www.example.org");
        CrawlURI curi = new CrawlURI(uuri);
        CharSequence cs = "<a src=\"http://www.example.com/\" href=\"http://www.archive.org/\">a</a>"; 
        LinkExtractorHTML ex = (LinkExtractorHTML)makeExtractor();
        ex.extract(curi, cs);
        
        System.out.println(curi.getOutLinks());
        
        assertTrue("more links found", curi.getOutLinks().size() == 1);
    }
    
    public void testExtraction() throws Exception {
        try {
            String[] valid = getValidTestData();
            for (int i = 0; i < valid.length; i += 2) {
                testOne(valid[i], valid[i + 1]);
            }
        } catch (Exception e) {
            e.printStackTrace(); // I hate maven.
            throw e;
        }
    }

    /**
     * Runs the given text through an Extractor, expecting the given
     * URL to be extracted.
     * 
     * @param text    the text to process
     * @param expectedURL   the URL that should be extracted from the text
     * @throws Exception  just in case
     */
    private void testOne(String text, String expectedURL) throws Exception {
        Collection<TestData> testDataCol = makeData(text, expectedURL);
        for (TestData testData: testDataCol) {
            Extractor extractor = makeExtractor();
            extractor.process(testData.uri);
                        
            assertEquals(1, testData.uri.getOutLinks().size());
            Link found = testData.uri.getOutLinks().iterator().next();
            assertEquals(expectedURL, found.getDestination().toString());
            assertNoSideEffects(testData.uri);
        }
    }
}
