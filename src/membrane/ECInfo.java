package membrane;

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

public class ECInfo {
	public static RobotController rc;
	public static int last_build_power;
	public static IntCycler ids = null;
	public static int enemy_data_to_relay;
	public static Direction last_build_direction = Direction.EAST;

	public static void initialize(RobotController rc) {
		ECInfo.rc = rc;
		last_build_power = -1;
	}
	
	public static void update() throws GameActionException {
		enemy_data_to_relay = 0;
		if (ids!=null) {
			for (int i=0; i<300; i++) {
				if (!rc.canGetFlag(ids.data)) {
					if (ids.next==ids) {
						ids = null;
						break;
					}
					ids.last.next = ids.next;
					ids.next.last = ids.last;
				}
				else {
					int flag = rc.getFlag(ids.data);
					//do stuff
				}
				ids = ids.next;
			}
		}
	}
}
