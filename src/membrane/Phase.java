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

public class Phase {
	
	public static RobotController rc;
	public static boolean is_gas = true;
	public static boolean is_membrane = false;
	
	public static void evaporate() throws GameActionException {
		is_gas = true;
		is_membrane = false;
	}
	
	public static void condense(Direction dir, int attack_defend_sign) throws GameActionException {  // attack_defend_sign is 1 for attack from interior and -1 for defend from exterior
		boolean condensing_on_membrane = false;
		int layer_num = 0;
		if (rc.canSenseLocation(Info.loc.add(dir))) {
			RobotInfo robot = rc.senseRobotAtLocation(Info.loc.add(dir));
			if (robot!=null) {
				condensing_on_membrane = robot.team==Info.friendly && (rc.getFlag(robot.ID)>>23==1);
				layer_num = (rc.getFlag(robot.ID)>>17)%4;
			}
		}
		is_gas = false;
		is_membrane = true;
		boolean diagonal = dir.dx*dir.dx+dir.dy*dir.dy==2;
		if (!condensing_on_membrane) {  // not part of membrane
			if (attack_defend_sign==1 && !diagonal) {
				Membrane.left = dir.rotateLeft().rotateLeft();
				Membrane.right = dir.rotateRight().rotateRight();
			}
			if (attack_defend_sign==-1 && !diagonal) {
				Membrane.left = dir.rotateRight().rotateRight();
				Membrane.right = dir.rotateLeft().rotateLeft();
			}
			if (attack_defend_sign==1 && diagonal) {
				Membrane.left = dir.rotateLeft();
				Membrane.right = dir.rotateRight();
			}
			if (attack_defend_sign==-1 && diagonal) {
				Membrane.left = dir.rotateRight();
				Membrane.right = dir.rotateLeft();
			}
			Membrane.layer_num = 0;
		}
		else {  // part of membrane
			if (attack_defend_sign==1 && !diagonal) {
				Membrane.left = dir.rotateLeft().rotateLeft();
				Membrane.right = dir.rotateRight().rotateRight();
			}
			if (attack_defend_sign==-1 && !diagonal) {
				Membrane.left = dir.rotateRight().rotateRight();
				Membrane.right = dir.rotateLeft().rotateLeft();
			}
			if (attack_defend_sign==1 && diagonal) {
				Membrane.left = dir.rotateLeft();
				Membrane.right = dir.rotateRight();
			}
			if (attack_defend_sign==-1 && diagonal) {
				Membrane.left = dir.rotateRight();
				Membrane.right = dir.rotateLeft();
			}
			Membrane.layer_num = (attack_defend_sign==1)?(layer_num+3)%4:layer_num;
		}
		Membrane.update();
	}
}
