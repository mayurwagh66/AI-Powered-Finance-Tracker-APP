# ProGuard rules for the Finance Assistant feature

# Keep the FinanceAssistantActivity class
-keep class com.example.myapp.finance.FinanceAssistantActivity { *; }

# Keep the FinanceViewModel class
-keep class com.example.myapp.finance.FinanceViewModel { *; }

# Keep the GeminiClient class
-keep class com.example.myapp.finance.GeminiClient { *; }

# Keep all model classes used in the Finance Assistant feature
-keep class com.example.myapp.finance.model.** { *; }

# Keep Retrofit and Gson classes if used for API calls
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }

# Keep any annotations used in the ViewModel
-keepattributes *Annotation*