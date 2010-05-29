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
package org.dyn4j.game2d.testbed.test;

import org.dyn4j.game2d.collision.Bounds;
import org.dyn4j.game2d.collision.RectangularBounds;
import org.dyn4j.game2d.dynamics.World;
import org.dyn4j.game2d.geometry.Mass;
import org.dyn4j.game2d.geometry.Rectangle;
import org.dyn4j.game2d.testbed.ContactCounter;
import org.dyn4j.game2d.testbed.Entity;
import org.dyn4j.game2d.testbed.Test;

/**
 * Tests a triangular stack of boxes.
 * @author William Bittle
 */
public class Pyramid extends Test {
	/** The height of the pyramid */
	private static final int HEIGHT = 15;
	
	/* (non-Javadoc)
	 * @see test.Test#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Tests a trianglar stack of boxes.  This test is the 2D equivalent of a pyramid.";
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.game2d.Test#initialize()
	 */
	@Override
	public void initialize() {
		// call the super method
		super.initialize();
		
		// set the camera position and zoom
		this.home();

		// set the bounds
		this.bounds = new Rectangle(16.0, 20.0);
		this.bounds.translate(0.0, 7.0);
		
		// create the world
		Bounds bounds = new RectangularBounds(this.bounds);
		this.world = new World(bounds);
		
		// setup the contact counter
		ContactCounter cc = new ContactCounter();
		this.world.setContactListener(cc);
		this.world.setStepListener(cc);
		
		// setup the bodies in the world
		this.setup();
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.game2d.Test#setup()
	 */
	@Override
	protected void setup() {
		// create the floor
		Rectangle floorRect = new Rectangle(15.0, 1.0);
		Entity floor = new Entity();
		floor.addShape(floorRect);
		floor.setMassFromShapes(Mass.Type.INFINITE);
		this.world.add(floor);
		
		// create the rows
		
		double width = 0.5;
		double height = 0.5;
		
		// reuse the geometry and mass
		Rectangle rect = new Rectangle(width, height);
		rect.setDensity(5.0);
		
		// the current x position
		double x = 0.0;
		// the current y position
		double y = 0.26;
		
		// the spacing between the boxes
		double yspacing = 0.01;
		double xspacing = 0.01;
		
		// loop to create the rows
		for (int i = 0; i < HEIGHT; i++) {
			// the number of boxes on this row
			int num = HEIGHT - i;
			// increment y
			y += height + yspacing;
			// set x
			x = -(num * (width + xspacing)) / 2.0 + ((width + xspacing) / 2.0);
			// loop to create the bodies in the rows
			for (int j = 0; j < num; j++) {
				// create a body
				Entity e = new Entity();
				e.addShape(rect);
				e.setMassFromShapes();
				// move it to the right position
				e.translate(x, y);
				// add it to the world
				this.world.add(e);
				// increment x
				x += (width + xspacing);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.game2d.Test#center()
	 */
	@Override
	public void home() {
		// set the scale
		this.scale = 32.0;
		// move the camera a bit
		this.offset.set(0.0, -3.0);
	}
}
