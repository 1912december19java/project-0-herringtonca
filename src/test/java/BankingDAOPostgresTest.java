import static org.junit.Assert.*;
import java.beans.Transient;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import com.revature.controller.Controller;
import com.revature.exception.UnavailableUsernameException;
import com.revature.model.User;
import com.revature.repository.BankingDAOPostgres;
import com.revature.service.Services;
import junit.framework.Assert.*;


public class BankingDAOPostgresTest {
  private static Controller cont;
  private static Services serve;
  private static Connection conn;
  private static BankingDAOPostgres bankingDAO;
  private static User user;
  static {
    try {
      conn = DriverManager.getConnection(System.getenv("connstring"), System.getenv("username"),
          System.getenv("password"));
      conn.setAutoCommit(false);
    } catch (SQLException e) {

    }
  }


  @Before
  public void setUp() {
    cont = new Controller();
    serve = new Services();
    bankingDAO = new BankingDAOPostgres();
    user = new User();
  }
  @After
  public void tearDown() {
    cont = null;
    serve = null;
    bankingDAO = null;
    user = null;
  }

  @AfterClass
  public static void undoChanges() {
    try {
      conn.rollback();
      conn.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test
  public void testLoginWithValidCredentials() {
    String username = "asdf";
    String password = "qwe123";
    boolean test = bankingDAO.login(username, password);
    assertTrue(test);
  }
  
  @Test
  public void testLoginAfterRegistration() {
    String username = "qoop";
    String password = "blorg";
    bankingDAO.register(username, password);
    bankingDAO.logout();
    boolean test = bankingDAO.login(username, password);
    assertTrue(test);
  }

  @Test
  public void testLoginWithWrongPassword() {
    String username = "asdf";
    String password = "qwe124";
    boolean test = bankingDAO.login(username, password);
    assertFalse(test);
  }

  @Test
  public void testLoginWithWrongUsername() {
    String username = "asdfg";
    String password = "qwe123";
    boolean test = bankingDAO.login(username, password);
    assertFalse(test);
  }
  
  @Test(expected = UnavailableUsernameException.class)
  public void testCheckUsernameAvailability() {
    String username = "asdf";
    try {
      bankingDAO.checkUsernameAvailability(username);
    } catch (UnavailableUsernameException e) {
      e.printStackTrace();
    }
  }


}
