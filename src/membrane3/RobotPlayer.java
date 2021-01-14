package membrane3;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public strictfp class RobotPlayer {
    static RobotController rc;

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        Info.initialize(rc);

        while (true) {
            try {
            	Info.update();
            	if (rc.getRoundNum()>3000) {rc.resign();}
            	if (Info.ready) {
            		switch (rc.getType()) {
            		case ENLIGHTENMENT_CENTER: EnlightenmentCenter.act(); break;
            		case POLITICIAN:           Politician.act();          break;
            		case SLANDERER:            Slanderer.act();           break;
            		case MUCKRAKER:            Muckraker.act();           break;
            		}
            	}
            	else {
            		switch (rc.getType()) {
            		case ENLIGHTENMENT_CENTER: EnlightenmentCenter.pause(); break;
            		case POLITICIAN:           Politician.pause();          break;
            		case SLANDERER:            Slanderer.pause();           break;
            		case MUCKRAKER:            Muckraker.pause();           break;
            		}
            	}
            	
            	Flag.display();

                Clock.yield();
            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                System.out.println(Info.x);
                System.out.println(Info.y);
                e.printStackTrace();
                Clock.yield();
//                rc.resign();
            }
        }
    }
}
