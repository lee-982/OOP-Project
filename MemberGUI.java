import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Swing GUI: input form, CRUD buttons and a JTable listing all members. */
public class MemberGUI extends JFrame {

    private final MemberDAO dao = new MemberDAO();

    private final JTextField nameField = new JTextField(15);
    private final JTextField idField = new JTextField(15);
    private final JTextField phoneField = new JTextField(15);
    private final JTextField countyField = new JTextField(15);
    private final JTextField contribField = new JTextField(15);
    private final JTextField dateField = new JTextField(15);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Full Name", "National ID", "Phone", "County",
                         "Monthly Contribution (KES)", "Join Date"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; } // read-only table
    };
    private final JTable table = new JTable(tableModel);

    private int selectedMemberId = -1; // -1 means no row selected

    public MemberGUI() {
        setTitle("Chama Member Registry");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildFormPanel(), BorderLayout.WEST);
        add(new JScrollPane(table), BorderLayout.CENTER);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelectedRow();
        });

        loadTable();
    }

    // ---------- Layout ----------
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Full Name:"));            panel.add(nameField);
        panel.add(new JLabel("National ID:"));          panel.add(idField);
        panel.add(new JLabel("Phone (07XXXXXXXX):"));   panel.add(phoneField);
        panel.add(new JLabel("County:"));               panel.add(countyField);
        panel.add(new JLabel("Monthly Contribution (KES):")); panel.add(contribField);
        panel.add(new JLabel("Join Date (yyyy-MM-dd):")); panel.add(dateField);

        JButton addBtn = new JButton("Add Member");
        JButton updateBtn = new JButton("Update Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton clearBtn = new JButton("Clear Form");

        addBtn.addActionListener(e -> addMember());
        updateBtn.addActionListener(e -> updateMember());
        deleteBtn.addActionListener(e -> deleteMember());
        clearBtn.addActionListener(e -> clearForm());

        panel.add(addBtn);
        panel.add(updateBtn);
        panel.add(deleteBtn);
        panel.add(clearBtn);
        return panel;
    }

    // ---------- CRUD actions ----------
    private void addMember() {
        Member m = readAndValidateForm();
        if (m == null) return;
        try {
            dao.addMember(m);
            JOptionPane.showMessageDialog(this, "Member added successfully.");
            loadTable();
            clearForm();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void updateMember() {
        if (selectedMemberId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a member from the table first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Member m = readAndValidateForm();
        if (m == null) return;
        m.setMemberId(selectedMemberId);
        try {
            dao.updateMember(m);
            JOptionPane.showMessageDialog(this, "Member updated successfully.");
            loadTable();
            clearForm();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void deleteMember() {
        if (selectedMemberId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a member from the table first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Confirmation before deleting
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this member?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            dao.deleteMember(selectedMemberId);
            loadTable();
            clearForm();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // ---------- Helpers ----------
    /** Reloads every row from the database into the JTable. */
    private void loadTable() {
        try {
            tableModel.setRowCount(0);
            List<Member> members = dao.getAllMembers();
            for (Member m : members) {
                tableModel.addRow(new Object[]{m.getMemberId(), m.getFullName(), m.getNationalId(),
                        m.getPhone(), m.getCounty(), m.getMonthlyContribution(), m.getJoinDate()});
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void fillFormFromSelectedRow() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        selectedMemberId = (int) tableModel.getValueAt(row, 0);
        nameField.setText(tableModel.getValueAt(row, 1).toString());
        idField.setText(tableModel.getValueAt(row, 2).toString());
        phoneField.setText(tableModel.getValueAt(row, 3).toString());
        countyField.setText(tableModel.getValueAt(row, 4).toString());
        contribField.setText(tableModel.getValueAt(row, 5).toString());
        dateField.setText(tableModel.getValueAt(row, 6).toString());
    }

    private void clearForm() {
        nameField.setText(""); idField.setText(""); phoneField.setText("");
        countyField.setText(""); contribField.setText(""); dateField.setText("");
        selectedMemberId = -1;
        table.clearSelection();
    }

    /** Validates all inputs. Returns a Member, or null (after showing a message) if invalid. */
    private Member readAndValidateForm() {
        String name = nameField.getText().trim();
        String nationalId = idField.getText().trim();
        String phone = phoneField.getText().trim();
        String county = countyField.getText().trim();
        String contribText = contribField.getText().trim();
        String dateText = dateField.getText().trim();

        // 1. No empty fields
        if (name.isEmpty() || nationalId.isEmpty() || phone.isEmpty()
                || county.isEmpty() || contribText.isEmpty() || dateText.isEmpty()) {
            showError("All fields are required.");
            return null;
        }
        // 2. National ID: 7 or 8 digits
        if (!nationalId.matches("\\d{7,8}")) {
            showError("National ID must be 7 or 8 digits.");
            return null;
        }
        // 3. Kenyan phone number: 07XXXXXXXX, 01XXXXXXXX or +2547XXXXXXXX
        if (!phone.matches("^(\\+254|0)[17]\\d{8}$")) {
            showError("Enter a valid Kenyan phone number, e.g. 0712345678.");
            return null;
        }
        // 4. Contribution must be a positive number
        double contribution;
        try {
            contribution = Double.parseDouble(contribText);
            if (contribution <= 0) {
                showError("Monthly contribution must be greater than 0.");
                return null;
            }
        } catch (NumberFormatException ex) {
            showError("Monthly contribution must be a number, e.g. 1500.");
            return null;
        }
        // 5. Date must be in yyyy-MM-dd format
        LocalDate joinDate;
        try {
            joinDate = LocalDate.parse(dateText);
        } catch (DateTimeParseException ex) {
            showError("Join date must be in the format yyyy-MM-dd, e.g. 2024-05-20.");
            return null;
        }
        return new Member(name, nationalId, phone, county, contribution, joinDate);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }

    private void showDbError(SQLException ex) {
        // Error code 1062 = duplicate entry (National ID is UNIQUE)
        if (ex.getErrorCode() == 1062) {
            showError("A member with this National ID already exists.");
        } else {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
