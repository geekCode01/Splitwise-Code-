package org.example;

import java.util.*;

public class Main {

    //Making them static allows you to create objects directly from Main,
    // without needing to create an instance of Main.
    public static class User {
        private String id;
        private String name;
        private String email;
        private String phone;

        public User(String id, String name, String email, String phone) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    /**
     * By making Split an abstract class, we are saying that Split represents a general concept of splitting an amount between users,
     * but the actual logic of how that split is calculated or validated is left for the subclasses to define.
     * The class itself provides a blueprint for common properties and behavior that all splits will share,
     * but it leaves the specific details for each type of split to be implemented in child classes.
     */

    public abstract static class Split {
        private User user;
        double amount;

        public Split(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }

    //EqualSplit class is a concrete implementation of a split where the amount will be equally divided.
    public static class EqualSplit extends Split {

        public EqualSplit(User user) {
            super(user); //super(user) calls the constructor of the Split class, passing the user object up to the Split class
            // so that the user field is initialized in the parent class (Split)
        }
    }

    public static class ExactSplit extends Split {

        public ExactSplit(User user, double amount) {
            super(user);
            this.amount = amount;
        }
    }

    public static class PercentSplit extends Split {
        double percent;

        public PercentSplit(User user, double percent) {
            super(user);
            this.percent = percent;
        }

        public double getPercent() {
            return percent;
        }

        public void setPercent(double percent) {
            this.percent = percent;
        }
    }

    /**
     * - Expense is a base class and cannot be instantiated directly. Its purpose is to provide a common structure
     * for all types of expenses, such as equal expenses, percentage-based expenses, or exact amount splits.
     * - stores the total amount of the expense, the user who paid for it, and a
     * list of splits representing how much each user owes.
     **/

    public abstract static class Expense {
        private String id;
        private double amount;
        private User paidBy;
        private List<Split> splits;

        public Expense(double amount, User paidBy, List<Split> splits) {
            this.amount = amount;
            this.paidBy = paidBy;
            this.splits = splits;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public User getPaidBy() {
            return paidBy;
        }

        public void setPaidBy(User paidBy) {
            this.paidBy = paidBy;
        }

        public List<Split> getSplits() {
            return splits;
        }

        public void setSplits(List<Split> splits) {
            this.splits = splits;
        }
    }

    //concrete implementation of Expense for equal splits
    public static class EqualExpense extends Expense {
        public EqualExpense(double amount, User paidBy, List<Split> splits) {
            super(amount, paidBy, splits); //super(user) calls the constructor of the Expense class, passing the object up to the Expense class
            // so that the fields are initialized in the parent class (Expense)
        }
    }

    //concrete implementation of Expense for exact splits
    public static class ExactExpense extends Expense {
        public ExactExpense(double amount, User paidBy, List<Split> splits) {
            super(amount, paidBy, splits);
        }
    }

    //concrete implementation of Expense for percent splits
    public static class PercentExpense extends Expense {
        public PercentExpense(double amount, User paidBy, List<Split> splits) {
            super(amount, paidBy, splits);
        }
    }

    //enum to represent different types of expenses
    public enum ExpenseType {
        EQUAL,
        EXACT,
        PERCENT
    }

    // responsible for creating different types of expenses (ExactExpense, PercentExpense, and EqualExpense)
    public static class ExpenseService {
        //This ensures that ExpenseService cannot be instantiated.
        // The class is only meant to provide a static utility method (createExpense), so no instances are needed.
        private ExpenseService() {
        }

        public static Expense createExpense(ExpenseType expenseType, double amount, User paidBy, List<Split> splits) {
            switch (expenseType) {
                case EXACT:
                    //The exact amount is assumed to already be distributed in the splits, so no additional logic is needed here.
                    return new ExactExpense(amount, paidBy, splits);
                case PERCENT:
                    for (Split split : splits) {
                        //we expect each split to be based on a percentage (as per the assumption in PERCENT type)
                        PercentSplit percentSplit = (PercentSplit) split;
                        //amount for each split is calculated as
                        //If percentSplit.getPercent() is 25 and the total amount is 100,
                        // the calculated amount would be (100 * 25) / 100.0 = 25
                        split.setAmount((amount * percentSplit.getPercent()) / 100.0);
                    }
                    return new PercentExpense(amount, paidBy, splits);
                case EQUAL:
                    // number of users splitting the expense
                    int totalSplits = splits.size();
                    //amount is divided equally among all users.
                    // The Math.round() function is used to round the result to 2 decimal places for currency purposes.
                    double splitAmount = ((double) Math.round(amount * 100 / totalSplits)) / 100.0;
                    for (Split split : splits) {
                        split.setAmount(splitAmount);
                    }
                    //Due to rounding, the total split amount might not exactly match the original total amount
                    //The difference is adjusted by adding it to the first user's split.
                    //f the total is $100 and the rounded split amounts add up to $99.99,
                    // the difference of $0.01 is added to the first split,
                    splits.get(0).setAmount(splitAmount + (amount - splitAmount * totalSplits));

                    return new EqualExpense(amount, paidBy, splits);
                default:
                    return null;
            }
        }
    }

