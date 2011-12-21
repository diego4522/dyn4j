/*
 * Copyright (c) 2011 William Bittle  http://www.dyn4j.org/
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
package org.dyn4j.dynamics.joint;

import org.dyn4j.Epsilon;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.Settings;
import org.dyn4j.dynamics.Step;
import org.dyn4j.geometry.Interval;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

/**
 * Represents a pulley between two {@link Body}s.
 * <p>
 * The pulley anchor points represent the "hanging" points for the respective
 * {@link Body}s.
 * <p>
 * The ratio allows this joint to act like a block-and-tackle.
 * <p>
 * Nearly identical to <a href="http://www.box2d.org">Box2d</a>'s equivalent class.
 * @see <a href="http://www.box2d.org">Box2d</a>
 * @author William Bittle
 * @version 3.0.1
 * @since 2.1.0
 */
public class PulleyJoint extends Joint {
	/** The joint type */
	public static final Joint.Type TYPE = new Joint.Type("Pulley");
	
	/** The world space pulley anchor point for the first {@link Body} */
	protected Vector2 pulleyAnchor1;
	
	/** The world space pulley anchor point for the second {@link Body} */
	protected Vector2 pulleyAnchor2;
	
	/** The local anchor point on the first {@link Body} */
	protected Vector2 localAnchor1;
	
	/** The local anchor point on the second {@link Body} */
	protected Vector2 localAnchor2;
	
	/** The pulley ratio for modeling a block-and-tackle */
	protected double ratio;
	
	/** The original length of the first side of the pulley */
	protected final double length1;
	
	/** The original length of the second side of the pulley */
	protected final double length2;
	
	/** The total length of the pulley system */
	protected double length;
	
	/** The normal from the first pulley anchor to the first {@link Body} anchor */
	protected Vector2 n1;
	
	/** The normal from the second pulley anchor to the second {@link Body} anchor */
	protected Vector2 n2;
	
	/** The effective mass of the two body system (Kinv = J * Minv * Jtrans) */
	protected double invK;
	
	/** The accumulated impulse from the previous time step */
	protected double impulse;
	
