/*
 * Image/J Plugins
 * Copyright (C) 2002-2013 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */
package net.sf.ij_plugins.filters;

import java.util.ArrayList;
import java.util.List;


/**
 * Implements a red-black tree.
 *
 * @author Jarek Sacha
 */
public final class RedBlackTreeFloat {

    private Node root;

    private static final int RED = 0;
    private static final int BLACK = 1;


    public RedBlackTreeFloat() {
        root = Node.NULL;
    }


    public void verify() throws IllegalStateException {

        // Rule 2
        if (root.color != BLACK) {
            throw new IllegalStateException("Root node is not black.");
        }

        // Rule 3
        if (Node.NULL.color != BLACK) {
            throw new IllegalStateException("NULL node is not black.");
        }

        verify(root);
    }


    private void verify(final Node node) {
        if (node == Node.NULL) {
            return;
        }

        // Rule 1
        if (!(node.color == RED || node.color == BLACK)) {
            throw new IllegalStateException("Node is neither red nor black: " + node.toString());
        }

        // Rule 4
        if (node.color == RED) {
            if (!(node.left.color == BLACK && node.right.color == BLACK)) {
                throw new IllegalStateException("Red node must have both children black: " + node.toString());
            }
        }

        // FIXME: Rule 5
        final List<Node> leaves = new ArrayList<>();
        findLeaves(node, leaves);
        if (leaves.size() > 0) {
            final int blackInPath = countBlackToParent(leaves.get(0), node);
            for (int i = 0; i < leaves.size(); i++) {
                final Node c = leaves.get(i);
                final int n = countBlackToParent(c, node);
                if (n != blackInPath) {
                    throw new IllegalStateException("Black path mismatch in sub-tree: "
                            + node.toString() + ". Path 0=" + blackInPath + ", path " + i + "=" + n + ".");
                }
            }
        }

        // FIXME: Rule 6 - size of the sub-tree agrees wit node.size field.

        // Verify children
        verify(node.left);
        verify(node.left);
    }


    private int countBlackToParent(final Node leaf, final Node node) {

        if (leaf == Node.NULL) {
            throw new IllegalArgumentException("Leaf node cannot be NULL");
        }

        if (leaf == node) {
            return 0;
        }

        final int r = leaf.color == BLACK ? 1 : 0;

        return r + countBlackToParent(leaf.parent, node);
    }


    private void findLeaves(final Node node, final List<Node> leaves) {
        if (node == Node.NULL) {
            return;
        }

        if (node.right == Node.NULL && node.left == Node.NULL) {
            leaves.add(node);
            return;
        }

        findLeaves(node.left, leaves);
        findLeaves(node.right, leaves);
    }


    /**
     * Remove all nodes from the tree.
     */
    public void clear() {
        // Remove one by one to remove circular references and enable garbage collection.
        Node x = root;
        while (x != Node.NULL) {
            remove(x);
            x = root;
        }
    }


    public int size() {
        return root.size;
    }


    public void insert(final float key) {

        Node y = Node.NULL;
        Node x = root;
        final Node z = new Node(key);

        // Find insertion point
        while (x != Node.NULL) {
            ++x.size;
            y = x;
            x = key < x.key ? x.left : x.right;
        }
        // Insert the new node
        z.parent = y;
        if (y == Node.NULL) {
            root = z;
        } else {
            if (key < y.key) {
                y.left = z;
            } else {
                y.right = z;
            }
        }

        insertFixup(z);
    }


    public boolean remove(final float key) {
        return remove(find(root, key));
    }


    private boolean remove(final Node node) {
        final Node z = node;
        if (z == Node.NULL) {
            return false;
        }

        final Node y = z.left == Node.NULL || z.right == Node.NULL
                ? z
                : successor(z);

        final Node x = y.left != Node.NULL
                ? y.left
                : y.right;

        //        if (x != Node.NULL) {
        x.parent = y.parent;
        //        }

        if (y.parent == Node.NULL) {
            root = x;
        } else {
            if (y == y.parent.left) {
                y.parent.left = x;
            } else {
                y.parent.right = x;
            }
        }

        if (y != z) {
            z.key = y.key;
        }

        // Update count
        Node v = y.parent;
        while (v != Node.NULL) {
            --v.size;
            v = v.parent;
        }

        if (y.color == BLACK) {
            deleteFixup(x);
        }

        y.clear();

        return true;

    }


    /**
     * Select <code>i</code>-th key in the tree (key with rank <code>i</code>).
     *
     * @param i rank of the key to elect.
     * @return value of key with rank <code>i</code>.
     */
    public float select(final int i) {
        if (i < 1) {
            throw new IllegalArgumentException("Rank argument i must be larger than zero.");
        }

        final Node n = select(root, i);
        if (n == Node.NULL) {
            throw new IllegalArgumentException("Input argument rank is too large.");
        }

        return n.key;
    }


    private Node select(final Node x, final int i) {
        final int r = x.left.size + 1;
        if (i == r) {
            return x;
        } else {
            return i < r ? select(x.left, i) : select(x.right, i - r);
        }
    }


    /**
     * Test if tree contains <code>key</code>.
     *
     * @param key the key to search for.
     * @return the matching key or null if not found.
     */
    public boolean contains(final float key) {
        return find(root, key) != Node.NULL;
    }


