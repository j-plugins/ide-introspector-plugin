package com.github.xepozz.ide.introspector.toolwindow

import com.github.xepozz.ide.introspector.core.PluginInventory
import com.github.xepozz.ide.introspector.model.PluginInfo
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

/**
 * Builds the explorer's tree on demand. Computation is synchronous over [PluginInventory]'s
 * cached snapshot; child population for heavy nodes (extension lists) is lazy.
 */
class PlatformExplorerTreeModel(
    private val inventory: PluginInventory,
    var viewMode: ViewMode,
    var filter: String = "",
    var hideBundled: Boolean = true,
) : DefaultTreeModel(DefaultMutableTreeNode(PlatformExplorerNode.Root("Platform Explorer"))) {

    fun rebuild() {
        val root = DefaultMutableTreeNode(PlatformExplorerNode.Root("Platform Explorer"))
        when (viewMode) {
            ViewMode.BY_PLUGIN -> populateByPlugin(root)
            ViewMode.BY_EXTENSION_POINT -> populateByEp(root)
            ViewMode.BY_DEPENDENCIES -> populateByDependencies(root)
        }
        setRoot(root)
    }

    private fun populateByPlugin(root: DefaultMutableTreeNode) {
        for (p in visiblePlugins()) {
            val pluginNode = DefaultMutableTreeNode(PlatformExplorerNode.PluginNode(p))
            val eps = inventory.extensionPoints().filter { it.declaredByPluginId == p.id }
            if (eps.isNotEmpty()) {
                val group = DefaultMutableTreeNode(
                    PlatformExplorerNode.GroupNode("Declared EPs", eps.size)
                )
                for (ep in eps) {
                    group.add(DefaultMutableTreeNode(PlatformExplorerNode.ExtensionPointNode(ep)))
                }
                pluginNode.add(group)
            }
            pluginNode.addGroup("Registered extensions", inventory.extensionsByPlugin(p.id)) {
                PlatformExplorerNode.ExtensionNode(it)
            }
            pluginNode.addGroup("Services", inventory.servicesByPlugin(p.id)) {
                PlatformExplorerNode.ServiceNode(it)
            }
            pluginNode.addGroup("Listeners", inventory.listenersByPlugin(p.id)) {
                PlatformExplorerNode.ListenerNode(it)
            }
            pluginNode.addGroup("Dependencies", p.dependencies) {
                PlatformExplorerNode.DependencyNode(it)
            }
            val dependants = inventory.plugins().filter { other ->
                other.id != p.id && other.dependencies.any { it.pluginId == p.id }
            }
            pluginNode.addGroup("Required by", dependants) {
                PlatformExplorerNode.PluginNode(it)
            }
            // Topics scanning walks the plugin's classpath (slow on bundled IDE plugins —
            // some have tens of thousands of classes). Limit to non-bundled plugins; bundled
            // topics stay accessible via the events.list_topics MCP tool.
            val topics = if (p.isBundled) emptyList() else inventory.topicsByPlugin(p.id)
            pluginNode.addGroup("Topics", topics) {
                PlatformExplorerNode.TopicNode(it)
            }
            root.add(pluginNode)
        }
    }

    private fun <T> DefaultMutableTreeNode.addGroup(
        label: String,
        items: List<T>,
        toNode: (T) -> PlatformExplorerNode,
    ) {
        if (items.isEmpty()) return
        val group = DefaultMutableTreeNode(PlatformExplorerNode.GroupNode(label, items.size))
        items.take(MAX_CHILDREN).forEach { item ->
            group.add(DefaultMutableTreeNode(toNode(item)))
        }
        if (items.size > MAX_CHILDREN) {
            group.add(DefaultMutableTreeNode(
                PlatformExplorerNode.LoadingNode("… and ${items.size - MAX_CHILDREN} more")
            ))
        }
        add(group)
    }

    private fun visiblePlugins(): List<PluginInfo> = inventory.plugins()
        .filter { !hideBundled || !it.isBundled }
        .filter { matchesFilter(it.id) || matchesFilter(it.name) }

    private fun populateByEp(root: DefaultMutableTreeNode) {
        val bundledIds: Set<String> = if (!hideBundled) emptySet()
            else inventory.plugins().filter { it.isBundled }.map { it.id }.toSet()
        val eps = inventory.extensionPoints()
            .filter { !hideBundled || it.declaredByPluginId !in bundledIds }
            .filter { matchesFilter(it.name) }
        for (ep in eps) {
            val epNode = DefaultMutableTreeNode(PlatformExplorerNode.ExtensionPointNode(ep))
            val list = inventory.extensionsForEpLive(ep.name)
            list.take(MAX_CHILDREN).forEach { e ->
                epNode.add(DefaultMutableTreeNode(PlatformExplorerNode.ExtensionNode(e)))
            }
            if (list.size > MAX_CHILDREN) {
                epNode.add(DefaultMutableTreeNode(
                    PlatformExplorerNode.LoadingNode("… and ${list.size - MAX_CHILDREN} more")
                ))
            }
            root.add(epNode)
        }
    }

    private fun populateByDependencies(root: DefaultMutableTreeNode) {
        for (p in visiblePlugins()) {
            if (p.dependencies.isEmpty()) continue
            val pluginNode = DefaultMutableTreeNode(PlatformExplorerNode.PluginNode(p))
            for (dep in p.dependencies) {
                pluginNode.add(DefaultMutableTreeNode(PlatformExplorerNode.DependencyNode(dep)))
            }
            root.add(pluginNode)
        }
    }

    private fun matchesFilter(value: String): Boolean {
        if (filter.isEmpty()) return true
        return value.contains(filter, ignoreCase = true)
    }

    companion object {
        const val MAX_CHILDREN = 200
    }
}