	/**
	 * Minimal constructor.
	 * <p>
	 * Creates a pulley joint between the two given {@link Body}s using the given anchor points.
	 * @param body1 the first {@link Body}
	 * @param body2 the second {@link Body}
	 * @param pulleyAnchor1 the first pulley anchor point
	 * @param pulleyAnchor2 the second pulley anchor point
	 * @param bodyAnchor1 the first {@link Body}'s anchor point
	 * @param bodyAnchor2 the second {@link Body}'s anchor point
	 * @throws NullPointerException if body1, body2, pulleyAnchor1, pulleyAnchor2, bodyAnchor1, or bodyAnchor2 is null
	 * @throws IllegalArgumentException if body1 == body2
	 */
	public PulleyJoint(Body body1, Body body2, Vector2 pulleyAnchor1, Vector2 pulleyAnchor2, Vector2 bodyAnchor1, Vector2 bodyAnchor2) {
		super(body1, body2, false);
		// verify the bodies are not the same instance
		if (body1 == body2) throw new IllegalArgumentException("Cannot create a pulley joint between the same body instance.");
		// verify the pulley anchor points are not null
		if (pulleyAnchor1 == null || pulleyAnchor2 == null) throw new NullPointerException("Neither pulley anchor point can be null.");
		// verify the body anchor points are not null
		if (bodyAnchor1 == null || bodyAnchor2 == null) throw new NullPointerException("Neither body anchor point can be null.");
		// set the pulley anchor points
		this.pulleyAnchor1 = pulleyAnchor1;
		this.pulleyAnchor2 = pulleyAnchor2;
		// get the local anchor points
		this.localAnchor1 = body1.getLocalPoint(bodyAnchor1);
		this.localAnchor2 = body2.getLocalPoint(bodyAnchor2);
		// default the ratio and minimum length
		this.ratio = 1.0;
		// compute the lengths
		this.length1 = bodyAnchor1.distance(pulleyAnchor1);
		this.length2 = bodyAnchor2.distance(pulleyAnchor2);
		// compute the lengths
		// length = l1 + ratio * l2
		this.length = this.length1 + this.length2;
		// initialize the other fields
		this.impulse = 0.0;
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PULLEY_JOINT[")
		.append(super.toString()).append("|")
		.append(this.pulleyAnchor1).append("|")
		.append(this.pulleyAnchor2).append("|")
		.append(this.localAnchor1).append("|")
		.append(this.localAnchor2).append("|")
		.append(this.ratio).append("|")
		.append(this.length1).append("|")
		.append(this.length2).append("|")
		.append(this.length).append("|")
		.append(this.impulse).append("]");
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#initializeConstraints(org.dyn4j.dynamics.Step)
	 */
	@Override
	public void initializeConstraints(Step step) {
		double linearTolerance = Settings.getInstance().getLinearTolerance();
		Transform t1 = body1.getTransform();
		Transform t2 = body2.getTransform();
		Mass m1 = body1.getMass();
		Mass m2 = body2.getMass();
		
		double invM1 = m1.getInverseMass();
		double invM2 = m2.getInverseMass();
		double invI1 = m1.getInverseInertia();
		double invI2 = m2.getInverseInertia();
		
		// put the body anchors in world space
		Vector2 r1 = t1.getTransformedR(this.body1.getLocalCenter().to(this.localAnchor1));
		Vector2 r2 = t2.getTransformedR(this.body2.getLocalCenter().to(this.localAnchor2));
		Vector2 p1 = r1.sum(this.body1.getWorldCenter());
		Vector2 p2 = r2.sum(this.body2.getWorldCenter());
		
		Vector2 s1 = this.pulleyAnchor1;
		Vector2 s2 = this.pulleyAnchor2;
		
		// compute the axes
		this.n1 = s1.to(p1);
		this.n2 = s2.to(p2);
		
		// get the lengths
		double l1 = this.n1.normalize();
		double l2 = this.n2.normalize();
		
		// check for near zero length
		if (l1 <= 10.0 * linearTolerance) {
			// zero out the axis
			this.n1.zero();
		}
		
		// check for near zero length		
		if (l2 <= 10.0 * linearTolerance) {
			// zero out the axis
			this.n2.zero();
		}
		
		// compute the inverse effective masses (K matrix, in this case its a scalar) for the constraints
		double r1CrossN1 = r1.cross(this.n1);
		double r2CrossN2 = r2.cross(this.n2);
		double pm1 = invM1 + invI1 * r1CrossN1 * r1CrossN1;
		double pm2 = invM2 + invI2 * r2CrossN2 * r2CrossN2;
		this.invK = pm1 + this.ratio * this.ratio * pm2;
		// make sure we can invert it
		if (this.invK > Epsilon.E) {
			this.invK = 1.0 / this.invK;
		} else {
			this.invK = 0.0;
		}
		
		// warm start the constraints taking
		// variable time steps into account
		double dtRatio = step.getDeltaTimeRatio();
		this.impulse *= dtRatio;
		
		// compute the impulse along the axes
		Vector2 J1 = this.n1.product(-this.impulse);
		Vector2 J2 = this.n2.product(-this.ratio * this.impulse);
		
		// apply the impulse
		this.body1.getVelocity().add(J1.product(invM1));
		this.body1.setAngularVelocity(this.body1.getAngularVelocity() + invI1 * r1.cross(J1));
		this.body2.getVelocity().add(J2.product(invM2));
		this.body2.setAngularVelocity(this.body2.getAngularVelocity() + invI2 * r2.cross(J2));
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solveVelocityConstraints(org.dyn4j.dynamics.Step)
	 */
	@Override
	public void solveVelocityConstraints(Step step) {
		Transform t1 = this.body1.getTransform();
		Transform t2 = this.body2.getTransform();
		Mass m1 = this.body1.getMass();
		Mass m2 = this.body2.getMass();
		
		double invM1 = m1.getInverseMass();
		double invM2 = m2.getInverseMass();
		double invI1 = m1.getInverseInertia();
		double invI2 = m2.getInverseInertia();
		
		// compute r1 and r2
		Vector2 r1 = t1.getTransformedR(this.body1.getLocalCenter().to(this.localAnchor1));
		Vector2 r2 = t2.getTransformedR(this.body2.getLocalCenter().to(this.localAnchor2));
		
		// compute the relative velocity
		Vector2 v1 = this.body1.getVelocity().sum(r1.cross(this.body1.getAngularVelocity()));
		Vector2 v2 = this.body2.getVelocity().sum(r2.cross(this.body2.getAngularVelocity()));
		
		// compute Jv + b
		double C = -this.n1.dot(v1) - this.ratio * this.n2.dot(v2);
		// compute the impulse
		double impulse = this.invK * (-C);
		this.impulse += impulse;
		
		// compute the impulse along each axis
		Vector2 J1 = this.n1.product(-impulse);
		Vector2 J2 = this.n2.product(-impulse * this.ratio);
		
		// apply the impulse
		this.body1.getVelocity().add(J1.product(invM1));
		this.body1.setAngularVelocity(this.body1.getAngularVelocity() + invI1 * r1.cross(J1));
		this.body2.getVelocity().add(J2.product(invM2));
		this.body2.setAngularVelocity(this.body2.getAngularVelocity() + invI2 * r2.cross(J2));
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solvePositionConstraints()
	 */
	@Override
	public boolean solvePositionConstraints() {
		Settings settings = Settings.getInstance();
		double linearTolerance = settings.getLinearTolerance();
		double maxLinearCorrection = settings.getMaxLinearCorrection();
		
		Transform t1 = body1.getTransform();
		Transform t2 = body2.getTransform();
		Mass m1 = body1.getMass();
		Mass m2 = body2.getMass();
		
		double invM1 = m1.getInverseMass();
		double invM2 = m2.getInverseMass();
		double invI1 = m1.getInverseInertia();
		double invI2 = m2.getInverseInertia();
		
		// put the body anchors in world space
		Vector2 r1 = t1.getTransformedR(this.body1.getLocalCenter().to(this.localAnchor1));
		Vector2 r2 = t2.getTransformedR(this.body2.getLocalCenter().to(this.localAnchor2));
		Vector2 p1 = r1.sum(this.body1.getWorldCenter());
		Vector2 p2 = r2.sum(this.body2.getWorldCenter());
		
		Vector2 s1 = this.pulleyAnchor1;
		Vector2 s2 = this.pulleyAnchor2;
		
		// compute the axes
		this.n1 = s1.to(p1);
		this.n2 = s2.to(p2);
		
		// normalize and save the length
		double l1 = this.n1.normalize();
		double l2 = this.n2.normalize();
		
		// make sure the length is not near zero
		if (l1 <= 10.0 * linearTolerance) {
			this.n1.zero();
		}
		// make sure the length is not near zero
		if (l2 <= 10.0 * linearTolerance) {
			this.n2.zero();
		}
		
		double linearError = 0.0;
		
		// recompute K
		double r1CrossN1 = r1.cross(this.n1);
		double r2CrossN2 = r2.cross(this.n2);
		double pm1 = invM1 + invI1 * r1CrossN1 * r1CrossN1;
		double pm2 = invM2 + invI2 * r2CrossN2 * r2CrossN2;
		this.invK = pm1 + this.ratio * this.ratio * pm2;
		// make sure we can invert it
		if (this.invK > Epsilon.E) {
			this.invK = 1.0 / this.invK;
		} else {
			this.invK = 0.0;
		}
		
		// compute the constraint error
		double C = this.length - l1 - this.ratio * l2;
		linearError = Math.abs(C);
		
		// clamp the error
		C = Interval.clamp(C + linearTolerance, -maxLinearCorrection, maxLinearCorrection);
		double impulse = -this.invK * C;
		
		// compute the impulse along the axes
		Vector2 J1 = this.n1.product(-impulse);
		Vector2 J2 = this.n2.product(-this.ratio * impulse);
		
		// apply the impulse
		this.body1.translate(J1.x * invM1, J1.y * invM1);
		this.body1.rotateAboutCenter(r1.cross(J1) * invI1);
		this.body2.translate(J2.x * invM2, J2.y * invM2);
		this.body2.rotateAboutCenter(r2.cross(J2) * invI2);
		
		return linearError < linearTolerance;
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getType()
	 */
	@Override
	public Type getType() {
		return PulleyJoint.TYPE;
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getAnchor1()
	 */
	public Vector2 getAnchor1() {
		return body1.getWorldPoint(this.localAnchor1);
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getAnchor2()
	 */
	public Vector2 getAnchor2() {
		return body2.getWorldPoint(this.localAnchor2);
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getReactionForce(double)
	 */
	@Override
	public Vector2 getReactionForce(double invdt) {
		return this.n2.product(this.impulse * invdt);
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getReactionTorque(double)
	 */
	@Override
	public double getReactionTorque(double invdt) {
		return 0.0;
	}
	
	/**
	 * Returns the pulley anchor point for the first {@link Body}
	 * in world coordinates.
	 * @return {@link Vector2}
	 */
	public Vector2 getPulleyAnchor1() {
		return this.pulleyAnchor1;
	}
	
	/**
	 * Returns the pulley anchor point for the second {@link Body}
	 * in world coordinates.
	 * @return {@link Vector2}
	 */
	public Vector2 getPulleyAnchor2() {
		return this.pulleyAnchor2;
	}
	
	/**
	 * Returns the total length of the pulley.
	 * <p>
	 * Typically this is computed when the joint is created by adding the distance from the
	 * first body anchor to the first pulley anchor with the distance from the second body anchor
	 * to the second pulley anchor.
	 * @since 3.0.1
	 * @return double
	 */
	public double getLength() {
		return this.length;
	}
	
	/**
	 * Returns the current length from the first pulley anchor point to the
	 * anchor point on the first {@link Body}.
	 * <p>
	 * This is used, in conjunction with length2, to compute the total length
	 * when the ratio is changed.
	 * @return double
	 */
	public double getLength1() {
		// get the body anchor point in world space
		Vector2 ba = this.body1.getWorldPoint(this.localAnchor1);
		return this.pulleyAnchor1.distance(ba);
	}

	/**
	 * Returns the current length from the second pulley anchor point to the
	 * anchor point on the second {@link Body}.
	 * @return double
	 */
	public double getLength2() {
		// get the body anchor point in world space
		Vector2 ba = this.body2.getWorldPoint(this.localAnchor2);
		return this.pulleyAnchor2.distance(ba);
	}
	
	/**
	 * Returns the pulley ratio.
	 * @return double
	 */
	public double getRatio() {
		return this.ratio;
	}
	
	/**
	 * Sets the pulley ratio.
	 * <p>
	 * The ratio value is used to simulate a block-and-tackle.  A ratio of 1.0 is the default
	 * and indicates that the pulley is not a block-and-tackle.
	 * <p>
	 * This method recomputes the total length of the pulley system.
	 * @param ratio the ratio; must be greater than zero
	 * @throws IllegalArgumentException if ratio is less than or equal to zero
	 */
	public void setRatio(double ratio) {
		if (ratio <= 0.0) throw new IllegalArgumentException("The pulley ratio must be greater than zero.");
		// make sure the ratio changed
		if (ratio != this.ratio) {
			// set the new ratio
			this.ratio = ratio;
			// compute the new length
			this.length = this.length1 + this.ratio * this.length2;
			// wake up both bodies
			this.body1.setAsleep(false);
			this.body2.setAsleep(false);
		}
	}
}