package cn.appsdream.layoutcode;

import cn.appsdream.layoutcode.widget.View;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
/**
 * Created by zewei on 2015-03-17.
 */
public class ClassJsonAction extends AnAction {
    public void actionPerformed(final AnActionEvent e) {
        View view= new LayoutInflater(e.getData(LangDataKeys.MODULE)).inflate(e.getData(LangDataKeys.PSI_FILE));
        view.toString();
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(true);
    }
}
