package micro;
import battlecode.common.*;

public class EnlightenmentCenter {
	public static RobotController rc;
	
	public static void act() throws GameActionException {
		if (ECInfo.n_enemies_adjacent>0) {  // uncover spawn tiles if blocked by enemy
			int[][] net_enemy_conviction = new int[3][3];
			for (Direction dir:Math2.UNIT_DIRECTIONS) {  // compute net convictions to see where enemies will remain after nearby empowers finish
				RobotInfo robot = (rc.canSenseLocation(Info.loc.add(dir)))?rc.senseRobotAtLocation(Info.loc.add(dir)):null;
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
			if (best_direction!=null && rc.canBuildRobot(RobotType.POLITICIAN, best_direction, conviction)) {  // don't let them convert the EC right after you spawn the politician
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
		Direction build_direction = null;
		int n_build_directions = 0;
		for (Direction dir : Direction.cardinalDirections()) {
            if (rc.canBuildRobot(RobotType.POLITICIAN, dir, 1)) {
                n_build_directions++;
                if (Math.random()<1.0/n_build_directions) {build_direction = dir;}
            }
        }
		if (n_build_directions==0) {return;}
		int conviction = 1;
		if (Math.random()<0.6) {
			conviction = Math.min(Info.conviction-1, (int)((ECInfo.total_income_per_build + 0.02*Info.conviction)/0.6));
		}
		else {
			conviction = 1;
		}
		RobotType build_type = (conviction>33)? RobotType.POLITICIAN : RobotType.MUCKRAKER;
		if (rc.canBuildRobot(build_type, build_direction, conviction)) {
			Action.buildRobot(build_type, build_direction, conviction);
		}
	}
	
	public static void pause() {
		
	}

}
