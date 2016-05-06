package cn.appsdream.layoutcode.widget;

import org.xmlpull.v1.XmlPullParser;

/**
 * Created by zewei on 2016-05-05.
 */
public class AttributeSet {
    private String id;
    private XmlPullParser mParser;

    public static AttributeSet asAttributeSet(XmlPullParser parser) {
        return new AttributeSet(parser);
    }


    private AttributeSet(XmlPullParser parser) {
        mParser = parser;
        id = getAttributeValue(parser.getNamespace("android"),"id");
    }

    public String getAttributeValue(String namespace, String name) {
        String value =  mParser.getAttributeValue(namespace, name);
        if (value != null){
            value=value.substring(value.indexOf("/")+1);
        }
        return value;
    }

    public String getIdAttribute() {
        return id;
    }

    public String getClassAttribute() {
        return getAttributeValue(mParser.getNamespace("android"), "class");
    }
}
