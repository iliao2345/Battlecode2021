package slands3;
import battlecode.common.*;

public class Action {
	public static RobotController rc;
	public static boolean acted;

	public static void move(Direction dir) throws GameActionException {
		if (dir!=Direction.CENTER) { 
			Info.last_move_direction = dir;
			rc.move(dir);
			acted = true;
		}
	}
	public static void expose(RobotInfo robot) throws GameActionException {
		rc.expose(robot.location);
		acted = true;
	}
	public static void buildRobot(RobotType type, Direction dir, int conviction) throws GameActionException {
		rc.buildRobot(type, dir, conviction);
		acted = true;
		if (type==RobotType.SLANDERER) {
			ECInfo.embezzler_ids[Info.round_num%GameConstants.EMBEZZLE_NUM_ROUNDS] = rc.senseRobotAtLocation(Info.loc.add(dir)).ID;
			ECInfo.embezzler_incomes[Info.round_num%GameConstants.EMBEZZLE_NUM_ROUNDS] = Math2.get_embezzle_income(conviction);
			ECInfo.slanderer_ids = new IntCycler(rc.senseRobotAtLocation(Info.loc.add(dir)).ID, ECInfo.slanderer_ids);
			ECInfo.n_slanderer_ids++;
			ECInfo.last_slanderer_built_round = Info.round_num;
			if (conviction>949) {ECInfo.last_repowerer_built_round = Info.round_num;}
		}
		else {
			ECInfo.unclassified_ids = new IntCycler(rc.senseRobotAtLocation(Info.loc.add(dir)).ID, ECInfo.unclassified_ids);
			ECInfo.n_unclassified_ids++;
		}
		ECInfo.last_build_direction = dir;
	}
	
	public static void buildTargetter(Direction dir, int conviction, MapLocation target_loc) throws GameActionException {
		buildRobot(RobotType.POLITICIAN, dir, conviction);
		int index = (target_loc.x%128)/2*64 + (target_loc.y%128)/2;
		ECInfo.targetter_exists[index] = true;
		ECInfo.targetter_ids = new IntCycler(rc.senseRobotAtLocation(Info.loc.add(dir)).ID, ECInfo.targetter_ids);
		ECInfo.targetter_indices = new IntCycler(index, ECInfo.targetter_indices);
		ECInfo.n_targetter_ids++;
		ECInfo.info_transfer_timer = 2;
		ECInfo.targetted_loc = target_loc;
		ECInfo.build_role = 0;
	}
	
	public static void buildGuard(Direction dir, int conviction, MapLocation target_loc, int id_127) throws GameActionException {
		rc.buildRobot(RobotType.POLITICIAN, dir, conviction);
		acted = true;
		ECInfo.last_build_direction = dir;
		ECInfo.targetting_guard_ids[id_127] = rc.senseRobotAtLocation(Info.loc.add(dir)).ID;
		ECInfo.targetting_guard_signal_rounds[id_127] = Info.round_num;
		ECInfo.guard_ids = new IntCycler(rc.senseRobotAtLocation(Info.loc.add(dir)).ID, ECInfo.guard_ids);
		ECInfo.guard_targets_128 = new IntCycler(id_127, ECInfo.guard_targets_128);
		ECInfo.n_guard_ids++;
		ECInfo.info_transfer_timer = 2;
		ECInfo.targetted_loc = target_loc;
		ECInfo.build_role = 1;
		rc.setIndicatorLine(Info.loc, target_loc, 255, 0, 0);
	}
}
