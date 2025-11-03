# Finance App (Android)

## What is this project?
A modern, offline-first personal finance tracker for Android. It helps users log income/expenses, manage budgets, gain insights and predictions, and receive smart reminders — all without relying on cloud AI or internet connectivity. Data is stored locally using SharedPreferences with JSON serialization.
<img width="600" height="600" alt="image" src="https://github.com/user-attachments/assets/e8d828ae-bb51-4130-b32b-6e53eb463d39" />

## Project Objectives
- Make expense tracking simple and fast
- Keep all analytics offline (privacy-first)
- Offer helpful predictions and insights using statistical and rule-based logic
- Motivate users with budget progress, alerts, and smart reminders

## Key Features
- Transactions
  - Add, edit, and view transactions with type, category, date, payment method, notes
  - Support for custom categories via "Other" and additional items
- Budgets
  - Set monthly budget and per-category budgets
  - Visual progress bars with color cues (green/orange/red)
  - Quick edit for each category budget
- Reports & Analytics
  - Pie chart of expenses by category
  - Bar chart of net income trend by month
  - Offline Predictive Expense Forecasting
    - Predicts next month’s spending using weighted average of the last 3 months
    - Displayed as: "Predicted Next Month Spending: ₹xxxx.xx"
  - Offline Smart Insights (Rule-Based)
    - Compares last two months and shows increase/decrease percentages
    - Per-category messages like "You saved ₹500 on Travel" or "Food spending increased by ₹300"
- Budget Recommendations
  - Suggest per-category targets equal to 90% of your average spend for the last 3 months
  - Example: "Your average Food spending is ₹4000; try limiting to ₹3600 next month."
- Goal Progress Tracking
  - Monthly budget progress and per-category progress
  - Warnings when a category reaches 80% and alerts when exceeded
  - Savings status line: "You're on track to save ₹xxx" or over-budget message
- Notifications
  - Budget warnings/exceeded alerts (local notifications)
  - Smart Reminder Prediction (Pattern Timing)
    - Learns recurring expense timing from your data (e.g., Rent on 1st, Recharge ~28 days)
    - Predicts the next due date and schedules a local reminder at 9:00 AM
    - All reminders are local using AlarmManager + BroadcastReceiver

## Offline “AI” Techniques Used
- Predictive Expense Forecasting: Weighted average of last 3 months (weights 3,2,1)
- Budget Recommendations: 90% of recent average per category (last 3 months)
- Smart Insights: Month-over-month comparisons and per-category deltas
- Smart Reminder Prediction: Median of recent intervals, normalized to common periods (weekly/biweekly/28-day/monthly)
## 📸 Project Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/80429253-cc2a-435a-8b3f-6302857ce7ce" width="30%" />
  <img src="https://github.com/user-attachments/assets/9fdf1fd1-c4a3-4153-ac47-294f9f7dfece" width="30%" />
  <img src="https://github.com/user-attachments/assets/d3e8005a-e4b6-49ee-83e3-a281d2b8c3e7" width="30%" />
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/79e68cad-21db-4753-88b5-dbe75e79d52d" width="30%" />
  <img src="https://github.com/user-attachments/assets/3e597600-7740-4b3d-80c3-9fe386a0ffb7" width="30%" />
  <img src="https://github.com/user-attachments/assets/3a58754c-d962-4ff0-b408-81026d4fdc77" width="30%" />
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/969934d6-9c55-41f8-8318-aed35a8028e0" width="30%" />
  <img src="https://github.com/user-attachments/assets/6691c577-a532-4cef-8e40-5622936dc110" width="30%" />
  <img src="https://github.com/user-attachments/assets/bfefb119-eccb-4dc6-98b0-a7162ca92d56" width="30%" />
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/195a76ce-7e6f-48e9-91f1-e11055279551" width="30%" />
  <img src="https://github.com/user-attachments/assets/b1d36d8a-930a-4702-85f1-b7d47de28761" width="30%" />
  <img src="https://github.com/user-attachments/assets/5d2307b7-e91c-4708-9dfc-e69bb6cbd0b2" width="30%" />
</p>





