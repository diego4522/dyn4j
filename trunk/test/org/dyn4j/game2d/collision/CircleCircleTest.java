/*
 * Copyright (c) 2010, William Bittle
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions 
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 *     and the following disclaimer in the documentation and/or other materials provided with the 
 *     distribution.
 *   * Neither the name of dyn4j nor the names of its contributors may be used to endorse or 
 *     promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dyn4j.game2d.collision;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.dyn4j.game2d.collision.broadphase.BroadphasePair;
import org.dyn4j.game2d.collision.broadphase.Sap;
import org.dyn4j.game2d.collision.manifold.ClippingManifoldSolver;
import org.dyn4j.game2d.collision.manifold.Manifold;
import org.dyn4j.game2d.collision.manifold.ManifoldPoint;
import org.dyn4j.game2d.collision.narrowphase.Gjk;
import org.dyn4j.game2d.collision.narrowphase.Penetration;
import org.dyn4j.game2d.collision.narrowphase.Sat;
import org.dyn4j.game2d.collision.narrowphase.Separation;
import org.dyn4j.game2d.geometry.Circle;
import org.dyn4j.game2d.geometry.Convex;
import org.dyn4j.game2d.geometry.Shape;
import org.dyn4j.game2d.geometry.Transform;
import org.dyn4j.game2d.geometry.Vector;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link Circle} - {@link Circle} collision detection.
 * @author William Bittle
 */
public class CircleCircleTest extends AbstractTest {
	/** The first test {@link Circle} */
	private Circle c1;
	
	/** The second test {@link Circle} */
	private Circle c2;
	
	/**
	 * Sets up the test.
	 */
	@Before
	public void setup() {
		this.c1 = new Circle(1.0);
		this.c2 = new Circle(0.5);
	}
	
	/**
	 * Tests {@link Shape} AABB.
	 */
	@Test
	public void detectShapeAABB() {
		Transform t1 = new Transform();
		Transform t2 = new Transform();
		
		// test containment
		TestCase.assertTrue(this.aabb.detect(c1, t1, c2, t2));
		TestCase.assertTrue(this.aabb.detect(c2, t2, c1, t1));
		
		// test overlap
		t1.translate(-1.0, 0.0);
		TestCase.assertTrue(this.aabb.detect(c1, t1, c2, t2));
		TestCase.assertTrue(this.aabb.detect(c2, t2, c1, t1));
		
		// test only AABB overlap
		t2.translate(0.0, 1.5);
		TestCase.assertTrue(this.aabb.detect(c1, t1, c2, t2));
		TestCase.assertTrue(this.aabb.detect(c2, t2, c1, t1));
		
		// test no overlap
		t1.translate(-1.0, 0.0);
		TestCase.assertFalse(this.aabb.detect(c1, t1, c2, t2));
		TestCase.assertFalse(this.aabb.detect(c2, t2, c1, t1));
	}
	
	/**
	 * Tests {@link Collidable} AABB.
	 */
	@Test	
	public void detectCollidableAABB() {
		List<Convex> shapes = null;
		
		// create some collidables
		shapes = new ArrayList<Convex>(1); shapes.add(c1);
		CollidableTest ct1 = new CollidableTest(shapes, Filter.DEFAULT_FILTER);
		
		shapes = new ArrayList<Convex>(1); shapes.add(c2);
		CollidableTest ct2 = new CollidableTest(shapes, Filter.DEFAULT_FILTER);
		
		// test containment
		TestCase.assertTrue(this.aabb.detect(ct1, ct2));
		TestCase.assertTrue(this.aabb.detect(ct2, ct1));
		
		// test overlap
		ct1.translate(-1.0, 0.0);
		TestCase.assertTrue(this.aabb.detect(ct1, ct2));
		TestCase.assertTrue(this.aabb.detect(ct2, ct1));
		
		// test only AABB overlap
		ct2.translate(0.0, 1.5);
		TestCase.assertTrue(this.aabb.detect(ct1, ct2));
		TestCase.assertTrue(this.aabb.detect(ct2, ct1));
		
		// test no overlap
		ct1.translate(-1.0, 0.0);
		TestCase.assertFalse(this.aabb.detect(ct1, ct2));
		TestCase.assertFalse(this.aabb.detect(ct2, ct1));
	}
	
