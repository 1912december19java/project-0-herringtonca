package com.revature.repository;

import com.revature.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import com.revature.exception.InsufficientFundsException;
import com.revature.exception.UnavailableUsernameException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.revature.model.User;

public class BankingDAOPostgres implements BankingDAO {

  private static Connection conn;
  private static User user = new User();
  private static Logger Log = Logger.getLogger(BankingDAOPostgres.class);

  static {
    try {
      conn = DriverManager.getConnection(System.getenv("connstring"), System.getenv("username"),
          System.getenv("password"));
    } catch (SQLException e) {
      Log.error("Database connection unsucessful.", e);
    }
  }

  @Override
  public void deposit(double amount) {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.prepareStatement("SELECT * FROM users WHERE username = '" + user.getUsername()
          + "' AND pass = '" + user.getPassword() + "'");

      if (stmt.execute()) {
        rs = stmt.getResultSet();
        rs.next();

        transaction("Deposit", amount, rs.getInt("user_id"));
      }
    } catch (SQLException e) {
      Log.fatal("SQLException was thrown", e);
    }
  }

  @Override
  public void withdraw(double amount) throws InsufficientFundsException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.prepareStatement(
          "SELECT users.user_id, users.username, SUM(transact) FROM users JOIN transactions ON users.user_id = transactions.user_id WHERE username = '"
              + user.getUsername() + "' GROUP BY users.user_id");

      if (stmt.execute()) {
        rs = stmt.getResultSet();
        if (rs.next()) {
          if (rs.getDouble("sum") - amount < 0)
            throw new InsufficientFundsException();
          else
            transaction("Withdraw", amount, rs.getInt("user_id"));
        } else {
          Log.error("User tried to withdraw more than available balance.");
          throw new InsufficientFundsException();
        }
      }
    } catch (SQLException e) {
      Log.fatal("SQLException was thrown", e);
    }

  }

  @Override
  public boolean login(String username, String password) {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND pass = ?");
      stmt.setString(1, username);
      stmt.setString(2, password);

      if (stmt.execute()) {
        rs = stmt.getResultSet();
      }

      if (!rs.next()) {
        System.out.println("Incorrect username or password, try again.");
        Log.error("User entered invalid credentials");
        return false;
      } else {
        Log.trace("Login successful");
        user.setUsername(username);
        user.setPassword(password);
        return true;
      }
    } catch (SQLException e) {
      Log.fatal("SQLException was thrown", e);
    }

    return true;
  }

  @Override
  public void register(String username, String password) {
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement("INSERT INTO users (username, pass) VALUES (?,?)");
      stmt.setString(1, username);
      stmt.setString(2, password);
      stmt.execute();
    } catch (SQLException e) {
      Log.fatal("SQLException was thrown", e);
    }
  }

  public void logout() {
    user.setPassword(null);
    user.setUsername(null);
  }

  public void getBalance() {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.prepareStatement(
          "SELECT SUM(transact) FROM users JOIN transactions ON users.user_id = transactions.user_id where username = '"
              + user.getUsername() + "' GROUP BY users.user_id");
      if (stmt.execute()) {
        rs = stmt.getResultSet();
        if (rs.next())
          System.out.printf("Your current available balance is: $%.2f", rs.getDouble("sum"));
        else
          System.out.println("Your current available balance is $0.00 ");
        System.out.println();
      }

    } catch (SQLException e) {
      Log.fatal("SQLException was thrown", e);
    }
  }

  public void getTransactionHistory() {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.prepareStatement(
          "SELECT transact, date_ FROM users JOIN transactions ON users.user_id = transactions.user_id where username = '"
              + user.getUsername() + "'");
      if (stmt.execute())
        rs = stmt.getResultSet();
      System.out.printf("%-11s| %-30s", "Transaction", "Date");
      System.out.println();
      while (rs.next()) {
        System.out.println();
        System.out.printf("%-11.2f|%-30s", rs.getDouble(1), rs.getString(2));
      }
      System.out.println();


    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void transaction(String transaction, double amount, int userId) {
    PreparedStatement stmt = null;
    try {
      if (transaction.equals("Withdraw")) {
        stmt = conn.prepareStatement(
            "INSERT INTO transactions VALUES(" + userId + ", -" + amount + ", NOW())");
        stmt.execute();
        Log.trace("Successfully withdrew $" + amount + " into account number " + userId);
      } else {
        stmt = conn.prepareStatement(
            "INSERT INTO transactions VALUES (" + userId + ", " + amount + ", NOW())");
        stmt.execute();
      }
    } catch (SQLException e) {
      Log.fatal("SQLException was thrown", e);
    }
  }

  public void checkUsernameAvailability(String username) throws UnavailableUsernameException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt =
          conn.prepareStatement("SELECT username FROM users WHERE username = '" + username + "'");
      stmt.execute();
      rs = stmt.getResultSet();
      if (rs.next()) {
        Log.error("tried to register with an existing username");
        throw new UnavailableUsernameException();
      }
    } catch (SQLException e) {
      Log.fatal("SQLException was thrown", e);
    }
  }
}
