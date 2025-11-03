# My Android App

This project is an Android application that includes a chatbot finance assistant feature. The application allows users to interact with a chatbot to get insights on their spending and saving habits using the Gemini API.

## Features

- **Finance Assistant**: A dedicated screen (`FinanceAssistantActivity`) where users can chat with a finance assistant chatbot.
- **Chat Interface**: The interface includes a `TextView` for displaying chat bubbles and an `EditText` for user input.
- **Gemini API Integration**: The application utilizes the Gemini API to answer user queries related to finance.

## Project Structure

```
my-android-app
├── app
│   ├── src
│   │   ├── main
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java
│   │   │   │   └── com
│   │   │   │       └── example
│   │   │   │           └── myapp
│   │   │   │               ├── MainActivity.kt
│   │   │   │               └── finance
│   │   │   │                   ├── FinanceAssistantActivity.kt
│   │   │   │                   ├── FinanceViewModel.kt
│   │   │   │                   └── GeminiClient.kt
│   │   │   └── res
│   │   │       ├── layout
│   │   │       │   └── activity_finance_assistant.xml
│   │   │       └── values
│   │   │           └── strings.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
├── gradle.properties
└── README.md
```

## Setup Instructions

1. Clone the repository to your local machine.
2. Open the project in your preferred IDE.
3. Ensure you have the necessary SDKs and dependencies installed.
4. Configure the Gemini API key in the `GeminiClient.kt` file.
5. Build and run the application on an Android device or emulator.

## Usage

- Launch the application and navigate to the Finance Assistant screen.
- Type your finance-related questions in the input field and press send.
- The chatbot will respond with relevant information based on your queries.

## Contributing

Contributions are welcome! Please feel free to submit a pull request or open an issue for any enhancements or bug fixes.

## License

This project is licensed under the MIT License. See the LICENSE file for details.