	/**
	 * Tests {@link Sap}.
	 */
	@Test
	public void detectSap() {
		List<Convex> shapes;
		List<BroadphasePair<CollidableTest>> pairs;
		
		// create some collidables
		shapes = new ArrayList<Convex>(1); shapes.add(c1);
		CollidableTest ct1 = new CollidableTest(shapes, Filter.DEFAULT_FILTER);
		
		shapes = new ArrayList<Convex>(1); shapes.add(c2);
		CollidableTest ct2 = new CollidableTest(shapes, Filter.DEFAULT_FILTER);
		
		List<CollidableTest> objs = new ArrayList<CollidableTest>();
		objs.add(ct1);
		objs.add(ct2);
		
		// test containment
		pairs = this.sap.detect(objs);
		TestCase.assertEquals(1, pairs.size());
		
		// test overlap
		ct1.translate(-1.0, 0.0);
		pairs = this.sap.detect(objs);
		TestCase.assertEquals(1, pairs.size());
		
		// test only AABB overlap
		ct2.translate(0.0, 1.5);
		pairs = this.sap.detect(objs);
		TestCase.assertEquals(1, pairs.size());
		
		// test no overlap
		ct1.translate(-1.0, 0.0);
		pairs = this.sap.detect(objs);
		TestCase.assertEquals(0, pairs.size());
	}
	
	/**
	 * Tests {@link Sat}.
	 */
	@Test
	public void detectSat() {
		Penetration p = new Penetration();
		Transform t1 = new Transform();
		Transform t2 = new Transform();
		Vector n = null;
		
		// test containment
		TestCase.assertTrue(this.sat.detect(c1, t1, c2, t2, p));
		TestCase.assertTrue(this.sat.detect(c1, t1, c2, t2));
		n = p.getNormal();
		TestCase.assertEquals(0.0, n.x);
		TestCase.assertEquals(0.0, n.y);
		TestCase.assertEquals(1.5, p.getDepth());
		// try reversing the shapes
		TestCase.assertTrue(this.sat.detect(c2, t2, c1, t1, p));
		TestCase.assertTrue(this.sat.detect(c2, t2, c1, t1));
		n = p.getNormal();
		TestCase.assertEquals(0.0, n.x);
		TestCase.assertEquals(0.0, n.y);
		TestCase.assertEquals(1.5, p.getDepth());
		
		// test overlap
		t1.translate(-1.0, 0.0);
		TestCase.assertTrue(this.sat.detect(c1, t1, c2, t2, p));
		TestCase.assertTrue(this.sat.detect(c1, t1, c2, t2));
		n = p.getNormal();
		TestCase.assertEquals(1.0, n.x);
		TestCase.assertEquals(0.0, n.y);
		TestCase.assertEquals(0.5, p.getDepth());
		// try reversing the shapes
		TestCase.assertTrue(this.sat.detect(c2, t2, c1, t1, p));
		TestCase.assertTrue(this.sat.detect(c2, t2, c1, t1));
		n = p.getNormal();
		TestCase.assertEquals(-1.0, n.x);
		TestCase.assertEquals(0.0, n.y);
		TestCase.assertEquals(0.5, p.getDepth());
		
		// test AABB overlap
		t2.translate(0.0, 1.5);
		TestCase.assertFalse(this.sat.detect(c1, t1, c2, t2, p));
		TestCase.assertFalse(this.sat.detect(c1, t1, c2, t2));
		// try reversing the shapes
		TestCase.assertFalse(this.sat.detect(c2, t2, c1, t1, p));
		TestCase.assertFalse(this.sat.detect(c2, t2, c1, t1));
		
		// test no overlap
		t1.translate(-1.0, 0.0);
		TestCase.assertFalse(this.sat.detect(c1, t1, c2, t2, p));
		TestCase.assertFalse(this.sat.detect(c1, t1, c2, t2));
		// try reversing the shapes
		TestCase.assertFalse(this.sat.detect(c2, t2, c1, t1, p));
		TestCase.assertFalse(this.sat.detect(c2, t2, c1, t1));
	}
	
