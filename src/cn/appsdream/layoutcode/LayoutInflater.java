package cn.appsdream.layoutcode;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ResourceFileUtil;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.FilteredQuery;
import org.jetbrains.annotations.Nullable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import cn.appsdream.layoutcode.widget.AttributeSet;
import cn.appsdream.layoutcode.widget.InflateException;
import cn.appsdream.layoutcode.widget.View;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zewei on 2016-05-05.
 */
public class LayoutInflater {

    private static final String TAG_MERGE = "merge";
    private static final String TAG_INCLUDE = "include";
    private static final String TAG_1995 = "blink";
    private static final String TAG_REQUEST_FOCUS = "requestFocus";
    private static final String TAG_TAG = "tag";
    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.webkit."
    };
    private static final String ATTR_LAYOUT = "layout";
    private Module mModule;
    private static final HashMap<String, PsiClass> sConstructorMap =
            new HashMap<>();
    private PsiElementFactory psiElementFactory;

    public LayoutInflater(Module module) {
        mModule = module;
        psiElementFactory = JavaPsiFacade.getInstance(mModule.getProject()).getElementFactory();
    }

    public View inflate(PsiFile file) {
        View view = inflate(getLayoutParser(file.getVirtualFile()));
        return view;
    }

    private XmlPullParser getLayoutParser(VirtualFile layout) {
        try {
            InputStream is = layout.getInputStream();
            XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
            xppf.setNamespaceAware(true);
            XmlPullParser parser = xppf.newPullParser();
            parser.setInput(is, null);
            return parser;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private VirtualFile getLayoutByName(String name) {
        List<VirtualFile> roots = SourceRootUtils.getResourceRoots(mModule);
        for (VirtualFile root : roots) {
            VirtualFile file = findChild(root, name);
            if (file != null)
                return file;
        }
        return null;
    }

    private VirtualFile findChild(VirtualFile dic, String name) {
        if (dic.isDirectory()) {
            for (VirtualFile virtualFile : dic.getChildren()) {
                VirtualFile file = findChild(virtualFile, name);
                if (file != null)
                    return file;
            }
        } else if (dic.getName().equals(name)) {
            return dic;
        }
        return null;
    }

    public View inflate(XmlPullParser parser) {
        View result = null;

        try {
            // Look for the root node.
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG &&
                    type != XmlPullParser.END_DOCUMENT) {
                // Empty
            }

            if (type != XmlPullParser.START_TAG) {
                throw new Exception(parser.getPositionDescription()
                        + ": No start tag found!");
            }
            final AttributeSet attrs = AttributeSet.asAttributeSet(parser);

            final String name = parser.getName();

            if (TAG_MERGE.equals(name)) {
                View root = new View(null, null);
                rInflate(parser, root, attrs);
                result = root;
            } else {
                // Temp is the root view that was found in the xml
                final View temp = createViewFromTag(null, name, attrs);

                // Inflate all children under temp against its context.
                rInflateChildren(parser, temp, attrs);
                result = temp;
            }

        } catch (Exception e) {
        }

        return result;
    }

    final void rInflateChildren(XmlPullParser parser, View parent, AttributeSet attrs) throws IOException, XmlPullParserException {
        rInflate(parser, parent, attrs);
    }

    /**
     * Recursive method used to descend down the xml hierarchy and instantiate
     * views, instantiate their children, and then call onFinishInflate().
     * <p>
     * <strong>Note:</strong> Default visibility so the BridgeInflater can
     * override it.
     */
    void rInflate(XmlPullParser parser, View parent, AttributeSet attrs) throws IOException, XmlPullParserException {

        final int depth = parser.getDepth();
        int type;

        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            final AttributeSet childAttrs = AttributeSet.asAttributeSet(parser);
            final String name = parser.getName();

            if (TAG_INCLUDE.equals(name)) {
                parseInclude(parent, attrs);
            } else {
                final View view = createViewFromTag(parent, name, childAttrs);
                rInflateChildren(parser, view, attrs);
                parent.addView(view);
            }
        }
    }


    private void parseInclude(View parent,
                              AttributeSet attrs) throws IOException, XmlPullParserException {
        int type;

        // If the layout is pointing to a theme attribute, we have to
        // massage the value to get a resource identifier out of it.
        final String value = attrs.getAttributeValue(null, ATTR_LAYOUT);

        final XmlPullParser childParser = getLayoutParser(getLayoutByName(value+".xml"));


        while ((type = childParser.next()) != XmlPullParser.START_TAG &&
                type != XmlPullParser.END_DOCUMENT) {
            // Empty.
        }

        if (type != XmlPullParser.START_TAG) {
            throw new InflateException(childParser.getPositionDescription() +
                    ": No start tag found!");
        }

        final AttributeSet childAttrs = AttributeSet.asAttributeSet(childParser);
        final String childName = childParser.getName();

        if (TAG_MERGE.equals(childName)) {
            // The <merge> tag doesn't support android:theme, so
            // nothing special to do here.
            rInflate(childParser, parent, childAttrs);
        } else {
            final View view = createViewFromTag(parent, childName,
                    childAttrs);
            String id = attrs.getIdAttribute();

            // Inflate all children.
            rInflateChildren(childParser, view, childAttrs);

            if (id != null) {
                view.setId(id);
            }
            parent.addView(view);
        }

    }

    /**
     * <strong>Note:</strong> default visibility so that
     * LayoutInflater_Delegate can call it.
     */
    final static void consumeChildElements(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        int type;
        final int currentDepth = parser.getDepth();
        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > currentDepth) && type != XmlPullParser.END_DOCUMENT) {
            // Empty
        }
    }

    /**
     * Convenience method for calling through to the five-arg createViewFromTag
     * method. This method passes {@code false} for the {@code ignoreThemeAttr}
     * argument and should be used for everything except {@code &gt;include>}
     * tag parsing.
     */
    private View createViewFromTag(View parent, String name, AttributeSet attrs) {
        if (name.equals("view")) {
            name = attrs.getClassAttribute();
        }

        View view;
        if (-1 == name.indexOf('.')) {
            view = onCreateView(parent, name, attrs);
        } else {
            view = createView(name, null, attrs);
        }

        return view;
    }

    protected View onCreateView(View parent, String name, AttributeSet attrs) {
        return onCreateView(name, attrs);
    }

    protected View onCreateView(String name, AttributeSet attrs) {
        for (String prefix : sClassPrefixList) {
            View view = createView(name, prefix, attrs);
            if (view != null) {
                return view;
            }
        }

        return createView(name, "android.view.", attrs);
    }

    public final View createView(String name, String prefix, AttributeSet attrs) {
        PsiClass psiType = sConstructorMap.get(name);
        if (psiType == null) {
            PsiClassType type = PsiType.getTypeByName(prefix != null ? (prefix + name) : name, mModule.getProject(), GlobalSearchScope.allScope(mModule.getProject()));
            psiType = PsiUtil.resolveClassInType(type);
            if (psiType == null)
                return null;
            sConstructorMap.put(name, psiType);
        }
        return new View(psiType, attrs);

    }
}
