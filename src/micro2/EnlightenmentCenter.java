package micro2;
import battlecode.common.*;

public class EnlightenmentCenter {
	public static RobotController rc;
	
	public static void act() throws GameActionException {
		// self-empower if no risk of opponent absorption
		if (Info.n_enemy_politicians+Info.n_enemy_muckrakers<=2 && Info.conviction*rc.getEmpowerFactor(Info.friendly, 11)-GameConstants.EMPOWER_TAX > Info.conviction && !ECInfo.exterminate_flag) {
			for (Direction dir : Direction.cardinalDirections()) {
	            if (rc.canBuildRobot(RobotType.POLITICIAN, dir, Info.conviction)) {
					Action.buildTargetter(dir, Info.conviction, Info.loc); return;
	            }
	        }
		}
		// self-empower if x2 buff with low enemy numbers nearby
		if (Info.n_enemy_politicians+Info.n_enemy_muckrakers<=5 && (Info.conviction*rc.getEmpowerFactor(Info.friendly, 11)-GameConstants.EMPOWER_TAX)/2 > Info.conviction && !ECInfo.exterminate_flag) {
			for (Direction dir : Direction.cardinalDirections()) {
	            if (rc.canBuildRobot(RobotType.POLITICIAN, dir, Info.conviction)) {
					Action.buildTargetter(dir, Info.conviction, Info.loc); return;
	            }
	        }
		}
		// self-empower if x3 buff with low enemy numbers nearby
		if (Info.n_enemy_politicians+Info.n_enemy_muckrakers<=8 && (Info.conviction*rc.getEmpowerFactor(Info.friendly, 11)-GameConstants.EMPOWER_TAX)/3 > Info.conviction && !ECInfo.exterminate_flag) {
			for (Direction dir : Direction.cardinalDirections()) {
	            if (rc.canBuildRobot(RobotType.POLITICIAN, dir, Info.conviction)) {
					Action.buildTargetter(dir, Info.conviction, Info.loc); return;
	            }
	        }
		}
		// self-empower if x4 on cardinal directions
		if ((Info.conviction*rc.getEmpowerFactor(Info.friendly, 11)-GameConstants.EMPOWER_TAX)/4 > Info.conviction && !ECInfo.exterminate_flag) {
			for (Direction dir : Direction.cardinalDirections()) {
	            if (rc.canBuildRobot(RobotType.POLITICIAN, dir, Info.conviction)) {
					Action.buildTargetter(dir, Info.conviction, Info.loc); return;
	            }
	        }
		}
		// self-empower if x8 on diagonal directions
		if ((Info.conviction*rc.getEmpowerFactor(Info.friendly, 11)-GameConstants.EMPOWER_TAX)/8 > Info.conviction && !ECInfo.exterminate_flag) {
			for (Direction dir : Math2.UNIT_DIRECTIONS) {
	            if (rc.canBuildRobot(RobotType.POLITICIAN, dir, Info.conviction)) {
					Action.buildTargetter(dir, Info.conviction, Info.loc); return;
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
		if (ECInfo.max_safe_build_limit<0) {  // if under conversion threat, try to build as close as possible to the largest nearby enemy politician
			RobotInfo largest_enemy_politician = null;
			int largest_conviction = Integer.MIN_VALUE;
			for (int i=Info.n_enemy_politicians; --i>=0;) {
				if (Info.enemy_politicians[i].conviction>largest_conviction) {
					largest_enemy_politician = Info.enemy_politicians[i];
					largest_conviction = largest_enemy_politician.conviction;
				}
			}
			int closest_distance_squared = Integer.MAX_VALUE;
			for (Direction dir:Math2.UNIT_DIRECTIONS) {
				if (rc.canBuildRobot(RobotType.POLITICIAN, dir, 1)) {
	                n_build_directions++;
	                if (Info.loc.add(dir).distanceSquaredTo(largest_enemy_politician.location)<closest_distance_squared) {
		                build_direction = dir;
	                	closest_distance_squared = Info.loc.add(dir).distanceSquaredTo(largest_enemy_politician.location);
	                }
	            }
	        }
		}
		else {  // if not under conversion threat, try to spread units
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
		}
		if (n_build_directions==0) {return;}
		if (ECInfo.guard_flag && !ECInfo.enough_guards) {  // guard slanderers at all costs
			int test_conviction = Math.max(14, ECInfo.sampled_muckraker_influence+GameConstants.EMPOWER_TAX+2);
			if (test_conviction < ECInfo.max_safe_build_limit) {
				if (rc.canBuildRobot(RobotType.POLITICIAN, build_direction, test_conviction)) {
					rc.setIndicatorDot(Info.loc.add(build_direction), 255, 255, 0);
					Action.buildRobot(RobotType.POLITICIAN, build_direction, test_conviction); return;
				}
			}
		}
		int test_conviction = Math.max(70, (int)(1.1*ECInfo.weakest_ec_influence)+GameConstants.EMPOWER_TAX);
		if (ECInfo.weakest_ec_loc!=null && ECInfo.max_safe_build_limit > test_conviction) {  // send monster sized politicians to finish off a weak EC
			if (rc.canBuildRobot(RobotType.POLITICIAN, build_direction, test_conviction)) {
				Action.buildTargetter(build_direction, test_conviction, ECInfo.weakest_ec_loc); return;
			}
		}
		test_conviction = Math.max(300, Info.conviction/2);
		if (ECInfo.weakest_ec_loc!=null && ECInfo.max_safe_build_limit > test_conviction && ECInfo.target_all_ecs_flag) {  // send monster sized politicians to finish off a weak EC
			if (rc.canBuildRobot(RobotType.POLITICIAN, build_direction, test_conviction)) {
				Action.buildTargetter(build_direction, test_conviction, ECInfo.weakest_ec_loc); return;
			}
		}
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
			if (best_direction!=null) {
				if (conviction < ECInfo.max_safe_build_limit) {
					Action.buildRobot(RobotType.POLITICIAN, best_direction, conviction); return;
				}
				else {
					ECInfo.bid_amount++; return;  // be the team's voter if trapped
				}
			}
		}
		if (ECInfo.exterminate_flag) {  // perform final extermination
			test_conviction = Math.max(20, (Info.conviction+Math.max(0, Exterminator.EXTERMINATE_FINISH_MONEY_TIME-Info.round_num)*ECInfo.total_income)
										  /(int)Math.max(1, (Exterminator.EXTERMINATE_FINISH_MONEY_TIME-Info.round_num)/2*rc.sensePassability(Info.loc)));
			if (test_conviction > 0) {
				if (rc.canBuildRobot(RobotType.POLITICIAN, build_direction, test_conviction)) {
					Action.buildRobot(RobotType.POLITICIAN, build_direction, test_conviction); return;
				}
			}
			return;
		}
		if (ECInfo.desired_guard_flag && ECInfo.enough_guards) {  // make slanderers if conviction is abundant
			System.out.println(ECInfo.max_safe_build_limit);
			test_conviction = Math2.embezzle_floor(Math.min(949, ECInfo.max_safe_build_limit));
			System.out.println(test_conviction);
			if (test_conviction > 0 && ECInfo.max_safe_build_limit > test_conviction) {
				if (rc.canBuildRobot(RobotType.SLANDERER, build_direction, test_conviction)) {
					Action.buildRobot(RobotType.SLANDERER, build_direction, test_conviction); return;
				}
			}
		}
		if (rc.canBuildRobot(RobotType.MUCKRAKER, build_direction, 1) && !ECInfo.majority_crowded) {
			Action.buildRobot(RobotType.MUCKRAKER, build_direction, 1); return;
		}
	}
	
	public static void pause() {
		
	}

}
