import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Data Access Object: all CRUD operations on the members table. */
public class MemberDAO {

    // CREATE
    public void addMember(Member m) throws SQLException {
        String sql = "INSERT INTO members (full_name, national_id, phone, county, "
                   + "monthly_contribution, join_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getFullName());
            ps.setString(2, m.getNationalId());
            ps.setString(3, m.getPhone());
            ps.setString(4, m.getCounty());
            ps.setDouble(5, m.getMonthlyContribution());
            ps.setDate(6, Date.valueOf(m.getJoinDate()));
            ps.executeUpdate();
        }
    }

    // READ
    public List<Member> getAllMembers() throws SQLException {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY member_id";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Member(
                        rs.getInt("member_id"),
                        rs.getString("full_name"),
                        rs.getString("national_id"),
                        rs.getString("phone"),
                        rs.getString("county"),
                        rs.getDouble("monthly_contribution"),
                        rs.getDate("join_date").toLocalDate()));
            }
        }
        return list;
    }

    // UPDATE
    public void updateMember(Member m) throws SQLException {
        String sql = "UPDATE members SET full_name=?, national_id=?, phone=?, county=?, "
                   + "monthly_contribution=?, join_date=? WHERE member_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getFullName());
            ps.setString(2, m.getNationalId());
            ps.setString(3, m.getPhone());
            ps.setString(4, m.getCounty());
            ps.setDouble(5, m.getMonthlyContribution());
            ps.setDate(6, Date.valueOf(m.getJoinDate()));
            ps.setInt(7, m.getMemberId());
            ps.executeUpdate();
        }
    }

    // DELETE
    public void deleteMember(int memberId) throws SQLException {
        String sql = "DELETE FROM members WHERE member_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.executeUpdate();
        }
    }
}