	/**
	 * Tests {@link Gjk}.
	 */
	@Test
	public void detectGjk() {
		Penetration p = new Penetration();
		Transform t1 = new Transform();
		Transform t2 = new Transform();
		Vector n = null;
		
		// test containment
		TestCase.assertTrue(this.gjk.detect(c1, t1, c2, t2, p));
		TestCase.assertTrue(this.gjk.detect(c1, t1, c2, t2));
		n = p.getNormal();
		TestCase.assertEquals(0.0, n.x);
		TestCase.assertEquals(0.0, n.y);
		TestCase.assertEquals(1.5, p.getDepth());
		// try reversing the shapes
		TestCase.assertTrue(this.gjk.detect(c2, t2, c1, t1, p));
		TestCase.assertTrue(this.gjk.detect(c2, t2, c1, t1));
		n = p.getNormal();
		TestCase.assertEquals(0.0, n.x);
		TestCase.assertEquals(0.0, n.y);
		TestCase.assertEquals(1.5, p.getDepth());
		
		// test overlap
		t1.translate(-1.0, 0.0);
		TestCase.assertTrue(this.gjk.detect(c1, t1, c2, t2, p));
		TestCase.assertTrue(this.gjk.detect(c1, t1, c2, t2));
		n = p.getNormal();
		TestCase.assertEquals(1.0, n.x);
		TestCase.assertEquals(0.0, n.y);
		TestCase.assertEquals(0.5, p.getDepth());
		// try reversing the shapes
		TestCase.assertTrue(this.gjk.detect(c2, t2, c1, t1, p));
		TestCase.assertTrue(this.gjk.detect(c2, t2, c1, t1));
		n = p.getNormal();
		TestCase.assertEquals(-1.0, n.x);
		TestCase.assertEquals(0.0, n.y);
		TestCase.assertEquals(0.5, p.getDepth());
		
		// test AABB overlap
		t2.translate(0.0, 1.5);
		TestCase.assertFalse(this.gjk.detect(c1, t1, c2, t2, p));
		TestCase.assertFalse(this.gjk.detect(c1, t1, c2, t2));
		// try reversing the shapes
		TestCase.assertFalse(this.gjk.detect(c2, t2, c1, t1, p));
		TestCase.assertFalse(this.gjk.detect(c2, t2, c1, t1));
		
		// test no overlap
		t1.translate(-1.0, 0.0);
		TestCase.assertFalse(this.gjk.detect(c1, t1, c2, t2, p));
		TestCase.assertFalse(this.gjk.detect(c1, t1, c2, t2));
		// try reversing the shapes
		TestCase.assertFalse(this.gjk.detect(c2, t2, c1, t1, p));
		TestCase.assertFalse(this.gjk.detect(c2, t2, c1, t1));
	}
	
	/**
	 * Tests the {@link Gjk} distance method.
	 */
	@Test
	public void gjkDistance() {
		Separation s = new Separation();
		
		Transform t1 = new Transform();
		Transform t2 = new Transform();
		
		Vector n = null;
		Vector p1 = null;
		Vector p2 = null;
		
		// test containment
		TestCase.assertFalse(this.gjk.distance(c1, t1, c2, t2, s));
		// try reversing the shapes
		TestCase.assertFalse(this.gjk.distance(c2, t2, c1, t1, s));
		
		// test overlap
		t1.translate(-1.0, 0.0);
		TestCase.assertFalse(this.gjk.distance(c1, t1, c2, t2, s));
		// try reversing the shapes
		TestCase.assertFalse(this.gjk.distance(c2, t2, c1, t1, s));
		
		// test AABB overlap
		t2.translate(0.0, 1.5);
		TestCase.assertTrue(this.gjk.distance(c1, t1, c2, t2, s));
		n = s.getNormal();
		p1 = s.getPoint1();
		p2 = s.getPoint2();
		TestCase.assertEquals(0.302, s.getDistance(), 1.0e-3);
		TestCase.assertEquals(0.554, n.x, 1.0e-3);
		TestCase.assertEquals(0.832, n.y, 1.0e-3);
		TestCase.assertEquals(-0.445, p1.x, 1.0e-3);
		TestCase.assertEquals(0.832, p1.y, 1.0e-3);
		TestCase.assertEquals(-0.277, p2.x, 1.0e-3);
		TestCase.assertEquals(1.083, p2.y, 1.0e-3);
		// try reversing the shapes
		TestCase.assertTrue(this.gjk.distance(c2, t2, c1, t1, s));
		n = s.getNormal();
		p1 = s.getPoint1();
		p2 = s.getPoint2();
		TestCase.assertEquals(0.302, s.getDistance(), 1.0e-3);
		TestCase.assertEquals(-0.554, n.x, 1.0e-3);
		TestCase.assertEquals(-0.832, n.y, 1.0e-3);
		TestCase.assertEquals(-0.277, p1.x, 1.0e-3);
		TestCase.assertEquals(1.083, p1.y, 1.0e-3);
		TestCase.assertEquals(-0.445, p2.x, 1.0e-3);
		TestCase.assertEquals(0.832, p2.y, 1.0e-3);
		
		// test no overlap
		t1.translate(-1.0, 0.0);
		TestCase.assertTrue(this.gjk.distance(c1, t1, c2, t2, s));
		n = s.getNormal();
		p1 = s.getPoint1();
		p2 = s.getPoint2();
		TestCase.assertEquals(1.000, s.getDistance(), 1.0e-3);
		TestCase.assertEquals(0.800, n.x, 1.0e-3);
		TestCase.assertEquals(0.600, n.y, 1.0e-3);
		TestCase.assertEquals(-1.200, p1.x, 1.0e-3);
		TestCase.assertEquals(0.600, p1.y, 1.0e-3);
		TestCase.assertEquals(-0.400, p2.x, 1.0e-3);
		TestCase.assertEquals(1.200, p2.y, 1.0e-3);
		// try reversing the shapes
		TestCase.assertTrue(this.gjk.distance(c2, t2, c1, t1, s));
		n = s.getNormal();
		p1 = s.getPoint1();
		p2 = s.getPoint2();
		TestCase.assertEquals(1.000, s.getDistance(), 1.0e-3);
		TestCase.assertEquals(-0.800, n.x, 1.0e-3);
		TestCase.assertEquals(-0.600, n.y, 1.0e-3);
		TestCase.assertEquals(-0.400, p1.x, 1.0e-3);
		TestCase.assertEquals(1.200, p1.y, 1.0e-3);
		TestCase.assertEquals(-1.200, p2.x, 1.0e-3);
		TestCase.assertEquals(0.600, p2.y, 1.0e-3);
	}
	
