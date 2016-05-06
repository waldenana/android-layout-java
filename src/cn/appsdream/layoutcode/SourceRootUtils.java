package cn.appsdream.layoutcode;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.*;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import java.util.*;

/**
 * Created by zewei on 2016-05-06.
 */
public final class SourceRootUtils {
    @NotNull
    private static Set<? extends JpsModuleSourceRootType<?>> toRootTypeSet(@NotNull JpsModuleSourceRootType<?>... rootTypes) {
        HashSet<JpsModuleSourceRootType<?>> rootTypesSet = new HashSet<JpsModuleSourceRootType<?>>();
        Collections.addAll(rootTypesSet, rootTypes);
        return rootTypesSet;
    }

    @NotNull
    public static List<VirtualFile> getResourceRoots(@NotNull Module project) {
        return getSourceRoots(project, toRootTypeSet(JavaResourceRootType.RESOURCE), true);
    }

    @NotNull
    public static List<VirtualFile> getResourceRoots(@NotNull Project project) {
        return getSourceRoots(project, toRootTypeSet(JavaResourceRootType.RESOURCE), true);
    }

    @NotNull
    public static List<VirtualFile> getSourceAndTestSourceRoots(@NotNull Project project) {
        return getSourceRoots(project, toRootTypeSet(JavaSourceRootType.SOURCE, JavaSourceRootType.TEST_SOURCE), true);
    }

    @NotNull
    public static List<VirtualFile> getSourceRoots(@NotNull Project project) {
        return getSourceRoots(project, toRootTypeSet(JavaSourceRootType.SOURCE), true);
    }

    @NotNull
    public static List<VirtualFile> getTestSourceRoots(@NotNull Project project) {
        return getSourceRoots(project, toRootTypeSet(JavaSourceRootType.TEST_SOURCE), true);
    }

    @NotNull
    public static List<VirtualFile> getSourceRoots(@NotNull Project project, @NotNull Set<? extends JpsModuleSourceRootType<?>> rootTypes, boolean excludeGeneratedRoots) {
        List<VirtualFile> roots = new ArrayList<VirtualFile>();
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            roots.addAll(getSourceRoots(module, rootTypes, excludeGeneratedRoots));
        }
        return roots;
    }

    @NotNull
    public static List<VirtualFile> getSourceRoots(@NotNull Module module) {
        return getSourceRoots(module, toRootTypeSet(JavaSourceRootType.SOURCE), true);
    }

    @NotNull
    public static List<VirtualFile> getTestSourceRoots(@NotNull Module module) {
        return getSourceRoots(module, toRootTypeSet(JavaSourceRootType.TEST_SOURCE), true);
    }

    @NotNull
    public static List<VirtualFile> getSourceRoots(@NotNull Module module, @NotNull Set<? extends JpsModuleSourceRootType<?>> rootTypes, boolean excludeGeneratedRoots) {
        List<VirtualFile> roots = new ArrayList<VirtualFile>();

        for (ContentEntry entry : ModuleRootManager.getInstance(module).getContentEntries()) {
            entry.getSourceFolders(rootTypes).stream().filter(sourceFolder ->
                    !(excludeGeneratedRoots && isForGeneratedSources(sourceFolder))
            ).forEach(sourceFolder -> {
                ContainerUtil.addIfNotNull(roots, sourceFolder.getFile());
            });
        }

        return roots;
    }

    private static boolean isForGeneratedSources(@NotNull SourceFolder sourceFolder) {
        if (sourceFolder.getRootType() == JavaModuleSourceRootTypes.SOURCES) {
            JavaSourceRootProperties properties = sourceFolder.getJpsElement().getProperties(JavaModuleSourceRootTypes.SOURCES);
            return properties != null && properties.isForGeneratedSources();
        } else if (sourceFolder.getRootType() == JavaResourceRootType.RESOURCE) {
            JavaResourceRootProperties properties = sourceFolder.getJpsElement().getProperties(JavaResourceRootType.RESOURCE);
            return properties != null && properties.isForGeneratedSources();
        }
        return false;
    }

    /**
     * Gets the most suitable source root from those available.
     * <p>
     * Returns the first matching case:
     * <ol>
     * <li>The selected file belongs to a module, that module has test roots and the selected file belongs one of those test roots => that test root</li>
     * <li>The selected file belongs to a module and that module has test roots => the first test root in the module</li>
     * <li>The selected file belongs to a module, that module has source roots and the selected file belongs one of those source roots => that source root</li>
     * <li>The selected file belongs to a module and that module has source roots = the first source root in the module</li>
     * <li>The project has test roots => the first test root</li>
     * <li>The project has source roots => the first source root</li>
     * <li>Otherwise returns Null</li>
     * </ol>
     *
     * @param project      the project
     * @param module       optional current module - will be null if a library class is selected
     * @param selectedFile the currently selected file
     * @return the preferred source root if one exists; otherwise null
     */
    @Nullable
    public static VirtualFile getPreferredSourceRootForTestClass(@NotNull Project project, @Nullable Module module, @Nullable VirtualFile selectedFile) {

        VirtualFile preferredSourceRoot = null;

        VirtualFile currentSourceRoot = selectedFile != null ?
                ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(selectedFile) : null;

        List<VirtualFile> candidateSourceRoots = null;

        if (module != null) {
            List<VirtualFile> validModuleTestSourceRoots = getTestSourceRoots(module);
            if (validModuleTestSourceRoots.size() > 0) {
                candidateSourceRoots = validModuleTestSourceRoots;
            } else {
                List<VirtualFile> validModuleSourceRoots = getSourceRoots(module);

                if (validModuleSourceRoots.size() > 0) {
                    candidateSourceRoots = validModuleSourceRoots;
                }
            }
        } else {
            List<VirtualFile> validProjectTestSourceRoots = getTestSourceRoots(project);
            if (validProjectTestSourceRoots.size() > 0) {
                candidateSourceRoots = validProjectTestSourceRoots;
            } else {
                List<VirtualFile> validProjectSourceRoots = getSourceRoots(project);

                if (validProjectSourceRoots.size() > 0) {
                    candidateSourceRoots = validProjectSourceRoots;
                }
            }
        }

        if (candidateSourceRoots != null) {
            if (currentSourceRoot != null && candidateSourceRoots.contains(currentSourceRoot)) {
                preferredSourceRoot = currentSourceRoot;
            } else {
                preferredSourceRoot = candidateSourceRoots.get(0);
            }
        }

        return preferredSourceRoot;
    }
}
