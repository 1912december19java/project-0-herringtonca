package com.revature.controller;

import java.util.Scanner;
import org.apache.log4j.Logger;
import com.revature.exception.InsufficientFundsException;
import com.revature.exception.UnavailableUsernameException;
import com.revature.exception.PasswordTooShortException;
import com.revature.service.Services;
import com.revature.model.User;

public class Controller {
  /**
   * instance variables to keep track of user information and make calls to the service layer
   */
  private static Logger log = Logger.getLogger(Controller.class);
  private Scanner input = new Scanner(System.in);
  private Services serve;
  private User user;
  private String state;

  // Controller constructor
  public Controller() {
    super();
    serve = new Services();
    user = new User(null, null);
    state = "Home Screen";
  }

  /**
   * run() method is called initially and prompts user to login or register. checks that they have
   * input a 1 or 2.
   */
  public void run() {
    while (true) {
      if (this.state.equals("Home Screen")) {
        homeScreen();
      }
      if (this.state.contentEquals("Login Screen")) {
        loginUser();
      }
      if (this.state.equals("Register Screen")) {
        registerUser();
      }
      if (this.state.equals("Transaction Screen")) {
        transactionScreen();
      }
      if (this.state.equals("Deposit Screen")) {
        deposit();
      }
      if (this.state.equals("Withdraw Screen")) {
        withdraw();
      }
    }
  }
/**
 * default screen all users see when they application is started
 */
  private void homeScreen() {
    System.out.println(
        "Welcome to The Bank! Your one stop shop for all your banking needs wants and desires!");
    System.out.println("Press 1 to Login \nPress 2 to Register");
    int userChoice = validateUserInputHomeScreen(input.nextLine());
    if (userChoice == 1) {
      this.state = "Login Screen";
    } else {
      this.state = "Register Screen";
    }
  }

  /**
   * userLogin() method asks user for username and password and makes a call to the service layer to
   * make sure the user entered valid data. Calls the transaction() method after they have entered
   * correct data.
   */
  public void loginUser() {
    String username = null;
    String password = null;
    boolean validUserInfo = false;
    while (!validUserInfo) {
      System.out.println("Enter your username or type \"back\" to go back to the Home Screen");
      username = input.nextLine();
      if (!username.equalsIgnoreCase("back")) {
        System.out.println("Enter your password");
        password = input.nextLine();

        validUserInfo = serve.loginUser(username, password);
      } else
        validUserInfo = true;
    }
    if (!username.equalsIgnoreCase("back")) {
      System.out.println("Login Successful.");
      user.setUsername(username);
      user.setPassword(password);
      this.state = "Transaction Screen";
    } else
      this.state = "Home Screen";
  }

  /**
   * userRegister() registers a new user by making a call to the service layer. it then logs the
   * user in and calls the transaction() method.
   */
  public void registerUser() {
    boolean availableUsername = false;
    boolean validPassword = false;
    System.out.println("Enter a username or enter \"back\" to return to the Home Screen");
    String username = input.nextLine();
    while (!availableUsername) {
      try {
        if (username.length() == 0) {
          System.out.println("Username cannot be empty");
          availableUsername = false;
          username = input.nextLine();
        } else if (username.equalsIgnoreCase("back"))
          availableUsername = true;
        else {
          serve.checkUsernameAvailability(username);
          availableUsername = true;
        }
      } catch (UnavailableUsernameException e) {
        System.out.println("That username is unavailable, try again");
        username = input.nextLine();
      }
    }
    if (!username.equalsIgnoreCase("back")) {
      System.out.println("Enter a password at least 6 characters long");
      String password = input.nextLine();
      while (!validPassword) {
        try {
          serve.validatePassword(password);
          validPassword = true;
        } catch (PasswordTooShortException e) {
          log.error("tried to register with a password that was too short", e);
          System.out.println("Password too short, try again");
          password = input.nextLine();
        }
      }
      serve.registerUser(username, password);
      serve.loginUser(username, password);
      System.out.println();
      System.out.println("Registration Successful!");
      System.out.println();
      user.setUsername(username);
      user.setPassword(password);
      this.state = "Transaction Screen";
    } else
      this.state = "Home Screen";
  }

  /**
   * transactionScreen() method shows the user a list of services offered and calls the appropriate
   * method based on their decision
   */
  public void transactionScreen() {
    System.out.println("Press 1 to deposit");
    System.out.println("Press 2 to withdraw");
    System.out.println("Press 3 to check balance");
    System.out.println("Press 4 to for transaction history");
    System.out.println("Type \"logout\" to logout");
    int userInput = validateUserInputTransactionScreen(input.nextLine().trim());

    if (userInput == 1) {
      this.state = "Deposit Screen";
    } else if (userInput == 2) {
      this.state = "Withdraw Screen";
    } else if (userInput == 3) {
      getBalance();
    } else if(userInput == 4){
      serve.getTransactionHistory();
    }else
      logout();
  }

