package com.revature.service;

import com.revature.exception.InsufficientFundsException;
import com.revature.exception.UnavailableUsernameException;
import com.revature.exception.PasswordTooShortException;
import com.revature.repository.BankingDAO;
import com.revature.repository.BankingDAOPostgres;
import com.revature.model.User;

public class Services {
  private BankingDAO bankingDAO = new BankingDAOPostgres();
  private User user = new User();

  public Services() {
    super();
  }

  public boolean loginUser(String username, String password) {
    if (bankingDAO.login(username, password)) {
      user.setPassword(password);
      user.setUsername(username);
      return true;
    } else
      return false;
  }

  public void checkUsernameAvailability(String username) throws UnavailableUsernameException {
    bankingDAO.checkUsernameAvailability(username);
  }

  public void validatePassword(String password) throws PasswordTooShortException {
    if (password.length() < 6) {
      throw new PasswordTooShortException();
    }
  }

  public void registerUser(String username, String password) {
    bankingDAO.register(username, password);
  }
  

  public void logout() {
    user.setUsername(null);
    user.setPassword(null);
    bankingDAO.logout();
  }

  public void withdraw(double amount) throws InsufficientFundsException {
    bankingDAO.withdraw(amount);
  }

  public void deposit(double amount) {
    bankingDAO.deposit(amount);
  }

  public void getBalance() {
    bankingDAO.getBalance();
  }
  
  public void getTransactionHistory() {
    bankingDAO.getTransactionHistory();
  }
}
