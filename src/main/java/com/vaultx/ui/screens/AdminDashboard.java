package com.vaultx.ui.screens;

import com.vaultx.models.*;
import com.vaultx.services.BankingService;
import com.vaultx.ui.components.ModernButton;
import com.vaultx.ui.components.ModernTextField;
import com.vaultx.ui.components.Toast;
import com.vaultx.utils.ThemeManager;
import com.vaultx.utils.ValidationUtils;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class AdminDashboard extends JFrame {
    private final BankingService bankingService = new BankingService();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);

    // UI References for updates
    private final JLabel customerCount = new JLabel("0");
    private final JLabel activeAccounts = new JLabel("0");
    private final JLabel bankBalance = new JLabel("Rs. 0.00");

    private final JTable userTable = new JTable();
    private final JTable accountTable = new JTable();
    private final JTable transactionTable = new JTable();
    private final JTable auditTable = new JTable();
    private final JTable ticketTable = new JTable();
    private final DefaultCategoryDataset volumeDataset = new DefaultCategoryDataset();
    private JButton activeMenuButton = null;

    public AdminDashboard() {
        setTitle("VaultX Master Admin - Control Center");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 850);
        setLocationRelativeTo(null);

        JPanel mainContainer = new JPanel(new MigLayout("fill, insets 0", "[260!]0[fill]", "fill"));
        mainContainer.add(createSidebar(), "growy");

        setupContentCards();
        mainContainer.add(contentCards, "grow");

        add(mainContainer);
        refreshAllData();
    }

    private void setupContentCards() {
        contentCards.add(createOverviewPanel(), "OVERVIEW");
        contentCards.add(createTablePanel("Customer Directory", userTable,
                new String[] { "Deactivate", "Activate", "Reset Pwd", "Purge User" }, e -> handleUserAction(e)),
                "USERS");
        contentCards.add(createTablePanel("Account Inventory", accountTable,
                new String[] { "Freeze", "Unfreeze", "Close Acc" }, e -> handleAccountAction(e)), "ACCOUNTS");
        contentCards.add(createTablePanel("Transaction Review & Monitoring", transactionTable,
                new String[] { "Approve Deposit", "Reverse" }, e -> handleTxAction(e)), "MONITOR");
        contentCards.add(createSimpleTablePanel("System Audit Logs", auditTable), "AUDIT");
        contentCards.add(createTablePanel("Support Tickets", ticketTable, new String[] { "Resolve Ticket" },
                e -> handleTicketAction()), "SUPPORT");
        contentCards.add(createSettingsPanel(), "SETTINGS");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new MigLayout("fill, insets 25", "fill", "[]40[]15[]15[]15[]15[]15[]15[]push[]"));
        sidebar.setBackground(ThemeManager.SURFACE);

        JLabel logo = new JLabel("VaultX Admin");
        logo.setFont(new Font("Inter", Font.BOLD, 22));
        logo.setForeground(ThemeManager.PRIMARY);
        sidebar.add(logo, "wrap, gapbottom 30");

        String[][] menu = {
                { "Master Dashboard", "OVERVIEW" },
                { "Users & Roles", "USERS" },
                { "Account Control", "ACCOUNTS" },
                { "Transaction Feed", "MONITOR" },
                { "Security Audit", "AUDIT" },
                { "Support Center", "SUPPORT" },
                { "System Config", "SETTINGS" }
        };
        for (String[] item : menu) {
            JButton btn = new JButton(item[0]);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setFont(new Font("Inter", Font.PLAIN, 15));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setForeground(ThemeManager.TEXT_SECONDARY);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                cardLayout.show(contentCards, item[1]);
                setActiveMenu(btn);
            });
            sidebar.add(btn, "wrap, height 40!");

            // Set Overview as default active
            if ("OVERVIEW".equals(item[1])) {
                activeMenuButton = btn;
                btn.setFont(new Font("Inter", Font.BOLD, 15));
                btn.setForeground(ThemeManager.PRIMARY);
                btn.setOpaque(true);
                btn.setBackground(new Color(ThemeManager.PRIMARY.getRed(), ThemeManager.PRIMARY.getGreen(),
                        ThemeManager.PRIMARY.getBlue(), 30));
            }
        }

        ModernButton logout = new ModernButton("Secure Logout", ModernButton.Type.SECONDARY);
        logout.addActionListener(e -> {
            new LoginScreen().setVisible(true);
            this.dispose();
        });

        ModernButton shutdown = new ModernButton("⏻ Shutdown", ModernButton.Type.SECONDARY);
        shutdown.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit VaultX?", "Confirm Exit",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION)
                System.exit(0);
        });

        sidebar.add(logout, "south, height 45!, gapbottom 10");
        sidebar.add(shutdown, "south, height 45!");

        return sidebar;
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 40", "fill", "[]20[]20[grow]"));
        panel.setBackground(ThemeManager.BACKGROUND);

        JLabel header = new JLabel("Bank Performance Overview");
        header.setFont(new Font("Inter", Font.BOLD, 28));
        header.setForeground(ThemeManager.TEXT_PRIMARY);

        JPanel statsRow = new JPanel(new MigLayout("fill, insets 0", "fill", "fill"));
        statsRow.setOpaque(false);
        statsRow.add(createStatCard("Total Customers", customerCount), "grow, gapright 20");
        statsRow.add(createStatCard("Active Accounts", activeAccounts), "grow, gapright 20");
        statsRow.add(createStatCard("Total Bank Assets", bankBalance), "grow");

        panel.add(header, "wrap");
        panel.add(statsRow, "growx, wrap");

        JPanel chartContainer = new JPanel(new MigLayout("fill, insets 0", "fill", "fill"));
        chartContainer.setOpaque(false);
        chartContainer.add(createChartArea(), "grow");

        panel.add(chartContainer, "grow");
        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new MigLayout("fill, insets 25", "fill", "[]10[]"));
        ThemeManager.applyCardStyle(card);
        JLabel t = new JLabel(title);
        t.setForeground(ThemeManager.TEXT_SECONDARY);
        t.setFont(new Font("Inter", Font.PLAIN, 14));
        valueLabel.setFont(new Font("Inter", Font.BOLD, 24));
        valueLabel.setForeground(ThemeManager.PRIMARY);
        card.add(t, "wrap");
        card.add(valueLabel);
        return card;
    }

    private JPanel createTablePanel(String titleStr, JTable table, String[] actions, java.awt.event.ActionListener l) {
        JPanel panel = new JPanel(new MigLayout("fill, insets 35", "fill", "[]20[grow]20[]"));
        panel.setBackground(ThemeManager.BACKGROUND);

        JLabel title = new JLabel(titleStr);
        title.setFont(new Font("Inter", Font.BOLD, 24));
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        for (String act : actions) {
            ModernButton b = new ModernButton(act, ModernButton.Type.PRIMARY);
            b.addActionListener(l);
            actionPanel.add(b);
        }

        panel.add(title, "wrap");
        panel.add(new JScrollPane(table), "grow, wrap");
        panel.add(actionPanel, "right");

        return panel;
    }

    private JPanel createSimpleTablePanel(String titleStr, JTable table) {
        JPanel panel = new JPanel(new MigLayout("fill, insets 35", "fill", "[]20[grow]"));
        panel.setBackground(ThemeManager.BACKGROUND);
        JLabel title = new JLabel(titleStr);
        title.setFont(new Font("Inter", Font.BOLD, 24));
        panel.add(title, "wrap");
        panel.add(new JScrollPane(table), "grow");
        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 40", "fill", "[]30[]"));
        panel.setBackground(ThemeManager.BACKGROUND);
        JLabel title = new JLabel("System Configuration & Infrastructure");
        title.setFont(new Font("Inter", Font.BOLD, 28));
        panel.add(title, "wrap");

        JPanel card = new JPanel(new MigLayout("fill, insets 30", "fill", "[]15[]15[]15[]25[]"));
        ThemeManager.applyCardStyle(card);

        JTextField depositLimitField = new JTextField(
                String.valueOf(bankingService.getSystemSetting("maxDepositLimit", 5000.0)), 10);
        JCheckBox maintenanceMode = new JCheckBox("System Maintenance Mode (Lockout Customers)",
                bankingService.getSystemSettingBool("maintenanceMode", false));
        JCheckBox allowReg = new JCheckBox("Allow User Self-Registration",
                bankingService.getSystemSettingBool("allowRegistration", true));

        card.add(maintenanceMode, "wrap");
        card.add(allowReg, "wrap");
        card.add(new JLabel("Max Deposit Limit (Auto-Approval Gate)"), "split 2");
        card.add(depositLimitField, "wrap");
        card.add(new JTextField("50000", 10), "wrap");

        ModernButton save = new ModernButton("Save Configuration", ModernButton.Type.PRIMARY);
        save.addActionListener(e -> {
            try {
                double limit = Double.parseDouble(depositLimitField.getText());
                ValidationUtils.validateAmount(limit); // Reuse validation logic
                bankingService.setSystemSetting("maxDepositLimit", limit);
                bankingService.setSystemSetting("maintenanceMode", maintenanceMode.isSelected());
                bankingService.setSystemSetting("allowRegistration", allowReg.isSelected());
                Toast.show(this, "Master Config Updated Successfully", Toast.Type.SUCCESS);
            } catch (NumberFormatException ex) {
                Toast.show(this, "Numerical Error: Invalid limit format", Toast.Type.ERROR);
            } catch (Exception ex) {
                Toast.show(this, "Config Error: " + ex.getMessage(), Toast.Type.ERROR);
            }
        });
        card.add(save, "gaptop 20");

        panel.add(card, "growx");
        return panel;
    }

    private JPanel createChartArea() {
        JFreeChart chart = ChartFactory.createBarChart("Daily Transaction Volume", "Day", "Amount (Rs.)",
                volumeDataset);
        chart.setBackgroundPaint(ThemeManager.CARD);
        chart.getPlot().setBackgroundPaint(ThemeManager.CARD);

        ChartPanel cp = new ChartPanel(chart);
        JPanel card = new JPanel(new BorderLayout());
        ThemeManager.applyCardStyle(card);
        card.add(cp);
        return card;
    }

    private void handleUserAction(java.awt.event.ActionEvent e) {
        int r = userTable.getSelectedRow();
        if (r == -1) {
            Toast.show(this, "Select a user row first", Toast.Type.ERROR);
            return;
        }
        int modelRow = userTable.convertRowIndexToModel(r);
        String email = (String) userTable.getModel().getValueAt(modelRow, 1);
        String cmd = e.getActionCommand();

        try {
            if ("Deactivate".equals(cmd)) {
                bankingService.updateUserStatus(email, "DEACTIVATED");
                Toast.show(this, "User " + email + " deactivated", Toast.Type.SUCCESS);
            } else if ("Activate".equals(cmd)) {
                bankingService.updateUserStatus(email, "ACTIVE");
                Toast.show(this, "User " + email + " activated", Toast.Type.SUCCESS);
            } else if ("Reset Pwd".equals(cmd)) {
                String np = JOptionPane.showInputDialog(this, "Enter temporary password (Master Strength Required):");
                if (np != null && !np.isEmpty()) {
                    ValidationUtils.validatePasswordStrength(np);
                    bankingService.resetUserPassword(email, np);
                    Toast.show(this, "Credential Reset Dispatched Successfully", Toast.Type.SUCCESS);
                }
            } else if ("Purge User".equals(cmd)) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Master Warning: This will permanently delete user " + email
                                + " and ALL their transaction history, accounts, and logs. Continue?",
                        "Confirm Data Purge", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    bankingService.purgeUser(email);
                    Toast.show(this, "System Cleaned: All data for " + email + " removed", Toast.Type.SUCCESS);
                }
            } else {
                Toast.show(this, "Action " + cmd + " is under development", Toast.Type.INFO);
            }
            refreshAllData();
        } catch (Exception ex) {
            Toast.show(this, "Error: " + ex.getMessage(), Toast.Type.ERROR);
        }
    }

    private void handleAccountAction(java.awt.event.ActionEvent e) {
        int r = accountTable.getSelectedRow();
        if (r == -1) {
            Toast.show(this, "Select an account row first", Toast.Type.ERROR);
            return;
        }
        int modelRow = accountTable.convertRowIndexToModel(r);
        String aid = (String) accountTable.getModel().getValueAt(modelRow, 0);
        String cmd = e.getActionCommand();

        try {
            if ("Freeze".equals(cmd)) {
                bankingService.updateAccountStatus(aid, "FROZEN");
                Toast.show(this, "Account " + aid + " is now FROZEN", Toast.Type.SUCCESS);
            } else if ("Unfreeze".equals(cmd)) {
                bankingService.updateAccountStatus(aid, "ACTIVE");
                Toast.show(this, "Account " + aid + " is now ACTIVE", Toast.Type.SUCCESS);
            } else if ("Close Acc".equals(cmd)) {
                bankingService.updateAccountStatus(aid, "CLOSED");
                Toast.show(this, "Account " + aid + " closed permanently", Toast.Type.SUCCESS);
            }
            refreshAllData();
        } catch (Exception ex) {
            Toast.show(this, "Error: " + ex.getMessage(), Toast.Type.ERROR);
        }
    }

    private void handleTxAction(java.awt.event.ActionEvent e) {
        int r = transactionTable.getSelectedRow();
        if (r == -1) {
            Toast.show(this, "Select a transaction", Toast.Type.ERROR);
            return;
        }
        String tid = (String) transactionTable.getValueAt(r, 0);
        String status = (String) transactionTable.getValueAt(r, 4);
        String cmd = e.getActionCommand();

        try {
            if ("Reverse".equals(cmd)) {
                if (!"PENDING".equalsIgnoreCase(status)) {
                    Toast.show(this, "Master Security Rule: Reversal blocked for finalized (" + status + ") records.",
                            Toast.Type.ERROR);
                    return;
                }
                bankingService.deleteTransaction(tid);
                Toast.show(this, "Transaction " + tid + " reversed", Toast.Type.SUCCESS);
            } else if ("Approve Deposit".equals(cmd)) {
                bankingService.approveTransaction(tid);
                Toast.show(this, "Transaction Approved & Account Updated", Toast.Type.SUCCESS);
            }
            refreshAllData();
        } catch (Exception ex) {
            Toast.show(this, "Error: " + ex.getMessage(), Toast.Type.ERROR);
        }
    }

    private void handleTicketAction() {
        int r = ticketTable.getSelectedRow();
        if (r == -1) {
            Toast.show(this, "Select a ticket to resolve", Toast.Type.ERROR);
            return;
        }
        String tId = (String) ticketTable.getValueAt(r, 1);
        String reply = JOptionPane.showInputDialog(this, "Enter reply to customer:");
        if (reply != null) {
            try {
                bankingService.resolveTicket(tId, reply);
                Toast.show(this, "Ticket resolved successfully!", Toast.Type.SUCCESS);
                refreshAllData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showTicketDetails() {
        int r = ticketTable.getSelectedRow();
        if (r < 0)
            return;

        String id = ticketTable.getValueAt(r, 0).toString();
        String user = ticketTable.getValueAt(r, 1).toString();
        String title = ticketTable.getValueAt(r, 2).toString();
        String status = ticketTable.getValueAt(r, 4).toString();

        new Thread(() -> {
            try {
                List<SupportTicket> all = bankingService.getAllTickets();
                SupportTicket t = all.stream().filter(tk -> tk.getTicketId().equals(id)).findFirst().orElse(null);
                if (t != null) {
                    SwingUtilities.invokeLater(() -> {
                        JTextArea area = new JTextArea(10, 40);
                        area.setText("TICKET INFORMATION\n" +
                                "ID: " + id + "\n" +
                                "USER: " + user + "\n" +
                                "STATUS: " + status + "\n" +
                                "TITLE: " + title + "\n\n" +
                                "--- CUSTOMER MESSAGE ---\n" +
                                t.getDescription() + "\n\n" +
                                "--- ADMIN REPLY ---\n" +
                                (t.getAdminReply() != null && !t.getAdminReply().isEmpty() ? t.getAdminReply()
                                        : "Pending review..."));
                        area.setEditable(false);
                        area.setLineWrap(true);
                        area.setWrapStyleWord(true);
                        area.setFont(new Font("Inter", Font.PLAIN, 13));
                        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Ticket Details",
                                JOptionPane.PLAIN_MESSAGE);
                    });
                }
            } catch (Exception ex) {
            }
        }).start();
    }

    private void refreshAllData() {
        new Thread(() -> {
            try {
                java.util.concurrent.CompletableFuture<Map<String, Object>> statsFuture = java.util.concurrent.CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                return bankingService.getSystemStats();
                            } catch (Exception e) {
                                return null;
                            }
                        });
                java.util.concurrent.CompletableFuture<List<User>> usersFuture = java.util.concurrent.CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                return bankingService.getAllUsers();
                            } catch (Exception e) {
                                return null;
                            }
                        });
                java.util.concurrent.CompletableFuture<List<Account>> accsFuture = java.util.concurrent.CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                return bankingService.getAllAccounts();
                            } catch (Exception e) {
                                return null;
                            }
                        });
                java.util.concurrent.CompletableFuture<List<com.vaultx.models.Transaction>> txsFuture = java.util.concurrent.CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                return bankingService.getAllTransactions();
                            } catch (Exception e) {
                                return null;
                            }
                        });
                java.util.concurrent.CompletableFuture<List<AuditLog>> logsFuture = java.util.concurrent.CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                return bankingService.getAllAuditLogs();
                            } catch (Exception e) {
                                return null;
                            }
                        });
                java.util.concurrent.CompletableFuture<List<SupportTicket>> tktsFuture = java.util.concurrent.CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                return bankingService.getAllTickets();
                            } catch (Exception e) {
                                return null;
                            }
                        });

                Map<String, Object> stats = statsFuture.get();
                List<User> users = usersFuture.get();
                List<Account> accounts = accsFuture.get();
                List<com.vaultx.models.Transaction> txs = txsFuture.get();
                List<AuditLog> logs = logsFuture.get();
                List<SupportTicket> tkts = tktsFuture.get();

                SwingUtilities.invokeLater(() -> {
                    if (stats != null) {
                        customerCount.setText(stats.get("totalCustomers").toString());
                        activeAccounts.setText(stats.get("activeAccounts").toString());
                        double total = (double) stats.get("totalBalance");
                        bankBalance.setText(String.format("Rs. %,.2f", total));
                        if (total < 0) {
                            bankBalance.setForeground(ThemeManager.ERROR);
                            bankBalance.setToolTipText("BANKRUPTCY ALERT: Total assets are negative!");
                        } else {
                            bankBalance.setForeground(ThemeManager.PRIMARY);
                        }
                    }
                    if (users != null)
                        updateTableModel(userTable, new String[] { "Name", "Email", "Phone", "Role", "Status" },
                                users.stream().filter(u -> !"ADMIN".equals(u.getRole())).map(u -> new Object[] {
                                        u.getFullName(), u.getEmail(), u.getPhone(), u.getRole(), u.getStatus() })
                                        .toArray(Object[][]::new));
                    if (accounts != null)
                        updateTableModel(accountTable,
                                new String[] { "Acc ID", "User ID", "Type", "Balance", "Status" },
                                accounts.stream()
                                        .map(a -> new Object[] { a.getAccountId(), a.getUserId(), a.getAccountType(),
                                                String.format("Rs. %.2f", a.getBalance()), a.getStatus() })
                                        .toArray(Object[][]::new));

                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm");
                    if (txs != null) {
                        updateTableModel(transactionTable,
                                new String[] { "TID", "From", "To", "Amt", "Status", "Date" },
                                txs.stream()
                                        .map(t -> new Object[] { t.getTransactionId(), t.getFromAccountId(),
                                                t.getToAccountId(), String.format("Rs. %.2f", t.getAmount()),
                                                t.getStatus(), sdf.format(new Date(t.getTimestamp())) })
                                        .toArray(Object[][]::new));

                        // Update Volume Chart with real-time data
                        volumeDataset.clear();
                        Map<String, Double> dayMap = new TreeMap<>(); // Sorted by day name
                        SimpleDateFormat dayFmt = new SimpleDateFormat("EEE");
                        for (com.vaultx.models.Transaction t : txs) {
                            String day = dayFmt.format(new Date(t.getTimestamp()));
                            dayMap.put(day, dayMap.getOrDefault(day, 0.0) + t.getAmount());
                        }
                        dayMap.forEach((day, total) -> volumeDataset.addValue(total, "Volume", day));
                    }

                    if (logs != null)
                        updateTableModel(auditTable, new String[] { "LogID", "User", "Action", "Details", "Time" },
                                logs.stream()
                                        .map(l -> new Object[] { l.getLogId(), l.getUserId(), l.getAction(),
                                                l.getDetails(), sdf.format(new Date(l.getTimestamp())) })
                                        .toArray(Object[][]::new));
                    if (tkts != null) {
                        updateTableModel(ticketTable, new String[] { "TID", "User", "Title", "Category", "Status" },
                                tkts.stream().map(t -> new Object[] { t.getTicketId(), t.getUserId(), t.getTitle(),
                                        t.getCategory(), t.getStatus() }).toArray(Object[][]::new));

                        // Add mouse listener for details if not present
                        if (ticketTable.getMouseListeners().length < 3) {
                            ticketTable.addMouseListener(new java.awt.event.MouseAdapter() {
                                public void mouseClicked(java.awt.event.MouseEvent e) {
                                    if (e.getClickCount() == 2)
                                        showTicketDetails();
                                }
                            });
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateTableModel(JTable table, String[] cols, Object[][] data) {
        table.setModel(new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        table.setRowHeight(35);
    }

    private void setActiveMenu(JButton newActive) {
        // Reset previous active button
        if (activeMenuButton != null) {
            activeMenuButton.setFont(new Font("Inter", Font.PLAIN, 15));
            activeMenuButton.setForeground(ThemeManager.TEXT_SECONDARY);
            activeMenuButton.setOpaque(false);
            activeMenuButton.setContentAreaFilled(false);
        }

        // Set new active button
        activeMenuButton = newActive;
        newActive.setFont(new Font("Inter", Font.BOLD, 15));
        newActive.setForeground(ThemeManager.PRIMARY);
        newActive.setOpaque(true);
        newActive.setBackground(new Color(ThemeManager.PRIMARY.getRed(), ThemeManager.PRIMARY.getGreen(),
                ThemeManager.PRIMARY.getBlue(), 30));
    }
}
