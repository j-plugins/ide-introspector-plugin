package com.github.xepozz.ide.introspector.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class PlatformExplorerCellRenderer : ColoredTreeCellRenderer() {

    override fun customizeCellRenderer(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean,
    ) {
        val node = (value as? DefaultMutableTreeNode)?.userObject as? PlatformExplorerNode ?: return
        when (node) {
            is PlatformExplorerNode.Root -> {
                icon = AllIcons.Nodes.Folder
                append(node.displayName)
            }
            is PlatformExplorerNode.PluginNode -> {
                icon = AllIcons.Nodes.Plugin
                append(node.plugin.name)
                appendGray("[${node.plugin.id}]")
                if (!node.plugin.isEnabled) {
                    append("  ")
                    append("(disabled)", SimpleTextAttributes.ERROR_ATTRIBUTES)
                }
                if (node.plugin.isBundled) {
                    appendGray("[bundled]")
                }
            }
            is PlatformExplorerNode.GroupNode -> {
                icon = AllIcons.Nodes.Folder
                append(node.displayName)
                appendGray("(${node.count})")
            }
            is PlatformExplorerNode.ExtensionPointNode -> {
                icon = AllIcons.Nodes.Interface
                append(node.ep.name)
                appendGray("[${node.ep.kind}]")
                appendGray("(${node.ep.extensionsCount})")
            }
            is PlatformExplorerNode.ExtensionNode -> {
                icon = AllIcons.Nodes.Class
                val e = node.extension
                val effective = e.effectiveClass ?: e.implementationClass ?: "(no impl class)"
                append(effective)
                // If the effective class differs from the bean wrapper, surface the wrapper
                // dimly so the reader can tell which EP shape this extension uses.
                if (e.effectiveClass != null && e.implementationClass != null &&
                    e.effectiveClass != e.implementationClass
                ) {
                    appendGray("via ${e.implementationClass.substringAfterLast('.')}")
                }
                appendGray("← ${e.providedByPluginId}")
            }
            is PlatformExplorerNode.DependencyNode -> {
                icon = AllIcons.Nodes.PpLib
                append(node.dep.pluginId)
                appendGray(if (node.dep.optional) "(optional)" else "(required)")
            }
            is PlatformExplorerNode.ServiceNode -> {
                icon = AllIcons.Nodes.Services
                val s = node.service
                append(s.implementationClass)
                appendGray("[${s.area}]")
                if (s.preload != "FALSE") {
                    appendGray("preload=${s.preload}")
                }
                if (s.source == "light_instantiated") {
                    append("  ")
                    append("(@Service)", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES)
                }
            }
            is PlatformExplorerNode.ListenerNode -> {
                icon = AllIcons.General.Inline_edit
                val l = node.listener
                append(l.listenerClass)
                appendGray("→ ${l.topicClass.substringAfterLast('.')}")
                appendGray("[${l.area}]")
            }
            is PlatformExplorerNode.TopicNode -> {
                icon = AllIcons.Hierarchy.Subtypes
                val t = node.topic
                append("${t.declaringClassName.substringAfterLast('.')}.${t.fieldName}")
                appendGray("→ ${t.listenerClassName.substringAfterLast('.')}")
                if (t.onCompanion) {
                    append("  ")
                    append("(companion)", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES)
                }
            }
            is PlatformExplorerNode.LoadingNode -> {
                icon = AllIcons.Process.Step_passive
                append(node.displayName, SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES)
            }
        }
    }

    private fun appendGray(text: String) {
        append("  ")
        append(text, SimpleTextAttributes.GRAY_ATTRIBUTES)
    }
}
