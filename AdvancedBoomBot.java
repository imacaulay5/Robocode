package iam;
import robocode.*;
import java.awt.Color;
import java.util.Random;
import robocode.util.*;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * AdvancedBoomBot - a robot by Iver
 */
public class AdvancedBoomBot extends AdvancedRobot
{

	// Create instance of Random class for random number generator
	Random rand = new Random();
	
	// Determines which direction the bot will move
	int moveDirection = 1;

	/**
	 * run: AdvancedBoomBot's default behavior
	 */
	public void run() {
		
		// Set robot colors
		setColors(Color.white,Color.white,Color.blue); // body,gun,radar
		setScanColor( Color.blue );
		
		// Robot, gun, and radar move independently
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		
		// Robot main loop
		while( true ) {
			// Scan for robots
			setTurnRadarRightRadians( 2 * Math.PI );
			// Change movement every 8 ticks
			if ( getTime() % 8 == 0 )
				movement();
		
			execute();	
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {

		// Calculate fire power depending on distance to enemy
		double firePower = Math.min(500 / e.getDistance(), 3);
		// Calculate bullet speed
		double bulletSpeed = 20 - firePower * 3;
		
		// Calculate angle for predicted location of robot
		double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
		
		// Find x position and y position of enemy robot
		double xPosition = getX() + e.getDistance() * Math.sin(absoluteBearing);
		double yPosition = getY() + e.getDistance() * Math.cos(absoluteBearing);
		// Find enemy velocity and heading
		double enemyVelocity = e.getVelocity();
		double enemyHeading = e.getHeadingRadians();
		
		// Calculate predicted shot angle
		double predictedBearing = calculateShotAngle( xPosition,
												yPosition,
												enemyVelocity,
												enemyHeading,
												bulletSpeed);
												
		// Calculate predicted bearing from gun heading					
		double bearingFromGun = Utils.normalRelativeAngle(predictedBearing - getGunHeadingRadians());

		// Turn gun toward predicted bearing
		setTurnGunRightRadians( bearingFromGun );
		
		// If gun heat is 0, FIRE!
		if (getGunHeat() == 0)
			fire(firePower);
		
		// If not able to shoot and aimed at robot, scan for another robot
		if (bearingFromGun == 0)
			scan();
	}

	/*
	 * Calculate angle at which enemy will be when the bullet reaches
	 */
	public double calculateShotAngle(double xPosition,
                                           double yPosition,
                                           double velocity,
										   double enemyHeading,
                                           double projectileSpeed)
    {
		// Initialize vectors
		double xVelocity;
		double yVelocity;
		
		// Calculate xVelocity and yVelocity using trigonometry
		// If in Quadrant I
		if (enemyHeading <= Math.PI/2 && enemyHeading >= 0)
		{
			xVelocity = velocity * Math.cos( (Math.PI/2) - enemyHeading );
			yVelocity = velocity * Math.sin( (Math.PI/2) - enemyHeading );
		}
		// If in Quadrant IV
		else if (enemyHeading <= Math.PI && enemyHeading > 0)
		{
			xVelocity = velocity * Math.sin( Math.PI - enemyHeading );
			yVelocity = -( velocity * Math.cos( Math.PI - enemyHeading ));
		}
		// If in Quadrant III
		else if (enemyHeading >= -(Math.PI/2) && enemyHeading < 0)
		{
			xVelocity = -( velocity * Math.cos( (Math.PI/2) - Math.abs(enemyHeading)));
			yVelocity = velocity * Math.sin( (Math.PI/2) - Math.abs(enemyHeading));
		}
		// If in Quadrant II
		else 
		{
			xVelocity = -( velocity * Math.sin( Math.PI - Math.abs(enemyHeading) ));
			yVelocity = -( velocity * Math.cos( Math.PI - Math.abs(enemyHeading) ));
		}
		
        // Initiate predicted coordinates of target
        double xPredicted = xPosition;
        double yPredicted = yPosition;
        double distanceProjectile = 0;
        int time = 0;
		// Calculate distance using Pythagorean Theorem
        double distanceTarget = Math.sqrt((xPosition * xPosition) + (yPosition * yPosition));

        // While loop (distance bullet per time stamp)
        while (distanceProjectile < distanceTarget)
        {
            // Increase time by one second
            time++;
            // See where projectile is after one second
            distanceProjectile += projectileSpeed;
            // Move the target in the predicted direction
            xPredicted += xVelocity;
            yPredicted += yVelocity;
            // Calculate new distance of target
            distanceTarget = Math.sqrt((xPredicted * xPredicted) + (yPredicted * yPredicted));
        }
        // Initialize bearing variable
		double bearing = Utils.normalAbsoluteAngle(Math.atan2(xPredicted - getX(),yPredicted - getY()));

		return bearing;
	}
	
	/*
	 * How the robot will move
	 */
	public void movement()
	{
		// Get random number
		int randInt = rand.nextInt(360);
		
		// Random number determines move direction
		if (randInt >= 180)
			moveDirection *= -1;
		
		// If randInt is divisible by 5 just move straight	
		if (randInt % 5 == 0)
			setAhead( randInt * moveDirection );
		// If randInt is even, turn right
		else if ( randInt % 2 == 0 )
		{
			setTurnRight( randInt/2 );
			setAhead( randInt * moveDirection );
		}
		// Else, turn left
		else
		{
			setTurnLeft( (int)(randInt/2)  );
			setAhead( randInt * moveDirection );
		}
		execute();
	}
	
	/**
	 * onHitRobot: What to do when you collide with another robot
	 */
	public void onHitRobot(HitRobotEvent e)
	{
		// Turn away from enemy
		setBack(20);
		setTurnRight(Math.PI/2);
		setAhead(100);
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Change movement direction
		moveDirection *= -1;
		movement();
		execute();
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Move back. turn around, and move forward
		setBack(20);
		setTurnRightRadians( Math.PI );
		setAhead(100);
		execute();
	}
}
