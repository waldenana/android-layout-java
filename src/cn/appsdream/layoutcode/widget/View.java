package cn.appsdream.layoutcode.widget;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;

/**
 * Created by zewei on 2016-05-05.
 */
public class View {
    protected AttributeSet attributeSet;
    protected PsiClass psiClass;
    private String id;
    private View mParent;
    private ArrayList<View> mChildren = new ArrayList<>();

    public View(PsiClass psiClass,AttributeSet attributeSet){
        this.psiClass = psiClass;
        this.attributeSet = attributeSet;
    }

    public void addView(View view) {
        mChildren.add(view);
        view.mParent = this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId(){
        if (id == null) {
            if (attributeSet == null)
                return "";
            return attributeSet.getIdAttribute();
        }
        return id;
    }
    public View getParent() {
        return mParent;
    }

    public String getName(){
        return psiClass.getName();
    }

    public PsiMethod[] getAllListener(){
        return psiClass.getAllMethods();
    }

    @Override
    public String toString() {
        if (psiClass == null)
            return "ViewRoot";
        return psiClass.getName()+" id="+getId();
    }
}
