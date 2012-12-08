package org.archive.modules.extractor;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Config;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.LoggerProvider;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.StreamedSource;
import net.htmlparser.jericho.Tag;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.io.input.CharSequenceReader;
import org.archive.modules.CrawlURI;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;

/**
 * 
 * 
 */
public class LinkExtractorHTML extends ExtractorHTML {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1684681316546343632L;

    final private static Logger logger = Logger
            .getLogger(LinkExtractorHTML.class.getName());

    private Set<String> excludeRels = new HashSet<String>();

    private Set<String> excludeTypes = new HashSet<String>();

    public LinkExtractorHTML() {

        Config.LoggerProvider = LoggerProvider.DISABLED;

        excludeRels.add("stylesheet");
        excludeRels.add("icon");
        excludeRels.add("pingback");

        excludeTypes.add("text/css");
    }

    /**
     * Run extractor. This method is package visible to ease testing.
     * 
     * @param curi
     *            CrawlURI we're processing.
     * @param cs
     *            Sequence from underlying ReplayCharSequence.
     */
    protected void extract(CrawlURI curi, CharSequence cs) {
        Reader csReader = new CharSequenceReader(cs);
        StreamedSource source;
        try {
            source = new StreamedSource(csReader);
        } catch (IOException e) {
            logger.warning(String.format("Error extracting links from: %s - %s", curi.getURI(), e));
            return;
        }
        
        for (Segment segment : source) {
            if (!(segment instanceof Tag)) {
                segment = null;
                continue;
            }
            
            // Process the segment as a tag
            Tag tag = (Tag) segment;
            String tagName = tag.getName();

            Attributes attributes = tag.parseAttributes();

            if (attributes == null) {
                continue;
            }

            String href = attributes.getValue("href");
            if (href == null) {
                continue;
            }
            
            if (tagName.equals(HTMLElementName.BASE)) {
                try {
                    UURI base = UURIFactory.getInstance(href);
                    curi.setBaseURI(base);
                } catch (URIException e) {
                    logUriError(e, curi.getUURI(), href);
                }
            }
            
            if (tagName.equals(HTMLElementName.A)
                    || tagName.equals(HTMLElementName.LINK)) {

                processTag(curi, tagName, attributes);
            }
        }
        
        try {
            source.close();
            csReader.close();
        } catch (IOException e) {
            logger.warning(String.format("Error extracting links for: %s - %s", curi.getURI(), e));
        } finally {
            source = null;
            csReader = null;
        }
        
        
    }

    protected void processTag(CrawlURI curi, String tagName,
            Attributes attributes) {
        String href = attributes.getValue("href");
        String rel = attributes.getValue("rel");
        String linkType = attributes.getValue("type");

        // Reject tags with known rel values
        if (rel != null && excludeRels.contains(rel.toLowerCase())) {
            return;
        }

        // Reject tags with known type values
        if (linkType != null && excludeTypes.contains(linkType.toLowerCase())) {
            return;
        }

        CharSequence context = elementContext(tagName, "href");
        processLink(curi, href, context);
    }
}
