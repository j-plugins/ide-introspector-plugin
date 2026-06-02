package com.github.xepozz.ide.introspector.core.interaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Test
import java.awt.GraphicsEnvironment
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class TreeInteractorTest {

    private class TreeFixture {
        val colors = DefaultMutableTreeNode("Colors")
        val red = DefaultMutableTreeNode("Red")
        val blue = DefaultMutableTreeNode("Blue")
        val shades = DefaultMutableTreeNode("Shades")
        val navy = DefaultMutableTreeNode("Navy")
        val sky = DefaultMutableTreeNode("Sky")
        val sizes = DefaultMutableTreeNode("Sizes")
        val small = DefaultMutableTreeNode("Small")

        val root = DefaultMutableTreeNode("Root").also { rootNode ->
            rootNode.add(colors)
            colors.add(red)
            colors.add(blue)
            blue.add(shades)
            shades.add(navy)
            shades.add(sky)
            rootNode.add(sizes)
            sizes.add(small)
        }

        val tree = JTree(DefaultTreeModel(root)).also { it.isRootVisible = true }
    }

    @Test
    fun `supports recognizes a JTree`() {
        val fixture = TreeFixture()
        assertTrue(TreeInteractor.supports(fixture.tree))
    }

    @Test
    fun `widget type is tree`() {
        assertEquals("tree", TreeInteractor.widgetType)
    }

    @Test
    fun `listItems enumerates only the currently visible rows`() {
        val fixture = TreeFixture()
        fixture.tree.collapsePath(TreePath(fixture.root))
        val items = TreeInteractor.listItems(fixture.tree)
        assertEquals(listOf("Root"), items.map { it.text })
    }

    @Test
    fun `listItems reports depth leaf and expanded for expanded rows`() {
        val fixture = TreeFixture()
        fixture.tree.expandPath(TreePath(fixture.root))
        val items = TreeInteractor.listItems(fixture.tree)
        val root = items.first { it.text == "Root" }
        val colors = items.first { it.text == "Colors" }
        assertEquals(0, root.depth)
        assertTrue(root.expanded)
        assertFalse(root.leaf)
        assertEquals(1, colors.depth)
        assertFalse(colors.leaf)
        assertFalse(colors.expanded)
    }

    @Test
    fun `listItems exposes the path of texts from root to each row`() {
        val fixture = TreeFixture()
        fixture.tree.expandPath(TreePath(fixture.root))
        val items = TreeInteractor.listItems(fixture.tree)
        val colors = items.first { it.text == "Colors" }
        assertEquals(listOf("Root", "Colors"), colors.path)
    }

    @Test
    fun `select by index resolves the matching visible row`() {
        val fixture = TreeFixture()
        fixture.tree.expandPath(TreePath(fixture.root))
        val outcome = TreeInteractor.select(fixture.tree, ItemSelector(index = 1))
        assertTrue(outcome.success)
        assertEquals("Colors", outcome.matchedItem?.text)
        assertEquals(TreePath(arrayOf(fixture.root, fixture.colors)), fixture.tree.selectionPath)
    }

    @Test
    fun `select by text resolves the matching visible row`() {
        val fixture = TreeFixture()
        fixture.tree.expandPath(TreePath(fixture.root))
        val outcome = TreeInteractor.select(fixture.tree, ItemSelector(text = "Sizes"))
        assertTrue(outcome.success)
        assertEquals("Sizes", outcome.matchedItem?.text)
        assertEquals(TreePath(arrayOf(fixture.root, fixture.sizes)), fixture.tree.selectionPath)
    }

    @Test
    fun `select by path reaches a node inside an initially collapsed subtree`() {
        val fixture = TreeFixture()
        fixture.tree.collapsePath(TreePath(fixture.root))
        assertFalse(fixture.tree.isExpanded(TreePath(fixture.root)))

        val outcome = TreeInteractor.select(
            fixture.tree,
            ItemSelector(path = listOf("Root", "Colors", "Blue", "Shades", "Navy")),
        )

        assertTrue(outcome.success)
        assertEquals("Navy", outcome.matchedItem?.text)
        assertEquals(
            TreePath(arrayOf(fixture.root, fixture.colors, fixture.blue, fixture.shades, fixture.navy)),
            fixture.tree.selectionPath,
        )
        assertTrue(fixture.tree.isExpanded(TreePath(arrayOf(fixture.root, fixture.colors, fixture.blue))))
    }

    @Test
    fun `select by path reports the resolved node in selectionAfter`() {
        val fixture = TreeFixture()
        val outcome = TreeInteractor.select(
            fixture.tree,
            ItemSelector(path = listOf("Root", "Sizes", "Small")),
        )
        assertEquals(listOf("Small"), outcome.selectionAfter.map { it.text })
    }

    @Test
    fun `select by path honors the match mode for segments`() {
        val fixture = TreeFixture()
        val outcome = TreeInteractor.select(
            fixture.tree,
            ItemSelector(path = listOf("root", "sizes"), matchMode = "contains"),
        )
        assertTrue(outcome.success)
        assertEquals("Sizes", outcome.matchedItem?.text)
    }

    @Test
    fun `select returns notFound when a path segment does not match`() {
        val fixture = TreeFixture()
        val outcome = TreeInteractor.select(
            fixture.tree,
            ItemSelector(path = listOf("Root", "Nonexistent")),
        )
        assertFalse(outcome.success)
        assertNull(outcome.matchedItem)
        assertTrue(outcome.warnings.isNotEmpty())
    }

    @Test
    fun `select returns notFound when the text matches no visible row`() {
        val fixture = TreeFixture()
        fixture.tree.expandPath(TreePath(fixture.root))
        val outcome = TreeInteractor.select(fixture.tree, ItemSelector(text = "Navy"))
        assertFalse(outcome.success)
        assertNull(outcome.matchedItem)
    }

    @Test
    fun `itemBounds resolves bounds for a node reached by path`() {
        assumeFalse(GraphicsEnvironment.isHeadless())
        val fixture = TreeFixture()
        fixture.tree.expandPath(TreePath(fixture.root))
        val bounds = TreeInteractor.itemBounds(
            fixture.tree,
            ItemSelector(path = listOf("Root", "Colors")),
        )
        assertNotNull(bounds)
        assertTrue(bounds!!.height > 0)
    }

    @Test
    fun `itemBounds returns null when the path does not match`() {
        val fixture = TreeFixture()
        val bounds = TreeInteractor.itemBounds(
            fixture.tree,
            ItemSelector(path = listOf("Root", "Nonexistent")),
        )
        assertNull(bounds)
    }
}
