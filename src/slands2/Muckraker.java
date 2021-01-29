package slands2;
import battlecode.common.*;

public class Muckraker {
	public static RobotController rc;
	
	public static void act() throws GameActionException {
		RobotInfo closest_enemy_ec = Info.closest_robot(Info.enemy, RobotType.ENLIGHTENMENT_CENTER);
		RobotInfo closest_neutral_ec = Info.closest_robot(Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER);
		RobotInfo closest_enemy_slanderer = Info.closest_robot(Info.enemy, RobotType.SLANDERER);
    	if (closest_enemy_slanderer!=null) {
    		Role.attach_to_relay_chain();
    		if (!Info.exterminate) {RelayChain.lock_target(Info.closest_robot(Info.enemy, RobotType.SLANDERER).location);}
    		if (Info.loc.distanceSquaredTo(closest_enemy_slanderer.location)<=RobotType.MUCKRAKER.actionRadiusSquared) {
    			Action.expose(closest_enemy_slanderer);
    		}
			Pathing.target(closest_enemy_slanderer.location, new boolean[3][3], 1);
			return;
    	}
		else if ((closest_enemy_ec!=null || closest_neutral_ec!=null) && (!Info.everything_buried || Role.is_burier) && !Info.exterminate) {
			Role.bury();
		}
		else {
			Role.attach_to_relay_chain();
		}
		double best_gains = Integer.MIN_VALUE;
		RobotInfo largest_nearby_politician = null;
		int largest_conviction = Integer.MIN_VALUE;
		for (int i=Info.n_friendly_politicians; --i>=0;) {
			if (Info.friendly_politicians[i].conviction>largest_conviction && (!Role.is_burier || rc.getFlag(Info.friendly_politicians[i].ID)>>20==1)) {
				largest_nearby_politician = Info.friendly_politicians[i];
				largest_conviction = largest_nearby_politician.conviction;
			}
		}
		for (int i=Info.n_enemy_politicians; --i>=0;) {
			if (Info.enemy_politicians[i].conviction>largest_conviction) {
				largest_nearby_politician = Info.enemy_politicians[i];
				largest_conviction = largest_nearby_politician.conviction;
			}
		}
		double[][] move_gains = new double[3][3];
		boolean[][] negative_gain_tiles = new boolean[3][3];
		if (largest_nearby_politician!=null) {
			move_gains = CombatInfo.compute_move_gains(largest_nearby_politician);
		}
		else {
			best_gains = Math.max(best_gains, 0);
		}
		for (Direction dir:Direction.allDirections()) {
			if (dir==Direction.CENTER || rc.canMove(dir)) {
				best_gains = Math.max(best_gains, move_gains[dir.dx+1][dir.dy+1]);
				negative_gain_tiles[dir.dx+1][dir.dy+1] = move_gains[dir.dx+1][dir.dy+1]<0;
			}
		}
		if (best_gains>Double.MIN_VALUE || best_gains<-Double.MIN_VALUE) {
			for (Direction dir:Direction.allDirections()) {
				if (dir==Direction.CENTER || rc.canMove(dir)) {
					if (best_gains==move_gains[dir.dx+1][dir.dy+1]) {
						Action.move(dir); return;
					}
				}
			}
		}
		else if (Role.is_burier) {
			Burier.bury(negative_gain_tiles); return;
		}
		else {
			Role.attach_to_relay_chain();
			if (Info.n_enemy_slanderers>0 && !Info.exterminate) {RelayChain.lock_target(Info.closest_robot(Info.enemy, RobotType.SLANDERER).location);}
			else if (!Info.everything_buried && Info.n_buriers>0 && !Info.exterminate) {RelayChain.lock_target(Info.weak_burier.location);}
			if (RelayChain.extend(negative_gain_tiles)) {return;}
			return;
		}
		throw new GameActionException(null, "Best gain action computed but not found!");
	}
	
	public static void pause() {
		
	}

}
