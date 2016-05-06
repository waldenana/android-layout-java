package cn.appsdream.layoutcode;

import cn.appsdream.layoutcode.widget.View;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;

/**
 * Created by zewei on 2016-05-06.
 */
public class JavaAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here

        View view= new LayoutInflater(e.getData(LangDataKeys.MODULE)).inflate(e.getData(LangDataKeys.PSI_FILE));
        view.toString();
    }
}
