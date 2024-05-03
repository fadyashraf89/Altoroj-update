package com.ibm.security.appscan.altoromutual.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ibm.security.appscan.Log4AltoroJ;
import com.ibm.security.appscan.altoromutual.util.DBUtil;
import com.ibm.security.appscan.altoromutual.util.*;

public class User implements java.io.Serializable {

	private static final long serialVersionUID = -4566649173574593144L;

	public static enum Role { User, Admin };

	private String username, firstName, lastName;
	private Role role = Role.User;
	private Date lastAccessDate = null;

	public User(String username, String firstName, String lastName) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		lastAccessDate = new Date();
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Role getRole() {
		return role;
	}

	public Date getLastAccessDate() {
		return lastAccessDate;
	}

	public void setLastAccessDate(Date lastAccessDate) {
		this.lastAccessDate = lastAccessDate;
	}

	public String getUsername() {
		return username;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public Account[] getAccounts() {
		try {
			return DBUtil.getAccounts(username);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Account lookupAccount(Long accountNumber) {
		for (Account account : getAccounts()) {
			if (account.getAccountId() == accountNumber)
				return account;
		}
		return null;
	}

	public long getCreditCardNumber() {
		for (Account account : getAccounts()) {
			if (DBUtil.CREDIT_CARD_ACCOUNT_NAME.equals(account.getAccountName()))
				return account.getAccountId();
		}
		return -1L;
	}

	public Transaction[] getUserTransactions(String startString, String endString, Account[] accounts) {
		try {
			if (startString == null || endString == null || startString.isEmpty() || endString.isEmpty()) {
				return null;
			}

			String query = "SELECT * FROM TRANSACTIONS WHERE (" + getAccountIdsCondition(accounts) + ") AND DATE BETWEEN ? AND ?";
			Connection connection = DBUtil.getConnection();
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, startString + " 00:00:00");
			statement.setString(2, endString + " 23:59:59");
			ResultSet resultSet = statement.executeQuery();

			ArrayList<Transaction> transactions = new ArrayList<>();
			while (resultSet.next()) {
				int transId = resultSet.getInt("TRANSACTION_ID");
				long actId = resultSet.getLong("ACCOUNTID");
				Timestamp date = resultSet.getTimestamp("DATE");
				String desc = resultSet.getString("TYPE");
				double amount = resultSet.getDouble("AMOUNT");
				transactions.add(new Transaction(transId, actId, new Date(date.getTime()), desc, amount));
			}

			return transactions.toArray(new Transaction[0]);
		} catch (SQLException e) {
			Log4AltoroJ.getInstance().logError("Error retrieving user transactions: " + e.getMessage());
			return null;
		}
	}

	private String getAccountIdsCondition(Account[] accounts) {
		StringBuilder condition = new StringBuilder();
		condition.append("(");
		for (int i = 0; i < accounts.length; i++) {
			if (i > 0) {
				condition.append(" OR ");
			}
			condition.append("ACCOUNTID = ?");
		}
		condition.append(")");
		return condition.toString();
	}
}
