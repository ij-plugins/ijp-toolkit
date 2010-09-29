/*
 * Image/J Plugins
 * Copyright (C) 2002-2010 Jarek Sacha
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

import junit.framework.TestCase;


/**
 * @author Jarek Sacha
 */
public class RedBlackTreeFloatTest extends TestCase {

    public RedBlackTreeFloatTest(String test) {
        super(test);
    }


    /**
     * The fixture set up called before every test method.
     */
    protected void setUp() throws Exception {
    }


    /**
     * The fixture clean up called after every test method.
     */
    protected void tearDown() throws Exception {
    }


    public void testInsert1() throws Exception {
        final RedBlackTreeFloat tree = new RedBlackTreeFloat();

        final float key1 = 5;
        tree.insert(key1);
        tree.verify();
        assertTrue(tree.contains(key1));

        final float key2 = 7;
        tree.insert(key2);
        tree.verify();
        assertTrue(tree.contains(key2));


    }


    public void testInsert2() throws Exception {
        final RedBlackTreeFloat tree = new RedBlackTreeFloat();


        final float[] keys = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        for (int i = 0; i < keys.length; i++) {
            tree.insert(keys[i]);
            tree.verify();
            assertTrue("" + i, tree.contains(keys[i]));
        }
    }


    public void testRemove1() throws Exception {
        final RedBlackTreeFloat tree = new RedBlackTreeFloat();

        final float key1 = 5;
        tree.insert(key1);
        tree.verify();
        assertTrue(tree.contains(key1));

        final float key2 = 7;
        tree.insert(key2);
        tree.verify();
        assertTrue(tree.contains(key2));


        assertTrue(tree.remove(key1));
        tree.verify();
        assertFalse(tree.contains(key1));

        assertFalse(tree.remove(8.1f));
        tree.verify();
    }


    public void testRemove2() throws Exception {
        final RedBlackTreeFloat tree = new RedBlackTreeFloat();


        final float[] keys = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        for (float key : keys) {
            tree.insert(key);
            tree.verify();
        }

        for (float key : keys) {
            assertTrue(tree.contains(key));
        }


        for (float key : keys) {
            assertTrue(tree.remove(key));
            tree.verify();
            assertFalse(tree.contains(key));
        }
    }


    public void testSelect1() throws Exception {
        final RedBlackTreeFloat tree = new RedBlackTreeFloat();


        final float[] keys = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        for (float key : keys) {
            tree.insert(key);
            tree.verify();
        }

        for (float key : keys) {
            assertTrue(tree.contains(key));
        }


        for (int i = 1; i <= keys.length; i++) {
            assertEquals(i, tree.select(i), 0.001);
        }
    }


    public void testSelect2() throws Exception {
        final RedBlackTreeFloat tree = new RedBlackTreeFloat();


        final float[] keys = {206, 202, 201, 220, 191, 199, 187, 217, 17};

        for (float key : keys) {
            tree.insert(key);
        }

        assertEquals(201, tree.select(5), 0.001);
    }


    public void testSelect3() throws Exception {
        final RedBlackTreeFloat tree = new RedBlackTreeFloat();


        final float[][] keys = {
                {206, 202, 201},
                {220, 191, 199},
                {187, 217, 17},
                {252, 240, 221}};

        for (int i = 0; i < keys[0].length; i++) {
            tree.insert(keys[0][i]);
        }
        assertEquals(3, tree.size());

        for (int i = 0; i < keys[1].length; i++) {
            tree.insert(keys[1][i]);
        }
        assertEquals(6, tree.size());

        for (int i = 0; i < keys[2].length; i++) {
            tree.insert(keys[2][i]);
        }
        assertEquals(9, tree.size());

        assertEquals(201, tree.select(5), 0.001);

        for (int i = 0; i < keys[0].length; i++) {
            tree.remove(keys[0][i]);
        }
        assertEquals(6, tree.size());

        for (int i = 0; i < keys[3].length; i++) {
            tree.insert(keys[3][i]);
        }
        assertEquals(9, tree.size());

        assertEquals(217, tree.select(5), 0.001);

    }
}