    public static class ExpenseManager {
        List<Expense> expenses; //A list to keep track of all expenses added to the system.
        public Map<String, User> userMap; //A map to associate user IDs (as String) with User objects, allowing quick lookups of users by their ID.
        Map<String, Map<String, Double>> balanceSheet; //A nested map that maintains a ledger of balances between users.
        //If userA owes userB $50, the structure would look like:
        //balanceSheet.get("userA").get("userB") == 50.0;

        public ExpenseManager() {
            expenses = new ArrayList<>();
            userMap = new HashMap<>();
            balanceSheet = new HashMap<>();
        }

        //Adds a new User to the userMap
        //initializes an empty ledger (an empty HashMap) for that user in the balanceSheet
        public void addUser(User user) {
            userMap.put(user.getId(), user);
            balanceSheet.put(user.getId(), new HashMap<>());
        }

        public void addExpense(ExpenseType expenseType, double amount, String paidBy, List<Split> splits) {
            // Create the expense using the ExpenseService and add it to the expenses list
            Expense expense = ExpenseService.createExpense(expenseType, amount, userMap.get(paidBy), splits);
            if (expense == null) {
                return;
            }
            expenses.add(expense);

            // Initialize balances for paidBy and each recipient (paidTo)
            Map<String, Double> paidByBalances = balanceSheet.computeIfAbsent(paidBy, k -> new HashMap<>());

            // Iterate through each split and update balances
            for (Split split : expense.getSplits()) {
                //For each split, it retrieves the ID of the user receiving the benefit (paidTo)
                // and the amount specified in the split (splitAmount).

                String paidTo = split.getUser().getId();
                double splitAmount = split.getAmount();

                // initializes the balances map for the user who is being paid (paidTo).
                Map<String, Double> paidToBalances = balanceSheet.computeIfAbsent(paidTo, k -> new HashMap<>());

                //line updates the balance of paidBy to reflect that they have contributed splitAmount for paidTo.
                //It retrieves the current balance for paidTo and adds the splitAmount.
                //If paidTo does not exist in the balance map, it defaults to 0.0
                paidByBalances.put(paidTo, paidByBalances.getOrDefault(paidTo, 0.0) + splitAmount);

                //This line updates the balance of paidTo to reflect that they now owe paidBy the splitAmount.
                // It retrieves the current balance for paidBy and subtracts the splitAmount,
                // effectively marking how much paidTo owes to paidBy
                paidToBalances.put(paidBy, paidToBalances.getOrDefault(paidBy, 0.0) - splitAmount);
            }
        }

        public void showBalance(String userId) {
            Map<String, Double> userBalances = balanceSheet.get(userId);

            // Check if the user's balance map is null or empty
            if (userBalances == null || userBalances.isEmpty()) {
                System.out.println("No balances");
                return; // Exit if there are no balances to show
            }

            // Flag to check if any non-zero balance exists
            boolean hasNonZeroBalance = false;

            for (Map.Entry<String, Double> userBalance : userBalances.entrySet()) {
                double amount = userBalance.getValue();
                if (amount != 0) {
                    hasNonZeroBalance = true;
                    printBalance(userId, userBalance.getKey(), amount);
                }
            }

            // If no balances are found, print a message
            if (!hasNonZeroBalance) {
                System.out.println("No balances");
            }
        }

