package PlayHelper;

/**
 * This class is helping to record the move information obtained from server.
 * @author Ruotong Wu
 * @version 1.2.0
 */
public class Move {

	private int teamId;     //The value of team id. Every move has a team id to indicate who made this move.
    private int gameId;     //The value of game id. Every move has a game id to indicate which game this move belongs to.
    private int moveId;     //The value fo move id. Every move assigned a move id to indicate the identity of the move
    private int x;          //Represent the row number of the move
    private int y;          //Represent the column number of the move


    /**
     * return the value of team id as an integer
     * @return team id
     */
    public int GetTeamID(){
        return this.teamId;
    }

    /**
     * return the value of game id as an integer
     * @return game id
     */
    public int GetGameID(){
        return this.gameId;
    }

    /**
     * return the value of move id as an integer
     * @return move id
     */
    public int GetMoveID(){
        return this.moveId;
    }

    /**
     * return the value of the row number as an integer
     * @return row number
     */
    public int GetX(){
        return this.x;
    }

    /**
     * return the value of the column number as an integer
     * @return column number
     */
    public int GetY(){
        return this.y;
    }

    /**
     * Initializing the Move instant with team id, game id, move id and the position
     * @param teamId the value of team id
     * @param gameId the value of game id
     * @param moveId the value of move id
     * @param x the position's row number
     * @param y the position's column number
     */
    public Move(int teamId,int gameId, int moveId,int x, int y) {
		this.teamId=teamId;
		this.gameId=gameId;
		this.moveId=moveId;
		this.x=x;
		this.y=y;
	}

    /**
     * Initializing the Move instant with team id, game id, move id and the position
     * @param teamId the value of team id
     * @param gameId the value of game id
     * @param moveId the value of move id
     * @param position the position of the move as an string, the format should be "x,y"
     * @throws Exception the format of the position could incorrect
     */
    public Move(int teamId,int gameId,int moveId,String position) throws Exception {
        this.teamId=teamId;
        this.gameId=gameId;
        this.moveId=moveId;
        SetPosition(position);
    }

    /**
	 * Set the Move Position with a string having format as "x,y" which x and y both are integer.
	 * @param positionString a string like "x,y" which contains the position information.
	 * @throws Exception if the format of the string is not "x,y", then throw a exception
	 */
    private void SetPosition(String positionString) throws Exception {
        String[] s=positionString.split(","); //split the string by ","
		try {
			this.x=Integer.parseInt(s[0]);
			this.y=Integer.parseInt(s[1]);
		}catch(Exception e) {
            //if the string is not split by comma,or the position value cannot be converted to integer, then throw this Exception.
			throw new Exception("The format of position is incorrect, please check it.");
		}
	}
}
