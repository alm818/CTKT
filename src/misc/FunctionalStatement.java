package misc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface FunctionalStatement {
	public void insert(PreparedStatement stm, Object[] objects) throws SQLException;
}
