package com.revature.repository;

import com.revature.exception.InsufficientFundsException;
import com.revature.exception.UnavailableUsernameException;

public interface BankingDAO {

  public void deposit(double amount);
  
  public void withdraw(double amount) throws InsufficientFundsException;
  
  public boolean login(String username, String password);
  
  public void register(String username, String password);
  
  public void getBalance();
  
  public void getTransactionHistory();
  
  public void logout();
  
  public void checkUsernameAvailability(String username) throws UnavailableUsernameException;
  
}