## Tech Stack
- Language: Java (Android)
- Charts: MPAndroidChart
- Storage: SharedPreferences + Gson (JSON)
- Notifications: NotificationManager, AlarmManager, BroadcastReceiver
- UI: Android Views with XML layouts

## Notable Classes
- Storage
  - `TransactionStorage` — saves/loads transactions locally
  - `NotificationStorage` — local storage for in-app notifications list
  - `Budget` model — keeps monthly and per-category budgets
  - `Transaction` model — transaction details
- Screens
  - `MainActivity` — dashboard, navigation, and bootstraps smart-reminder scheduling
  - `AddTransactionActivity` — create a transaction; triggers budget checks and reschedules reminders
  - `TransactionListActivity`, `EditTransactionActivity` — history and editing
  - `BudgetActivity` — monthly and per-category budgets, progress, recommendations, savings status
  - `ReportsActivity` — charts, predicted spending, and smart insights
  - `NotificationsActivity` — shows local budget warnings and savings progress messages
- Notifications & Reminders
  - `NotificationHelper` — single place to build and display notifications
  - `ReminderScheduler` — learns patterns and schedules reminders with `AlarmManager`
  - `ReminderReceiver` — receives alarm and shows reminder notification

## Project Structure
```
finance_app/
  app/
    src/main/
      java/com/example/finance_app/
        AddTransactionActivity.java
        BudgetActivity.java
        EditTransactionActivity.java
        MainActivity.java
        NotificationHelper.java
        NotificationStorage.java
        NotificationsActivity.java
        ReminderReceiver.java
        ReminderScheduler.java
        ReportsActivity.java
        TransactionListActivity.java
        TransactionStorage.java
        models/
          Budget.java
          Notification.java
          Transaction.java
      res/layout/
        activity_add_transaction.xml
        activity_budget.xml
        activity_notifications.xml
        activity_reports.xml
        item_budget_progress.xml
      AndroidManifest.xml
```

## How It Works
- Data Flow
  - User creates transactions in `AddTransactionActivity`
  - `TransactionStorage` persists them as JSON in SharedPreferences
  - `BudgetActivity` reads transactions and budget data to render progress and recommendations
  - `ReportsActivity` transforms transaction data into chart datasets and textual insights
  - `NotificationsActivity` builds a list of budget warnings and savings progress
- Predictive Analytics
  - `ReportsActivity` derives monthly totals from historical expenses
  - Computes weighted average for prediction and MoM insights (and per-category deltas)
- Smart Reminders
  - `ReminderScheduler.schedulePredictedReminders(Context)`
    - Groups expense transactions by category
    - Computes recent intervals between occurrences
    - Normalizes to common periods and schedules next reminder at 9:00 AM
  - Called on app launch (in `MainActivity`) and after saving a transaction

## Permissions
- `POST_NOTIFICATIONS` — to show notifications (Android 13+ runtime permission may be required)
- `VIBRATE` — for tactile feedback on alerts

## Setup & Build
1. Requirements
   - Android Studio Giraffe+ (or newer)
   - JDK 17 (per Gradle settings)
   - Android SDK as per `local.properties`
2. Clone and open in Android Studio
3. Sync Gradle
4. Build and run on an emulator or device (Android 8.0+ recommended)

## Usage Tips
- Set your monthly budget in `BudgetActivity` to unlock savings status messaging
- Add per-category budgets (tap each budget card) to receive category alerts
- Start by logging a few months of expenses to enable predictions and smart reminders
- For recurring expenses (rent/recharge), consistent logging helps the predictor learn the interval

## Extending the App
- Storage
  - Replace SharedPreferences with Room DB for richer queries and migrations
- Analytics
  - Add more robust smoothing (e.g., exponential moving average) or outlier handling
- Reminders
  - Add in-app reminder management (snooze, edit, disable per category)
- UI/UX
  - Add dark mode toggle, animations, and accessibility improvements

## Troubleshooting
- No notifications?
  - Ensure notification permission is granted (Android 13+)
  - Check device battery optimization settings (alarms may be delayed)
- Predictions look off?
  - Add more historical data and verify that transactions are marked as "Expense"
- Budgets not updating?
  - Tap the budget cards to edit; press "Update Budget Progress" to refresh

## License
This project is provided as-is for educational and personal use. Add a license file if you plan to distribute.
