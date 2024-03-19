package com.david.giczi.gpsurvey.utils;


import android.graphics.Point;

import com.david.giczi.gpsurvey.domain.MeasPoint;

public class AzimuthAndDistance {
	
	private final MeasPoint pointA;
	private final MeasPoint pointB;
	
	public AzimuthAndDistance(MeasPoint pointA, MeasPoint pointB) {
		this.pointA = pointA;
		this.pointB = pointB;
	}
	
	public double calcAzimuth() {
		
		double deltaX = pointB.getY() - pointA.getY();
		double deltaY = pointB.getX() - pointA.getX();
		
		if( deltaX >= 0 && deltaY > 0 ) {
			return Math.atan(deltaX / deltaY);
		}
		else if( deltaX >= 0 &&  0 > deltaY ) {
			return Math.PI - Math.atan(deltaX / Math.abs(deltaY));
		}
		else if( 0 >= deltaX && 0 > deltaY ) {
			return Math.PI + Math.atan(Math.abs(deltaX) / Math.abs(deltaY));
		}
		else if( 0 >= deltaX && deltaY > 0 ) {
			return 2 * Math.PI - Math.atan(Math.abs(deltaX) / deltaY);
		}
		else if(deltaX > 0) {
			return Math.PI / 2;
		}
		else if(0 > deltaX) {
			return 3 * Math.PI / 2;
		}
		
		return Double.NaN;
	}
	 
	public double calcDistance() {
		return Math.sqrt(Math.pow(pointA.getX() - pointB.getX(), 2)
				+ Math.pow(pointA.getY() - pointB.getY(), 2));
	}

	public MeasPoint getPointA() {
		return pointA;
	}

	public MeasPoint getPointB() {
		return pointB;
	}
	
	
}