  /**
   * deposit() method prompts a user to enter an amount to deposit into their account. gives user an
   * option to logout. checks that user input a double then makes a call to the service layer.
   */
  public void deposit() {
    System.out.println("Enter amount to deposit");
    System.out.println("Type \"logout\" to logout");
    double depositAmount = isValidDoubleOrLogout();
    if (depositAmount != -1) {
      serve.deposit(depositAmount);
      getBalance();
      state = "Transaction Screen";
    } else
      logout();
  }

  /**
   * withdraw() method allows user to withdraw funds from their account. verifies user input and
   * makes a call to the service layer. gives the user an option to logout.
   * 
   * catches and handles an InsufficientFundsException if the user enters an amount higher than
   * their available balance
   */
  public void withdraw() {
    System.out.println("Enter amount to withdraw");
    System.out.println("Type \"logout\" to logout");
    double withdrawAmount = isValidDoubleOrLogout();
    if (withdrawAmount != -1) {
      try {
        serve.withdraw(withdrawAmount);
        getBalance();
        state = "Transaction Screen";
      } catch (InsufficientFundsException e) {
        System.out.println("Transaction failed: Insufficient funds");
        this.state = "Transaction Screen";
      }
    } else
      logout();
  }

  /**
   * validateUserInputHomeScreen() is a private method that validates the users input for the home
   * screen options. it catches a NumberFormatException if the user input something other than an
   * int and asks for another input.
   * 
   * @param String userInput
   * @return int
   */
  private int validateUserInputHomeScreen(String userInput) {
    boolean valid = false;
    while (!valid) {
      try {
        if (Integer.parseInt(userInput) == 1 || Integer.parseInt(userInput) == 2) {
          valid = true;
        } else {
          log.error("entered a number that was not 1 or 2");
          System.out.println("Invalid number entry");
          userInput = input.nextLine();
        }
      } catch (NumberFormatException e) {
        log.error("input included more than just numbers", e);
        System.out.println("You entered something we were not expecting, try again.");
        userInput = input.nextLine();
      }
    }
    return Integer.parseInt(userInput);
  }

  /**
   * validateUserInputTransactionScreen checks the users input for the options at the transaction
   * screen. gives them an option to logout. catches a NumberFormatException if they input a string
   * other than "logout".
   * 
   * @param String userInput
   * @return int
   */
  private int validateUserInputTransactionScreen(String userInput) {

    boolean valid = false;
    while (!valid) {
      try {
        if (userInput.equalsIgnoreCase("logout")) {
          valid = true;
          this.state = "Home Screen";
        } else if (Integer.parseInt(userInput) == 1 || Integer.parseInt(userInput) == 2
            || Integer.parseInt(userInput) == 3 || Integer.parseInt(userInput) == 4) {
          valid = true;
        } else {
          log.error("entered a number other than 1, 2, 3, or 4");
          System.out.println("Invalid number entry");
          userInput = input.nextLine();
        }
      } catch (NumberFormatException e) {
        log.error("input included something other than numbers and was not logout", e);
        System.out.println("You entered something we were not expecting, try again.");
        userInput = input.nextLine();
      }
    }
    if (!userInput.equalsIgnoreCase("logout"))
      return Integer.parseInt(userInput);
    else
      return -1;
  }

  /**
   * isValidDouble() method checks that the user has input a double. if they have not, it asks them
   * to re-enter, they can also enter logout to logout
   * 
   * @param String amount
   * @return double
   */
  private double isValidDoubleOrLogout() {
    String amount = this.input.nextLine();
    boolean valid = false;
    if (amount.equalsIgnoreCase("logout")) {
      valid = true;
    }
    while (!valid) {
      if (amount.charAt(0) == '-') {
        log.error("entered a negative number");
        System.out.println("You entered a negative number, try again.");
        amount = input.nextLine();
      }
      try {
        Double.parseDouble(amount);
        valid = true;
      } catch (NumberFormatException e) {
        log.error("input included something other than numbers and was not logout", e);
        System.out.println("You entered something we were not expecting, try again.");
        amount = input.nextLine();
      }
      return Double.parseDouble(amount);
    }

    return -1;
  }

  /**
   * sends a call to the service layer to retrieve current balance
   */
  public void getBalance() {
    serve.getBalance();
  }

  /**
   * Sets user credentials to null, sends a call up to service layer to do the same
   */
  public void logout() {
    serve.logout();
    this.state = "Home Screen";
    user.setUsername(null);
    user.setPassword(null);
  }
}
