package com.vaultx.ui.screens;

import com.vaultx.auth.SessionManager;
import com.vaultx.models.*;
import com.vaultx.services.BankingService;
import com.vaultx.ui.components.ModernButton;
import com.vaultx.ui.components.ModernTextField;
import com.vaultx.ui.components.Toast;
import com.vaultx.utils.ThemeManager;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CustomerDashboard extends JFrame {
    private final BankingService bankingService = new BankingService();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    
    private final JLabel balanceLabel = new JLabel("Rs. 0.00");
    private boolean balanceVisible = true;
    private final JTable statementTable = new JTable();
    private final JTable recentActivityTable = new JTable();
    private final JTable ticketTable = new JTable();
    private Account currentAccount;
    private JButton activeMenuButton = null;
    private DefaultPieDataset spendingDataset = new DefaultPieDataset();

    public CustomerDashboard() {
        setTitle("VaultX Banking - Client Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 780);
        setLocationRelativeTo(null);

        // Initialize tables with default structure
        DefaultTableModel emptyModel = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Date", "Detail", "Type", "Amount"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        statementTable.setModel(emptyModel);
        statementTable.setRowHeight(35);
        
        // Use a separate model instance for recent activity to avoid shared state issues
        recentActivityTable.setModel(new DefaultTableModel(
            new Object[][]{},
            new String[]{"Date", "Detail", "Type", "Amount"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });
        recentActivityTable.setRowHeight(35);

        loadInitialData();

        JPanel mainContainer = new JPanel(new MigLayout("fill, insets 0", "[240!]0[fill]", "fill"));
        mainContainer.add(createSidebar(), "growy");
        
        setupContentCards();
        mainContainer.add(contentCards, "grow");

        add(mainContainer);
        
        // Refresh UI after everything is set up
        SwingUtilities.invokeLater(() -> refreshUI());
    }

    private void loadInitialData() {
        try {
            User u = SessionManager.getInstance().getCurrentUser();
            currentAccount = bankingService.getAccountByUserId(u.getEmail());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupContentCards() {
        contentCards.add(createMainDashboard(), "DASHBOARD");
        contentCards.add(createTransferModule(), "PAYMENTS");
        contentCards.add(createStatementModule(), "STATEMENTS");
        contentCards.add(createTicketModule(), "SUPPORT");
        contentCards.add(createAnalyticsModule(), "ANALYTICS");
        contentCards.add(createProfileModule(), "PROFILE");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new MigLayout("fill, insets 25", "fill", "[]50[]15[]15[]15[]15[]15[]push[]"));
        sidebar.setBackground(ThemeManager.SURFACE);
        
        JLabel logo = new JLabel("VaultX");
        logo.setFont(new Font("Inter", Font.BOLD, 26));
        logo.setForeground(ThemeManager.PRIMARY);
        sidebar.add(logo, "wrap, gapbottom 40");
        
        String[][] menu = {
            {"Overview", "DASHBOARD"}, 
            {"Payments", "PAYMENTS"}, 
            {"e-Statements", "STATEMENTS"},
            {"Spending", "ANALYTICS"},
            {"Help Center", "SUPPORT"},
            {"Profile", "PROFILE"}
        };
        for (String[] itm : menu) {
            JButton b = new JButton(itm[0]);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setFont(new Font("Inter", Font.PLAIN, 15));
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setForeground(ThemeManager.TEXT_SECONDARY);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> {
                cardLayout.show(contentCards, itm[1]);
                setActiveMenu(b);
            });
            sidebar.add(b, "wrap, height 40!");
            
            // Set Overview as default active
            if ("DASHBOARD".equals(itm[1])) {
                activeMenuButton = b;
                b.setFont(new Font("Inter", Font.BOLD, 15));
                b.setForeground(ThemeManager.PRIMARY);
                b.setOpaque(true);
                b.setBackground(new Color(ThemeManager.PRIMARY.getRed(), ThemeManager.PRIMARY.getGreen(), ThemeManager.PRIMARY.getBlue(), 30));
            }
        }
        
        ModernButton logout = new ModernButton("Sign Out", ModernButton.Type.SECONDARY);
        logout.addActionListener(e -> { new LoginScreen().setVisible(true); this.dispose(); });
        
        ModernButton shutdown = new ModernButton("⏻ Shutdown", ModernButton.Type.SECONDARY);
        shutdown.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit VaultX?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) System.exit(0);
        });
        
        sidebar.add(logout, "south, height 40!, gapbottom 10");
        sidebar.add(shutdown, "south, height 40!");
        
        return sidebar;
    }

    private JPanel createMainDashboard() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 35", "fill", "[]20[]20[grow]"));
        panel.setBackground(ThemeManager.BACKGROUND);

        User u = SessionManager.getInstance().getCurrentUser();
        JLabel greet = new JLabel("Good day, " + u.getFullName().split(" ")[0]);
        greet.setFont(new Font("Inter", Font.BOLD, 26));

        JPanel balCard = new JPanel(new MigLayout("fill, insets 25", "fill", "[]10[]"));
        ThemeManager.applyCardStyle(balCard);
        JLabel pt = new JLabel("AVAILABLE BALANCE");
        pt.setForeground(ThemeManager.TEXT_SECONDARY);
        balanceLabel.setFont(new Font("Inter", Font.BOLD, 38));
        balanceLabel.setForeground(ThemeManager.SUCCESS);
        
        JButton toggleBalance = new JButton("👁");
        toggleBalance.setPreferredSize(new Dimension(40, 40));
        toggleBalance.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        toggleBalance.addActionListener(e -> {
            balanceVisible = !balanceVisible;
            if (balanceVisible) {
                balanceLabel.setText(String.format("Rs. %,.2f", currentAccount != null ? currentAccount.getBalance() : 0.0));
                toggleBalance.setText("👁");
            } else {
                balanceLabel.setText("••••••••");
                toggleBalance.setText("🙈");
            }
        });
        
        JPanel balRow = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
        balRow.setOpaque(false);
        balRow.add(balanceLabel);
        balRow.add(toggleBalance, "gapleft 15");
        
        balCard.add(pt, "wrap");
        balCard.add(balRow);

        JPanel quickActions = new JPanel(new MigLayout("fill, insets 0", "fill", "fill"));
        quickActions.setOpaque(false);
        ModernButton dep = new ModernButton("Deposit Funds", ModernButton.Type.PRIMARY);
        dep.addActionListener(e -> handleDeposit());
        ModernButton with = new ModernButton("Withdraw Funds", ModernButton.Type.PRIMARY);
        with.addActionListener(e -> handleWithdraw());
        ModernButton pay = new ModernButton("Pay Someone", ModernButton.Type.SECONDARY);
        pay.addActionListener(e -> cardLayout.show(contentCards, "PAYMENTS"));
        quickActions.add(dep, "grow, gapright 10, height 55!");
        quickActions.add(with, "grow, gapright 10, height 55!");
        quickActions.add(pay, "grow, height 55!");

        panel.add(greet, "wrap");
        panel.add(balCard, "growx, wrap, gapbottom 20");
        panel.add(quickActions, "growx, wrap, gapbottom 30");
        
        JPanel recArea = new JPanel(new MigLayout("fill, insets 20", "fill", "[]10[grow]"));
        ThemeManager.applyCardStyle(recArea);
        recArea.add(new JLabel("RECENT ACTIVITY"), "wrap");
        recArea.add(new JScrollPane(recentActivityTable), "grow");
        panel.add(recArea, "grow");

        return panel;
    }

    private JPanel createTransferModule() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 50", "center", "center"));
        panel.setBackground(ThemeManager.BACKGROUND);
        JPanel card = new JPanel(new MigLayout("fill, insets 35", "[fill]", "[]30[]10[]20[]10[]20[]20[]"));
        ThemeManager.applyCardStyle(card);
        card.setPreferredSize(new Dimension(480, 580));
        
        JLabel t = new JLabel("Fast & Secure Transfer");
        t.setFont(new Font("Inter", Font.BOLD, 22));
        ModernTextField rId = new ModernTextField("Recipient Account ID");
        JLabel rName = new JLabel("Enter ID to search...");
        rName.setFont(new Font("Inter", Font.ITALIC, 13));
        rName.setForeground(ThemeManager.PRIMARY);

        rId.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { lookup(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { lookup(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { lookup(); }
            private void lookup() {
                String id = rId.getText().trim();
                new Thread(() -> {
                    try {
                        Account acc = bankingService.getAccountById(id);
                        if(acc != null) {
                            User u = bankingService.getAllUsers().stream().filter(usr -> usr.getEmail().equals(acc.getUserId())).findFirst().orElse(null);
                            SwingUtilities.invokeLater(() -> rName.setText(u != null ? "✓ Recipient: " + u.getFullName() : "Account found"));
                        } else { SwingUtilities.invokeLater(() -> rName.setText("Account not found")); }
                    } catch(Exception ex) {}
                }).start();
            }
        });

        ModernTextField amt = new ModernTextField("Amount (Rs.)");
        ModernTextField note = new ModernTextField("Reference (Optional)");
        ModernButton b = new ModernButton("Execute Payment", ModernButton.Type.PRIMARY);
        b.addActionListener(e -> {
            try {
                bankingService.performTransfer(currentAccount.getAccountId(), rId.getText().trim(), Double.parseDouble(amt.getText()), note.getText());
                Toast.show(this, "Payment Dispatched Successfully!", Toast.Type.SUCCESS);
                refreshUI();
                cardLayout.show(contentCards, "DASHBOARD");
            } catch (Exception ex) { Toast.show(this, ex.getMessage(), Toast.Type.ERROR); }
        });
        
        card.add(t, "wrap");
        card.add(new JLabel("RECIPIENT ACCOUNT ID"), "wrap");
        card.add(rId, "wrap, height 45!");
        card.add(rName, "wrap, gaptop 2");
        card.add(new JLabel("TRANSFER AMOUNT"), "wrap");
        card.add(amt, "wrap, height 45!");
        card.add(new JLabel("REMARKS"), "wrap");
        card.add(note, "wrap, height 45!");
        card.add(b, "wrap, height 55!, gaptop 25");
        panel.add(card);
        return panel;
    }

    private JPanel createStatementModule() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 35", "fill", "[]20[grow]"));
        panel.setBackground(ThemeManager.BACKGROUND);
        JLabel t = new JLabel("Account Statements");
        t.setFont(new Font("Inter", Font.BOLD, 24));
        panel.add(t, "wrap");
        panel.add(new JScrollPane(statementTable), "grow");
        return panel;
    }

    private JPanel createTicketModule() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 35", "fill", "[]20[grow]20[]"));
        panel.setBackground(ThemeManager.BACKGROUND);
        JLabel t = new JLabel("Support & Help Desk");
        t.setFont(new Font("Inter", Font.BOLD, 24));
        panel.add(t, "wrap");
        panel.add(new JScrollPane(ticketTable), "grow, wrap");
        
        ModernButton newBatch = new ModernButton("Raise New Ticket", ModernButton.Type.PRIMARY);
        newBatch.addActionListener(e -> handleRaiseTicket());
        panel.add(newBatch, "right");
        return panel;
    }

    private JPanel createAnalyticsModule() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 35", "center", "[]30[grow]"));
        panel.setBackground(ThemeManager.BACKGROUND);
        JLabel t = new JLabel("Spending & Expense Analytics");
        t.setFont(new Font("Inter", Font.BOLD, 24));
        panel.add(t, "wrap");
        
        // Initialize with empty data
        spendingDataset.setValue("Transfers", 0);
        spendingDataset.setValue("Withdrawals", 0);
        spendingDataset.setValue("Deposits (Inflow)", 0);

        JFreeChart chart = ChartFactory.createPieChart("Expenditure Breakdown", spendingDataset, true, true, false);
        chart.setBackgroundPaint(ThemeManager.CARD);
        
        JPanel card = new JPanel(new BorderLayout());
        ThemeManager.applyCardStyle(card);
        card.add(new ChartPanel(chart));
        panel.add(card, "grow");
        return panel;
    }

    private JPanel createProfileModule() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 40", "fill", "[]30[]"));
        panel.setBackground(ThemeManager.BACKGROUND);
        User u = SessionManager.getInstance().getCurrentUser();
        JLabel t = new JLabel("Security & Profile");
        t.setFont(new Font("Inter", Font.BOLD, 26));
        panel.add(t, "wrap");
        
        JPanel card = new JPanel(new MigLayout("fill, insets 30", "fill", "[]15[]15[]15[]15[]"));
        ThemeManager.applyCardStyle(card);
        card.add(new JLabel("User ID/Email: " + u.getEmail()), "wrap");
        card.add(new JLabel("Full Name: " + u.getFullName()), "wrap");
        card.add(new JLabel("Account ID: " + currentAccount.getAccountId()), "wrap");
        card.add(new JLabel("Mobile: " + u.getPhone()), "wrap");
        card.add(new ModernButton("Update Security Questions", ModernButton.Type.SECONDARY), "wrap");
        panel.add(card, "growx");
        return panel;
    }

    private void handleDeposit() {
        String val = JOptionPane.showInputDialog(this, "Enter amount to deposit (Rs. ):");
        if(val != null) {
            try {
                boolean isAdmin = "ADMIN".equals(SessionManager.getInstance().getCurrentUser().getRole());
                bankingService.performDeposit(currentAccount.getAccountId(), Double.parseDouble(val), isAdmin);
                Toast.show(this, "Funds Deposited Successfully!", Toast.Type.SUCCESS);
                refreshUI();
            } catch (Exception ex) { 
                if(ex.getMessage().contains("Awaiting Admin Approval")) {
                    Toast.show(this, ex.getMessage(), Toast.Type.INFO);
                    refreshUI();
                } else {
                    Toast.show(this, "Error: " + ex.getMessage(), Toast.Type.ERROR); 
                }
            }
        }
    }

    private void handleWithdraw() {
        String val = JOptionPane.showInputDialog(this, "Enter amount to withdraw (Rs. ):");
        if(val != null) {
            try {
                bankingService.performWithdrawal(currentAccount.getAccountId(), Double.parseDouble(val));
                Toast.show(this, "Funds Withdrawn Successfully!", Toast.Type.SUCCESS);
                refreshUI();
            } catch (Exception ex) { Toast.show(this, ex.getMessage(), Toast.Type.ERROR); }
        }
    }

    private void handleRaiseTicket() {
        String title = JOptionPane.showInputDialog(this, "Issue Title:");
        String msg = JOptionPane.showInputDialog(this, "Describe the issue:");
        if(title != null && msg != null) {
            try {
                bankingService.createTicket(new SupportTicket(SessionManager.getInstance().getCurrentUser().getEmail(), title, "GENERAL", msg));
                Toast.show(this, "Ticket submitted to admin", Toast.Type.SUCCESS);
                refreshUI();
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    private void refreshUI() {
        new Thread(() -> {
            try {
                java.util.concurrent.CompletableFuture<Void> initFuture = java.util.concurrent.CompletableFuture.runAsync(() -> loadInitialData());
                initFuture.get(); // Need currentAccount for next calls

                java.util.concurrent.CompletableFuture<List<com.vaultx.models.Transaction>> txsFuture = java.util.concurrent.CompletableFuture.supplyAsync(() -> { try { return bankingService.getTransactions(currentAccount.getAccountId()); } catch(Exception e) { return null; } });
                java.util.concurrent.CompletableFuture<List<com.vaultx.models.SupportTicket>> tktsFuture = java.util.concurrent.CompletableFuture.supplyAsync(() -> { try { return bankingService.getTicketsForUser(currentAccount.getUserId()); } catch(Exception e) { return null; } });

                List<com.vaultx.models.Transaction> txs = txsFuture.get();
                List<com.vaultx.models.SupportTicket> tkts = tktsFuture.get();
                
                SwingUtilities.invokeLater(() -> {
                    if(currentAccount != null) {
                        if (balanceVisible) {
                            balanceLabel.setText(String.format("Rs. %,.2f", currentAccount.getBalance()));
                        } else {
                            balanceLabel.setText("••••••••");
                        }
                    }
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm");
                    if(txs != null && !txs.isEmpty()) {
                        // Update chart data
                        double transfers = 0, withdrawals = 0, deposits = 0;
                        for(com.vaultx.models.Transaction tx : txs) {
                            if ("DEPOSIT".equals(tx.getType())) deposits += tx.getAmount();
                            else if ("WITHDRAWAL".equals(tx.getType())) withdrawals += tx.getAmount();
                            else if ("TRANSFER".equals(tx.getType())) transfers += tx.getAmount();
                        }
                        spendingDataset.setValue("Transfers", transfers);
                        spendingDataset.setValue("Withdrawals", withdrawals);
                        spendingDataset.setValue("Deposits (Inflow)", deposits);
                        
                        // Update recent activity table (Limit 10)
                        String[] tCols = {"Date", "Detail", "Type", "Amount"};
                        Object[][] recentData = txs.stream()
                            .limit(10)
                            .map(t -> {
                                boolean out = t.getFromAccountId().equals(currentAccount.getAccountId());
                                return new Object[]{
                                    sdf.format(new Date(t.getTimestamp())), 
                                    t.getDescription(), 
                                    t.getType(), 
                                    (out?"-":"+")+String.format("Rs. %.2f", t.getAmount())
                                };
                            }).toArray(Object[][]::new);
                        
                        DefaultTableModel recentModel = new DefaultTableModel(recentData, tCols) {
                            @Override
                            public boolean isCellEditable(int row, int column) { return false; }
                        };
                        recentActivityTable.setModel(recentModel);
                        
                        // Update full statement table (All Transactions)
                        Object[][] fullData = txs.stream()
                            .map(t -> {
                                boolean out = t.getFromAccountId().equals(currentAccount.getAccountId());
                                return new Object[]{
                                    sdf.format(new Date(t.getTimestamp())), 
                                    t.getDescription(), 
                                    t.getType(), 
                                    (out?"-":"+")+String.format("Rs. %.2f", t.getAmount())
                                };
                            }).toArray(Object[][]::new);
                            
                        DefaultTableModel fullModel = new DefaultTableModel(fullData, tCols) {
                            @Override
                            public boolean isCellEditable(int row, int column) { return false; }
                        };
                        statementTable.setModel(fullModel);
                        
                        // Force UI update
                        if (recentActivityTable.getParent() != null) {
                            recentActivityTable.getParent().revalidate();
                            recentActivityTable.getParent().repaint();
                        }
                        if (statementTable.getParent() != null) {
                            statementTable.getParent().revalidate();
                            statementTable.getParent().repaint();
                        }
                    }

                    if(tkts != null) {
                        String[] kCols = {"Ticket ID", "Title", "Status", "Admin Reply"};
                        Object[][] kData = tkts.stream().map(t -> new Object[]{t.getTicketId(), t.getTitle(), t.getStatus(), t.getAdminReply()}).toArray(Object[][]::new);
                        ticketTable.setModel(new DefaultTableModel(kData, kCols) {
                            @Override
                            public boolean isCellEditable(int row, int column) { return false; }
                        });
                        ticketTable.setRowHeight(35);
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
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
        newActive.setBackground(new Color(ThemeManager.PRIMARY.getRed(), ThemeManager.PRIMARY.getGreen(), ThemeManager.PRIMARY.getBlue(), 30));
    }
}
