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

public class EnlightenmentCenter {
	
	public static RobotController rc;
	
	public static void act() throws GameActionException {
		Flag.set_ec();
		if (Info.round_num>2995) {rc.bid((int)(Info.conviction/3)); return;}
		if (ECInfo.n_enemies_adjacent>0) {  // uncover spawn tiles if blocked by enemy
			int[][] net_enemy_conviction = new int[3][3];
			for (Direction dir:Math2.UNIT_DIRECTIONS) {  // compute net convictions to see where enemies will remain after nearby empowers finish
				RobotInfo robot = Info.adjacent_robots[dir.dx+1][dir.dy+1];
				if (robot!=null) {
					if (robot.team==Info.enemy) {
						net_enemy_conviction[dir.dx+1][dir.dy+1] += robot.conviction+1;
					}
					if (robot.team==Info.friendly && robot.type==RobotType.POLITICIAN) {
						int damage = (robot.conviction-GameConstants.EMPOWER_TAX)/4;
						if (damage>0) {
							net_enemy_conviction[dir.rotateLeft().dx+1][dir.rotateLeft().dy+1] -= damage;
							net_enemy_conviction[dir.rotateRight().dx+1][dir.rotateRight().dy+1] -= damage;
						}
					}
				}
			}
			int[][] min_adjacent_net_enemy_conviction = new int[3][3];
			for (Direction dir:Math2.UNIT_DIRECTIONS) {  // compute cost required to remove at least 1 nearby enemy
				min_adjacent_net_enemy_conviction[dir.dx+1][dir.dy+1] = Integer.MAX_VALUE;
			}
			for (Direction dir:Math2.UNIT_DIRECTIONS) {  // compute cost required to remove at least 1 nearby enemy
				if (net_enemy_conviction[dir.dx+1][dir.dy+1]>0) {
					Direction left_side = dir.rotateLeft();
					Direction right_side = dir.rotateRight();
					min_adjacent_net_enemy_conviction[left_side.dx+1][left_side.dy+1] = Math.min(min_adjacent_net_enemy_conviction[left_side.dx+1][left_side.dy+1], net_enemy_conviction[dir.dx+1][dir.dy+1]);
					min_adjacent_net_enemy_conviction[right_side.dx+1][right_side.dy+1] = Math.min(min_adjacent_net_enemy_conviction[right_side.dx+1][right_side.dy+1], net_enemy_conviction[dir.dx+1][dir.dy+1]);
				}
			}
			int min_cost = Integer.MAX_VALUE;
			Direction best_direction = null;
			for (Direction dir:Math2.UNIT_DIRECTIONS) {  // compute cost required to remove at least 1 nearby enemy
				if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, 1) && (best_direction==null || min_adjacent_net_enemy_conviction[dir.dx+1][dir.dy+1]<min_cost)) {
					best_direction = dir;
					min_cost = min_adjacent_net_enemy_conviction[dir.dx+1][dir.dy+1];
				}
			}
			int conviction = min_cost*4+GameConstants.EMPOWER_TAX;
			if (best_direction!=null && conviction<ECInfo.maximum_safe_build_conviction) {  // don't let them convert the EC right after you spawn the politician
				Action.buildRobot(RobotType.POLITICIAN, best_direction, conviction);
				return;
			}
		}
		if (Info.enemy_politicians.length+Info.enemy_muckrakers.length==0 && Info.conviction*rc.getEmpowerFactor(Info.friendly, 11)-GameConstants.EMPOWER_TAX > Info.conviction) {
			for (Direction dir : Direction.cardinalDirections()) {
	            if (rc.canBuildRobot(RobotType.POLITICIAN, dir, Info.conviction)) {
	                Action.buildRobot(RobotType.POLITICIAN, dir, Info.conviction);
	                return;
	            }
	        }
		}
		if (ECInfo.standard_available_build_direction==null) {return;}
		// WARNING: condition for building politicians for the membrane must be sufficiently separated from the threshold for reabsorption
		if (Info.muckraker_warning_level<4 && (Info.friendly_muckrakers.length+Info.friendly_politicians.length>=Info.friendly_slanderers.length*3)
				|| ECInfo.approx_membrane_thickness>2.5 && ECInfo.approx_membrane_politician_ratio>0.6) {  // build slanderers if it's safe
			int conviction = Math2.embezzle_floor(Info.conviction);
			if (conviction>0 && conviction<ECInfo.maximum_safe_build_conviction && Info.round_num<2700) {
				Action.buildRobot(RobotType.SLANDERER, ECInfo.standard_available_build_direction, conviction);
				return;
			}
		}
		int conviction = (int)((ECInfo.total_income_per_build + 0.02*Info.conviction)/0.6);
		if (ECInfo.approx_membrane_thickness>1.3 && ECInfo.approx_membrane_politician_ratio<0.6 && Info.conviction>conviction || Info.round_num>2700) {  // enforce wall if it can't quickly be pierced
			if ((int)(ECInfo.total_income_per_build/0.6)<ECInfo.maximum_safe_build_conviction) {
				Action.buildRobot(RobotType.POLITICIAN, ECInfo.standard_available_build_direction, conviction);
				return;
			}
		}
		
		boolean space_to_build = false;  // default build cheap muckraker for membrane and gas
		for (int i=0; i<8; i++) {
			ECInfo.last_build_direction = ECInfo.last_build_direction.rotateLeft().rotateLeft().rotateLeft();
			if (rc.canBuildRobot(RobotType.MUCKRAKER, ECInfo.last_build_direction, 1)) {
				space_to_build = true;
                break;
			}
		}
		if (space_to_build) {
			Action.buildRobot(RobotType.MUCKRAKER, ECInfo.last_build_direction, 1);
			return;
		}
    }
    
    public static void pause() throws GameActionException {
		Flag.set_ec();
    }
}