        public void showBalances() {
            // Flag to check if any balances are printed
            boolean hasBalances = false;

            // Iterate through each user's balance map in the balance sheet
            for (Map.Entry<String, Map<String, Double>> userBalancesEntry : balanceSheet.entrySet()) {
                String userId = userBalancesEntry.getKey();
                Map<String, Double> userBalances = userBalancesEntry.getValue();

                // Iterate through the balances for the current user
                for (Map.Entry<String, Double> balanceEntry : userBalances.entrySet()) {
                    double amount = balanceEntry.getValue();
                    // Check if the amount is positive
                    if (amount > 0) {
                        hasBalances = true;
                        printBalance(userId, balanceEntry.getKey(), amount);
                    }
                }
            }

            // If no balances were found, print a message
            if (!hasBalances) {
                System.out.println("No balances");
            }
        }

        private void printBalance(String user1, String user2, double amount) {
            //These lines retrieve the names of the two users involved in the transaction from the userMap
            String user1Name = userMap.get(user1).getName();
            String user2Name = userMap.get(user2).getName();

            //If amount is negative, it indicates that user1 owes user2
            if (amount < 0) {
                System.out.printf("%s owes %s: %.2f%n", user1Name, user2Name, Math.abs(amount));
            }
            //If amount is positive, it indicates that user2 owes user1
            else if (amount > 0) {
                System.out.printf("%s owes %s: %.2f%n", user2Name, user1Name, Math.abs(amount));
            }
        }

        public void pay(String paidBy, String paidTo, double amount) {
            // Validate user IDs
            // checks if both paidBy and paidTo exist in the userMap
            if (!userMap.containsKey(paidBy) || !userMap.containsKey(paidTo)) {
                System.out.println("Invalid user IDs provided.");
                return;
            }

            // retrieve the balance maps for both the paying user (paidBy) and the receiving user (paidTo)
            // from the balanceSheet
            Map<String, Double> balancesPaidBy = balanceSheet.get(paidBy);
            Map<String, Double> balancesPaidTo = balanceSheet.get(paidTo);

            // Update balances
            // it adds the payment amount to the balance owed to paidTo
            balancesPaidBy.put(paidTo, balancesPaidBy.getOrDefault(paidTo, 0.0) + amount);
            // it deducts the payment amount from the balance owed to paidBy
            balancesPaidTo.put(paidBy, balancesPaidTo.getOrDefault(paidBy, 0.0) - amount);

            // Print payment information
            System.out.printf("%s paid %.2f to %s%n", userMap.get(paidBy).getName(), amount, userMap.get(paidTo).getName());

            // Check if all balances are clear
            if (balancesPaidBy.get(paidTo) == 0.0 && balancesPaidTo.get(paidBy) == 0.0) {
                System.out.printf("All balances between %s and %s are clear.%n", userMap.get(paidBy).getName(), userMap.get(paidTo).getName());
            }
        }
    }

    // register user
    public class UserInitializer {
        public static void initializeUsers(ExpenseManager expenseManager) {
            expenseManager.addUser(new User("u1", "User1", "gaurav@workat.tech", "9876543210"));
            expenseManager.addUser(new User("u2", "User2", "sagar@workat.tech", "9876543210"));
            expenseManager.addUser(new User("u3", "User3", "hi@workat.tech", "9876543210"));
            expenseManager.addUser(new User("u4", "User4", "mock-interviews@workat.tech", "9876543210"));
        }
    }

    public interface Command {
        void execute(String[] commands);
    }

    public static class ExpenseCommand implements Command {
        private final ExpenseManager expenseManager;

        public ExpenseCommand(ExpenseManager expenseManager) {
            this.expenseManager = expenseManager;
        }

        @Override
        public void execute(String[] commands) {

            String paidBy = commands[1]; // User ID of the person paying
            double amount = Double.parseDouble(commands[2]); // Amount to be spent
            int noOfUsers = Integer.parseInt(commands[3]); // Number of users involved
            String expenseType = commands[4 + noOfUsers];
            List<Split> splits = new ArrayList<>();

            // Populate splits based on the expense type
            switch (expenseType) {
                case "EQUAL":
                    // Iterates over the number of users to create equal splits based on the provided user IDs
                    for (int i = 0; i < noOfUsers; i++) {
                        splits.add(new EqualSplit(expenseManager.userMap.get(commands[4 + i])));
                    }
                    // adds the expense to the ExpenseManager
                    expenseManager.addExpense(ExpenseType.EQUAL, amount, paidBy, splits);
                    break;
                case "EXACT":
                    // over the number of users to create equal splits based on the provided user IDs
                    for (int i = 0; i < noOfUsers; i++) {
                        splits.add(new ExactSplit(expenseManager.userMap.get(commands[4 + i]), Double.parseDouble(commands[5 + noOfUsers + i])));
                    }
                    // adds the expense to the ExpenseManager
                    expenseManager.addExpense(ExpenseType.EXACT, amount, paidBy, splits);
                    break;
                case "PERCENT":
                    // Creates percent splits based on user IDs and specified percentages for each user
                    for (int i = 0; i < noOfUsers; i++) {
                        splits.add(new PercentSplit(expenseManager.userMap.get(commands[4 + i]), Double.parseDouble(commands[5 + noOfUsers + i])));
                    }
                    expenseManager.addExpense(ExpenseType.PERCENT, amount, paidBy, splits);
                    break;
                default:
                    System.out.println("Invalid expense type.");
            }
        }
    }

