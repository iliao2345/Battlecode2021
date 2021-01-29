package last_working_bot;
import battlecode.common.*;

public class EnlightenmentCenter {
	public static RobotController rc;
	
	public static void act() throws GameActionException {
		Direction build_direction = null;
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
	            if (rc.canBuildRobot(RobotType.POLITICIAN, dir, 1)) {
	                build_direction = dir;
	                break;
	            }
	        }
		}
		if (build_direction==null) {return;}
		if (ECInfo.guard_flag && !ECInfo.enough_guards) {  // guard slanderers at all costs
			int test_conviction = 20;
			if (test_conviction < ECInfo.max_safe_build_limit) {
				if (rc.canBuildRobot(RobotType.POLITICIAN, build_direction, test_conviction)) {
					Action.buildRobot(RobotType.POLITICIAN, build_direction, test_conviction); return;
				}
			}
		}
		int test_conviction = Math.max(50, ECInfo.weakest_ec_influence)+GameConstants.EMPOWER_TAX+1;
		if (ECInfo.weakest_ec_loc!=null && ECInfo.max_safe_build_limit > test_conviction && (Info.n_adjacent_robots<Info.n_adjacent_tiles_on_map-1 || Info.n_adjacent_tiles_on_map-ECInfo.open_spawn_tiles<3)) {  // send monster sized politicians to finish off a weak EC
			Direction build_direction2 = null;
			int closest_distance_squared = Integer.MAX_VALUE;
			for (Direction dir:Math2.UNIT_DIRECTIONS) {
				if (rc.canBuildRobot(RobotType.POLITICIAN, dir, 1)) {
	                if (Info.loc.add(dir).distanceSquaredTo(ECInfo.weakest_ec_loc)<closest_distance_squared) {
	                	build_direction2 = dir;
	                	closest_distance_squared = Info.loc.add(dir).distanceSquaredTo(ECInfo.weakest_ec_loc);
	                }
	            }
	        }
			if (rc.canBuildRobot(RobotType.POLITICIAN, build_direction2, test_conviction)) {
				Action.buildTargetter(build_direction2, test_conviction, ECInfo.weakest_ec_loc); return;
			}
		}
		test_conviction = Math.max(300, Info.conviction/2);
		if (ECInfo.weakest_ec_loc!=null && ECInfo.max_safe_build_limit > test_conviction && ECInfo.target_all_ecs_flag && (Info.n_adjacent_robots<Info.n_adjacent_tiles_on_map-1 || Info.n_adjacent_tiles_on_map-ECInfo.open_spawn_tiles<3)) {  // send monster sized politicians to finish off a weak EC
			if (rc.canBuildRobot(RobotType.POLITICIAN, build_direction, test_conviction)) {
				Action.buildTargetter(build_direction, test_conviction, ECInfo.weakest_ec_loc); return;
			}
		}
		if (ECInfo.open_spawn_tiles<ECInfo.open_spawn_tiles_required || Info.n_adjacent_robots>=Info.n_adjacent_tiles_on_map-1 && Info.n_adjacent_tiles_on_map-ECInfo.open_spawn_tiles>=3) {  // uncover spawn tiles if blocked by enemy
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
			test_conviction = Math2.embezzle_floor(Math.min(949, ECInfo.max_safe_build_limit));
			if (test_conviction > 0 && ECInfo.max_safe_build_limit > test_conviction) {
				if (rc.canBuildRobot(RobotType.SLANDERER, build_direction, test_conviction)) {
					Action.buildRobot(RobotType.SLANDERER, build_direction, test_conviction); return;
				}
			}
		}
		if (Info.round_num<10 && rc.canBuildRobot(RobotType.POLITICIAN, build_direction, 1)) {
			Action.buildRobot(RobotType.POLITICIAN, build_direction, 1); return;
		}
		if (rc.canBuildRobot(RobotType.MUCKRAKER, build_direction, 1) && !ECInfo.map_controlled) {
			Action.buildRobot(RobotType.MUCKRAKER, build_direction, 1); return;
		}
	}
	
	public static void pause() {
		
	}
	
	public static void bid() throws GameActionException {
		Info.conviction = rc.getConviction();
		int bids_required = 751-rc.getTeamVotes();
		int rounds_left = 1500-Info.round_num;
		if (bids_required>0 && Info.round_num>ECInfo.BID_START_TURN) {
			if (Info.round_num>Exterminator.EXTERMINATE_FINISH_MONEY_TIME) {
				int bid = Info.conviction/rounds_left+ECInfo.passive_income;
				if (rc.canBid(bid)) {rc.bid(bid);}
			}
			else {
				if (Info.team_votes>Info.last_team_votes || ECInfo.last_bid_1) {ECInfo.bid_power--;}
				else {ECInfo.bid_power += 1.5;}
				ECInfo.bid_power = Math.max(1, ECInfo.bid_power);
				int bid_amount = (int) (5*(Math.exp(ECInfo.bid_power/5.0)-1));
				if (ECInfo.map_controlled) {bid_amount = 3*Info.conviction/Math.min(rounds_left+5, bids_required+5)+ECInfo.passive_income+ECInfo.embezzle_income/5 - (int)(Math.log(Math.random()))+1;}
				if (bid_amount>ECInfo.max_safe_build_limit || Info.conviction<300) {
					if (rc.canBid(1)) {rc.bid(1); ECInfo.last_bid_1 = true;}
				}
				else if (rc.canBid(bid_amount) && bid_amount<ECInfo.max_safe_build_limit) {
					ECInfo.last_bid_1 = false;
					rc.bid(bid_amount);
				}
			}
		}
	}

}
