package iam;
import robocode.*;
import java.awt.Color;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.util.Random;

//API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * BoomBot - a robot by Iver
 */
public class BoomBot extends Robot
{
	/**
	 * run: BoomBot's default behavior
	 */
	
	// Create instance of Random class for random number generator
	Random rand = new Random();

	// Determines which direction the bot will move
	int moveDirection = 1;
	
	public void run() {
		
		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar
		setColors(Color.white, Color.white, Color.red);
		setScanColor( Color.red );	
		
		// Endless loop
		while( true ) {
			// Gun, Radar, and Robot move independently
			setAdjustRadarForGunTurn(true);
			setAdjustRadarForRobotTurn(true);
			setAdjustGunForRobotTurn(true);
			
			if ( getTime() % 5 == 0 )
				movement();
				
			turnRadarRight(360);		
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {

		// Move radar with gun
		setAdjustRadarForGunTurn(false);
		
		// Calculate distance from robot to enemy
		double firePower = Math.min(500 / e.getDistance(), 3);

		// Initialize variable to calculate lead to put on bullet
		double adjustForSpeed = 0;
		
		// The further the enemy is, the more you lead your bullet
		if ( e.getDistance() > 500 )
			adjustForSpeed = (e.getVelocity()*1200)/e.getDistance();
		else if ( e.getDistance() > 300 )
			adjustForSpeed = (e.getVelocity()*1000)/e.getDistance();
		else if ( e.getDistance() > 150 )
			adjustForSpeed = (e.getVelocity()*800)/e.getDistance();
		else if ( e.getDistance() > 30 )
			adjustForSpeed = (e.getVelocity()*600)/e.getDistance();
		
		// Find absolute location of robot
		double absoluteBearing = getHeading() + e.getBearing();
		// Find bearing of scanned robot to gun
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

		// Keep adjusting gun towards robot
		turnGunRight(bearingFromGun + adjustForSpeed);
		// If gun heat is 0, fire
		if (getGunHeat() == 0)
				fire(firePower);

		// If not able to shoot and aimed at robot, move and scan again
		if (bearingFromGun == 0) {
			scan();
		}
	}
	
	/**
	 * movement: How your robot will move
	 */
	public void movement()
	{
		// Get random number 
		int randInt = rand.nextInt(360);
		
		// Random number determines move direction
		if (randInt >= 180)
			moveDirection *= -1;
			
		if (randInt % 5 == 0)
			ahead( randInt * moveDirection );
		else if ( randInt % 2 == 0 )
		{
			turnRight( randInt/2 );
			ahead( randInt * moveDirection );
		}
		else
		{
			turnLeft( (int)(randInt/2)  );
			ahead( randInt * moveDirection );
		}
	}
	
	/**
	 * onHitRobot: What to do when you collide with another robot
	 */
	public void onHitRobot(HitRobotEvent e)
	{
		// Turn away from enemy
		back(20);
		turnRight(90);
		ahead(50);
	}


	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		moveDirection *= -1;
		
		if ( getTime() % 2 == 0)
			movement();
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Turn away from wall
		back(20);
		turnRight(180);
		ahead(50);
	}	
}
