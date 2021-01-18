package micro;
import battlecode.common.*;

public class EnlightenmentCenter {
	public static RobotController rc;
	
	public static void act() throws GameActionException {
		if (ECInfo.open_spawn_tiles<ECInfo.open_spawn_tiles_required) {  // uncover spawn tiles if blocked by enemy
			int[][] net_enemy_conviction = new int[3][3];
			for (Direction dir:Math2.UNIT_DIRECTIONS) {  // compute net convictions to see where enemies will remain after nearby empowers finish
				RobotInfo robot = (rc.canSenseLocation(Info.loc.add(dir)))?rc.senseRobotAtLocation(Info.loc.add(dir)):null;
				if (robot!=null) {
					if (robot.team==Info.enemy) {
						net_enemy_conviction[dir.dx+1][dir.dy+1] += (robot.type==RobotType.POLITICIAN)? robot.influence+robot.conviction : robot.conviction+1;
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
				if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, 1) && min_adjacent_net_enemy_conviction[dir.dx+1][dir.dy+1]!=Integer.MAX_VALUE
						&& (best_direction==null || min_adjacent_net_enemy_conviction[dir.dx+1][dir.dy+1]<min_cost)) {
					best_direction = dir;
					min_cost = min_adjacent_net_enemy_conviction[dir.dx+1][dir.dy+1];
				}
			}
			int conviction = Math.max(26, min_cost*3+GameConstants.EMPOWER_TAX);
			if (best_direction!=null && rc.canBuildRobot(RobotType.POLITICIAN, best_direction, conviction)) {
				Action.buildRobot(RobotType.POLITICIAN, best_direction, conviction);
				return;
			}
		}
		if (Info.enemy_politicians.length+Info.enemy_muckrakers.length==0 && Info.conviction*rc.getEmpowerFactor(Info.friendly, 11)-GameConstants.EMPOWER_TAX > Info.conviction && !ECInfo.exterminate_flag) {
			for (Direction dir : Direction.cardinalDirections()) {
	            if (rc.canBuildRobot(RobotType.POLITICIAN, dir, Info.conviction/3)) {
					Action.buildTargetter(dir, Info.conviction/3, Info.loc); return;
	            }
	        }
		}
		boolean[][] targetter_present = new boolean[3][3];
		for (Direction dir : Math2.UNIT_DIRECTIONS) {
			if (rc.canSenseLocation(Info.loc.add(dir))) {
				RobotInfo robot = rc.senseRobotAtLocation(Info.loc.add(dir));
				if (robot!=null && robot.team==Info.friendly && rc.getFlag(robot.ID)>>20==1) {
					targetter_present[dir.dx+1][dir.dy+1] = true;
				}
			}
		}
		Direction build_direction = null;
		int n_build_directions = 0;
		Direction dir = ECInfo.last_build_direction;
		for (int i=8; --i>=0;) {
			dir = dir.rotateLeft().rotateLeft().rotateLeft();
			Direction left = dir.rotateLeft();
			Direction right = dir.rotateRight();
            if (rc.canBuildRobot(RobotType.POLITICIAN, dir, 1) && !targetter_present[left.dx+1][left.dy+1] && !targetter_present[right.dx+1][right.dy+1]) {
                n_build_directions++;
                build_direction = dir;
            }
        }
		if (n_build_directions==0) {return;}
		if (ECInfo.guard_flag && !ECInfo.enough_guards) {  // guard slanderers at all costs
			int test_conviction = Math.max(1, ECInfo.sampled_muckraker_influence+GameConstants.EMPOWER_TAX+2);
			if (test_conviction < Info.conviction) {
				if (rc.canBuildRobot(RobotType.POLITICIAN, build_direction, test_conviction)) {
					Action.buildRobot(RobotType.POLITICIAN, build_direction, test_conviction); return;
				}
			}
		}
		int test_conviction = Math.max(70, (int)(1.1*ECInfo.weakest_ec_influence)+GameConstants.EMPOWER_TAX);
		if (ECInfo.weakest_ec_loc!=null && Info.conviction > test_conviction) {  // send monster sized politicians to finish off a weak EC
			if (rc.canBuildRobot(RobotType.POLITICIAN, build_direction, test_conviction)) {
				Action.buildTargetter(build_direction, test_conviction, ECInfo.weakest_ec_loc); return;
			}
		}
		if (ECInfo.exterminate_flag) {  // perform final extermination
			test_conviction = Math.max(20, (Info.conviction+Math.max(0, 1400-Info.round_num)*ECInfo.total_income)
										  /(int)Math.max(1, (1400-Info.round_num)/2*rc.sensePassability(Info.loc)));
			if (test_conviction > 0) {
				if (rc.canBuildRobot(RobotType.POLITICIAN, build_direction, test_conviction)) {
					Action.buildRobot(RobotType.POLITICIAN, build_direction, test_conviction); return;
				}
			}
			return;
		}
		if (ECInfo.desired_guard_flag) {  // make slanderers if conviction is abundant
			test_conviction = Math2.embezzle_floor(Math.min(949, Info.conviction));
			if (test_conviction > 0) {
				if (rc.canBuildRobot(RobotType.SLANDERER, build_direction, test_conviction)) {
					Action.buildRobot(RobotType.SLANDERER, build_direction, test_conviction); return;
				}
			}
		}
		test_conviction = Math.max(1, (int) (Math.ceil((ECInfo.sampled_bury_guard_influence-GameConstants.EMPOWER_TAX)/2.0)/0.7+1));
		if (test_conviction < 30) {  // rush using muckrakers which are slightly too large to double kill
			if (rc.canBuildRobot(RobotType.MUCKRAKER, build_direction, test_conviction)) {
				Action.buildRobot(RobotType.MUCKRAKER, build_direction, test_conviction); return;
			}
		}
		if (rc.canBuildRobot(RobotType.MUCKRAKER, build_direction, 1)) {
			Action.buildRobot(RobotType.MUCKRAKER, build_direction, 1); return;
		}
	}
	
	public static void pause() {
		
	}

}
