package slands2;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
    	
        RobotPlayer.rc = rc;
        Info.initialize(rc);

        while (true) {
            try {
            	Info.update();
            	if (rc.getRoundNum()>1500) {rc.resign();}
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
            	if (rc.getType()==RobotType.ENLIGHTENMENT_CENTER) {
            		EnlightenmentCenter.bid();
            	}
            	
            	Flag.set();
            	Flag.display();
            	
//            	if (rc.getRoundNum()>Info.round_num) {throw new Exception("OUT OF BYTECODE!");}

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
