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

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.codezealot.game.input.Input;
import org.codezealot.game.input.Keyboard;
import org.codezealot.game.input.Mouse;
import org.codezealot.game.input.Input.Hold;
import org.dyn4j.game2d.collision.Bounds;
import org.dyn4j.game2d.collision.RectangularBounds;
import org.dyn4j.game2d.dynamics.Mass;
import org.dyn4j.game2d.dynamics.World;
import org.dyn4j.game2d.geometry.Convex;
import org.dyn4j.game2d.geometry.Rectangle;
import org.dyn4j.game2d.testbed.ContactCounter;
import org.dyn4j.game2d.testbed.Entity;
import org.dyn4j.game2d.testbed.Test;

/**
 * Tests bodies being put to sleep.
 * @author William Bittle
 */
public class Sleep extends Test {
	/* (non-Javadoc)
	 * @see test.Test#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Tests bodies being put to sleep. This test ensures that bodies are " +
			   "put to sleep when they come to rest.  Bodies are put to sleep after " +
			   "they are at rest for the configured sleep time";
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.game2d.Test#initialize()
	 */
	@Override
	public void initialize() {
		// call the super method
		super.initialize();
		
		// setup the camera
		this.home();
		
		// set the bounds
		this.bounds = new Rectangle(16.0, 15.0);
		
		// create the world
		Bounds bounds = new RectangularBounds(this.bounds);
		this.world = new World(bounds);
		
		// setup the contact counter
		ContactCounter cc = new ContactCounter();
		this.world.setContactListener(cc);
		this.world.setStepListener(cc);
		
		// setup the bodies
		this.setup();
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.game2d.Test#setup()
	 */
	@Override
	protected void setup() {
		// create the floor
		Rectangle rect2 = new Rectangle(15.0, 1.0);
		List<Convex> geometry2 = new ArrayList<Convex>();
		geometry2.add(rect2);
		Mass mass2 = Mass.create(rect2.getCenter());
		
		Entity obj0 = new Entity(geometry2, mass2);
		this.world.add(obj0);
		
		// create the stack
		
		Rectangle rect = new Rectangle(1.0, 1.0);
		List<Convex> geometry = new ArrayList<Convex>(1);
		geometry.add(rect);
		Mass mass = Mass.create(rect, 1.0);
		
		Entity obj1 = new Entity(geometry, mass);
		obj1.translate(0.0, 1.0);
		this.world.add(obj1);
		
		Entity obj2 = new Entity(geometry, mass);
		obj2.translate(0.0, 2.0);
		this.world.add(obj2);

		Entity obj3 = new Entity(geometry, mass);
		obj3.translate(0.0, 3.0);
		this.world.add(obj3);
		
		Entity obj4 = new Entity(geometry, mass);
		obj4.translate(0.0, 4.0);
		this.world.add(obj4);
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.game2d.Test#home()
	 */
	@Override
	public void home() {
		// set the scale
		this.scale = 64.0;
		// set the camera offset
		this.offset.zero();
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.game2d.Test#initializeInput(org.codezealot.game.input.Keyboard, org.codezealot.game.input.Mouse)
	 */
	@Override
	public void initializeInput(Keyboard keyboard, Mouse mouse) {
		super.initializeInput(keyboard, mouse);
		// register to listen for the enter key
		keyboard.add(new Input(KeyEvent.VK_ENTER, Hold.NO_HOLD));
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.game2d.Test#poll(org.codezealot.game.input.Keyboard, org.codezealot.game.input.Mouse)
	 */
	@Override
	public void poll(Keyboard keyboard, Mouse mouse) {
		super.poll(keyboard, mouse);
		
		// check for the enter key
		if (keyboard.isPressed(KeyEvent.VK_ENTER)) {
			// check the size of the list
			if (this.world.getBodies().size() > 1) {
				this.world.remove(this.world.getBodies().get(1));
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.game2d.Test#getControls()
	 */
	@Override
	public String[][] getControls() {
		return new String[][] {{"Enter", "Removes a body from the world"}};
	}
}