    public static class PayCommand implements Command {
        private final ExpenseManager expenseManager;

        public PayCommand(ExpenseManager expenseManager) {
            this.expenseManager = expenseManager;
        }

        @Override
        public void execute(String[] commands) {
            String paidBy = commands[1];
            String paidTo = commands[2];
            double amount = Double.parseDouble(commands[3]);
            expenseManager.pay(paidBy, paidTo, amount);
        }
    }

    public static class ShowCommand implements Command {
        private final ExpenseManager expenseManager;

        public ShowCommand(ExpenseManager expenseManager) {
            this.expenseManager = expenseManager;
        }

        @Override
        public void execute(String[] commands) {
            if (commands.length == 1) {
                expenseManager.showBalances();
            } else {
                expenseManager.showBalance(commands[1]);
            }
        }
    }

    public static class CommandHandler {
        private final Map<String, Command> commandMap;

        public CommandHandler(ExpenseManager expenseManager) {
            commandMap = new HashMap<>();
            commandMap.put("SHOW", new ShowCommand(expenseManager));
            commandMap.put("PAY", new PayCommand(expenseManager));
            commandMap.put("EXPENSE", new ExpenseCommand(expenseManager));
        }

        public void handleCommand(String commandType, String[] commands) {
            Command command = commandMap.get(commandType); // Use commandMap instead of commands
            if (command != null) {
                command.execute(commands);
            } else {
                System.out.println("Invalid command. Please try again.");
            }
        }
    }

    public static void main(String[] args) {
        ExpenseManager expenseManager = new ExpenseManager();
        UserInitializer.initializeUsers(expenseManager);

        // Create a CommandHandler for processing commands
        CommandHandler commandHandler = new CommandHandler(expenseManager);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("> "); // Prompt for user input
            String command = scanner.nextLine();
            String[] commands = command.split("\\s+"); // Split command by whitespace
            String commandType = commands[0]; // Get the command type

            if (commandType.equalsIgnoreCase("EXIT")) {
                System.out.println("Exiting the application.");
                break;
            }
            commandHandler.handleCommand(commandType, commands);
        }
    }
}



> EXPENSE u1 1000 4 u1 u2 u3 u4 EQUAL
> SHOW u1
User2 owes User1: 250.00
User3 owes User1: 250.00
User4 owes User1: 250.00
> SHOW
User2 owes User1: 250.00
User3 owes User1: 250.00
User4 owes User1: 250.00
> EXPENSE u1 1250 2 u2 u3 EXACT 370 880
> SHOW
User2 owes User1: 620.00
User3 owes User1: 1130.00
User4 owes User1: 250.00
> EXPENSE u4 1200 4 u1 u2 u3 u4 PERCENT 40 20 20 20
> SHOW 
User2 owes User1: 620.00
User3 owes User1: 1130.00
User1 owes User4: 230.00
User2 owes User4: 240.00
User3 owes User4: 240.00
> SHOW u2
User2 owes User1: 620.00
User2 owes User4: 240.00
> SHOW 
User2 owes User1: 620.00
User3 owes User1: 1130.00
User1 owes User4: 230.00
User2 owes User4: 240.00
User3 owes User4: 240.00
> PAY u2 u1 600.0
User2 paid 600.00 to User1
> SHOW
User2 owes User1: 20.00
User3 owes User1: 1130.00
User1 owes User4: 230.00
User2 owes User4: 240.00
User3 owes User4: 240.00
> PAY u2 u1 20.0
User2 paid 20.00 to User1
All balances between User2 and User1 are clear.
> SHOW
User3 owes User1: 1130.00
User1 owes User4: 230.00
User2 owes User4: 240.00
User3 owes User4: 240.00
> EXIT
Exiting the application.

Process finished with exit code 0