	/**
	 * Test the {@link ClippingManifoldSolver}.
	 */
	@Test
	public void getClipManifold() {
		Manifold m = new Manifold();
		Penetration p = new Penetration();
		
		Transform t1 = new Transform();
		Transform t2 = new Transform();
		
		ManifoldPoint mp = null;
		Vector p1 = null;
		
		// test containment gjk
		this.gjk.detect(c1, t1, c2, t2, p);
		TestCase.assertTrue(this.cmfs.getManifold(p, c1, t1, c2, t2, m));
		TestCase.assertEquals(1, m.getPoints().size());
		// try reversing the shapes
		TestCase.assertTrue(this.cmfs.getManifold(p, c2, t2, c1, t1, m));
		TestCase.assertEquals(1, m.getPoints().size());
		
		// test containment sat
		this.sat.detect(c1, t1, c2, t2, p);
		TestCase.assertTrue(this.cmfs.getManifold(p, c1, t1, c2, t2, m));
		TestCase.assertEquals(1, m.getPoints().size());
		// try reversing the shapes
		TestCase.assertTrue(this.cmfs.getManifold(p, c2, t2, c1, t1, m));
		TestCase.assertEquals(1, m.getPoints().size());
		
		t1.translate(-1.0, 0.0);
		
		// test overlap gjk
		this.gjk.detect(c1, t1, c2, t2, p);
		TestCase.assertTrue(this.cmfs.getManifold(p, c1, t1, c2, t2, m));
		TestCase.assertEquals(1, m.getPoints().size());
		mp = m.getPoints().get(0);
		p1 = mp.getPoint();
		TestCase.assertEquals(0.0, p1.x);
		TestCase.assertEquals(0.0, p1.y);
		TestCase.assertEquals(0.5, mp.getDepth());
		// try reversing the shapes
		TestCase.assertTrue(this.cmfs.getManifold(p, c2, t2, c1, t1, m));
		TestCase.assertEquals(1, m.getPoints().size());
		mp = m.getPoints().get(0);
		p1 = mp.getPoint();
		TestCase.assertEquals(-0.5, p1.x);
		TestCase.assertEquals(0.0, p1.y);
		TestCase.assertEquals(0.5, mp.getDepth());
		
		// test overlap sat
		this.sat.detect(c1, t1, c2, t2, p);
		TestCase.assertTrue(this.cmfs.getManifold(p, c1, t1, c2, t2, m));
		TestCase.assertEquals(1, m.getPoints().size());
		mp = m.getPoints().get(0);
		p1 = mp.getPoint();
		TestCase.assertEquals(0.0, p1.x);
		TestCase.assertEquals(0.0, p1.y);
		TestCase.assertEquals(0.5, mp.getDepth());
		// try reversing the shapes
		TestCase.assertTrue(this.cmfs.getManifold(p, c2, t2, c1, t1, m));
		TestCase.assertEquals(1, m.getPoints().size());
		mp = m.getPoints().get(0);
		p1 = mp.getPoint();
		TestCase.assertEquals(-0.5, p1.x);
		TestCase.assertEquals(0.0, p1.y);
		TestCase.assertEquals(0.5, mp.getDepth());
	}
}
