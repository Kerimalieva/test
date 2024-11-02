import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CustomerManagementApp extends JFrame {
    private JTextField nameField, emailField, phoneField;

    private JButton addButton, viewButton, updateButton, deleteButton;

    private JTable customerTable;
    int selectedCustomerId;

    public CustomerManagementApp() {
        setTitle("Customer Management App");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));

        panel.add(new JLabel("Имя: "));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Email: "));
        emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Телефон: "));
        phoneField = new JTextField();
        panel.add(phoneField);

        addButton = new JButton("Добавить");
        viewButton = new JButton("Просмотр");
        updateButton = new JButton("Обновить");
        deleteButton = new JButton("Удалить");

        panel.add(addButton);
        panel.add(viewButton);
        panel.add(updateButton);
        panel.add(deleteButton);

        add(panel);

        // Обработчик для кнопки добавления клиента
        addButton.addActionListener(e -> addCustomer());

        // Обработчик для кнопки просмотра клиентов
        viewButton.addActionListener(e -> viewCustomer());

        // Обработчик для кнопки обновления клиента
        updateButton.addActionListener(e -> openUpdateDialog());

        deleteButton.addActionListener(e -> deleteCustomer());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CustomerManagementApp app = new CustomerManagementApp();
            app.setVisible(true);
        });
    }

    private void addCustomer() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        String sql = "INSERT INTO customers (name, email, phone) VALUES (?, ?, ?);";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Клиент добавлен успешно!");

            // Очищаем поля ввода
            nameField.setText("");
            emailField.setText("");
            phoneField.setText("");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при добавлении клиента: " + ex.getMessage());
        }
    }

    private void viewCustomer() {
        String[] columnNames = {"ID", "Имя", "Email", "Телефон"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        String sql = "SELECT * FROM customers";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");

                Object[] row = {id, name, email, phone};
                tableModel.addRow(row);
            }

            customerTable = new JTable(tableModel);
            customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            customerTable.getSelectionModel().addListSelectionListener(e -> {
                int selectedRow = customerTable.getSelectedRow();
                if (selectedRow != -1) {
                    selectedCustomerId = (int) tableModel.getValueAt(selectedRow, 0);
                    nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    emailField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    phoneField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                }
            });

            JOptionPane.showMessageDialog(this, new JScrollPane(customerTable), "Список клиентов", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при просмотре клиентов: " + e);
        }
    }

    private void openUpdateDialog() {
        if (selectedCustomerId == 0) {
            JOptionPane.showMessageDialog(this, "Выберите клиента для обновления");
            return;
        }

        // Создаем диалоговое окно обновления
        JDialog updateDialog = new JDialog(this, "Обновить клиента", true);
        updateDialog.setLayout(new GridLayout(4, 2));
        updateDialog.setSize(300, 200);

        // Поля для ввода данных
        JTextField updateNameField = new JTextField(nameField.getText());
        JTextField updateEmailField = new JTextField(emailField.getText());
        JTextField updatePhoneField = new JTextField(phoneField.getText());

        updateDialog.add(new JLabel("Имя: "));
        updateDialog.add(updateNameField);
        updateDialog.add(new JLabel("Email: "));
        updateDialog.add(updateEmailField);
        updateDialog.add(new JLabel("Телефон: "));
        updateDialog.add(updatePhoneField);

        JButton saveButton = new JButton("Сохранить");
        updateDialog.add(saveButton);

        saveButton.addActionListener(e -> {
            // Вызов метода для обновления клиента
            updateCustomer(updateNameField.getText(), updateEmailField.getText(), updatePhoneField.getText());
            updateDialog.dispose(); // Закрываем диалоговое окно
        });

        updateDialog.setLocationRelativeTo(this);
        updateDialog.setVisible(true);
    }

    private void updateCustomer(String name, String email, String phone) {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setInt(4, selectedCustomerId); // Используем сохраненный ID

            int rowsAffected = stmt.executeUpdate(); // Выполняем обновление

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Данные клиента обновлены успешно!");
            } else {
                JOptionPane.showMessageDialog(this, "Клиент с таким ID не найден.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при обновлении клиента: " + ex.getMessage());
        }
    }

    public void deleteCustomer() {
        if (selectedCustomerId == 0) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, выберите клиента для удаления.");
            return; // Если клиент не выбран, выходим
        }

        String sql = "DELETE FROM customers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selectedCustomerId); // Используем сохраненный ID
            int rowsAffected = stmt.executeUpdate(); // Выполняем удаление

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Клиент удален успешно!");
                selectedCustomerId = 0; // Сбрасываем выбранного клиента
                // Обновите таблицу после удаления
                viewCustomer();
            } else {
                JOptionPane.showMessageDialog(this, "Клиент с таким ID не найден.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при удалении клиента: " + ex.getMessage());
        }
    }
}
