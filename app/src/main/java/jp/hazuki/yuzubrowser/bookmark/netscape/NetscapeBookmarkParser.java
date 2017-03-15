package jp.hazuki.yuzubrowser.bookmark.netscape;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;

import jp.hazuki.yuzubrowser.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.bookmark.BookmarkSite;

public class NetscapeBookmarkParser {

    private BookmarkFolder parentFolder;
    private int hierarchy;

    public NetscapeBookmarkParser(BookmarkFolder rootFolder) {
        parentFolder = rootFolder;
    }

    public void parse(BufferedReader reader) throws XmlPullParserException, NetscapeBookmarkException, IOException {
        String docType = reader.readLine();

        if (docType == null)
            throw new NetscapeBookmarkException();

        if (!"<!doctype netscape-bookmark-file-1>".equals(docType.toLowerCase()))
            throw new NetscapeBookmarkException();

        XmlPullParser parser = createParser();
        parser.setInput(reader);

        hierarchy = 0;

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                startTag(parser.getName().toLowerCase(), parser);
            } else if (eventType == XmlPullParser.END_TAG) {
                endTag(parser.getName().toLowerCase(), parser);
            }
            eventType = parser.next();
        }
    }

    private void startTag(String tag, XmlPullParser parser) throws IOException, XmlPullParserException {
        if ("h3".equals(tag)) {
            hierarchy++;
            BookmarkFolder folder = new BookmarkFolder(parser.nextText(), parentFolder);
            parentFolder.add(folder);
            parentFolder = folder;
        } else if ("a".equals(tag)) {
            String url = parser.getAttributeValue(null, "HREF");
            if (url != null) {
                BookmarkSite site = new BookmarkSite(parser.nextText(), url);
                parentFolder.add(site);
            }
        }
    }

    private void endTag(String tag, XmlPullParser parser) {
        if ("dl".equals(tag)) {
            if (--hierarchy >= 0)
                parentFolder = parentFolder.parent;
        }
    }

    private XmlPullParser createParser() throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setValidating(false);
        factory.setFeature(Xml.FEATURE_RELAXED, true);
        factory.setNamespaceAware(true);
        return factory.newPullParser();
    }
}
