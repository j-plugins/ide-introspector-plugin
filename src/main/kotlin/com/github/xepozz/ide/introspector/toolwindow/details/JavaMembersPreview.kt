package com.github.xepozz.ide.introspector.toolwindow.details

import com.github.xepozz.ide.introspector.util.readActionBlocking
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import java.awt.Component
import javax.swing.JComponent

/**
 * Java-PSI-backed implementation of the members section. ONLY referenced from inside
 * [MembersSection] guarded by a `Class.forName("com.intellij.psi.JavaPsiFacade")` check, so the
 * JVM only verifies this class when the Java module is loaded.
 */
internal object JavaMembersPreview {

    private const val METHODS_PREVIEW_LIMIT = 12

    fun build(project: Project, fqn: String): JComponent {
        val cls = readActionBlocking {
            JavaPsiFacade.getInstance(project).findClass(fqn, GlobalSearchScope.allScope(project))
        } ?: return notFound(fqn)

        val (methods, constructors) = cls.methods.partition { !it.isConstructor }
        val interfaces = cls.interfaces.mapNotNull { it.qualifiedName }
        val superFqn = cls.superClass?.qualifiedName?.takeIf { it != "java.lang.Object" }

        val form = DetailForm().apply {
            section("Members")
            superFqn?.let { row("Extends", FqnLink.render(project, it)) }
            if (interfaces.isNotEmpty()) row("Implements", stackedLinks(project, interfaces))
            row("Methods", JBLabel(methods.size.toString()))
            row("Constructors", JBLabel(constructors.size.toString()))
            row("Fields", JBLabel(cls.fields.size.toString()))
            if (cls.innerClasses.isNotEmpty()) row("Inner classes", JBLabel(cls.innerClasses.size.toString()))
            if (methods.isNotEmpty()) {
                val shown = minOf(methods.size, METHODS_PREVIEW_LIMIT)
                section("Declared methods (showing $shown of ${methods.size})")
                methods.take(METHODS_PREVIEW_LIMIT).forEach { row(it.name, signatureLabel(it)) }
            }
        }
        return form.build()
    }

    /** Vertical strip of FqnLinks for multi-valued rows like "Implements". */
    private fun stackedLinks(project: Project, fqns: List<String>): JComponent = verticalPanel().apply {
        for (fqn in fqns) {
            val link = FqnLink.render(project, fqn)
            link.alignmentX = Component.LEFT_ALIGNMENT
            add(link)
        }
    }

    private fun signatureLabel(m: PsiMethod): JComponent {
        val params = m.parameterList.parameters.joinToString(", ") { it.type.presentableText }
        val ret = m.returnType?.presentableText ?: ""
        val text = "(${params})${if (ret.isNotEmpty()) ": $ret" else ""}"
        return infoLabel(text).apply {
            font = JBFont.small()
        }
    }

    private fun notFound(fqn: String): JComponent = infoLabel(
        "Class $fqn not resolvable in this project's scope."
    ).apply {
        border = JBUI.Borders.empty(4, 0)
    }
}
