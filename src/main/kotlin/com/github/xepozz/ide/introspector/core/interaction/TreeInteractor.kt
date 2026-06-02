package com.github.xepozz.ide.introspector.core.interaction

import com.github.xepozz.ide.introspector.model.WidgetItem
import java.awt.Component
import java.awt.Rectangle
import javax.swing.JTree
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath

object TreeInteractor : WidgetInteractor {
    override val widgetType: String = "tree"

    override fun supports(component: Component): Boolean = component is JTree

    override fun listItems(component: Component): List<WidgetItem> {
        val tree = component as JTree
        return (0 until tree.rowCount).map { row ->
            val treePath = tree.getPathForRow(row)
            widgetItemForPath(tree, treePath, row)
        }
    }

    override fun select(component: Component, selector: ItemSelector): InteractionOutcome {
        val tree = component as JTree
        val treePath = resolvePath(tree, selector)
            ?: return InteractionOutcome.notFound(describeMiss(selector))
        treePath
            .parentPath
            ?.let { tree.expandPath(it) }
        tree.selectionPath = treePath
        tree.scrollPathToVisible(treePath)
        return InteractionOutcome(
            matchedItem = widgetItemForPath(tree, treePath, tree.getRowForPath(treePath)),
            selectionAfter = currentSelection(tree),
        )
    }

    override fun itemBounds(component: Component, selector: ItemSelector): Rectangle? {
        val tree = component as JTree
        val treePath = resolvePath(tree, selector)
            ?: return null
        return tree.getPathBounds(treePath)
    }

    private fun resolvePath(tree: JTree, selector: ItemSelector): TreePath? {
        selector.path?.let { segments ->
            return resolvePathBySegments(tree, segments, selector.matchMode)
        }
        val row = ItemSelectorResolver.resolveIndex(listItems(tree), selector)
            ?: return null
        return tree.getPathForRow(row)
    }

    private fun resolvePathBySegments(tree: JTree, segments: List<String>, matchMode: String): TreePath? {
        val model = tree.model
        val root = model.root
            ?: return null
        if (segments.isEmpty()) return null
        val rootMatchesFirstSegment = ItemTextMatcher.matches(nodeText(tree, model, root), segments.first(), matchMode)
        val descendingSegments = if (rootMatchesFirstSegment) segments.drop(1) else segments
        var treePath = TreePath(root)
        for (segment in descendingSegments) {
            tree.expandPath(treePath)
            val parent = treePath.lastPathComponent
            val match = (0 until model.getChildCount(parent))
                .map { model.getChild(parent, it) }
                .firstOrNull { child ->
                    ItemTextMatcher.matches(
                        nodeText(tree, model, child),
                        segment,
                        matchMode,
                    )
                }
                ?: return null
            treePath = treePath.pathByAddingChild(match)
        }
        return treePath
    }

    private fun currentSelection(tree: JTree): List<WidgetItem> =
        tree.selectionRows
            ?.sorted()
            ?.map { row -> widgetItemForPath(tree, tree.getPathForRow(row), row) }
            ?: emptyList()

    private fun widgetItemForPath(tree: JTree, treePath: TreePath, row: Int): WidgetItem {
        val model = tree.model
        val node = treePath.lastPathComponent
        val leaf = model.isLeaf(node)
        val expanded = tree.isExpanded(treePath)
        val selected = row >= 0 && tree.isRowSelected(row)
        return WidgetItem(
            index = row,
            text = nodeText(tree, model, node, selected, expanded, leaf, row),
            selected = selected,
            enabled = tree.isEnabled,
            path = pathTexts(tree, treePath),
            depth = treePath.pathCount - 1,
            expanded = expanded,
            leaf = leaf,
        )
    }

    private fun pathTexts(tree: JTree, treePath: TreePath): List<String> {
        val model = tree.model
        return treePath
            .path
            .map { node -> nodeText(tree, model, node) }
    }

    private fun nodeText(
        tree: JTree,
        model: TreeModel,
        node: Any?,
        selected: Boolean = false,
        expanded: Boolean = false,
        leaf: Boolean = model.isLeaf(node),
        row: Int = -1,
    ): String =
        tree.convertValueToText(node, selected, expanded, leaf, row, false)

    private fun describeMiss(selector: ItemSelector): String =
        when {
            selector.path != null -> "No tree node matched path ${selector.path}"
            selector.text != null -> "No visible tree row matched text '${selector.text}' (mode=${selector.matchMode})"
            selector.index != null -> "No visible tree row at index ${selector.index}"
            else -> "No selector criteria supplied for tree"
        }
}
