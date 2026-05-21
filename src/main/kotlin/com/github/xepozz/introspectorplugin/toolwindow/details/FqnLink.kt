package com.github.xepozz.introspectorplugin.toolwindow.details

import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.components.ActionLink
import com.intellij.util.ui.JBUI
import javax.swing.JComponent
import javax.swing.JLabel

/**
 * Renders a fully-qualified class name as a clickable link. Clicking attempts to navigate to
 * the source file via two strategies, in order:
 *
 *   1. JavaPsiFacade.findClass — exact, but requires the com.intellij.modules.java module.
 *      Resolved by reflection so this view stays loadable in non-Java IDEs (PyCharm CE etc.).
 *   2. FilenameIndex by simple class name — works everywhere, opens the first matching file.
 *
 * When neither strategy yields anything the link is rendered but a no-op on click — better than
 * a dead label.
 */
object FqnLink {

    fun render(project: Project?, fqn: String?): JComponent {
        val text = fqn?.takeIf { it.isNotBlank() } ?: return JLabel("—")
        if (project == null) return JLabel(text)
        val link = ActionLink(text) { _ -> navigateTo(project, text) }
        link.border = JBUI.Borders.empty()
        return link
    }

    /** Plain label variant when the link semantics don't apply (e.g. unresolved value). */
    fun text(s: String?): JComponent = JLabel(s?.takeIf { it.isNotBlank() } ?: "—")

    private fun navigateTo(project: Project, fqn: String) {
        // Try the precise PSI lookup first via reflection so this class stays loadable when
        // com.intellij.modules.java is absent. Tightly cached by the platform — cheap on repeats.
        val psiFile = tryFindByPsiFacade(project, fqn) ?: tryFindBySimpleName(project, fqn)
        psiFile?.virtualFile?.let { vFile ->
            OpenFileDescriptor(project, vFile).navigate(true)
        }
    }

    private fun tryFindByPsiFacade(project: Project, fqn: String): com.intellij.psi.PsiFile? {
        return try {
            val facadeClass = Class.forName("com.intellij.psi.JavaPsiFacade")
            val getInstance = facadeClass.getMethod("getInstance", Project::class.java)
            val facade = getInstance.invoke(null, project)
            val findClass = facadeClass.getMethod(
                "findClass", String::class.java, GlobalSearchScope::class.java
            )
            val psiClass = findClass.invoke(
                facade, fqn, GlobalSearchScope.allScope(project)
            ) ?: return null
            // PsiClass.getNavigationElement().getContainingFile() — uses the same reflection
            // approach to stay loose on the type.
            val getNav = psiClass.javaClass.getMethod("getNavigationElement")
            val nav = getNav.invoke(psiClass)
            val getContaining = nav.javaClass.methods.firstOrNull { it.name == "getContainingFile" }
                ?: return null
            getContaining.invoke(nav) as? com.intellij.psi.PsiFile
        } catch (_: ClassNotFoundException) {
            null
        } catch (_: Throwable) {
            null
        }
    }

    private fun tryFindBySimpleName(project: Project, fqn: String): com.intellij.psi.PsiFile? {
        val simple = fqn.substringAfterLast('.').substringAfterLast('$')
        if (simple.isBlank()) return null
        // Best-effort: try the common source extensions. We don't know whether the class is
        // Java/Kotlin/Groovy without resolving it; FilenameIndex requires a concrete file name.
        for (ext in listOf("kt", "java", "groovy", "scala")) {
            val files = FilenameIndex.getVirtualFilesByName(
                "$simple.$ext", GlobalSearchScope.allScope(project)
            )
            val first = files.firstOrNull() ?: continue
            val psi = com.intellij.psi.PsiManager.getInstance(project).findFile(first)
            if (psi != null) return psi
        }
        return null
    }
}