    /**
     * Test if the tree is logically empty.
     *
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return root == Node.NULL;
    }


    public void printTree() {
        printTree(root);
    }


    private void insertFixup(final Node node) {
        Node z = node;
        while (z.parent.color == RED) {
            if (z.parent == z.parent.parent.left) {
                final Node y = z.parent.parent.right;
                if (y.color == RED) {
                    z.parent.color = BLACK;
                    y.color = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.right) {
                        z = z.parent;
                        leftRotate(z);
                    }
                    z.parent.color = BLACK;
                    z.parent.parent.color = RED;
                    rightRotate(z.parent.parent);
                }
            } else {
                final Node y = z.parent.parent.left;
                if (y.color == RED) {
                    z.parent.color = BLACK;
                    y.color = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.left) {
                        z = z.parent;
                        rightRotate(z);
                    }
                    z.parent.color = BLACK;
                    z.parent.parent.color = RED;
                    leftRotate(z.parent.parent);
                }
            }
        }
        root.color = BLACK;
    }


    private void rightRotate(final Node x) {
        final Node y = x.left;
        x.left = y.right;

        if (y.right != Node.NULL) {
            y.right.parent = x;
        }

        y.parent = x.parent;
        if (x.parent == Node.NULL) {
            root = y;
        } else {
            if (x == x.parent.right) {
                x.parent.right = y;
            } else {
                x.parent.left = y;
            }
        }

        y.right = x;
        x.parent = y;
        y.size = x.size;
        x.size = x.left.size + x.right.size + 1;
    }


    private void leftRotate(final Node x) {
        final Node y = x.right;
        x.right = y.left;

        if (y.left != Node.NULL) {
            y.left.parent = x;
        }

        y.parent = x.parent;
        if (x.parent == Node.NULL) {
            root = y;
        } else {
            if (x == x.parent.left) {
                x.parent.left = y;
            } else {
                x.parent.right = y;
            }
        }

        y.left = x;
        x.parent = y;
        y.size = x.size;
        x.size = x.left.size + x.right.size + 1;
    }


    /**
     * Finds node with a smallest key greater than key of given <code>node</code>.
     *
     * @param node node to find successor for.
     * @return node that is successor of the input node.
     */
    private Node successor(final Node node) {
        Node x = node;
        if (x.right != Node.NULL) {
            return treeMinimum(x.right);
        }
        Node y = x.parent;
        while (y != Node.NULL && x == y.right) {
            x = y;
            y = y.parent;
        }

        return y;
    }


    private Node treeMinimum(final Node node) {
        Node x = node;
        while (x.left != Node.NULL) {
            x = x.left;
        }

        return x;
    }


    private void deleteFixup(final Node node) {
        Node x = node;
        while (x != root && x.color == BLACK) {
            if (x == x.parent.left) {
                Node w = x.parent.right;
                if (w.color == RED) {
                    w.color = BLACK;
                    x.parent.color = RED;
                    leftRotate(x.parent);
                    w = x.parent.right;
                }
                if (w.left.color == BLACK && w.right.color == BLACK) {
                    w.color = RED;
                    x = x.parent;
                } else {
                    if (w.right.color == BLACK) {
                        w.left.color = BLACK;
                        w.color = RED;
                        rightRotate(w);
                        w = x.parent.right;
                    }
                    w.color = x.parent.color;
                    x.parent.color = BLACK;
                    w.right.color = BLACK;
                    leftRotate(x.parent);
                    x = root;
                }
            } else {
                Node w = x.parent.left;
                if (w.color == RED) {
                    w.color = BLACK;
                    x.parent.color = RED;
                    rightRotate(x.parent);
                    w = x.parent.left;
                }
                if (w.right.color == BLACK && w.left.color == BLACK) {
                    w.color = RED;
                    x = x.parent;
                } else {
                    if (w.left.color == BLACK) {
                        w.right.color = BLACK;
                        w.color = RED;
                        leftRotate(w);
                        w = x.parent.left;
                    }
                    w.color = x.parent.color;
                    x.parent.color = BLACK;
                    w.left.color = BLACK;
                    rightRotate(x.parent);
                    x = root;
                }
            }
        }
        x.color = BLACK;
    }


    private Node find(final Node node, final float key) {
        Node x = node;
        //        if (x == Node.NULL || key == x.key) {
        //            return x;
        //        }
        //
        //        return (key < x.key) ? find(x.left, key) : find(x.right, key);

        while (x != Node.NULL && key != x.key) {
            x = (key < x.key) ? x.left : x.right;
        }

        return x;
    }


    private void printTree(final Node node) {
        if (node != Node.NULL) {
            printTree(node.left);
            System.out.println(node);
            printTree(node.right);
        }
    }


    /**
     * Tree node.
     */
    private static final class Node {

        public static final Node NULL;

        /**
         * Data stored by the node
         */
        private float key;
        /**
         * Left child
         */
        private Node left;
        /**
         * Right child
         */
        private Node right;
        private Node parent;
        private int color;
        private int size;


        static {
            NULL = new Node(Float.NaN);
            NULL.key = Float.NaN;
            NULL.left = NULL;
            NULL.right = NULL;
            NULL.parent = NULL;
            NULL.color = BLACK;
            NULL.size = 0;
        }


        Node(final float element) {
            this.key = element;
            this.left = NULL;
            this.right = NULL;
            this.parent = NULL;
            this.color = RedBlackTreeFloat.RED;
            this.size = 1;
        }


        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("key: ");
            builder.append(key);
            builder.append(", size: ");
            builder.append(size);
            builder.append(", color: ");
            builder.append(color == RED ? "RED" : color == BLACK ? "BLACK" : "?");
            return builder.toString();
        }


        public void clear() {
            if (this != NULL) {
                this.key = Float.NaN;
                this.left = NULL;
                this.right = NULL;
                this.color = RedBlackTreeFloat.RED;
                this.parent = NULL;
                this.size = 1;
            }
        }


        static boolean verifyNull() {
            assert Float.isNaN(NULL.key);
            assert NULL.left == NULL;
            assert NULL.right == NULL;
            assert NULL.parent == NULL;
            assert NULL.color == BLACK;
            assert NULL.size == 0;
            return true;
        }
    }
}


